package bj.albon.arith.dynamic.limiter.strategy;

import bj.albon.arith.dynamic.limiter.config.DynamicProperties;
import bj.albon.arith.dynamic.limiter.counter.AmountStatisticStream;
import bj.albon.arith.dynamic.limiter.counter.BucketCounterStream;
import bj.albon.arith.dynamic.limiter.model.CriticalEventInfo;
import bj.albon.arith.dynamic.limiter.model.EventType;
import bj.albon.arith.dynamic.limiter.service.CriticalEventManager;
import bj.albon.arith.dynamic.limiter.util.BucketUtil;
import bj.albon.arith.dynamic.limiter.util.QMonitorKey;
import bj.albon.arith.dynamic.limiter.util.MonitorUtil;
import bj.albon.arith.dynamic.limiter.util.StreamUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import bj.albon.arith.dynamic.limiter.model.CriticalEventType;
import bj.albon.arith.dynamic.limiter.model.StrategyEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action1;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author albon
 *         Date: 17-7-21
 *         Time: 下午4:00
 */
public class CircuitBreakerStrategy extends AbstractStrategy {
    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerStrategy.class);

    private AmountStatisticStream successStream;
    private AmountStatisticStream failureStream;

    private volatile int successCount = 0;
    private volatile double failureCount = 0;

    private AtomicBoolean circuitOpen = new AtomicBoolean(false);
    private AtomicInteger tryNum = new AtomicInteger(0);
    private AtomicInteger trySuccessNum = new AtomicInteger(0);
    private AtomicLong circuitOpenedOrLastTestedTime = new AtomicLong(0);

    public CircuitBreakerStrategy(DynamicProperties properties) {
        super(properties);

        rebuildStatisticStream();
    }

    @Override
    public List<EventType> support() {
        return Lists.newArrayList(EventType.SUCCESS, EventType.FAILURE);
    }

    @Override
    public String name() {
        return StrategyEnum.CircuitBreaker.name();
    }

    @Override
    public boolean exceed() {
        if (circuitOpen.get()) {
            long timeCircuitOpenedOrWasLastTested = circuitOpenedOrLastTestedTime.get();
            if (System.currentTimeMillis() > timeCircuitOpenedOrWasLastTested + properties.getCircuitBreakerWindowInMs()
                    && circuitOpenedOrLastTestedTime.compareAndSet(timeCircuitOpenedOrWasLastTested,
                    System.currentTimeMillis())) {
                // 熔断超过阈值, 重设熔断开始时间, 设定尝试次数
                tryNum.set(properties.getContinuousSuccessNumCloseCircuitBreaker());
                trySuccessNum.set(0);

                MonitorUtil.recordOne(QMonitorKey.CIRCUIT_BREAKER_HALF_OPEN, properties.getKey());
                logger.info("{} circuitBreakerHalfOpen", properties.getKey());
            }

            if (tryNum.get() > 0 && tryNum.decrementAndGet() >= 0) {
                return false;
            }

            return true;
        }

        if (successCount == 0 && failureCount == 0) {
            return false;
        }

        boolean exceed = failureCount > properties.getFailureCountThreshold()
                && failureCount / (successCount + failureCount) * 100 >= properties.getFailureRateThreshold();
        if (exceed && circuitOpen.compareAndSet(false, true)) {
            logger.info("{} circuitBreakerOpen", properties.getKey());
            MonitorUtil.recordOne(QMonitorKey.CIRCUIT_BREAKER_OPEN, properties.getKey());

            circuitOpenedOrLastTestedTime.set(System.currentTimeMillis());

            CriticalEventManager.getInstance()
                    .inform(new CriticalEventInfo(properties.getKey(), CriticalEventType.CIRCUIT_BREAKER_OPEN));
        }

        return exceed;
    }

    @Override
    public void record(EventType eventType, long value) {
        Preconditions.checkArgument(eventType.equals(EventType.SUCCESS) || eventType.equals(EventType.FAILURE),
                "EventType not support: " + eventType);

        if (circuitOpen.get()) {
            if (eventType.equals(EventType.SUCCESS)) {
                if (trySuccessNum.incrementAndGet() == properties.getContinuousSuccessNumCloseCircuitBreaker()
                        && circuitOpen.compareAndSet(true, false)) {
                    logger.info("{} circuitBreakerClose", properties.getKey());
                    MonitorUtil.recordOne(QMonitorKey.CIRCUIT_BREAKER_CLOSE, properties.getKey());
                    trySuccessNum.set(0);

                    CriticalEventManager.getInstance().inform(
                            new CriticalEventInfo(properties.getKey(), CriticalEventType.CIRCUIT_BREAKER_CLOSE));
                }
            } else {
                trySuccessNum.set(0);
            }
        } else {
            if (eventType.equals(EventType.FAILURE)) {
                failureStream.publish(1);
            } else if (eventType.equals(EventType.SUCCESS)) {
                successStream.publish(1);
            }
        }
    }

    @Override
    public void update() {
        rebuildStatisticStream();
    }

    private void rebuildStatisticStream() {
        int bucketNum = BucketUtil.computeBucketNum(properties);
        int bucketSizeInMs = properties.getBucketSizeInMs();

        // 如果 stream 已经有值，并且滑动窗口参数新老一致，就不需要更新了
        if (StreamUtil.bucketSizeEqual(successStream, bucketNum, bucketSizeInMs)) {
            return;
        }

        logger.info("key: {}, buildStream bucketNum: {}, bucketSizeInMs: {}", properties.getKey(), bucketNum, bucketSizeInMs);
        List<BucketCounterStream> oldStreamList = StreamUtil.addToListIfNotNull(successStream, failureStream);

        successStream = AmountStatisticStream.getInstance(bucketNum, bucketSizeInMs);
        failureStream = AmountStatisticStream.getInstance(bucketNum, bucketSizeInMs);

        successStream.subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                successCount = integer;
                logger.debug("successCount = {}", successCount);
            }
        });

        failureStream.subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                failureCount = integer;
                logger.debug("failureCount = {}", failureCount);
            }
        });

        StreamUtil.close(oldStreamList);
    }
}

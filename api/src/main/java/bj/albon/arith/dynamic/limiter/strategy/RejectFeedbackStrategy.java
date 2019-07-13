package bj.albon.arith.dynamic.limiter.strategy;

import bj.albon.arith.dynamic.limiter.config.DynamicProperties;
import bj.albon.arith.dynamic.limiter.counter.AmountStatisticStream;
import bj.albon.arith.dynamic.limiter.counter.BucketCounterStream;
import bj.albon.arith.dynamic.limiter.model.EventType;
import com.google.common.collect.Lists;
import bj.albon.arith.dynamic.limiter.model.StrategyEnum;
import bj.albon.arith.dynamic.limiter.util.BucketUtil;
import bj.albon.arith.dynamic.limiter.util.QMonitorKey;
import bj.albon.arith.dynamic.limiter.util.MonitorUtil;
import bj.albon.arith.dynamic.limiter.util.StreamUtil;
import bj.albon.arith.dynamic.limiter.util.TrafficShaper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action1;

import java.math.BigDecimal;
import java.util.List;

/**
 * 根据根据过去几秒的通过率，决定未来这一秒的通过率
 * 用于(接收消息)->(获取资源)->(计算队列)的程序架构下，根据计算队列入队成功率，决定接收消息后的处理概率
 *
 * @author albon
 *         Date: 17-7-21
 *         Time: 下午4:00
 */
public class RejectFeedbackStrategy extends AbstractStrategy {
    private static final Logger logger = LoggerFactory.getLogger(RejectFeedbackStrategy.class);

    private AmountStatisticStream passStream;
    private AmountStatisticStream rejectStream;

    private volatile double rejectCount = 0;
    private String rateLimiterKey;

    public RejectFeedbackStrategy(final DynamicProperties properties) {
        super(properties);

        rateLimiterKey = buildRateLimiterKey(properties);

        rebuildStatisticStream();
    }

    @Override
    public String name() {
        return StrategyEnum.RejectFeedback.name();
    }

    @Override
    public List<EventType> support() {
        return Lists.newArrayList(EventType.SUCCESS, EventType.FAILURE);
    }

    @Override
    public boolean exceed() {
        if (rejectCount > properties.getFailureCountThreshold()) {
            return !TrafficShaper.tryEnterIfExist(rateLimiterKey);
        } else {
            return false;
        }
    }

    @Override
    public void record(EventType eventType, long value) {
        if (eventType.equals(EventType.SUCCESS)) {
            passStream.publish(1);
        } else if (eventType.equals(EventType.FAILURE)) {
            rejectStream.publish(1);
        } else {
            throw new UnsupportedOperationException("EventType not support: " + eventType);
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
        if (StreamUtil.bucketSizeEqual(passStream, bucketNum, bucketSizeInMs)) {
            return;
        }

        logger.info("key: {}, buildStream bucketNum: {}, bucketSizeInMs: {}", properties.getKey(), bucketNum, bucketSizeInMs);
        List<BucketCounterStream> oldStreamList = StreamUtil.addToListIfNotNull(passStream, rejectStream);

        passStream = AmountStatisticStream.getInstance(bucketNum, bucketSizeInMs);
        rejectStream = AmountStatisticStream.getInstance(bucketNum, bucketSizeInMs);

        passStream.subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer passCount) {
                int qps = properties.getDynamicLimiterQpsFactor().multiply(BigDecimal.valueOf(passCount))
                        .divide(BigDecimal.valueOf(properties.getWindowSizeInSecond())).intValue() + 1;
                TrafficShaper.updateResourceQps(rateLimiterKey, qps);

                MonitorUtil.recordTime(QMonitorKey.DYNAMIC_LIMITER_QPS, properties.getKey(), name(), qps);
            }
        });

        rejectStream.subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                rejectCount = integer;
            }
        });

        StreamUtil.close(oldStreamList);
    }
}

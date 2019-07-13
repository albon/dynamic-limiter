package bj.albon.arith.dynamic.limiter.strategy;

import bj.albon.arith.dynamic.limiter.config.DynamicProperties;
import bj.albon.arith.dynamic.limiter.counter.AmountStatisticStream;
import bj.albon.arith.dynamic.limiter.counter.BucketCounterStream;
import bj.albon.arith.dynamic.limiter.counter.HealthyQpsStatisticStream;
import bj.albon.arith.dynamic.limiter.service.CriticalEventManager;
import bj.albon.arith.dynamic.limiter.strategy.monitor.IMonitor;
import bj.albon.arith.dynamic.limiter.strategy.monitor.MonitorFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import bj.albon.arith.dynamic.limiter.model.CriticalEventInfo;
import bj.albon.arith.dynamic.limiter.model.CriticalEventType;
import bj.albon.arith.dynamic.limiter.model.EventType;
import bj.albon.arith.dynamic.limiter.model.HealthStatus;
import bj.albon.arith.dynamic.limiter.model.HealthyQpsInfo;
import bj.albon.arith.dynamic.limiter.model.LastWorsenQps;
import bj.albon.arith.dynamic.limiter.model.MonitorType;
import bj.albon.arith.dynamic.limiter.model.StrategyEnum;
import bj.albon.arith.dynamic.limiter.model.SystemHealthyComputeInfo;
import bj.albon.arith.dynamic.limiter.model.SystemHealthyMonitorInfo;
import bj.albon.arith.dynamic.limiter.util.BucketUtil;
import bj.albon.arith.dynamic.limiter.util.QMonitorKey;
import bj.albon.arith.dynamic.limiter.util.MonitorUtil;
import bj.albon.arith.dynamic.limiter.util.StreamUtil;
import bj.albon.arith.dynamic.limiter.util.SystemHealthyUtil;
import bj.albon.arith.dynamic.limiter.util.TrafficShaper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action1;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 根据负载变化情况，动态限流
 *
 * @author albon
 *         Date: 17-8-2
 *         Time: 上午9:35
 */
public class LoadDynamicLimitStrategy extends AbstractStrategy {
    private static final Logger logger = LoggerFactory.getLogger(LoadDynamicLimitStrategy.class);
    public static final int ONE_EVENT = 1;
    public static final int UPDATE_QPS_FREQ = 10000; // 每隔多少毫秒更新一次 QPS
    public static final int HEALTHY_QPS_STREAM_NUM_BUCKETS = 5; // 长一点儿，减少干扰。
    public static final int HEALTHY_QPS_STREAM_BUCKET_SIZE_IN_MS = 60000;
    public static final int LOWEST_QPS = 1;
    public static final long INVALID_QPS = -1L;
    public static final double UNCHANGE_FACTOR = 1.0;

    private String rateLimiterKey;
    private AtomicBoolean isLimiting = new AtomicBoolean(false);

    private HealthyQpsStatisticStream healthyQpsStream;
    private AmountStatisticStream exceedStream;
    private AmountStatisticStream passStream;

    // 系统在健康状态下，处理请求的 QPS
    private volatile long healthyQps;
    // 记录被拦截的 QPS
    private volatile long exceedQps;
    private volatile LastWorsenQps lastWorsenQps;

    private volatile SystemHealthyMonitorInfo systemHealthyMonitorInfo;

    public LoadDynamicLimitStrategy(final DynamicProperties properties) {
        super(properties);

        rateLimiterKey = buildRateLimiterKey(properties);

        systemHealthyMonitorInfo = buildSystemHealthyMonitorInfo(properties);

        healthyQpsStream = HealthyQpsStatisticStream.getInstance(HEALTHY_QPS_STREAM_NUM_BUCKETS,
                HEALTHY_QPS_STREAM_BUCKET_SIZE_IN_MS);
        healthyQpsStream.subscribe(new Action1<HealthyQpsInfo>() {
            @Override
            public void call(HealthyQpsInfo healthyQpsInfo) {
                if (healthyQpsInfo.isValid() && healthyQpsInfo.getCount() > 0) {
                    healthyQps = healthyQpsInfo.getTime() / healthyQpsInfo.getCount();
                    logger.info("{} updateHealthyQps = {}", properties.getKey(), healthyQps);
                }
            }
        });

        rebuildStatisticStream();
    }

    private void rebuildStatisticStream() {
        int bucketNum = BucketUtil.computeBucketNum(properties);
        int bucketSizeInMs = properties.getBucketSizeInMs();

        // 如果 stream 已经有值，并且滑动窗口参数新老一致，就不需要更新了
        if (StreamUtil.bucketSizeEqual(exceedStream, bucketNum, bucketSizeInMs)) {
            return;
        }

        logger.info("key: {}, buildStream bucketNum: {}, bucketSizeInMs: {}", properties.getKey(), bucketNum, bucketSizeInMs);
        List<BucketCounterStream> oldStreamList = StreamUtil.addToListIfNotNull(passStream, exceedStream);

        exceedStream = AmountStatisticStream.getInstance(bucketNum, bucketSizeInMs);
        exceedStream.subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer passCount) {
                exceedQps = computeCurrentQps(passCount);
                logger.debug("{} updateExceedQps = {}", properties.getKey(), exceedQps);
            }
        });

        passStream = AmountStatisticStream.getInstance(bucketNum, bucketSizeInMs);
        passStream.subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer passCount) {
                updateRateLimiter(passCount);
            }
        });

        StreamUtil.close(oldStreamList);
    }

    private SystemHealthyMonitorInfo buildSystemHealthyMonitorInfo(DynamicProperties properties) {
        SystemHealthyComputeInfo systemHealthyComputeInfo = properties.getSystemHealthyComputeInfo();
        Map<MonitorType, IMonitor> monitorMap = buildMonitorMap(systemHealthyComputeInfo.getMonitorTypeList());
        return new SystemHealthyMonitorInfo(monitorMap, systemHealthyComputeInfo);
    }

    @Override
    public String name() {
        return StrategyEnum.LoadDynamicLimit.name();
    }

    /**
     * 更新限流阈值
     *
     * @param requestNum 过去一段时间请求处理量
     */
    private void updateRateLimiter(long requestNum) {
        long currentQps = computeCurrentQps(requestNum);

        HealthStatus healthStatus = SystemHealthyUtil.compute(systemHealthyMonitorInfo);
        if (healthStatus.isHealthy()) {
            healthyQpsStream.publish(currentQps);
        } else {
            healthyQpsStream.publish(INVALID_QPS);
        }

        long qps;
        double factor = computeQpsFactor(healthStatus);
        // 根据历史数据计算限流 QPS
        qps = computeLimitQpsByHealthyQps(currentQps, healthStatus, factor);

        TrafficShaper.updateResourceQps(rateLimiterKey, qps);

        logger.debug("{} currentDynamicLimiterQps: {}, currentQps: {}, healthStatus: {}, lastWorsenQps: {}, isLimiting: {}, this: {}",
                properties.getKey(), qps, currentQps, healthStatus, lastWorsenQps, isLimiting.get(), this);
        MonitorUtil.recordTime(QMonitorKey.DYNAMIC_LIMITER_QPS, properties.getKey(), name(), qps);
    }

    /**
     * 利用历史健康 QPS 计算限流阈值
     *
     * @param currentQps   过去一段时间的请求处理 QPS
     * @param healthStatus 系统健康状态
     * @param factor       QPS 更新乘数因子
     */
    private long computeLimitQpsByHealthyQps(long currentQps, HealthStatus healthStatus, double factor) {
        long qps;
        if (lastWorsenQps == null) {
            // 系统状态恶化，并且过去一段时间的 QPS 大于健康时期的 QPS，就用健康时期的 QPS 来做限流阈值
            if (healthStatus.equals(HealthStatus.WORSE)) {
                qps = computeFirstLimitQpsInWorsenStatus(currentQps, healthyQps);
            } else {
                qps = computeQpsWithHealthyQps(currentQps, factor, healthyQps);
            }

            if (!healthStatus.isHealthy()) {
                logger.info(
                        "{} computeFirstLimitQps qps: {}, currentQps: {}, healthyQps: {}, healthStatus: {}, factor: {}",
                        properties.getKey(), qps, currentQps, healthyQps, healthStatus, factor);
                lastWorsenQps = new LastWorsenQps(qps);
                if (isLimiting.compareAndSet(false, true)) {
                    logger.info("key: {}, strategy: {}, dynamicLimiter open", properties.getKey(), name());
                    CriticalEventManager.getInstance()
                            .inform(new CriticalEventInfo(properties.getKey(), CriticalEventType.DYNAMIC_LIMITER_OPEN));
                }
            }
        } else {
            // NOT_HEALTHY 状态下，如果存在 lastWorsenQps，则根据 lastWorsenQps 来限流。
            // worsen 状态下，离上次更新 qps 不到半分钟，也使用 lastWorsenQps 来限流。
            qps = lastWorsenQps.getQps();
            if (healthStatus.isHealthy() && doBurstyTrafficDisappear() && isLimiting.compareAndSet(true, false)) {
                // 健康状态下，exceedQps <= currentQps 表示突发流量消失了，关闭限流即可
                lastWorsenQps = null;
                qps = computeQpsWithHealthyQps(currentQps, factor, healthyQps);

                logger.info("key: {}, strategy: {}, dynamicLimiter close", properties.getKey(), name());
                CriticalEventManager.getInstance()
                        .inform(new CriticalEventInfo(properties.getKey(), CriticalEventType.DYNAMIC_LIMITER_CLOSE));
            } else if (!healthStatus.equals(HealthStatus.NOT_HEALTHY) && canWeUpdateLastWorsenQps()) {
                // 如果离上次更新 qps 大于一定时间，则根据系数来更新
                qps = computeLimitQpsSimple(lastWorsenQps.getQps(), factor);
                logger.info("{} updateDynamicLimitQps: {}, factor: {}, lastQps: {}, status: {}", properties.getKey(), qps,
                        factor, lastWorsenQps.getQps(), healthStatus);
                lastWorsenQps = new LastWorsenQps(qps);
            }
        }

        return qps;
    }

    private long computeQpsWithHealthyQps(long currentQps, double factor, long healthyQps) {
        if (healthyQps > currentQps) {
            return healthyQps;
        } else {
            return computeLimitQpsSimple(currentQps, factor);
        }
    }

    /**
     * 当系统健康状况恶化时，计算初始限流的 QPS
     *
     * @param currentQps 过去几秒处理的 QPS
     * @param healthyQps 健康状态下的平均 QPS
     * @return
     */
    private long computeFirstLimitQpsInWorsenStatus(long currentQps, long healthyQps) {
        long qps;
        if (currentQps > healthyQps && healthyQps > 0) {
            qps = (long) (Math.sqrt((double) currentQps / healthyQps) * healthyQps);
        } else {
            qps = currentQps / 2;
        }

        return Math.max(qps, LOWEST_QPS);
    }

    @Override
    public boolean exceed() {
        boolean exceed = doExceed();

        logger.debug("key: {}, doExceed: {}, isLimiting: {}", properties.getKey(), exceed, isLimiting.get());
        if (exceed) {
            exceedStream.publish(ONE_EVENT);
        } else {
            passStream.publish(ONE_EVENT);
        }

        return exceed;
    }

    private boolean doExceed() {
        if (isLimiting.get()) {
            return !TrafficShaper.tryEnterIfExist(rateLimiterKey);
        }

        return false;
    }

    @Override
    public void record(EventType eventType, long value) {
        if (eventType.equals(EventType.TIME)) {
            IMonitor iMonitor = systemHealthyMonitorInfo.getMonitorMap().get(MonitorType.TIME);
            if (iMonitor != null) {
                iMonitor.record(eventType, value);
            }
        } else {
            throw new UnsupportedOperationException("EventType not support: " + eventType);
        }
    }

    @Override
    public List<EventType> support() {
        return Lists.newArrayList(EventType.TIME);
    }

    /**
     * 判断突发流量消失了吗？
     *
     * @return 返回 true，表示消失了。
     */
    private boolean doBurstyTrafficDisappear() {
        return exceedQps <= 0;
    }

    /**
     * 根据请求处理量和滑动窗口时长，计算 QPS
     *
     * @param requestNum 请求处理量
     * @return
     */
    private long computeCurrentQps(long requestNum) {
        return BigDecimal.valueOf(requestNum).divide(BigDecimal.valueOf(properties.getWindowSizeInSecond()), 0, BigDecimal.ROUND_UP)
                .longValue();
    }

    /**
     * 判断是否可以更新限流 qps 了，避免更新 qps 过于频繁。
     *
     * @return 返回 true，表示可以更新。
     */
    private boolean canWeUpdateLastWorsenQps() {
        return System.currentTimeMillis() - lastWorsenQps.getTime() > UPDATE_QPS_FREQ;
    }

    /**
     * 根据系统健康状态，判断 QPS 更新的乘数因子
     *
     * @param healthStatus 系统健康状态
     * @return QPS 更新的乘数因子
     */
    private double computeQpsFactor(HealthStatus healthStatus) {
        double factor = UNCHANGE_FACTOR;
        if (healthStatus.equals(HealthStatus.WORSE)) {
            factor = properties.getDynamicLimiterQpsFactor().doubleValue();
        } else if (healthStatus.equals(HealthStatus.HEALTHY)) {
            factor = 1.0 / Math.sqrt(properties.getDynamicLimiterQpsFactor().doubleValue());
        } else if (healthStatus.equals(HealthStatus.EXCELLENT)) {
            factor = properties.getDynamicLimiterQpsFactor().doubleValue();
            factor = 1.0 / (factor * factor);
        }
        return factor;
    }

    /**
     * 根据历史 QPS 和乘数因子，计算限流阈值
     *
     * @param currentQps 历史 QPS
     * @param factor     乘数因子
     * @return
     */
    private long computeLimitQpsSimple(long currentQps, double factor) {
        long newQps = BigDecimal.valueOf(factor).multiply(BigDecimal.valueOf(currentQps)).longValue();

        if (factor > UNCHANGE_FACTOR) {
            // 避免 currentQps = 1，factor = 1.1 时，newQps = 1
            newQps = Math.max(newQps, currentQps + 1);
        }

        newQps = Math.max(newQps, LOWEST_QPS);
        return newQps;
    }

    @Override
    public void update() {
        // 更新 Monitor
        rebuildSystemMonitor();

        // 更新 Stream
        rebuildStatisticStream();
    }

    private void rebuildSystemMonitor() {
        SystemHealthyComputeInfo newComputeInfo = properties.getSystemHealthyComputeInfo();
        SystemHealthyComputeInfo oldComputeInfo = systemHealthyMonitorInfo.getSystemHealthyComputeInfo();
        if (newComputeInfo.equals(oldComputeInfo)) {
            return;
        }

        logger.info("systemHealthyComputeInfo need update, systemHealthyComputeInfo: {}, newComputeInfo: {}",
                oldComputeInfo, newComputeInfo);
        Map<MonitorType, IMonitor> oldMonitorMap = systemHealthyMonitorInfo.getMonitorMap();
        systemHealthyMonitorInfo = buildSystemHealthyMonitorInfo(properties);
        SystemHealthyUtil.closeMonitor(oldMonitorMap);
    }

    private Map<MonitorType, IMonitor> buildMonitorMap(List<MonitorType> monitorTypeList) {
        Map<MonitorType, IMonitor> monitorMap = Maps.newHashMap();

        for (MonitorType monitorType : monitorTypeList) {
            monitorMap.put(monitorType, MonitorFactory.create(monitorType, properties));
        }

        return monitorMap;
    }
}

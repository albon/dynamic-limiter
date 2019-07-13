package bj.albon.arith.dynamic.limiter.limiter;

import bj.albon.arith.dynamic.limiter.config.DynamicProperties;
import bj.albon.arith.dynamic.limiter.model.DynamicLimiterException;
import bj.albon.arith.dynamic.limiter.model.EventType;
import bj.albon.arith.dynamic.limiter.model.SystemInfo;
import bj.albon.arith.dynamic.limiter.strategy.AbstractStrategy;
import bj.albon.arith.dynamic.limiter.strategy.StrategyFactory;
import bj.albon.arith.dynamic.limiter.util.QMonitorKey;
import bj.albon.arith.dynamic.limiter.util.MonitorUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import bj.albon.arith.dynamic.limiter.config.DynamicPropertiesFactory;
import bj.albon.arith.dynamic.limiter.config.GlobalConfig;
import bj.albon.arith.dynamic.limiter.model.StrategyEnum;
import bj.albon.arith.dynamic.limiter.util.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * DynamicLimiter 接口实现类。
 *
 * @author albon
 *         Date: 17-7-21
 *         Time: 上午11:19
 */
class DynamicLimiterImpl extends AbstractDynamicLimiter {
    private static final Logger logger = LoggerFactory.getLogger(DynamicLimiterImpl.class);

    private String key;
    private DynamicProperties properties;
    private Map<StrategyEnum, AbstractStrategy> strategyMap = Maps.newHashMap();
    private Map<EventType, AbstractStrategy> eventStrategyMap = Maps.newHashMap();

    protected DynamicLimiterImpl() {
    }

    protected void init(String key, List<StrategyEnum> strategyList) {
        try {
            this.key = key;
            this.properties = DynamicPropertiesFactory.get(key);
            for (StrategyEnum strategy : strategyList) {
                AbstractStrategy abstractStrategy = StrategyFactory.create(strategy, properties);
                strategyMap.put(strategy, abstractStrategy);

                for (EventType eventType : abstractStrategy.support()) {
                    // 要求同一个event不能由两个abstractStrategy接收
                    Preconditions.checkArgument(!eventStrategyMap.containsKey(eventType),
                            String.format("相同event: %s, 不同strategy %s 和 %s", eventType, abstractStrategy,
                                    eventStrategyMap.get(eventType))); // 使用 String.format 是因为低版本 Guava 没有 %s
                                                                       // 格式化字符串的功能，为了方便框架使用方
                    eventStrategyMap.put(eventType, abstractStrategy);
                }
            }
        } catch (Exception e) {
            logger.error("init dynamic limiter error, params: {}", strategyList, e);
            throw new DynamicLimiterException("init dynamic limiter error", e);
        }
    }

    /**
     * 合并各策略判断结果，暂时写成“与”逻辑，后续可以修改成根据配置决定计算逻辑
     *
     * @return
     */
    public boolean exceed() {
        long start = System.nanoTime();

        try {
            if (isCloseAllDynamicLimiter()) {
                return false;
            }

            for (AbstractStrategy strategy : strategyMap.values()) {
                if (strategy.exceed()) {
                    MonitorUtil.recordOne(QMonitorKey.DYNAMIC_LIMITER_EXCEED, key, strategy.name());
                } else {
                    return false;
                }
            }

            if (properties.isForceClose()) {
                MonitorUtil.recordOne(QMonitorKey.DYNAMIC_LIMITER_EXCEED_BUT_FORCE_CLOSE, key);
                return false;
            }

            MonitorUtil.recordOne(QMonitorKey.DYNAMIC_LIMITER_RETURN_EXCEED, key);
            return true;
        } catch (Exception e) {
            logger.error("dynamic_limiter_error_exceed key: {}", key, e);
            MonitorUtil.recordOne(QMonitorKey.DYNAMIC_LIMITER_ERROR_EXCEED, key);
        } finally {
            Monitor.recordOne(QMonitorKey.DYNAMIC_LIMITER_EXCEED_TOTAL, System.nanoTime() - start);
        }

        return false;
    }

    private boolean isCloseAllDynamicLimiter() {
        if (GlobalConfig.instance().isCloseAllDynamicLimiter()) {
            return true;
        }

        if (GlobalConfig.instance().isCloseWhenHealthCheckNotExist()) {
            return true;
        }

        if (GlobalConfig.instance().getCloseToTimeInMillis() > System.currentTimeMillis()) {
            return true;
        }

        return false;
    }

    /**
     * 记录1次事件的发生
     * 
     * @param eventType
     */
    public void record(EventType eventType) {
        Preconditions.checkNotNull(eventType);
        Preconditions.checkArgument(!eventType.equals(EventType.TIME),
                "record TIME event please use method: void record(EventType eventType, long value)");

        record(eventType, 1);
    }

    /**
     * 记录事件的发生
     * 
     * @param eventType
     * @param value 当事件为 TIME 时，表示事件处理时间，其他时候表示事件发生次数
     */
    public void record(EventType eventType, long value) {
        Preconditions.checkNotNull(eventType);

        long start = System.nanoTime();
        try {
            if (isCloseAllDynamicLimiter()) {
                return;
            }

            MonitorUtil.recordOne(QMonitorKey.DYNAMIC_LIMITER_RECORD_EVENT, key, eventType.name());

            AbstractStrategy strategyService = eventStrategyMap.get(eventType);
            strategyService.record(eventType, value);
        } catch (Exception e) {
            logger.error("dynamic_limiter_error_record event: {}", eventType, e);
            MonitorUtil.recordOne(QMonitorKey.DYNAMIC_LIMITER_ERROR_RECORD, key);
        } finally {
            Monitor.recordOne(QMonitorKey.DYNAMIC_LIMITER_RECORD_TOTAL, System.nanoTime() - start);
        }
    }

    @Override
    public void update() {
        Map<StrategyEnum, AbstractStrategy> tmpMap = this.strategyMap;
        if (tmpMap == null) {
            return;
        }

        for (AbstractStrategy abstractStrategy : tmpMap.values()) {
            abstractStrategy.update();
        }
    }

    @Override
    public DynamicProperties getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "DynamicLimiterImpl{" + "key='" + key + '\'' + ", properties=" + properties + ", strategyMap="
                + strategyMap + ", eventStrategyMap=" + eventStrategyMap + '}';
    }
}

package bj.albon.arith.dynamic.limiter.strategy;

import bj.albon.arith.dynamic.limiter.config.DynamicProperties;
import bj.albon.arith.dynamic.limiter.model.EventType;

import java.util.List;

/**
 * Created by albon on 17/7/23.
 */
public abstract class AbstractStrategy {
    static {
        // 设置 computation 线程池最大线程数目为 1，减少线程切换
        System.setProperty("rx.scheduler.max-computation-threads", "1");
    }

    // DynamicLimiter 配置信息
    protected DynamicProperties properties;

    public AbstractStrategy(DynamicProperties properties) {
        this.properties = properties;
    }

    /**
     * 标记策略名称
     * @return
     */
    public abstract String name();

    /**
     * 判断是否需要限流或熔断
     * @return  需要限流熔断则返回 true，否则 false
     */
    public abstract boolean exceed();

    /**
     * 记录事件的发生
     * @param eventType 事件类型
     * @param value     事件类型为 TIME 时，表示时间；其他情况表示量
     */
    public abstract void record(EventType eventType, long value);

    /**
     * 标记支持接收哪些事件类型
     * @return  接收的事件类型列表
     */
    public abstract List<EventType> support();

    /**
     * DynamicLimiter Properties 配置变更之后，调用此方法来更新一些数据。
     */
    public void update() {
        // default to nothing
    }

    protected String buildRateLimiterKey(DynamicProperties properties) {
        return properties.getKey() + "." + name() + ".rate.limiter";
    }
}

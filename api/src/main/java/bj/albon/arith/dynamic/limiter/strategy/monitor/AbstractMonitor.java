package bj.albon.arith.dynamic.limiter.strategy.monitor;

import bj.albon.arith.dynamic.limiter.config.DynamicProperties;

/**
 * 抽象监控信息类。
 * 通过构造方法引入 DynamicProperties 配置信息。
 */
public abstract class AbstractMonitor implements IMonitor {
    protected static final double EXCELLENT_RATE = 0.7;

    protected DynamicProperties properties;

    public AbstractMonitor(DynamicProperties properties) {
        this.properties = properties;
    }

    @Override
    public void close() {
        // default to nothing
    }
}

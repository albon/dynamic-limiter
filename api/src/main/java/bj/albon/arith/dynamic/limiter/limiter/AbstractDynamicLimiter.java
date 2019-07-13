package bj.albon.arith.dynamic.limiter.limiter;

import bj.albon.arith.dynamic.limiter.config.DynamicProperties;

/**
 * 這里放一些，可以在本 Jar 包其他类中使用，而又不希望 API 使用方使用的方法。
 */
public abstract class AbstractDynamicLimiter implements DynamicLimiter {

    /**
     * 配置更新后，更新 DynamicLimiter 内部数据。
     */
    public abstract void update();

    /**
     * 获取 DynamicLimiter 对应的配置 DynamicProperties
     *
     * @return 配置 DynamicProperties
     */
    public abstract DynamicProperties getProperties();

}

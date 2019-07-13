package bj.albon.arith.dynamic.limiter.config;

import bj.albon.arith.dynamic.limiter.model.PropertiesValid;
import bj.albon.arith.dynamic.limiter.model.SystemHealthyComputeInfo;
import bj.albon.arith.config.parser.api.annotation.AutoParseField;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

/**
 * 限流降级配置参数，limiter level
 * Created by albon on 17/7/23.
 */
@PropertiesValid
public class DynamicProperties {

    private String key;

    // 强制关闭此限流降级工具, limiter level
    @AutoParseField(key = "force.close")
    private boolean forceClose = false;

    // 动态限流下，当load低于此值时，不再限流，慢慢恢复
    // Load 策略下，判断系统负载过高的阈值，默认值为 CPU 核数
    @AutoParseField(key = "qps.need.limit.load.threshold")
    private double qpsNeedLimitLoadThreshold = Math.max(DefaultConfig.LOAD_THRESHOLD - 1, 1);

    // 根据负载动态限流下，load 高于此值时，限流阈值开始降低
    @AutoParseField(key = "qps.need.decrease.load.threshold")
    private double qpsNeedDecreaseLoadThreshold = DefaultConfig.LOAD_THRESHOLD + 1;

    // 动态限流下，当 cpu 利用率低于此值时，不再限流，慢慢恢复
    @AutoParseField(key = "qps.need.limit.cpu.rate.threshold")
    private double qpsNeedLimitCPURateThreshold = DefaultConfig.DEFAULT_QPS_NEED_LIMIT_CPU_RATE_THRESHOLD;

    // 根据负载动态限流下，cpu 利用率高于此值时，限流阈值开始降低
    @AutoParseField(key = "qps.need.decrease.cpu.rate.threshold")
    private double qpsNeedDecreaseCPURateThreshold = DefaultConfig.DEFAULT_QPS_NEED_DECREASE_CPU_RATE_THRESHOLD;

    // 动态限流策略下，动态限流系数，真实qps = base * factor，默认为 0.9
    @AutoParseField(key = "dynamic.limiter.qps.factor")
    @DecimalMax("1.0")
    @DecimalMin("0.0")
    private BigDecimal dynamicLimiterQpsFactor = DefaultConfig.QPS_FACTOR_IN_DYNAMIC_LIMITER;

    // 计数滑动窗口时长(单位为秒)
    @Min(1)
    @AutoParseField(key = "window.size.in.second")
    private int windowSizeInSecond = DefaultConfig.WINDOW_SIZE_IN_SECOND;

    // 计数滑动窗口滚动频率(单位毫秒)
    @Min(1)
    @AutoParseField(key = "bucket.size.in.ms")
    private int bucketSizeInMs = DefaultConfig.BUCKET_SIZE_IN_MS;

    // 触发熔断的接口失败率阈值
    @Min(0)
    @Max(100)
    @AutoParseField(key = "failure.rate.threshold")
    private int failureRateThreshold = DefaultConfig.FAILURE_RATE_THRESHOLD;

    /**
     * 熔断 CircuitBreaker 策略下，表示触发熔断的接口失败量阈值
     * 动态限流 RejectFeedback 策略下，表示开启限流阈值，即失败多少次开始限流
     */
    @Min(0)
    @AutoParseField(key = "failure.count.threshold")
    private int failureCountThreshold = DefaultConfig.FAILURE_COUNT_THRESHOLD;

    // 熔断多久之后尝试恢复(单位毫秒)
    @Min(1)
    @AutoParseField(key = "circuit.breaker.window.in.ms")
    private int circuitBreakerWindowInMs = DefaultConfig.CIRCUIT_BREAKER_WINDOW_IN_MS;

    // 熔断半关闭状态，连续成功多少次关闭熔断
    @Min(1)
    @AutoParseField(key = "continuous.success.num.close.circuit.breaker")
    private int continuousSuccessNumCloseCircuitBreaker = DefaultConfig.CONTINUOUS_SUCCESS_NUM_CLOSE_CIRCUIT_BREAKER;

    @Min(0)
    @AutoParseField(key = "qps.need.decrease.time.in.ms.threshold")
    private long qpsNeedDecreaseTimeInMsThreshold = DefaultConfig.DEFAULT_QPS_NEED_DECREASE_TIME_IN_MS_THRESHOLD;

    @Min(0)
    @AutoParseField(key = "qps.need.limit.time.in.ms.threshold")
    private long qpsNeedLimitTimeInMsThreshold = DefaultConfig.DEFAULT_QPS_NEED_LIMIT_TIME_IN_MS_THRESHOLD;

    @AutoParseField(key = "dynamic.limit.system.healthy.compute.formula")
    private SystemHealthyComputeInfo systemHealthyComputeInfo = SystemHealthyComputeInfo.DEFAULT;

    @AutoParseField(key = "load.dynamic.limit.http.fallback.url")
    private String loadDynamicLimitHttpFallbackUrl;

    public String getLoadDynamicLimitHttpFallbackUrl() {
        return loadDynamicLimitHttpFallbackUrl;
    }

    public SystemHealthyComputeInfo getSystemHealthyComputeInfo() {
        return systemHealthyComputeInfo;
    }

    public double getQpsNeedLimitCPURateThreshold() {
        return qpsNeedLimitCPURateThreshold;
    }

    public double getQpsNeedDecreaseCPURateThreshold() {
        return qpsNeedDecreaseCPURateThreshold;
    }

    public long getQpsNeedDecreaseTimeInMsThreshold() {
        return qpsNeedDecreaseTimeInMsThreshold;
    }

    public void setQpsNeedDecreaseTimeInMsThreshold(long qpsNeedDecreaseTimeInMsThreshold) {
        this.qpsNeedDecreaseTimeInMsThreshold = qpsNeedDecreaseTimeInMsThreshold;
    }

    public long getQpsNeedLimitTimeInMsThreshold() {
        return qpsNeedLimitTimeInMsThreshold;
    }

    public void setQpsNeedLimitTimeInMsThreshold(long qpsNeedLimitTimeInMsThreshold) {
        this.qpsNeedLimitTimeInMsThreshold = qpsNeedLimitTimeInMsThreshold;
    }

    public double getQpsNeedLimitLoadThreshold() {
        return qpsNeedLimitLoadThreshold;
    }

    public void setQpsNeedLimitLoadThreshold(double qpsNeedLimitLoadThreshold) {
        this.qpsNeedLimitLoadThreshold = qpsNeedLimitLoadThreshold;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public BigDecimal getDynamicLimiterQpsFactor() {
        return dynamicLimiterQpsFactor;
    }

    public void setDynamicLimiterQpsFactor(BigDecimal dynamicLimiterQpsFactor) {
        this.dynamicLimiterQpsFactor = dynamicLimiterQpsFactor;
    }

    public int getContinuousSuccessNumCloseCircuitBreaker() {
        return continuousSuccessNumCloseCircuitBreaker;
    }

    public void setContinuousSuccessNumCloseCircuitBreaker(int continuousSuccessNumCloseCircuitBreaker) {
        this.continuousSuccessNumCloseCircuitBreaker = continuousSuccessNumCloseCircuitBreaker;
    }

    public int getCircuitBreakerWindowInMs() {
        return circuitBreakerWindowInMs;
    }

    public void setCircuitBreakerWindowInMs(int circuitBreakerWindowInMs) {
        this.circuitBreakerWindowInMs = circuitBreakerWindowInMs;
    }

    public boolean isForceClose() {
        return forceClose;
    }

    public void setForceClose(boolean forceClose) {
        this.forceClose = forceClose;
    }

    public int getFailureRateThreshold() {
        return failureRateThreshold;
    }

    public void setFailureRateThreshold(int failureRateThreshold) {
        this.failureRateThreshold = failureRateThreshold;
    }

    public int getFailureCountThreshold() {
        return failureCountThreshold;
    }

    public void setFailureCountThreshold(int failureCountThreshold) {
        this.failureCountThreshold = failureCountThreshold;
    }

    public double getQpsNeedDecreaseLoadThreshold() {
        return qpsNeedDecreaseLoadThreshold;
    }

    public void setQpsNeedDecreaseLoadThreshold(double qpsNeedDecreaseLoadThreshold) {
        this.qpsNeedDecreaseLoadThreshold = qpsNeedDecreaseLoadThreshold;
    }

    public int getWindowSizeInSecond() {
        return windowSizeInSecond;
    }

    public void setWindowSizeInSecond(int windowSizeInSecond) {
        this.windowSizeInSecond = windowSizeInSecond;
    }

    public int getBucketSizeInMs() {
        return bucketSizeInMs;
    }

    public void setBucketSizeInMs(int bucketSizeInMs) {
        this.bucketSizeInMs = bucketSizeInMs;
    }

    @Override
    public String toString() {
        return "DynamicProperties{" + "key='" + key + '\'' + ", forceClose=" + forceClose + ", dynamicLimiterQpsFactor="
                + dynamicLimiterQpsFactor + ", qpsNeedLimitLoadThreshold=" + qpsNeedLimitLoadThreshold
                + ", qpsNeedDecreaseLoadThreshold=" + qpsNeedDecreaseLoadThreshold + ", qpsNeedLimitCPURateThreshold="
                + qpsNeedLimitCPURateThreshold + ", qpsNeedDecreaseCPURateThreshold=" + qpsNeedDecreaseCPURateThreshold
                + ", windowSizeInSecond=" + windowSizeInSecond + ", bucketSizeInMs=" + bucketSizeInMs
                + ", failureRateThreshold=" + failureRateThreshold + ", failureCountThreshold=" + failureCountThreshold
                + ", circuitBreakerWindowInMs=" + circuitBreakerWindowInMs
                + ", continuousSuccessNumCloseCircuitBreaker=" + continuousSuccessNumCloseCircuitBreaker
                + ", qpsNeedDecreaseTimeInMsThreshold=" + qpsNeedDecreaseTimeInMsThreshold
                + ", qpsNeedLimitTimeInMsThreshold=" + qpsNeedLimitTimeInMsThreshold + ", systemHealthyComputeInfo="
                + systemHealthyComputeInfo + '}';
    }
}

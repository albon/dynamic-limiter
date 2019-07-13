package bj.albon.arith.dynamic.limiter.model;

/**
 * @author albon
 *         Date: 17-7-21
 *         Time: 下午2:36
 */
public enum StrategyEnum {
    RejectFeedback, // 负载反馈限流
    CircuitBreaker, // 熔断器
    LoadDynamicLimit; // 根据负载动态限流

}

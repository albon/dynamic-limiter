package bj.albon.arith.dynamic.limiter.model;

/**
 * 重大事件类型，比如触发熔断、熔断恢复。
 */
public enum CriticalEventType {

    CIRCUIT_BREAKER_OPEN, // 触发熔断
    CIRCUIT_BREAKER_CLOSE, // 熔断恢复
    DYNAMIC_LIMITER_OPEN, // 触发动态限流
    DYNAMIC_LIMITER_CLOSE; // 动态限流关闭
}

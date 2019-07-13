package bj.albon.arith.dynamic.limiter.limiter;

import bj.albon.arith.dynamic.limiter.model.EventType;

/**
 * 限流、熔断器接口。
 *
 * @author albon
 *         Date: 17-7-21
 *         Time: 上午11:19
 */
public interface DynamicLimiter {

    /**
     * 合并各策略判断结果，暂时写成“与”逻辑，后续可以修改成根据配置决定计算逻辑
     *
     * @return 需要限流或熔断, 则返回 true; 否则, 返回 false
     */
    boolean exceed();

    /**
     * 记录1次事件的发生
     * 
     * @param eventType 事件类型: 成功 SUCCESS、失败 FAILURE、请求 PASS
     */
    void record(EventType eventType);

    /**
     * 记录事件的发生
     * 
     * @param eventType 事件类型: 成功 SUCCESS、失败 FAILURE、请求 PASS、时间 TIME
     * @param value 当事件为 TIME 时，表示事件处理时间，其他时候表示事件发生次数
     */
    void record(EventType eventType, long value);
}

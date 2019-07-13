package bj.albon.arith.dynamic.limiter.strategy.monitor;

import bj.albon.arith.dynamic.limiter.model.EventType;
import bj.albon.arith.dynamic.limiter.model.HealthStatus;
import bj.albon.arith.dynamic.limiter.model.MonitorType;

public interface IMonitor {

    /**
     * 标识类型
     * 
     * @return MonitorType
     */
    MonitorType type();

    /**
     * 根据此监控，判断系统健康状态。
     * 在动态限流策略里，根据此状态来决定是否要限流。
     * 
     * @return 返回 HealthStatus 枚举。
     */
    HealthStatus status();

    /**
     * 记录监控信息。
     * 接口响应时间之类的监控通过此方法记录，而系统相关监控不需要。
     * 
     * @param eventType 监控的信息类型
     * @param value 监控值
     */
    void record(EventType eventType, long value);

    /**
     * 关闭监控器做一些清理操作
     */
    void close();
}

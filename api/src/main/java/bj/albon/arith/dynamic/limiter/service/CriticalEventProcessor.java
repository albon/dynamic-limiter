package bj.albon.arith.dynamic.limiter.service;

import bj.albon.arith.dynamic.limiter.model.CriticalEventInfo;

/**
 * 重大事件处理器。
 * 请勿在该方法里做耗时操作，否则会阻塞其他事件处理。
 */
public interface CriticalEventProcessor {

    void process(CriticalEventInfo criticalEventInfo);

}

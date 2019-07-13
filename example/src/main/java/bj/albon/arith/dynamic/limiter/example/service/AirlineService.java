package bj.albon.arith.dynamic.limiter.example.service;

import bj.albon.arith.dynamic.limiter.example.util.DynamicLimiterUtil;
import bj.albon.arith.dynamic.limiter.model.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 熔断器用法示例
 */
public class AirlineService {
    private static final Logger logger = LoggerFactory.getLogger(AirlineService.class);

    /**
     * 熔断器测试接口
     *
     * @param useCorrectUrl true 表示使用正确的 url。false 表示使用错误的 url，错误 url 调用失败是用来触发熔断的。
     * @return 返回 true 表示正常执行。返回 false 表示触发了熔断，没有实际调用外部接口。
     */
    public static boolean query(boolean useCorrectUrl) {
        // 在调用外部服务之前，调用 exceed 方法判断是否需要调用外部服务
        if (DynamicLimiterUtil.CIRCUIT_BREAKER.exceed()) {
            return false;
        }

        try {
            // do something
            boolean success = Math.random() > 0.1;
            if (success) {
                DynamicLimiterUtil.CIRCUIT_BREAKER.record(EventType.SUCCESS);
            } else {
                DynamicLimiterUtil.CIRCUIT_BREAKER.record(EventType.FAILURE);
            }
        } catch (Exception e) {
            DynamicLimiterUtil.CIRCUIT_BREAKER.record(EventType.FAILURE);
            logger.error("call http error", e);
        }

        return true;
    }
}

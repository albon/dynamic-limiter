package bj.albon.arith.dynamic.limiter.example.util;

import bj.albon.arith.dynamic.limiter.config.DynamicLimiterConfig;
import bj.albon.arith.dynamic.limiter.limiter.DynamicLimiter;
import bj.albon.arith.dynamic.limiter.limiter.DynamicLimiterFactory;
import bj.albon.arith.dynamic.limiter.model.StrategyEnum;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

import java.util.Map;

public class DynamicLimiterUtil {

    // 动态限流器
    public static final DynamicLimiter LOAD_DYNAMIC_LIMITER = DynamicLimiterFactory.create(
            "loadDynamicLimiter", StrategyEnum.LoadDynamicLimit);

    // 熔断器
    public static final DynamicLimiter CIRCUIT_BREAKER = DynamicLimiterFactory.create(
            "circuitBreaker", StrategyEnum.CircuitBreaker);

    private static final Map<String, String> configMap = Maps.newHashMap();

    static {

        try {
            /**
             * 完整的可调参数可以看 DynamicProperties 类
             *
             * DynamicLimiterConfig 参数配置支持热更新，可以把参数配置在支持热更新的"配置中心里"。
             */

            // 设置动态限流参数
            configMap.put("dynamic.limiter.loadDynamicLimiter.qps.need.limit.load.threshold", "4");
            configMap.put("dynamic.limiter.loadDynamicLimiter.qps.need.decrease.load.threshold", "2");
            configMap.put("dynamic.limiter.loadDynamicLimiter.qps.need.limit.cpu.rate.threshold", "90");
            configMap.put("dynamic.limiter.loadDynamicLimiter.qps.need.decrease.cpu.rate.threshold", "70");
            // CPU 或 LOAD 超标时，便开始限流
            configMap.put("dynamic.limiter.loadDynamicLimiter.dynamic.limit.system.healthy.compute.formula",
                    "CPU | LOAD");

            // 设置熔断器参数
            configMap.put("dynamic.limiter.circuitBreaker.failure.rate.threshold", "80");
            configMap.put("dynamic.limiter.officialCircuitBreaker.continuous.success.num.close.circuit.breaker", "5");

            DynamicLimiterConfig.getInstance().reload(configMap);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }
}

package bj.albon.arith.dynamic.limiter.limiter;

import bj.albon.arith.dynamic.limiter.util.Constant;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import bj.albon.arith.dynamic.limiter.config.DynamicProperties;
import bj.albon.arith.dynamic.limiter.config.DynamicPropertiesFactory;
import bj.albon.arith.dynamic.limiter.model.StrategyEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * 限流器、熔断器等的创建工厂。
 *
 * @author albon
 *         Date: 17-7-21
 *         Time: 上午11:21
 */
public class DynamicLimiterFactory {
    private static final Logger logger = LoggerFactory.getLogger(DynamicLimiterFactory.class);

    private static ConcurrentMap<String, DynamicLimiter> dynamicLimiterMap = Maps.newConcurrentMap();

    /**
     * 创建并返回限流器或熔断器。
     *
     * @param limiterName 唯一标识, 同一个名称只会创建一次, 后续调用取的都是第一次创建的结果
     * @param strategyEnum 要创建的 dynamic limiter 所使用的具体策略, 比如: 熔断 CircuitBreaker、动态限流 LoadDynamicLimit
     * @return 创建成功的 dynamic limiter, 即限流器或熔断器
     * @throws IllegalArgumentException 参数 limiterName 为空或等于 default时, 或参数 strategyList 为空时, 会抛出此异常
     * @see DynamicLimiter
     */
    public static DynamicLimiter create(String limiterName, StrategyEnum strategyEnum) {
        return create(limiterName, Lists.newArrayList(strategyEnum));
    }

    /**
     * 创建并返回限流器或熔断器。
     *
     * @param limiterName 唯一标识, 同一个名称只会创建一次, 后续调用取的都是第一次创建的结果
     * @param strategyList 要创建的 dynamic limiter 所使用的具体策略, 比如: 熔断 CircuitBreaker、动态限流 LoadDynamicLimit
     *            原本设计为 List，是考虑到可以让多个策略共同工作，目前看在实际中同时使用多个策略的情况很少。
     * @return 创建成功的 dynamic limiter, 即限流器或熔断器
     * @throws IllegalArgumentException 参数 limiterName 为空或等于 default时, 或参数 strategyList 为空时, 会抛出此异常
     * @see DynamicLimiter
     */
    @Deprecated
    public static DynamicLimiter create(String limiterName, List<StrategyEnum> strategyList) {
        Preconditions.checkArgument(StringUtils.isNotBlank(limiterName));
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(strategyList));
        Preconditions.checkArgument(!limiterName.equals(Constant.DEFAULT_LIMITER), "limiterName con't be default");

        if (dynamicLimiterMap.containsKey(limiterName)) {
            return dynamicLimiterMap.get(limiterName);
        }

        synchronized (DynamicPropertiesFactory.class) {
            if (dynamicLimiterMap.containsKey(limiterName)) {
                return dynamicLimiterMap.get(limiterName);
            }
            
            DynamicLimiterImpl dynamicLimiter = new DynamicLimiterImpl();
            dynamicLimiter.init(limiterName, strategyList);

            logger.info("addDynamicLimiter name: {}, dynamicLimiter: {}, strategyList: {}", limiterName, dynamicLimiter,
                    strategyList);
            dynamicLimiterMap.put(limiterName, dynamicLimiter);
            return dynamicLimiter;
        }
    }

    /**
     * 根据 limiterName 获取对应的 DynamicLimiter。
     *
     * @param limiterName   DynamicLimiter 唯一标识
     * @return              DynamicLimiter，如果不存在，会返回 null。
     */
    public static DynamicLimiter query(String limiterName) {
        Preconditions.checkArgument(StringUtils.isNotBlank(limiterName));

        return dynamicLimiterMap.get(limiterName);
    }

    public static DynamicLimiter createIfPropertyExist(String limiterName, StrategyEnum strategyEnum) {
        Preconditions.checkArgument(StringUtils.isNotBlank(limiterName));
        Preconditions.checkArgument(!limiterName.equals(Constant.DEFAULT_LIMITER), "limiterName con't be default");
        Preconditions.checkNotNull(strategyEnum);

        if (dynamicLimiterMap.containsKey(limiterName)) {
            return dynamicLimiterMap.get(limiterName);
        }

        DynamicProperties dynamicProperties = DynamicPropertiesFactory.query(limiterName);
        if (dynamicProperties == null) {
            return null;
        }

        return create(limiterName, strategyEnum);
    }
}

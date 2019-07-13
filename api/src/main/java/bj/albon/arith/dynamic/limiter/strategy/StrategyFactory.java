package bj.albon.arith.dynamic.limiter.strategy;

import bj.albon.arith.dynamic.limiter.config.DynamicProperties;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import bj.albon.arith.dynamic.limiter.model.StrategyEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * 策略工厂
 *
 * @author albon
 *         Date: 17-7-21
 *         Time: 下午2:32
 */
public class StrategyFactory {
    private static final Logger logger = LoggerFactory.getLogger(StrategyFactory.class);

    private static final Map<StrategyEnum, Class<? extends AbstractStrategy>> strategyMap = Maps.newHashMap();

    static {
        loadStrategy(StrategyEnum.RejectFeedback, RejectFeedbackStrategy.class);
        loadStrategy(StrategyEnum.CircuitBreaker, CircuitBreakerStrategy.class);
        loadStrategy(StrategyEnum.LoadDynamicLimit, LoadDynamicLimitStrategy.class);
    }

    private static synchronized void loadStrategy(StrategyEnum strategy, Class zClass) {
        logger.info("loadStrategy strategy: {}, class: {}", strategy, zClass);
        Preconditions.checkNotNull(strategy, "strategy con't be null!");
        Preconditions.checkNotNull(zClass, "strategy con't be null!");
        Preconditions.checkArgument(AbstractStrategy.class.isAssignableFrom(zClass));

        if (strategyMap.containsKey(strategy)) {
            Class existedClass = strategyMap.get(strategy);
            if (!existedClass.equals(zClass)) {
                String errorMessage = StringUtils.join("class conflict strategy: ", strategy, ", existedClass: ",
                        existedClass.getName(), ", newClass: ", zClass.getName());
                logger.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
        } else {
            strategyMap.put(strategy, zClass);
        }
    }

    public static AbstractStrategy create(StrategyEnum strategy, DynamicProperties dynamicProperties)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Preconditions.checkNotNull(strategy, "strategy con't be null!");
        Preconditions.checkNotNull(dynamicProperties, "dynamicProperties con't be null!");

        Class aClass = strategyMap.get(strategy);
        Constructor constructor = aClass.getConstructor(DynamicProperties.class);
        AbstractStrategy abstractStrategy = (AbstractStrategy) constructor.newInstance(dynamicProperties);
        logger.info("key: {}, strategy: {}", dynamicProperties.getKey(), strategy);

        return abstractStrategy;
    }
}

package bj.albon.arith.dynamic.limiter.config;

import bj.albon.arith.dynamic.limiter.util.Constant;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by albon on 17/7/23.
 */
public class DynamicPropertiesFactory {
    private static final Logger logger = LoggerFactory.getLogger(DynamicPropertiesFactory.class);
    private static final String[] IGNORE_PROPERTIES = { "key" };

    private static ConcurrentMap<String, DynamicProperties> dynamicPropertiesMap = Maps.newConcurrentMap();

    public static DynamicProperties get(String key) {
        if (dynamicPropertiesMap.containsKey(key)) {
            return dynamicPropertiesMap.get(key);
        }

        DynamicProperties dynamicProperties = new DynamicProperties();
        if (!key.equals(Constant.DEFAULT_LIMITER)) {
            DynamicProperties defaultProperties = get(Constant.DEFAULT_LIMITER);
            BeanUtils.copyProperties(defaultProperties, dynamicProperties);
        }
        dynamicProperties.setKey(key);

        DynamicProperties existingProperties = dynamicPropertiesMap.putIfAbsent(key, dynamicProperties);
        return existingProperties == null ? dynamicProperties : existingProperties;
    }

    public static DynamicProperties query(String key) {
        return dynamicPropertiesMap.get(key);
    }

    /**
     * 更新其他使用默认配置的properties
     * 
     * @param excludeKeySet
     */
    public static void applyDefaultPropertiesToOthers(Set<String> excludeKeySet) {
        DynamicProperties defaultProperties = get(Constant.DEFAULT_LIMITER);
        Iterator<DynamicProperties> iterator = dynamicPropertiesMap.values().iterator();
        while (iterator.hasNext()) {
            DynamicProperties properties = iterator.next();
            if (properties.getKey().equals(Constant.DEFAULT_LIMITER) || excludeKeySet.contains(properties.getKey())) {
                continue;
            }

            logger.info("dynamicPropertiesUpdate key: {}, before update: {}", properties.getKey(), properties);
            BeanUtils.copyProperties(defaultProperties, properties, IGNORE_PROPERTIES);
            logger.info("dynamicPropertiesUpdate key: {}, after update: {}", properties.getKey(), properties);
        }
    }
}

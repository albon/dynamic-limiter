package bj.albon.arith.dynamic.limiter.config;

import bj.albon.arith.config.parser.api.exception.ConfigParseException;
import bj.albon.arith.config.parser.api.service.ConfigParser;
import bj.albon.arith.config.parser.api.service.FieldParserFactory;
import bj.albon.arith.dynamic.limiter.config.parser.StringListStringMapFieldParser;
import bj.albon.arith.dynamic.limiter.config.parser.SystemHealthyComputeInfoFieldParser;
import bj.albon.arith.dynamic.limiter.limiter.DynamicLimiter;
import bj.albon.arith.dynamic.limiter.limiter.DynamicLimiterFactory;
import bj.albon.arith.dynamic.limiter.util.Constant;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import bj.albon.arith.dynamic.limiter.limiter.AbstractDynamicLimiter;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author albon
 *         Date: 17-7-24
 *         Time: 下午3:52
 */
public class DynamicLimiterConfig {
    private static final Logger logger = LoggerFactory.getLogger(DynamicLimiterConfig.class);
    private static final String LIMITER_PREFIX = "dynamic.limiter.";
    private static final String GLOBAL_PREFIX = "global.";

    public static final String SEPARATOR = ".";
    private static final DynamicLimiterConfig INSTANCE = new DynamicLimiterConfig();

    public static DynamicLimiterConfig getInstance() {
        return INSTANCE;
    }

    public void reload(Map<String, String> configMap) throws ConfigParseException {
        try {
            logger.info("configMap: {}", configMap);

            Map<String, String> globalConfigMap = filterGlobalConfigMap(configMap);
            logger.info("globalConfig globalConfigMap: {}", globalConfigMap);
            logger.info("globalConfig old: {}", GlobalConfig.instance());
            ConfigParser.parse(globalConfigMap, GlobalConfig.instance());
            logger.info("globalConfig new: {}", GlobalConfig.instance());

            Map<String, Map<String, String>> limiterPropertyMap = groupByLimiterKey(configMap);

            Map<String, String> defaultConfigMap = limiterPropertyMap.get(Constant.DEFAULT_LIMITER);
            parseAndUpdateDefaultProperties(defaultConfigMap);

            Set<String> customizeKeySet = Sets.newHashSet();
            Map<String, List<String>> limiterAliasMap = GlobalConfig.instance().getLimiterAliasMap();
            for (Map.Entry<String, Map<String, String>> entry : limiterPropertyMap.entrySet()) {
                String key = entry.getKey();
                // 合并 default 配置和个性化配置
                Map<String, String> config = mergeDefaultAndCustomize(defaultConfigMap, entry.getValue());

                // 一对多的映射关系
                List<String> limiterKeyList = limiterAliasMap.containsKey(key) ? limiterAliasMap.get(key)
                        : Lists.newArrayList(key);
                customizeKeySet.addAll(limiterKeyList);
                for (String limiterKey : limiterKeyList) {
                    DynamicProperties dynamicProperties = DynamicPropertiesFactory.get(limiterKey);
                    parseAndUpdateProperties(limiterKey, config, dynamicProperties);

                    DynamicLimiter dynamicLimiter = DynamicLimiterFactory.query(dynamicProperties.getKey());
                    if (dynamicLimiter != null && dynamicLimiter instanceof AbstractDynamicLimiter) {
                        logger.info("call dynamicLimiter.update key: {}", dynamicProperties.getKey());
                        ((AbstractDynamicLimiter) dynamicLimiter).update();
                    }
                }
            }

            DynamicPropertiesFactory.applyDefaultPropertiesToOthers(customizeKeySet);
        } catch (Exception e) {
            logger.error("dynamicLimiter config reload error", e);
            throw e;
        }
    }

    private Map<String, String> mergeDefaultAndCustomize(Map<String, String> defaultMap,
            Map<String, String> customizeMap) {
        Map<String, String> configMap = Maps.newHashMap();
        if (defaultMap != null) {
            configMap.putAll(defaultMap);
        }
        if (customizeMap != null) {
            configMap.putAll(customizeMap);
        }
        return configMap;
    }

    private void parseAndUpdateDefaultProperties(Map<String, String> defaultConfigMap) throws ConfigParseException {
        if (MapUtils.isEmpty(defaultConfigMap)) {
            return;
        }

        DynamicProperties dynamicProperties = DynamicPropertiesFactory.get(Constant.DEFAULT_LIMITER);
        parseAndUpdateProperties(Constant.DEFAULT_LIMITER, defaultConfigMap, dynamicProperties);
    }

    private void parseAndUpdateProperties(String key, Map<String, String> config, DynamicProperties dynamicProperties)
            throws ConfigParseException {
        logger.info("dynamicProperties key: {}, configMap: {}", key, config);
        logger.info("dynamicProperties key: {}, old: {}", key, dynamicProperties);
        ConfigParser.parse(config, dynamicProperties);
        logger.info("dynamicProperties key: {}, new: {}", key, dynamicProperties);
    }

    private Map<String, String> filterGlobalConfigMap(Map<String, String> configMap) {
        Map<String, String> globalMap = Maps.newHashMap();
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            if (!entry.getKey().startsWith(GLOBAL_PREFIX)) {
                continue;
            }

            globalMap.put(entry.getKey().substring(GLOBAL_PREFIX.length()), entry.getValue());
        }
        return globalMap;
    }

    private Map<String, Map<String, String>> groupByLimiterKey(Map<String, String> configMap) {
        Map<String, Map<String, String>> limiterPropertyMap = Maps.newHashMap();
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            if (!entry.getKey().startsWith(LIMITER_PREFIX)) {
                continue;
            }

            String postfix = entry.getKey().substring(LIMITER_PREFIX.length());
            int keyEndPos = postfix.indexOf(SEPARATOR);
            if (keyEndPos <= 0) {
                continue;
            }

            String limiterKey = postfix.substring(0, keyEndPos);
            String fieldKey = postfix.substring(keyEndPos + 1);

            if (!limiterPropertyMap.containsKey(limiterKey)) {
                Map<String, String> valueMap = Maps.newHashMap();
                limiterPropertyMap.put(limiterKey, valueMap);
            }

            limiterPropertyMap.get(limiterKey).put(fieldKey, entry.getValue());
        }

        return limiterPropertyMap;
    }

    static {
        FieldParserFactory.addFieldParser(new SystemHealthyComputeInfoFieldParser());
        FieldParserFactory.addFieldParser(new StringListStringMapFieldParser());
    }
}

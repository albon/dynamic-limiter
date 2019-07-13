package bj.albon.arith.dynamic.limiter.config.parser;

import bj.albon.arith.config.parser.api.util.SplitUtil;
import com.google.common.collect.Maps;
import bj.albon.arith.config.parser.api.service.MultiFieldParser;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @author albon
 *         Date: 17-8-14
 *         Time: 下午3:12
 */
public class StringListStringMapFieldParser extends MultiFieldParser<Map<String, List<String>>> {
    @Override
    public Map<String, List<String>> parse(Map<String, String> configMap, String prefix) {
        Map<String, List<String>> resultMap = Maps.newHashMap();

        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith(prefix)) {
                continue;
            }

            String subKey = key.substring(prefix.length());
            if (StringUtils.isBlank(subKey)) {
                continue;
            }

            List<String> valueList = SplitUtil.getList(SplitUtil.SPLIT_COMMA, entry.getValue());
            if (CollectionUtils.isEmpty(valueList)) {
                continue;
            }

            resultMap.put(subKey, valueList);
        }

        return resultMap;
    }
}

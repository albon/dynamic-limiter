package bj.albon.arith.dynamic.limiter.config.parser;

import bj.albon.arith.config.parser.api.util.SplitUtil;
import bj.albon.arith.dynamic.limiter.model.MonitorType;
import bj.albon.arith.dynamic.limiter.model.SystemHealthyComputeInfo;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import bj.albon.arith.config.parser.api.service.SingleFieldParser;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class SystemHealthyComputeInfoFieldParser extends SingleFieldParser<SystemHealthyComputeInfo> {
    @Override
    public SystemHealthyComputeInfo parse(String value) {
        Preconditions.checkArgument(StringUtils.isNotBlank(value));
        Preconditions.checkArgument(
                !(value.contains(SystemHealthyComputeInfo.ComputeType.AND.symbol)
                        && value.contains(SystemHealthyComputeInfo.ComputeType.OR.symbol)),
                "健康度计算公式不能既有 & 又有 |, 公式： " + value);

        SystemHealthyComputeInfo computeInfo = new SystemHealthyComputeInfo();
        SystemHealthyComputeInfo.ComputeType computeType = value
                .contains(SystemHealthyComputeInfo.ComputeType.AND.symbol) ? SystemHealthyComputeInfo.ComputeType.AND
                        : SystemHealthyComputeInfo.ComputeType.OR;
        computeInfo.setComputeType(computeType);
        computeInfo.setMonitorTypeList(extractMonitorType(value, computeType));

        return computeInfo;
    }

    private List<MonitorType> extractMonitorType(String value, SystemHealthyComputeInfo.ComputeType computeType) {
        List<String> wordList = SplitUtil.getList(computeType.symbol, value);
        List<MonitorType> monitorTypeList = Lists.newArrayList();

        for (String word : wordList) {
            MonitorType monitorType = MonitorType.valueOf(word);
            Preconditions.checkNotNull(monitorType, "MonitorType %s not found!", word);

            monitorTypeList.add(monitorType);
        }

        return monitorTypeList;
    }
}

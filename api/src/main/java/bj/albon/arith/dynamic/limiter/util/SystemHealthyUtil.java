package bj.albon.arith.dynamic.limiter.util;

import bj.albon.arith.dynamic.limiter.model.HealthStatus;
import bj.albon.arith.dynamic.limiter.model.MonitorType;
import bj.albon.arith.dynamic.limiter.model.SystemHealthyMonitorInfo;
import com.google.common.collect.Lists;
import bj.albon.arith.dynamic.limiter.model.SystemHealthyComputeInfo;
import bj.albon.arith.dynamic.limiter.strategy.monitor.IMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class SystemHealthyUtil {
    private static final Logger logger = LoggerFactory.getLogger(SystemHealthyUtil.class);

    /**
     * 根据 IMonitor 计算当前系统健康程度
     * 
     * @param systemHealthyMonitorInfo IMonitor 信息和计算公式信息
     * @return HealthStatus
     */
    public static HealthStatus compute(SystemHealthyMonitorInfo systemHealthyMonitorInfo) {
        Map<MonitorType, IMonitor> monitorMap = systemHealthyMonitorInfo.getMonitorMap();
        SystemHealthyComputeInfo systemHealthyComputeInfo = systemHealthyMonitorInfo.getSystemHealthyComputeInfo();

        List<HealthStatus> healthStatusList = Lists.newArrayList();
        for (Map.Entry<MonitorType, IMonitor> entry : monitorMap.entrySet()) {
            healthStatusList.add(entry.getValue().status());
        }

        HealthStatus healthStatus = systemHealthyComputeInfo.getComputeType().choose(healthStatusList);
        logger.debug("computeSystemHealthy healthStatusList: {}, choose: {}", healthStatusList, healthStatus);
        return healthStatus;
    }

    /**
     * 关闭 IMonitor，避免内存泄露
     * 
     * @param oldMonitorMap 不再使用的 Monitor
     */
    public static void closeMonitor(Map<MonitorType, IMonitor> oldMonitorMap) {
        if (oldMonitorMap == null) {
            return;
        }

        for (Map.Entry<MonitorType, IMonitor> entry : oldMonitorMap.entrySet()) {
            IMonitor monitor = entry.getValue();
            monitor.close();
        }
    }
}

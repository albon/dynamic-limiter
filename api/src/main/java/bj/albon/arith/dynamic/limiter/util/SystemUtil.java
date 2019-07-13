package bj.albon.arith.dynamic.limiter.util;

import bj.albon.arith.dynamic.limiter.model.SystemInfo;
import bj.albon.arith.dynamic.limiter.config.GlobalConfig;
import bj.albon.arith.dynamic.limiter.model.CPUUseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * 工具类，获取系统负载信息
 * 
 * @author albon
 *         Date: 17-7-24
 *         Time: 上午10:29
 */
public class SystemUtil {
    private static final Logger logger = LoggerFactory.getLogger(SystemUtil.class);

    public static void monitorLoad() {
        double currentLoad = SystemUtil.getLoad();
        SystemInfo.getInstance().setCurrentLoad(currentLoad);
        logger.debug("currentLoad = {}", currentLoad);
    }

    public static void monitorCPURate() {
        CPUUseInfo lastUpdateCPUUseInfo = SystemInfo.getInstance().getLastUpdateCPUUseInfo();
        CPUUseInfo cpuUseInfo = CPUUtil.readCPUUseInfo();

        double cpuRate = 0.0;
        if (cpuUseInfo != null && lastUpdateCPUUseInfo != null) {
            cpuRate = CPUUtil.computeCPURate(cpuUseInfo, lastUpdateCPUUseInfo);
        }

        SystemInfo.getInstance().setCpuRate(cpuRate);
        SystemInfo.getInstance().setLastUpdateCPUUseInfo(cpuUseInfo);
        logger.debug("cpuRate = {}", cpuRate);
    }

    public static int getAvailableProcessors() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        return operatingSystemMXBean.getAvailableProcessors();
    }

    private static double getLoad() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        return operatingSystemMXBean.getSystemLoadAverage();
    }
}

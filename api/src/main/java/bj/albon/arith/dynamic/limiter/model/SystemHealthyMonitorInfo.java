package bj.albon.arith.dynamic.limiter.model;

import bj.albon.arith.dynamic.limiter.strategy.monitor.IMonitor;

import java.util.Map;

/**
 * @author albon
 *         Date: 17-11-23
 *         Time: 下午8:05
 */
public class SystemHealthyMonitorInfo {
    private SystemHealthyComputeInfo systemHealthyComputeInfo;
    private Map<MonitorType, IMonitor> monitorMap;

    public SystemHealthyMonitorInfo(Map<MonitorType, IMonitor> monitorMap,
            SystemHealthyComputeInfo systemHealthyComputeInfo) {
        this.monitorMap = monitorMap;
        this.systemHealthyComputeInfo = systemHealthyComputeInfo;
    }

    public Map<MonitorType, IMonitor> getMonitorMap() {
        return monitorMap;
    }

    public void setMonitorMap(Map<MonitorType, IMonitor> monitorMap) {
        this.monitorMap = monitorMap;
    }

    public SystemHealthyComputeInfo getSystemHealthyComputeInfo() {
        return systemHealthyComputeInfo;
    }

    public void setSystemHealthyComputeInfo(SystemHealthyComputeInfo systemHealthyComputeInfo) {
        this.systemHealthyComputeInfo = systemHealthyComputeInfo;
    }

    @Override
    public String toString() {
        return "SystemHealthyMonitorInfo{" + "systemHealthyComputeInfo=" + systemHealthyComputeInfo + ", monitorMap="
                + monitorMap + '}';
    }
}

package bj.albon.arith.dynamic.limiter.model;

/**
 * @author albon
 *         Date: 17-11-29
 *         Time: 上午10:11
 */
public final class SystemInfo {
    private static final SystemInfo INSTANCE = new SystemInfo();

    private volatile double currentLoad = 0;
    private volatile double cpuRate = 0;
    private volatile CPUUseInfo lastUpdateCPUUseInfo;

    private SystemInfo() {
        // do nothing
    }

    public static SystemInfo getInstance() {
        return INSTANCE;
    }

    public double getCurrentLoad() {
        return currentLoad;
    }

    public void setCurrentLoad(double currentLoad) {
        this.currentLoad = currentLoad;
    }

    public double getCpuRate() {
        return cpuRate;
    }

    public void setCpuRate(double cpuRate) {
        this.cpuRate = cpuRate;
    }

    public CPUUseInfo getLastUpdateCPUUseInfo() {
        return lastUpdateCPUUseInfo;
    }

    public void setLastUpdateCPUUseInfo(CPUUseInfo lastUpdateCPUUseInfo) {
        this.lastUpdateCPUUseInfo = lastUpdateCPUUseInfo;
    }
}

package bj.albon.arith.dynamic.limiter.model;

/**
 * @author albon
 *         Date: 17-11-23
 *         Time: 下午3:12
 */
public class CPUUseInfo {
    private long idleTime;
    private long totalTime;

    public CPUUseInfo(long idleTime, long totalTime) {
        this.idleTime = idleTime;
        this.totalTime = totalTime;
    }

    public long getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(long idleTime) {
        this.idleTime = idleTime;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    @Override
    public String toString() {
        return "CPUUseInfo{" + "idleTime=" + idleTime + ", totalTime=" + totalTime + '}';
    }
}

package bj.albon.arith.dynamic.limiter.model;

public class CriticalEventInfo {
    // Dynamic Limiter 的唯一标识
    private String name;
    private CriticalEventType criticalEventType;

    public CriticalEventInfo() {
    }

    public CriticalEventInfo(String name, CriticalEventType criticalEventType) {
        this.name = name;
        this.criticalEventType = criticalEventType;
    }

    public String getName() {
        return name;
    }

    public CriticalEventType getCriticalEventType() {
        return criticalEventType;
    }

    @Override
    public String toString() {
        return "CriticalEventInfo{" + "name='" + name + '\'' + ", criticalEventType=" + criticalEventType + '}';
    }
}

package bj.albon.arith.dynamic.limiter.strategy.monitor;

import bj.albon.arith.dynamic.limiter.config.DynamicProperties;
import bj.albon.arith.dynamic.limiter.model.EventType;
import bj.albon.arith.dynamic.limiter.model.HealthStatus;
import bj.albon.arith.dynamic.limiter.model.MonitorType;
import bj.albon.arith.dynamic.limiter.model.SystemInfo;

public class CPUMonitor extends AbstractMonitor {
    public CPUMonitor(DynamicProperties properties) {
        super(properties);
    }

    @Override
    public MonitorType type() {
        return MonitorType.CPU;
    }

    @Override
    public HealthStatus status() {
        double cpuRate = SystemInfo.getInstance().getCpuRate();
        if (cpuRate < properties.getQpsNeedLimitCPURateThreshold() * EXCELLENT_RATE) {
            return HealthStatus.EXCELLENT;
        } else if (cpuRate < properties.getQpsNeedLimitCPURateThreshold()) {
            return HealthStatus.HEALTHY;
        } else if (cpuRate < properties.getQpsNeedDecreaseCPURateThreshold()) {
            return HealthStatus.NOT_HEALTHY;
        } else {
            return HealthStatus.WORSE;
        }
    }

    @Override
    public void record(EventType eventType, long value) {
        // do nothing, this monitor get info from system
    }
}

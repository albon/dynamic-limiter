package bj.albon.arith.dynamic.limiter.strategy.monitor;

import bj.albon.arith.dynamic.limiter.model.EventType;
import bj.albon.arith.dynamic.limiter.model.HealthStatus;
import bj.albon.arith.dynamic.limiter.model.MonitorType;
import bj.albon.arith.dynamic.limiter.model.SystemInfo;
import bj.albon.arith.dynamic.limiter.config.DynamicProperties;

public class LoadMonitor extends AbstractMonitor {

    public LoadMonitor(DynamicProperties properties) {
        super(properties);
    }

    @Override
    public MonitorType type() {
        return MonitorType.LOAD;
    }

    @Override
    public HealthStatus status() {
        double currentLoad = SystemInfo.getInstance().getCurrentLoad();
        if (currentLoad < properties.getQpsNeedLimitLoadThreshold() * EXCELLENT_RATE) {
            return HealthStatus.EXCELLENT;
        } else if (currentLoad < properties.getQpsNeedLimitLoadThreshold()) {
            return HealthStatus.HEALTHY;
        } else if (currentLoad < properties.getQpsNeedDecreaseLoadThreshold()) {
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

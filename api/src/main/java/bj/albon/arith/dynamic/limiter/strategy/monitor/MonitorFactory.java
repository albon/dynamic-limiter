package bj.albon.arith.dynamic.limiter.strategy.monitor;

import bj.albon.arith.dynamic.limiter.model.DynamicLimiterException;
import bj.albon.arith.dynamic.limiter.model.MonitorType;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import bj.albon.arith.dynamic.limiter.config.DynamicProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Map;

public class MonitorFactory {
    private static final Logger logger = LoggerFactory.getLogger(MonitorFactory.class);

    private static final Map<MonitorType, Class<? extends AbstractMonitor>> monitorMap = Maps.newHashMap();

    static {
        monitorMap.put(MonitorType.LOAD, LoadMonitor.class);
        monitorMap.put(MonitorType.CPU, CPUMonitor.class);
        monitorMap.put(MonitorType.TIME, TimeMonitor.class);
    }

    public static IMonitor create(MonitorType type, DynamicProperties properties) {
        Preconditions.checkNotNull(type, "MonitorType con't be null!");
        Preconditions.checkNotNull(properties, "properties con't be null!");

        Class<? extends AbstractMonitor> aClass = monitorMap.get(type);
        Preconditions.checkNotNull(aClass, "Class con't be find, monitorType = %s", type);

        try {
            Constructor constructor = aClass.getConstructor(DynamicProperties.class);
            AbstractMonitor abstractMonitor = (AbstractMonitor) constructor.newInstance(properties);
            return abstractMonitor;
        } catch (Exception e) {
            logger.error("IMonitor create error, type = {}", type, e);
            throw new DynamicLimiterException("IMonitor create error", e);
        }
    }

}

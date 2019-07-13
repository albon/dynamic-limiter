package bj.albon.arith.dynamic.limiter.strategy.monitor;

import bj.albon.arith.dynamic.limiter.model.EventType;
import bj.albon.arith.dynamic.limiter.model.HealthStatus;
import bj.albon.arith.dynamic.limiter.model.MonitorItem;
import bj.albon.arith.dynamic.limiter.model.MonitorType;
import com.google.common.base.Preconditions;
import bj.albon.arith.dynamic.limiter.config.DynamicProperties;
import bj.albon.arith.dynamic.limiter.counter.AvgTimeStatisticStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action1;

public class TimeMonitor extends AbstractMonitor {
    private static final Logger logger = LoggerFactory.getLogger(TimeMonitor.class);

    // TimeMonitor 用于记录最近处理耗时，所以滑动窗口长度不能太长
    private static final int NUM_BUCKETS = 5;
    private static final int BUCKET_SIZE_IN_MS = 1000;

    private AvgTimeStatisticStream timeStream;
    private long time = 0L;

    public TimeMonitor(DynamicProperties properties) {
        super(properties);

        timeStream = AvgTimeStatisticStream.getInstance(NUM_BUCKETS, BUCKET_SIZE_IN_MS);
        timeStream.subscribe(new Action1<MonitorItem>() {
            @Override
            public void call(MonitorItem monitorItem) {
                if (monitorItem.getCount() > 0) {
                    time = monitorItem.getTime() / monitorItem.getCount();
                } else {
                    time = 0L;
                }

                logger.debug("timeMonitor: {}", time);
            }
        });
    }

    @Override
    public MonitorType type() {
        return MonitorType.TIME;
    }

    @Override
    public HealthStatus status() {
        long currentTime = time;
        if (currentTime < properties.getQpsNeedLimitTimeInMsThreshold() * EXCELLENT_RATE) {
            return HealthStatus.EXCELLENT;
        } else if (currentTime < properties.getQpsNeedLimitTimeInMsThreshold()) {
            return HealthStatus.HEALTHY;
        } else if (currentTime < properties.getQpsNeedDecreaseTimeInMsThreshold()) {
            return HealthStatus.NOT_HEALTHY;
        } else {
            return HealthStatus.WORSE;
        }
    }

    @Override
    public void record(EventType eventType, long value) {
        Preconditions.checkArgument(eventType.equals(EventType.TIME));

        timeStream.publish(value);
    }

    /**
     * 关闭 timeStream 之后，将不再计数
     */
    @Override
    public void close() {
        timeStream.close();
    }
}

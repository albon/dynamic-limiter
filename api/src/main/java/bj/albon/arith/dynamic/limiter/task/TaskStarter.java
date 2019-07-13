package bj.albon.arith.dynamic.limiter.task;

import bj.albon.arith.dynamic.limiter.util.QMonitorKey;
import bj.albon.arith.dynamic.limiter.util.SystemUtil;
import bj.albon.arith.dynamic.limiter.util.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author albon
 *         Date: 17-8-1
 *         Time: 下午2:15
 */
@Component
public class TaskStarter {
    private static final Logger logger = LoggerFactory.getLogger(TaskStarter.class);

    private static final int INITIAL_DELAY_SECONDS = 1;
    private static final int SYSTEM_MONITOR_TASK_SECONDS = 1;
    private static final int CPU_MONITOR_PERIOD_SECONDS = 5; // CPU 计算时时间需要长一些，避免计算本身的消耗影响 CPU 占用率

    @PostConstruct
    public void init() {
        logger.info("initScheduledTask start ......");
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

        scheduledExecutorService.scheduleAtFixedRate(new MonitorTask(), INITIAL_DELAY_SECONDS,
                SYSTEM_MONITOR_TASK_SECONDS, TimeUnit.SECONDS);

        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                monitorCPURate();
            }
        }, INITIAL_DELAY_SECONDS, CPU_MONITOR_PERIOD_SECONDS, TimeUnit.SECONDS);
        logger.info("initScheduledTask end ......");
    }

    private void monitorCPURate() {
        try {
            SystemUtil.monitorCPURate();
        } catch (Exception e) {
            logger.error(QMonitorKey.DYNAMIC_LIMITER_MONITOR_CPU_RATE_ERROR, e);
            Monitor.recordOne(QMonitorKey.DYNAMIC_LIMITER_MONITOR_CPU_RATE_ERROR);
        }
    }
}

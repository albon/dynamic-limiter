package bj.albon.arith.dynamic.limiter.task;

import bj.albon.arith.dynamic.limiter.util.QMonitorKey;
import bj.albon.arith.dynamic.limiter.util.SystemUtil;
import bj.albon.arith.dynamic.limiter.util.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 定时任务，监控系统 load
 * 
 * @author albon
 *         Date: 17-7-21
 *         Time: 下午4:59
 */
public class MonitorTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MonitorTask.class);

    @Override
    public void run() {
        try {
            SystemUtil.monitorLoad();
        } catch (Exception e) {
            logger.error(QMonitorKey.DYNAMIC_LIMITER_ERROR_MONITOR_LOAD, e);
            Monitor.recordOne(QMonitorKey.DYNAMIC_LIMITER_ERROR_MONITOR_LOAD);
        }
    }

}

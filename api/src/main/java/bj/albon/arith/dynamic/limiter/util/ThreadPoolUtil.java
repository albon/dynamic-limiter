package bj.albon.arith.dynamic.limiter.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 对线程池创建进行封装，内置默认的异常处理、线程命名。
 *
 * @author albon
 * @date 16-8-29 上午11:51
 */
public class ThreadPoolUtil {
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolUtil.class);

    public static ThreadPoolExecutor createArrayBlockingQueuePoolExecutor(int corePoolSize, int maximumPoolSize,
            long keepAliveTime, TimeUnit timeUnit, int blockingQueueSize, final String tag) {

        logger.info(
                "create thread pool with blocking array queue, tag : {}, corePoolSize : {}, maximumPoolSize : {}"
                        + ", keepAliveTime : {}, unit : {}, blockingQueueSize : {}",
                tag, corePoolSize, maximumPoolSize, keepAliveTime, timeUnit, blockingQueueSize);

        RejectedExecutionHandler defaultRejectHandler = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                logger.error(
                        StringUtils.join(tag,
                                "_thread_full:queue_size={}, ActiveCount={}, CorePoolSize={}, CompletedTaskCount={}"),
                        executor.getQueue().size(), executor.getActiveCount(), executor.getCorePoolSize(),
                        executor.getCompletedTaskCount());
                Monitor.recordOne(StringUtils.join(tag, "_reject_error"));
            }
        };

        ThreadFactory threadFactory = new BasicThreadFactory.Builder().namingPattern(tag + "-thread-%d")
                .uncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        String errorInfo = tag + "_thread_execute_error";
                        logger.error(errorInfo, e);
                        Monitor.recordOne(errorInfo);
                    }
                }).build();

        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, timeUnit,
                new ArrayBlockingQueue<Runnable>(blockingQueueSize), threadFactory, defaultRejectHandler);
    }

    /**
     * 已一定概率记录线程池监控，因为获取数据的方法里有锁。
     * 
     * @param tag 线程池标记，用于区别监控
     * @param executorService 线程池
     */
    public static void recordPoolInfo(String tag, ThreadPoolExecutor executorService) {
        try {
            // 减少监控频率
            if (System.currentTimeMillis() % 1000 > 5l) {
                return;
            }

            ThreadPoolExecutor executor = (ThreadPoolExecutor) executorService;
            Monitor.recordOne(StringUtils.join(tag, "_pool_active_count"), executor.getActiveCount());
            Monitor.recordOne(StringUtils.join(tag, "_pool_queue_size"), executor.getQueue().size());
            Monitor.recordOne(StringUtils.join(tag, "_pool_size"), executor.getPoolSize());
            Monitor.recordOne(StringUtils.join(tag, "_pool_core_size"), executor.getCorePoolSize());
        } catch (Exception e) {
            logger.error("record_pool_info_error", e);
            Monitor.recordOne("record_pool_info_error");
        }
    }
}

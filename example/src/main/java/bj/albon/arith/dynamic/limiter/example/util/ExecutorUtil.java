package bj.albon.arith.dynamic.limiter.example.util;

import bj.albon.arith.dynamic.limiter.util.ThreadPoolUtil;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author albon
 *         Date: 17-12-27
 *         Time: 上午10:28
 */
public class ExecutorUtil {
    // 线程数设大一点儿，方便模拟 Load 升高的情况
    public static final ThreadPoolExecutor EXECUTOR = ThreadPoolUtil.createArrayBlockingQueuePoolExecutor(
            500, 1000, 1, TimeUnit.MINUTES, 100, "test");
}

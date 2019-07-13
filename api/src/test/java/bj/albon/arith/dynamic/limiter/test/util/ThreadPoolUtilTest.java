package bj.albon.arith.dynamic.limiter.test.util;

import bj.albon.arith.dynamic.limiter.util.ThreadPoolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author albon
 *         Date: 17-11-30
 *         Time: 上午10:45
 */
public class ThreadPoolUtilTest {
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolUtilTest.class);

    public static void main(String[] args) throws InterruptedException {
        ThreadPoolExecutor executor = ThreadPoolUtil.createArrayBlockingQueuePoolExecutor(1, 1, 1, TimeUnit.MINUTES, 1,
                "test");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                logger.info("run ...");
                throw new RuntimeException("just test");
            }
        });

        Thread.sleep(2000);
        executor.shutdown();
    }

}

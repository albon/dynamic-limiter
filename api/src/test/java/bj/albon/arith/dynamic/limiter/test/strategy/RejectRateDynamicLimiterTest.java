package bj.albon.arith.dynamic.limiter.test.strategy;

import bj.albon.arith.dynamic.limiter.limiter.DynamicLimiter;
import bj.albon.arith.dynamic.limiter.limiter.DynamicLimiterFactory;
import bj.albon.arith.dynamic.limiter.model.EventType;
import bj.albon.arith.dynamic.limiter.test.util.CriticalEventManagerTest;
import com.google.common.collect.Lists;
import bj.albon.arith.dynamic.limiter.model.StrategyEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author albon
 *         Date: 17-7-24
 *         Time: 下午7:26
 */
public class RejectRateDynamicLimiterTest {
    private static final Logger logger = LoggerFactory.getLogger(CriticalEventManagerTest.class);

    public static void main(String[] args) throws InterruptedException {
        final DynamicLimiter dynamicLimiter = DynamicLimiterFactory.create("rejectRate",
                Lists.newArrayList(StrategyEnum.RejectFeedback));

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        final CountDownLatch countDownLatch = new CountDownLatch(2);

        final AtomicInteger rejectCount = new AtomicInteger(0);

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 1000; ++i) {
                    EventType event = (i % 10 >= 8) ? EventType.FAILURE : EventType.SUCCESS;
                    dynamicLimiter.record(event, 1);

                    if (event.equals(EventType.FAILURE)) {
                        rejectCount.incrementAndGet();
                    }
                    try {
                        Thread.sleep((long) (100 * Math.random() + 10));
                    } catch (InterruptedException e) {
                        logger.error("sleep error", e);
                    }
                }

                countDownLatch.countDown();
            }
        });

        final AtomicInteger exceedCount = new AtomicInteger(0);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 1000; ++i) {
                    boolean exceed = dynamicLimiter.exceed();
                    logger.info(i + "exceed: " + exceed);

                    if (exceed) {
                        exceedCount.incrementAndGet();
                    }
                    try {
                        Thread.sleep((long) (100 * Math.random() + 10));
                    } catch (InterruptedException e) {
                        logger.error("sleep error", e);
                    }
                }
                countDownLatch.countDown();
            }
        });

        countDownLatch.await();
        logger.info("rejectCount = " + rejectCount + ", exceedCount = " + exceedCount);

        System.exit(0);
    }

}

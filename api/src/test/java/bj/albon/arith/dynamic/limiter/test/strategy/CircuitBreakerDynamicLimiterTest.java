package bj.albon.arith.dynamic.limiter.test.strategy;

import bj.albon.arith.dynamic.limiter.config.DynamicProperties;
import bj.albon.arith.dynamic.limiter.limiter.AbstractDynamicLimiter;
import bj.albon.arith.dynamic.limiter.limiter.DynamicLimiter;
import bj.albon.arith.dynamic.limiter.limiter.DynamicLimiterFactory;
import bj.albon.arith.dynamic.limiter.model.EventType;
import bj.albon.arith.dynamic.limiter.config.DynamicPropertiesFactory;
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
public class CircuitBreakerDynamicLimiterTest {
    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerDynamicLimiterTest.class);
    public static final String CIRCUIT_BREAKER = "circuitBreaker";

    public static void main(String[] args) throws InterruptedException {
        final DynamicLimiter dynamicLimiter = DynamicLimiterFactory.create(CIRCUIT_BREAKER, StrategyEnum.CircuitBreaker);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        final CountDownLatch countDownLatch = new CountDownLatch(2);

        final AtomicInteger rejectCount = new AtomicInteger(0);

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 8000; ++i) {
                    EventType event = (i % 10 >= 8 || i > 500) ? EventType.SUCCESS : EventType.FAILURE;
                    dynamicLimiter.record(event, 1);

                    if (event.equals(EventType.FAILURE)) {
                        rejectCount.incrementAndGet();
                    }
                    try {
                        Thread.sleep(5);
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
                for (int i = 0; i < 2000; ++i) {
                    boolean exceed = dynamicLimiter.exceed();
//                    logger.info("round: {}, exceed: {}", i, exceed);

                    if (exceed) {
                        exceedCount.incrementAndGet();
                    }
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        logger.error("sleep error", e);
                    }
                }
                countDownLatch.countDown();
            }
        });

        Thread.sleep(10000);

        DynamicProperties dynamicProperties = DynamicPropertiesFactory.get(CIRCUIT_BREAKER);
        dynamicProperties.setBucketSizeInMs(4000);
        dynamicProperties.setWindowSizeInSecond(12);

        ((AbstractDynamicLimiter) dynamicLimiter).update();

        countDownLatch.await();
        logger.info("rejectCount = " + rejectCount + ", exceedCount = " + exceedCount);

        System.exit(0);
    }

}

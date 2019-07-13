package bj.albon.arith.dynamic.limit.example.test;

import bj.albon.arith.config.parser.api.util.HttpUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 动态限流测试代码，测试使用 LoadDynamicLimitHTTPFilter 限流的情况
 */
public abstract class AbstractTestLoadDynamicLimit {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractTestLoadDynamicLimit.class);

    /**
     * 1秒1次请求，测试压力小，不会限流的情况
     *
     * @throws InterruptedException
     */
    @Test
    public void testNormal() throws InterruptedException {
        int exceedCount = 0;

        for (int i = 0; i < 10; ++i) {
            Thread.sleep(500);
            String content = HttpUtil.get(getTestUrl());
            if (!Boolean.valueOf(content)) {
                ++exceedCount;
            }
        }

        logger.info("testNormal exceedCount = {}", exceedCount);
    }

    /**
     * 连续不间断请求，测试压力大，会被限流的情况
     */
    @Test
    public void testExceed() throws InterruptedException {
        int parallelNum = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(parallelNum);
        final CountDownLatch countDownLatch = new CountDownLatch(parallelNum);

        for (int i = 0; i < parallelNum; ++i) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    logger.info("start ...");
                    int exceedCount = 0;
                    try {
                        long start = System.currentTimeMillis();
                        for (int i = 0; i < 100000; ++i) {
                            String content = HttpUtil.get(getTestUrl());
                            if (!Boolean.valueOf(content)) {
                                ++exceedCount;
                            }
                            if (i > 0 && i % 1000 == 0) {
                                logger.debug("avgTime: {}", (System.currentTimeMillis() - start) / 1000);
                                start = System.currentTimeMillis();
                            }
                        }
                    } catch (Exception e) {
                        logger.error("execute error", e);
                    } finally {
                        logger.info("testExceed exceedCount = {}", exceedCount);
                        countDownLatch.countDown();
                    }
                }
            });
        }

        countDownLatch.await();
    }

    protected abstract String getTestUrl();
}

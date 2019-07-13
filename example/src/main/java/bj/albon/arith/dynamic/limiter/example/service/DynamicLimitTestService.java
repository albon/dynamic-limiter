package bj.albon.arith.dynamic.limiter.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 示例代码：一个用来测试动态限流的服务
 */
public class DynamicLimitTestService implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(DynamicLimitTestService.class);

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10; ++i) {
            int count = 0;
            for (int j = 0; j < 3000000; ++j) {
                count += j;
            }
            logger.info("run {}, {}", i, count);
        }
        logger.info("time: {}", System.currentTimeMillis() - start);
    }
}

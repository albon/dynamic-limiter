package bj.albon.arith.dynamic.limit.example.test;

import bj.albon.arith.config.parser.api.util.HttpUtil;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 熔断器测试代码
 */
public class TestCircuitBreaker {
    private static final Logger logger = LoggerFactory.getLogger(TestCircuitBreaker.class);
    // 该接口位于 example 模块中。useCorrectUrl 参数表示是否使用正确的 URL，错误 URL 会触发熔断。
    private static final String TEST_URL = "http://127.0.0.1:8080/test/circuitBreaker?useCorrectUrl=%s";

    /**
     * 测试正常情况
     *
     * @throws InterruptedException
     */
    @Test
    public void testNormal() throws InterruptedException {
        int exceedCount = 0;

        exceedCount += requestCircuitBreakerInterface(1000, true);

        logger.info("testNormal exceedCount: {}", exceedCount);
        Assert.assertEquals(exceedCount, 0);
    }

    /**
     * 测试触发熔断的情况
     *
     * @throws InterruptedException
     */
    @Test
    public void testCircuitBreak() throws InterruptedException {
        int exceedCount = 0;
        exceedCount += requestCircuitBreakerInterface(100, true);

        exceedCount += requestCircuitBreakerInterface(1000, false);

        exceedCount += requestCircuitBreakerInterface(200, true);

        logger.info("testCircuitBreak exceedCount: {}", exceedCount);
        Assert.assertTrue(exceedCount > 0);
    }

    /**
     * 请求外部接口，并记录返回false的量
     *
     * @param times         请求次数
     * @param useCorrectUrl 请求接口里的 useCorrectUrl 参数，表示是否使用正确的 URL
     * @return 接口返回 false 次数，false 表示熔断了
     * @throws InterruptedException
     */
    private int requestCircuitBreakerInterface(int times, boolean useCorrectUrl) throws InterruptedException {
        int exceedCount = 0;
        for (int i = 0; i < times; ++i) {
            Thread.sleep(50);
            String content = HttpUtil.get(String.format(TEST_URL, useCorrectUrl));
            if (content != null && !Boolean.valueOf(content)) {
                ++exceedCount;
            }
        }
        return exceedCount;
    }

}

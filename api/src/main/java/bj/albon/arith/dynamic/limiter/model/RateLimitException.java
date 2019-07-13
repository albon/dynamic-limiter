package bj.albon.arith.dynamic.limiter.model;

/**
 * 限流异常，用于动态限流的 Dubbo Filter 里限流时抛出。
 *
 * @author albon
 *         Date: 17-12-25
 *         Time: 下午5:51
 */
public class RateLimitException extends RuntimeException {
    public RateLimitException(String key) {
        super(key + " rate limited");
    }
}

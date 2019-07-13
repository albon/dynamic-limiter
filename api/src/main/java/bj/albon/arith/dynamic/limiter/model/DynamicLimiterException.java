package bj.albon.arith.dynamic.limiter.model;

/**
 * @author albon
 *         Date: 17-12-27
 *         Time: 下午5:51
 */
public class DynamicLimiterException extends RuntimeException {
    public DynamicLimiterException() {
    }

    public DynamicLimiterException(String message) {
        super(message);
    }

    public DynamicLimiterException(String message, Throwable cause) {
        super(message, cause);
    }

    public DynamicLimiterException(Throwable cause) {
        super(cause);
    }

    public DynamicLimiterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

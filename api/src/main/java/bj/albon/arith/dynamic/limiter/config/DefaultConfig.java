package bj.albon.arith.dynamic.limiter.config;

import bj.albon.arith.dynamic.limiter.util.SystemUtil;

import java.math.BigDecimal;

/**
 * Created by albon on 17/7/23.
 */
public class DefaultConfig {

    public static final int SECOND_IN_MS = 1000;
    public static final int BUCKET_NUM = 5;
    public static final int BUCKET_SIZE_IN_MS = 1000;
    public static final int WINDOW_SIZE_IN_SECOND = BUCKET_NUM * BUCKET_SIZE_IN_MS / SECOND_IN_MS;

    public static final int FAILURE_COUNT_THRESHOLD = 10;
    public static final int FAILURE_RATE_THRESHOLD = 60;

    public static final int LOAD_THRESHOLD = SystemUtil.getAvailableProcessors();

    public static final long ZERO = 0;
    public static final int CIRCUIT_BREAKER_WINDOW_IN_MS = 5000;
    public static final int CONTINUOUS_SUCCESS_NUM_CLOSE_CIRCUIT_BREAKER = 5;
    public static final BigDecimal QPS_FACTOR_IN_DYNAMIC_LIMITER = BigDecimal.valueOf(0.9);

    public static final long DEFAULT_QPS_NEED_DECREASE_TIME_IN_MS_THRESHOLD = 2000;
    public static final long DEFAULT_QPS_NEED_LIMIT_TIME_IN_MS_THRESHOLD = 1000;
    public static final int DEFAULT_QPS_NEED_DECREASE_CPU_RATE_THRESHOLD = 90;
    public static final int DEFAULT_QPS_NEED_LIMIT_CPU_RATE_THRESHOLD = 70;
}

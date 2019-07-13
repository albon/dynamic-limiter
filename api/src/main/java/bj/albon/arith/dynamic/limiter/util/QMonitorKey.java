package bj.albon.arith.dynamic.limiter.util;

/**
 * @author albon
 *         Date: 17-7-28
 *         Time: 上午10:01
 */
public class QMonitorKey {
    public static final String DYNAMIC_LIMITER_ERROR_EXCEED = "dynamic_limiter_error_exceed";
    public static final String DYNAMIC_LIMITER_ERROR_RECORD = "dynamic_limiter_error_record";
    public static final String DYNAMIC_LIMITER_RECORD_TOTAL = "dynamic_limiter_record_total";
    public static final String DYNAMIC_LIMITER_EXCEED_TOTAL = "dynamic_limiter_exceed_total";

    public static final String DYNAMIC_LIMITER_QPS = "dynamic_limiter_qps";
    public static final String DYNAMIC_LIMITER_EXCEED_BUT_FORCE_CLOSE = "dynamic_limiter_exceed_but_force_close";
    public static final String DYNAMIC_LIMITER_RETURN_EXCEED = "dynamic_limiter_return_exceed";
    public static final String DYNAMIC_LIMITER_EXCEED = "dynamic_limiter_exceed";
    public static final String DYNAMIC_LIMITER_RECORD_EVENT = "dynamic_limiter_record_event";

    public static final String CIRCUIT_BREAKER_HALF_OPEN = "circuit_breaker_half_open";
    public static final String CIRCUIT_BREAKER_OPEN = "circuit_breaker_open";
    public static final String CIRCUIT_BREAKER_CLOSE = "circuit_breaker_close";
    public static final String DYNAMIC_LIMITER_ERROR_MONITOR_HEALTH_CHECK = "dynamic_limiter_error_monitor_health_check";

    public static final String READ_CPU_USE_INFO_ERROR = "dynamic_limiter_error_read_cpu_use_info";
    public static final String CPU_READER_CLOSE_ERROR = "dynamic_limiter_error_cpu_reader_close";
    public static final String CPU_PROCESS_CLOSE_ERROR = "dynamic_limiter_error_cpu_process_close";
    public static final String DYNAMIC_LIMITER_ERROR_MONITOR_LOAD = "dynamic_limiter_error_monitor_load";

    public static final String DYNAMIC_LIMITER_MONITOR_CPU_RATE_ERROR = "dynamic_limiter_error_monitor_cpu_rate";
    public static final String DYNAMIC_LIMITER_SUBSCRIBE_ERROR = "dynamic_limiter_error_subscribe";
}

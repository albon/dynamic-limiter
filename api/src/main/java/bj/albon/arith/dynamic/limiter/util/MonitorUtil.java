package bj.albon.arith.dynamic.limiter.util;

/**
 * @author albon
 *         Date: 17-7-25
 *         Time: 上午10:00
 */
public class MonitorUtil {

    public static void recordTime(String prefix, String key, long time) {
        Monitor.recordOne(prefix + "_" + key, time);
    }

    public static void recordTime(String prefix, String key, String subKey, long time) {
        Monitor.recordOne(prefix + "_" + key + "_" + subKey, time);
    }

    public static void recordOne(String prefix, String key) {
        Monitor.recordOne(prefix + "_" + key);
    }

    public static void recordOne(String prefix, String key, String subKey) {
        Monitor.recordOne(prefix + "_" + key + "_" + subKey);
    }
}

package bj.albon.arith.dynamic.limiter.config;

import com.google.common.collect.Maps;
import bj.albon.arith.dynamic.limiter.config.parser.StringListStringMapFieldParser;
import bj.albon.arith.config.parser.api.annotation.AutoParseField;
import bj.albon.arith.config.parser.api.model.ParserType;

import java.util.List;
import java.util.Map;

/**
 * @author albon
 *         Date: 17-7-24
 *         Time: 下午4:49
 */
public final class GlobalConfig {
    private static final GlobalConfig INSTANCE = new GlobalConfig();

    private GlobalConfig() {
    }

    public static GlobalConfig instance() {
        return INSTANCE;
    }

    @AutoParseField(key = "close.all.dynamic.limiter")
    private boolean closeAllDynamicLimiter = false;

    @AutoParseField(key = "close.when.health.check.not.exist")
    private boolean closeWhenHealthCheckNotExist = false;

    @AutoParseField(key = "close.seconds.after.restart")
    private long closeSecondsAfterRestart = DefaultConfig.ZERO;
    // healthcheck 恢复时，根据 closeSecondsAfterRestart 的值和当前时间计算
    private long closeToTimeInMillis = DefaultConfig.ZERO;

    @AutoParseField(key = "limiter.alias.", type = ParserType.MULTI, parser = StringListStringMapFieldParser.class)
    private Map<String, List<String>> limiterAliasMap = Maps.newHashMap();

    public Map<String, List<String>> getLimiterAliasMap() {
        return limiterAliasMap;
    }

    public long getCloseToTimeInMillis() {
        return closeToTimeInMillis;
    }

    public void setCloseToTimeInMillis(long closeToTimeInMillis) {
        this.closeToTimeInMillis = closeToTimeInMillis;
    }

    public long getCloseSecondsAfterRestart() {
        return closeSecondsAfterRestart;
    }

    public boolean isCloseWhenHealthCheckNotExist() {
        return closeWhenHealthCheckNotExist;
    }

    public boolean isCloseAllDynamicLimiter() {
        return closeAllDynamicLimiter;
    }

    @Override
    public String toString() {
        return "GlobalConfig{" + "closeAllDynamicLimiter=" + closeAllDynamicLimiter + ", closeWhenHealthCheckNotExist="
                + closeWhenHealthCheckNotExist + ", closeSecondsAfterRestart=" + closeSecondsAfterRestart
                + ", closeToTimeInMillis=" + closeToTimeInMillis + ", limiterAliasMap=" + limiterAliasMap + '}';
    }
}

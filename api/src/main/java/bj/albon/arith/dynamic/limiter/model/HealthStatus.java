package bj.albon.arith.dynamic.limiter.model;

/**
 * 系统健康状态
 * 必须保证：随着系统健康程度变差，CODE 升高。因为在综合多种因素，计算健康度时，用到了其大小关系。
 */
public enum HealthStatus {
    EXCELLENT(1), // 非常健康，系统压力较小
    HEALTHY(2), // 健康
    NOT_HEALTHY(3), // 不健康
    WORSE(4); // 恶化，严重不健康

    public int CODE;

    HealthStatus(int code) {
        this.CODE = code;
    }

    public boolean isHealthy() {
        return this.equals(EXCELLENT) || this.equals(HEALTHY);
    }
}

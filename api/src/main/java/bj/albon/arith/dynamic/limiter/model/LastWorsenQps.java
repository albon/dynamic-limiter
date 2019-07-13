package bj.albon.arith.dynamic.limiter.model;

public class LastWorsenQps {

    private long qps;
    private long time;

    public LastWorsenQps(long qps) {
        this.qps = qps;
        this.time = System.currentTimeMillis();
    }

    public LastWorsenQps(long qps, long time) {
        this.qps = qps;
        this.time = time;
    }

    public long getQps() {
        return qps;
    }

    public void setQps(long qps) {
        this.qps = qps;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void updateQps(long qps) {
        this.qps = qps;
        this.time = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "LastWorsenQps{" +
                "qps=" + qps +
                ", time=" + time +
                '}';
    }
}

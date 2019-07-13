package bj.albon.arith.dynamic.limiter.model;

/**
 * Created by albon on 17/7/23.
 */
public class MonitorItem {
    private long count;
    private long time;

    public MonitorItem(long count, long time) {
        this.count = count;
        this.time = time;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "MonitorItem{" + "count=" + count + ", time=" + time + '}';
    }
}

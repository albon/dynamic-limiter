package bj.albon.arith.dynamic.limiter.model;

import com.google.common.collect.Lists;

import java.util.List;

public class SystemHealthyComputeInfo {
    public static final SystemHealthyComputeInfo DEFAULT = new SystemHealthyComputeInfo();
    static {
        DEFAULT.setMonitorTypeList(Lists.newArrayList(MonitorType.LOAD));
        DEFAULT.setComputeType(ComputeType.OR);
    }

    private List<MonitorType> monitorTypeList;
    private ComputeType computeType;

    public ComputeType getComputeType() {
        return computeType;
    }

    public void setComputeType(ComputeType computeType) {
        this.computeType = computeType;
    }

    public void setMonitorTypeList(List<MonitorType> monitorTypeList) {
        this.monitorTypeList = monitorTypeList;
    }

    public List<MonitorType> getMonitorTypeList() {
        return monitorTypeList;
    }

    @Override
    public String toString() {
        return "SystemHealthyComputeInfo{" + "monitorTypeList=" + monitorTypeList + ", computeType=" + computeType
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SystemHealthyComputeInfo that = (SystemHealthyComputeInfo) o;

        if (monitorTypeList != null ? !monitorTypeList.equals(that.monitorTypeList) : that.monitorTypeList != null)
            return false;
        return computeType == that.computeType;
    }

    @Override
    public int hashCode() {
        int result = monitorTypeList != null ? monitorTypeList.hashCode() : 0;
        result = 31 * result + (computeType != null ? computeType.hashCode() : 0);
        return result;
    }

    public enum ComputeType {
        AND("&") {
            public HealthStatus choose(List<HealthStatus> healthStatusList) {
                HealthStatus status = healthStatusList.get(0);

                for (HealthStatus healthStatus : healthStatusList) {
                    if (healthStatus.CODE < status.CODE) {
                        status = healthStatus;
                    }
                }

                return status;
            }
        },
        OR("|") {
            public HealthStatus choose(List<HealthStatus> healthStatusList) {
                HealthStatus status = healthStatusList.get(0);

                for (HealthStatus healthStatus : healthStatusList) {
                    if (healthStatus.CODE > status.CODE) {
                        status = healthStatus;
                    }
                }

                return status;
            }
        };

        public String symbol;

        ComputeType(String symbol) {
            this.symbol = symbol;
        }

        public abstract HealthStatus choose(List<HealthStatus> healthStatusList);
    }

}

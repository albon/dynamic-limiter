package bj.albon.arith.dynamic.limiter.test.util;

import com.google.common.collect.Lists;
import bj.albon.arith.dynamic.limiter.model.HealthStatus;
import bj.albon.arith.dynamic.limiter.model.SystemHealthyComputeInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author albon
 *         Date: 17-11-29
 *         Time: 上午11:10
 */
public class SystemHealthyTest {

    @Test
    public void testComputeTypeOR() {
        List<HealthStatus> healthStatusList = Lists.newArrayList(HealthStatus.NOT_HEALTHY, HealthStatus.HEALTHY);
        HealthStatus healthStatus = SystemHealthyComputeInfo.ComputeType.OR.choose(healthStatusList);
        Assert.assertEquals(healthStatus, HealthStatus.NOT_HEALTHY);
    }

    @Test
    public void testComputeTypeAND() {
        List<HealthStatus> healthStatusList = Lists.newArrayList(HealthStatus.NOT_HEALTHY, HealthStatus.HEALTHY);
        HealthStatus healthStatus = SystemHealthyComputeInfo.ComputeType.AND.choose(healthStatusList);
        Assert.assertEquals(healthStatus, HealthStatus.HEALTHY);
    }
}

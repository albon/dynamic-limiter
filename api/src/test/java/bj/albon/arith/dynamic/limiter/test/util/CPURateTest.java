package bj.albon.arith.dynamic.limiter.test.util;

import bj.albon.arith.dynamic.limiter.model.SystemInfo;
import bj.albon.arith.dynamic.limiter.model.CPUUseInfo;
import bj.albon.arith.dynamic.limiter.util.CPUUtil;
import bj.albon.arith.dynamic.limiter.util.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author albon
 *         Date: 17-11-23
 *         Time: 下午3:17
 */
public class CPURateTest {
    private static final Logger logger = LoggerFactory.getLogger(CPURateTest.class);

    public static void main(String[] args) throws InterruptedException {
        CPUUseInfo cpuUseInfo = CPUUtil.readCPUUseInfo();
        logger.info("cpuUseInfo = {}, {}", cpuUseInfo, cpuUseInfo.getIdleTime() * 1.0 / cpuUseInfo.getTotalTime());

        SystemUtil.monitorCPURate();
        Thread.sleep(10000);
        SystemUtil.monitorCPURate();

        logger.info("cpuRate = {}", SystemInfo.getInstance().getCpuRate());
    }

}

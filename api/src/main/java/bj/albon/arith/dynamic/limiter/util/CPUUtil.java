package bj.albon.arith.dynamic.limiter.util;

import bj.albon.arith.dynamic.limiter.model.CPUUseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CPUUtil {
    private static final Logger logger = LoggerFactory.getLogger(CPUUtil.class);
    private static final String CPU_CAT_COMMAND = "cat /proc/stat";
    private static final String CPU_INFO_PREFIX = "cpu";
    private static final String SPACE_REGEX = "\\s+";
    private static final int IDLE_CPU_POS = 4;

    /**
     * 读取 cat /proc/stat 命令返回的 CPU 数据
     * 
     * @return
     */
    public static CPUUseInfo readCPUUseInfo() {
        BufferedReader reader = null;
        Process process = null;
        try {
            Runtime runtime = Runtime.getRuntime();
            process = runtime.exec(CPU_CAT_COMMAND);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // 分别为系统启动后空闲的CPU时间和总的CPU时间
            long idleCpuTime = 0, totalCpuTime = 0;
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(CPU_INFO_PREFIX)) {
                    line = line.trim();
                    String[] numberArray = line.split(SPACE_REGEX);
                    idleCpuTime = Long.parseLong(numberArray[IDLE_CPU_POS]);
                    for (String number : numberArray) {
                        if (!number.equals(CPU_INFO_PREFIX)) {
                            totalCpuTime += Long.parseLong(number);
                        }
                    }
                    break;
                }
            }
            return new CPUUseInfo(idleCpuTime, totalCpuTime);
        } catch (Exception e) {
            logger.error(QMonitorKey.READ_CPU_USE_INFO_ERROR, e);
            Monitor.recordOne(QMonitorKey.READ_CPU_USE_INFO_ERROR);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                logger.error(QMonitorKey.CPU_READER_CLOSE_ERROR, e);
                Monitor.recordOne(QMonitorKey.CPU_READER_CLOSE_ERROR);
            }

            try {
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                logger.error(QMonitorKey.CPU_PROCESS_CLOSE_ERROR, e);
                Monitor.recordOne(QMonitorKey.CPU_PROCESS_CLOSE_ERROR);
            }
        }

        return null;
    }

    /**
     * 根据前后两次 CPU 统计数据，计算 CPU 使用率
     * 
     * @param cpuUseInfo 本次 CPU 数据
     * @param lastUpdateCPUUseInfo 上次读取的 CPU 数据
     * @return CPU 使用率
     */
    public static double computeCPURate(CPUUseInfo cpuUseInfo, CPUUseInfo lastUpdateCPUUseInfo) {
        long total = cpuUseInfo.getTotalTime() - lastUpdateCPUUseInfo.getTotalTime();
        double idle = cpuUseInfo.getIdleTime() - lastUpdateCPUUseInfo.getIdleTime();

        if (total > 0) {
            return (1 - idle / total) * 100;
        } else {
            return 0.0;
        }
    }
}

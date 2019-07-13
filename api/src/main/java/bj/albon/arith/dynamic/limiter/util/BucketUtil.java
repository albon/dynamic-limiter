package bj.albon.arith.dynamic.limiter.util;

import bj.albon.arith.dynamic.limiter.config.DefaultConfig;
import bj.albon.arith.dynamic.limiter.config.DynamicProperties;

/**
 * @author albon
 *         Date: 17-7-24
 *         Time: 下午4:31
 */
public class BucketUtil {

    /**
     * 根据滑动窗口时长和桶 Bucket 时长计算 Bucket 数目
     * 
     * @param properties 滑动窗口配置信息, 内含窗口时长和桶 Bucket 时长
     * @return 一个滑动窗口内部包含的桶 Bucket 数目
     */
    public static int computeBucketNum(DynamicProperties properties) {
        return properties.getWindowSizeInSecond() * DefaultConfig.SECOND_IN_MS / properties.getBucketSizeInMs();
    }

}

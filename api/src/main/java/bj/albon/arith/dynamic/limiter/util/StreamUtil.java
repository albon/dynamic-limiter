package bj.albon.arith.dynamic.limiter.util;

import com.google.common.collect.Lists;
import bj.albon.arith.dynamic.limiter.counter.BucketCounterStream;

import java.util.List;

/**
 * 计数的 stream 工具类
 *
 * @author albon
 *         Date: 17-12-22
 *         Time: 下午5:52
 */
public class StreamUtil {

    public static List<BucketCounterStream> addToListIfNotNull(BucketCounterStream... streams) {
        List<BucketCounterStream> streamList = Lists.newArrayList();
        for (BucketCounterStream stream : streams) {
            if (stream != null) {
                streamList.add(stream);
            }
        }
        return streamList;
    }

    /**
     * 关闭这些 stream
     *
     * @param streamList
     */
    public static void close(List<BucketCounterStream> streamList) {
        if (streamList == null) {
            return;
        }

        for (BucketCounterStream stream : streamList) {
            stream.close();
        }
    }

    /**
     * 判断 oldStream 对应的滑动窗口大小和新输入的滑动窗口大小是否一致
     *
     * @param oldStream
     * @param bucketNum
     * @param bucketSizeInMs
     * @return 新老一致, 则返回 true. 否则, false.
     */
    public static boolean bucketSizeEqual(BucketCounterStream oldStream, int bucketNum, int bucketSizeInMs) {
        if (oldStream == null) {
            return false;
        }

        return oldStream.getBucketSizeInMs() == bucketSizeInMs && oldStream.getNumBuckets() == bucketNum;
    }
}

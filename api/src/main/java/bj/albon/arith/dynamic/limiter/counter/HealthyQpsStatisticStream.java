package bj.albon.arith.dynamic.limiter.counter;

import bj.albon.arith.dynamic.limiter.model.HealthyQpsInfo;
import rx.functions.Func2;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * 健康状态下 qps 统计
 * 1. 如果其中混杂着不健康状态下的 qps，则清 0。
 * 2. 不健康状态下的 qps，输入方需要记录为负值。
 * 3. 如果存在数据为空的情况，也排除，避免被平均的 QPS 不准确。
 * Created by albon on 17/7/23.
 */
public final class HealthyQpsStatisticStream extends BucketRollingCounterStream<Long, HealthyQpsInfo, HealthyQpsInfo> {

    private HealthyQpsStatisticStream(Subject<Long, Long> inputStream, int numBuckets, int bucketSizeInMs,
            Func2<HealthyQpsInfo, Long, HealthyQpsInfo> appendRawEventToBucket,
            Func2<HealthyQpsInfo, HealthyQpsInfo, HealthyQpsInfo> reduceBucket) {
        super(inputStream, numBuckets, bucketSizeInMs, appendRawEventToBucket, reduceBucket);
    }

    public static HealthyQpsStatisticStream getInstance(int numBuckets, int bucketSizeInMs) {
        PublishSubject<Long> publishSubject = PublishSubject.create();
        SerializedSubject<Long, Long> inputStream = publishSubject.toSerialized();

        /**
         * 一旦存在为 0 的数，则将数据置为无效。避免计算出不准确的 QPS
         */
        Func2<HealthyQpsInfo, Long, HealthyQpsInfo> appendRawEventToBucket = new Func2<HealthyQpsInfo, Long, HealthyQpsInfo>() {
            @Override
            public HealthyQpsInfo call(HealthyQpsInfo healthyQpsInfo, Long aLong) {
                if (!isQpsValid(aLong)) {
                    healthyQpsInfo.setValid(false);
                } else if (healthyQpsInfo.isValid()) {
                    healthyQpsInfo.setCount(healthyQpsInfo.getCount() + 1);
                    healthyQpsInfo.setTime(healthyQpsInfo.getTime() + aLong);
                }
                return healthyQpsInfo;
            }
        };

        Func2<HealthyQpsInfo, HealthyQpsInfo, HealthyQpsInfo> reduceBucketToWindow = new Func2<HealthyQpsInfo, HealthyQpsInfo, HealthyQpsInfo>() {
            @Override
            public HealthyQpsInfo call(HealthyQpsInfo healthyQpsInfo, HealthyQpsInfo anotherQpsInfo) {
                if (!anotherQpsInfo.isValid() || !isQpsValid(anotherQpsInfo.getCount())) {
                    healthyQpsInfo.setValid(false);
                } else if (healthyQpsInfo.isValid()) {
                    healthyQpsInfo.setCount(healthyQpsInfo.getCount() + anotherQpsInfo.getCount());
                    healthyQpsInfo.setTime(healthyQpsInfo.getTime() + anotherQpsInfo.getTime());
                }

                return healthyQpsInfo;
            }
        };

        return new HealthyQpsStatisticStream(inputStream, numBuckets, bucketSizeInMs, appendRawEventToBucket,
                reduceBucketToWindow);
    }

    private static boolean isQpsValid(Long aLong) {
        return aLong > 0;
    }

    @Override
    protected HealthyQpsInfo getEmptyBucketSummary() {
        return new HealthyQpsInfo(0, 0);
    }

    @Override
    protected HealthyQpsInfo getEmptyOutputValue() {
        return new HealthyQpsInfo(0, 0);
    }
}

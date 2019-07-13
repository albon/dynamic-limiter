package bj.albon.arith.dynamic.limiter.counter;

import rx.functions.Func2;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * 计数器, 用于统计接口调用量: 成功量, 失败量, 调用量
 * Created by albon on 17/7/23.
 */
public final class AmountStatisticStream extends BucketRollingCounterStream<Integer, Integer, Integer> {

    private AmountStatisticStream(Subject<Integer, Integer> inputStream, int numBuckets, int bucketSizeInMs,
            Func2<Integer, Integer, Integer> appendRawEventToBucket, Func2<Integer, Integer, Integer> reduceBucket) {
        super(inputStream, numBuckets, bucketSizeInMs, appendRawEventToBucket, reduceBucket);
    }

    public static AmountStatisticStream getInstance(int numBuckets, int bucketSizeInMs) {
        PublishSubject<Integer> publishSubject = PublishSubject.create();
        SerializedSubject<Integer, Integer> inputStream = publishSubject.toSerialized();

        Func2<Integer, Integer, Integer> INTEGER_SUM = new Func2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer integer, Integer integer2) {
                return integer + integer2;
            }
        };
        return new AmountStatisticStream(inputStream, numBuckets, bucketSizeInMs, INTEGER_SUM, INTEGER_SUM);
    }

    @Override
    protected Integer getEmptyBucketSummary() {
        return 0;
    }

    @Override
    protected Integer getEmptyOutputValue() {
        return 0;
    }
}

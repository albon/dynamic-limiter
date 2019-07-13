package bj.albon.arith.dynamic.limiter.counter;

import bj.albon.arith.dynamic.limiter.model.MonitorItem;
import rx.functions.Func2;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by albon on 17/7/23.
 */
public final class AvgTimeStatisticStream extends BucketRollingCounterStream<Long, MonitorItem, MonitorItem> {

    private AvgTimeStatisticStream(Subject<Long, Long> inputStream, int numBuckets, int bucketSizeInMs,
            Func2<MonitorItem, Long, MonitorItem> appendRawEventToBucket,
            Func2<MonitorItem, MonitorItem, MonitorItem> reduceBucket) {
        super(inputStream, numBuckets, bucketSizeInMs, appendRawEventToBucket, reduceBucket);
    }

    public static AvgTimeStatisticStream getInstance(int numBuckets, int bucketSizeInMs) {
        PublishSubject<Long> publishSubject = PublishSubject.create();
        SerializedSubject<Long, Long> inputStream = publishSubject.toSerialized();

        Func2<MonitorItem, Long, MonitorItem> appendRawEventToBucket = new Func2<MonitorItem, Long, MonitorItem>() {
            @Override
            public MonitorItem call(MonitorItem monitorItem, Long aLong) {
                monitorItem.setCount(monitorItem.getCount() + 1);
                monitorItem.setTime(monitorItem.getTime() + aLong);
                return monitorItem;
            }
        };
        return new AvgTimeStatisticStream(inputStream, numBuckets, bucketSizeInMs, appendRawEventToBucket,
                new Func2<MonitorItem, MonitorItem, MonitorItem>() {
                    @Override
                    public MonitorItem call(MonitorItem monitorItem, MonitorItem monitorItem2) {
                        monitorItem.setCount(monitorItem.getCount() + monitorItem2.getCount());
                        monitorItem.setTime(monitorItem.getTime() + monitorItem2.getTime());
                        return monitorItem;
                    }
                });
    }

    @Override
    protected MonitorItem getEmptyBucketSummary() {
        return new MonitorItem(0, 0);
    }

    @Override
    protected MonitorItem getEmptyOutputValue() {
        return new MonitorItem(0, 0);
    }
}

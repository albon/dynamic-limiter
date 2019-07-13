package bj.albon.arith.dynamic.limiter.counter;

import bj.albon.arith.dynamic.limiter.util.QMonitorKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.Subject;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by albon on 17/7/23.
 */
public abstract class BucketRollingCounterStream<Event, Bucket, Output> extends BucketCounterStream<Event, Bucket> {
    private static final Logger logger = LoggerFactory.getLogger(BucketRollingCounterStream.class);

    private Observable<Output> sourceStream;
    private final AtomicBoolean isSourceCurrentlySubscribed = new AtomicBoolean(false);

    protected BucketRollingCounterStream(Subject<Event, Event> inputStream, final int numBuckets, int bucketSizeInMs,
                                         Func2<Bucket, Event, Bucket> appendRawEventToBucket, final Func2<Output, Bucket, Output> reduceBucket) {
        super(inputStream, numBuckets, bucketSizeInMs, appendRawEventToBucket);

        Func1<Observable<Bucket>, Observable<Output>> reduceWindowToSummary = new Func1<Observable<Bucket>, Observable<Output>>() {
            @Override
            public Observable<Output> call(Observable<Bucket> window) {
                return window.scan(getEmptyOutputValue(), reduceBucket).skip(numBuckets);
            }
        };

        this.sourceStream = bucketedStream // stream broken up into buckets
                .window(numBuckets, 1) // emit overlapping windows of buckets
                .flatMap(reduceWindowToSummary) // convert a window of bucket-summaries into a single summary
                .skip(numBuckets).doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        isSourceCurrentlySubscribed.set(true);
                    }
                }).doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        isSourceCurrentlySubscribed.set(false);
                    }
                }).share() // multiple subscribers should get same data
                .onBackpressureDrop(); // if there are slow consumers, data should not buffer
    }

    public Observable<Output> observe() {
        return sourceStream;
    }

    /**
     * 对 Action1 里的异常做统一的处理和监控
     *
     * @param action1 订阅消息的 Action1
     */
    public void subscribe(final Action1<Output> action1) {
        observe().subscribe(new Action1<Output>() {
            @Override
            public void call(Output output) {
                try {
                    action1.call(output);
                } catch (Throwable throwable) {
                    logger.error(QMonitorKey.DYNAMIC_LIMITER_SUBSCRIBE_ERROR, throwable);
                }
            }
        });
    }

    protected abstract Output getEmptyOutputValue();
}

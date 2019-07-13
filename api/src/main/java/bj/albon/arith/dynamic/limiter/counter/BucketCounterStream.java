package bj.albon.arith.dynamic.limiter.counter;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.Subject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by albon on 17/7/23.
 */
public abstract class BucketCounterStream<Event, Bucket> {

    protected final int numBuckets;
    protected final int bucketSizeInMs;
    protected final Observable<Bucket> bucketedStream;
    protected final Subject<Event, Event> inputStream;
    private final Func1<Observable<Event>, Observable<Bucket>> reduceBucketToSummary;

    protected BucketCounterStream(final Subject<Event, Event> inputStream, int numBuckets, final int bucketSizeInMs,
            final Func2<Bucket, Event, Bucket> appendRawEventToBucket) {
        this.numBuckets = numBuckets;
        this.bucketSizeInMs = bucketSizeInMs;

        this.inputStream = inputStream;
        this.reduceBucketToSummary = new Func1<Observable<Event>, Observable<Bucket>>() {
            @Override
            public Observable<Bucket> call(Observable<Event> eventBucket) {
                return eventBucket.reduce(getEmptyBucketSummary(), appendRawEventToBucket);
            }
        };

        final List<Bucket> emptyEventCountsToStart = new ArrayList<Bucket>();
        for (int i = 0; i < numBuckets; i++) {
            emptyEventCountsToStart.add(getEmptyBucketSummary());
        }

        this.bucketedStream = Observable.defer(new Func0<Observable<Bucket>>() {
            @Override
            public Observable<Bucket> call() {
                return inputStream.window(bucketSizeInMs, TimeUnit.MILLISECONDS) // bucket it by the counter window so
                                                                                 // we can emit to the next operator in
                                                                                 // time chunks, not on every OnNext
                        .flatMap(reduceBucketToSummary) // for a given bucket, turn it into a long array containing
                                                        // counts of event types
                        .startWith(emptyEventCountsToStart); // start it with empty arrays to make consumer logic as
                                                             // generic as possible (windows are always full)
            }
        });
    }

    public void publish(Event event) {
        inputStream.onNext(event);
    }

    protected abstract Bucket getEmptyBucketSummary();

    public void close() {
        inputStream.onCompleted();
    }

    public int getNumBuckets() {
        return numBuckets;
    }

    public int getBucketSizeInMs() {
        return bucketSizeInMs;
    }
}

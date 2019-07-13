package bj.albon.arith.dynamic.limiter.test.stream;

import bj.albon.arith.dynamic.limiter.counter.AmountStatisticStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action1;

/**
 * Created by albon on 17/7/23.
 */
public class AmountStatisticStreamTest {
    private static final Logger logger = LoggerFactory.getLogger(AmountStatisticStreamTest.class);

    public static void main(String[] args) throws InterruptedException {
        final AmountStatisticStream instance = AmountStatisticStream.getInstance(10, 500);

        instance.subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                logger.info("call {}", integer);
            }
        });

        for (int i = 0; i < 200; ++i) {
            instance.publish(1);
            Thread.sleep(200);
        }

        Thread.sleep(10000);
    }
}

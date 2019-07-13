package bj.albon.arith.dynamic.limiter.test.stream;

import bj.albon.arith.dynamic.limiter.counter.AvgTimeStatisticStream;
import bj.albon.arith.dynamic.limiter.model.MonitorItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action1;

/**
 * Created by albon on 17/7/23.
 */
public class AvgTimeStatisticStreamTest {
    private static final Logger logger = LoggerFactory.getLogger(AvgTimeStatisticStreamTest.class);

    public static void main(String[] args) throws InterruptedException {
        AvgTimeStatisticStream instance = AvgTimeStatisticStream.getInstance(10, 500);

        instance.subscribe(new Action1<MonitorItem>() {
            @Override
            public void call(MonitorItem monitorItem) {
                if (monitorItem.getCount() > 0) {
                    logger.info("time: {}, count: {}, avg time: {}", monitorItem.getTime(), monitorItem.getCount(),
                            monitorItem.getTime() / monitorItem.getCount());
                }
            }
        });

        for (int i = 0; i < 100; ++i) {
            instance.publish(100L);
            Thread.sleep(200);
        }

        Thread.sleep(5000);
    }

}

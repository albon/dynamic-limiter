package bj.albon.arith.dynamic.limiter.test.strategy;

import bj.albon.arith.dynamic.limiter.config.DynamicProperties;
import bj.albon.arith.dynamic.limiter.counter.AmountStatisticStream;
import bj.albon.arith.dynamic.limiter.model.SystemInfo;
import bj.albon.arith.dynamic.limiter.strategy.LoadDynamicLimitStrategy;
import bj.albon.arith.dynamic.limiter.util.BucketUtil;
import bj.albon.arith.dynamic.limiter.task.TaskStarter;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action1;

import java.math.BigDecimal;

/**
 * @author albon
 *         Date: 17-8-2
 *         Time: 上午9:48
 */
public class LoadDynamicLimitStrategyTest {
    private static final Logger logger = LoggerFactory.getLogger(LoadDynamicLimitStrategyTest.class);

    public static void main(String[] args) throws InterruptedException {
        // 启动load监控任务
        new TaskStarter().init();

        DynamicProperties properties = new DynamicProperties();
        properties.setKey("loadDynamicLimit");
        properties.setDynamicLimiterQpsFactor(BigDecimal.valueOf(0.8));
        properties.setQpsNeedDecreaseLoadThreshold(1.8);
        properties.setQpsNeedLimitLoadThreshold(1.2);
        properties.setWindowSizeInSecond(10);
        properties.setBucketSizeInMs(1000);
        LoadDynamicLimitStrategy strategy = new LoadDynamicLimitStrategy(properties);

        int bucketNum = BucketUtil.computeBucketNum(properties);
        int bucketSizeInMs = properties.getBucketSizeInMs();

        // passStream 和 rejectStream 用于打日志，输出load和qps信息
        AmountStatisticStream passStream = AmountStatisticStream.getInstance(bucketNum, bucketSizeInMs);
        AmountStatisticStream rejectStream = AmountStatisticStream.getInstance(bucketNum, bucketSizeInMs);
        passStream.subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer passCount) {
                logger.info(LocalDateTime.now().toString() + " load = " + SystemInfo.getInstance().getCurrentLoad()
                        + " pass = " + passCount);
            }
        });
        rejectStream.subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                logger.info(LocalDateTime.now().toString() + " load = " + SystemInfo.getInstance().getCurrentLoad()
                        + " reject = " + integer);
            }
        });

        for (int i = 0; i < 200000; ++i) {
            if (strategy.exceed()) {
                rejectStream.publish(1);
                Thread.sleep(5);
            } else {
                passStream.publish(1);
                Thread.sleep(10);
            }
        }

        while (true) {
            if (strategy.exceed()) {
                rejectStream.publish(1);
                Thread.sleep(5);
            } else {
                passStream.publish(1);
            }
        }
    }
}

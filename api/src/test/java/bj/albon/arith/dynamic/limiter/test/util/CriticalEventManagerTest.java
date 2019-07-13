package bj.albon.arith.dynamic.limiter.test.util;

import bj.albon.arith.dynamic.limiter.model.CriticalEventInfo;
import bj.albon.arith.dynamic.limiter.model.CriticalEventType;
import bj.albon.arith.dynamic.limiter.service.CriticalEventManager;
import bj.albon.arith.dynamic.limiter.service.CriticalEventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CriticalEventManagerTest {
    private static final Logger logger = LoggerFactory.getLogger(CriticalEventManagerTest.class);

    public static void main(String[] args) {
        CriticalEventManager.getInstance().registerCriticalEventProcessor(new CriticalEventProcessor() {
            @Override
            public void process(CriticalEventInfo criticalEventInfo) {
                logger.info("process: {}", criticalEventInfo);
            }
        });

        CriticalEventManager.getInstance()
                .inform(new CriticalEventInfo("test", CriticalEventType.CIRCUIT_BREAKER_OPEN));
        CriticalEventManager.getInstance()
                .inform(new CriticalEventInfo("test", CriticalEventType.CIRCUIT_BREAKER_CLOSE));
    }
}

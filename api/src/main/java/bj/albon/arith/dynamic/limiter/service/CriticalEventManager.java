package bj.albon.arith.dynamic.limiter.service;

import bj.albon.arith.dynamic.limiter.model.CriticalEventInfo;
import bj.albon.arith.dynamic.limiter.util.ThreadPoolUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 负责管理重大事件处理器 CriticalEventProcessor
 */
public class CriticalEventManager {
    private static final int CORE_POOL_SIZE = 1;
    private static final int MAXIMUM_POOL_SIZE = 1;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final int BLOCKING_QUEUE_SIZE = 10;

    private static class LazyHolder {
        private static final CriticalEventManager INSTANCE = new CriticalEventManager();
    }

    public static CriticalEventManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private final ExecutorService executorService = ThreadPoolUtil.createArrayBlockingQueuePoolExecutor(CORE_POOL_SIZE,
            MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.MINUTES, BLOCKING_QUEUE_SIZE, "criticalEventManager");
    private final AtomicReference<CriticalEventProcessor> processorAtomicReference = new AtomicReference<>();

    public void registerCriticalEventProcessor(CriticalEventProcessor processor) {
        if (!processorAtomicReference.compareAndSet(null, processor)) {
            throw new IllegalStateException("Another processor was already registered.");
        }
    }

    public void inform(final CriticalEventInfo criticalEventInfo) {
        final CriticalEventProcessor processor = processorAtomicReference.get();
        if (processor == null) {
            return;
        }

        // 异常在 ExceptionHandler 里统一处理
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                processor.process(criticalEventInfo);
            }
        });
    }

}

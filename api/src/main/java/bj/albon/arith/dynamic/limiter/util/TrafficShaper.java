package bj.albon.arith.dynamic.limiter.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.ConcurrentMap;

/**
 * QpsControl
 * 
 * @author albon
 * @date 16-10-8 下午1:51
 */
public class TrafficShaper {
    private static final ConcurrentMap<String, RateLimiter> resourceLimiterMap = Maps.newConcurrentMap();

    public static void updateResourceQps(String resource, double qps) {
        Preconditions.checkNotNull(resource);

        RateLimiter limiter = resourceLimiterMap.get(resource);
        if (limiter == null) {
            limiter = RateLimiter.create(qps);
            RateLimiter putByOtherThread = resourceLimiterMap.putIfAbsent(resource, limiter);
            if (putByOtherThread != null) {
                limiter = putByOtherThread;
            }
        }
        limiter.setRate(qps);
    }

    public static void removeResource(String resource) {
        resourceLimiterMap.remove(resource);
    }

    /**
     * 尝试qps控制
     * 
     * @throws ResourceNotFoundException 无resource，抛出异常
     * @return 尝试成功/失败
     */
    public static boolean tryEnter(String resource) {
        RateLimiter limiter = resourceLimiterMap.get(resource);
        if (limiter == null) {
            throw new ResourceNotFoundException(resource);
        }
        return limiter.tryAcquire();
    }

    /**
     * 尝试qps控制
     *
     * 如果找不到 resource 则返回 true
     * 
     * @return 尝试成功/失败
     */
    public static boolean tryEnterIfExist(String resource) {
        RateLimiter limiter = resourceLimiterMap.get(resource);
        if (limiter == null) {
            return true;
        }
        return limiter.tryAcquire();
    }

    /**
     * qps控制
     * 
     * @throws RateLimitException 如果qps限制，抛出异常
     * @throws ResourceNotFoundException 无resource，抛出异常
     */
    public static void enterWithException(String resource) throws RateLimitException {
        RateLimiter limiter = resourceLimiterMap.get(resource);
        if (limiter == null) {
            throw new ResourceNotFoundException(resource);
        }
        if (!limiter.tryAcquire()) {
            throw new RateLimitException(resource);
        }
    }

    /**
     * 阻塞qps控制
     * 
     * @throws ResourceNotFoundException 无resource，抛出异常
     */
    public static void blockingEnter(String resource) {
        RateLimiter limiter = resourceLimiterMap.get(resource);
        if (limiter == null) {
            throw new ResourceNotFoundException(resource);
        }
        limiter.acquire();
    }

    public static class RateLimitException extends Exception {

        private static final long serialVersionUID = 1L;

        private String resource;

        public String getResource() {
            return resource;
        }

        public RateLimitException(String resource) {
            super(resource + " should not be visited so frequently");
            this.resource = resource;
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    public static class ResourceNotFoundException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private String resource;

        public String getResource() {
            return resource;
        }

        public ResourceNotFoundException(String resource) {
            super(resource + " resource not found");
            this.resource = resource;
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}

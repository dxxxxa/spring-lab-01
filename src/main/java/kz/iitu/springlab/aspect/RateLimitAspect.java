package kz.iitu.springlab.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Aspect
@Component
@Order(4)
public class RateLimitAspect {

    private static final int MAX_CALLS = 3;
    private static final long WINDOW_MS = 10_000;

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Around("kz.iitu.springlab.aspect.Pointcuts.serviceOperation()")
    public Object rateLimit(ProceedingJoinPoint pjp) throws Throwable {

        String method = pjp.getSignature().toShortString();

        Window window = windows.computeIfAbsent(
                method,
                key -> new Window()
        );

        synchronized (window) {
            long now = System.currentTimeMillis();

            if (now - window.startTime >= WINDOW_MS) {
                window.startTime = now;
                window.calls.set(0);
            }

            int currentCall = window.calls.incrementAndGet();

            if (currentCall > MAX_CALLS) {
                throw new IllegalStateException(
                        "Rate limit exceeded for " + method
                                + ": maximum " + MAX_CALLS
                                + " calls per 10 seconds"
                );
            }

            return pjp.proceed();
        }
    }

    private static class Window {
        private long startTime = System.currentTimeMillis();
        private final AtomicInteger calls = new AtomicInteger(0);
    }
}
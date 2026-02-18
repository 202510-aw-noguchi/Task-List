package com.example.todo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceAspect {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceAspect.class);
    private final AopTraceStore aopTraceStore;

    public PerformanceAspect(AopTraceStore aopTraceStore) {
        this.aopTraceStore = aopTraceStore;
    }

    @Pointcut("execution(* com.example.todo.service.*.*(..))")
    public void serviceMethods() {
    }

    @Around("serviceMethods()")
    public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            String method = joinPoint.getSignature().getName();
            logger.info("Performance {} took {} ms", method, elapsed);
            aopTraceStore.addInfo(method, "Performance took " + elapsed + " ms");
        }
    }
}

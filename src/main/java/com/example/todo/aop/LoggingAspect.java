package com.example.todo.aop;

import java.util.Arrays;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    private final AopTraceStore aopTraceStore;

    public LoggingAspect(AopTraceStore aopTraceStore) {
        this.aopTraceStore = aopTraceStore;
    }

    @Pointcut("execution(* com.example.todo.service.*.*(..))")
    public void serviceMethods() {
    }

    @Before("serviceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        String method = joinPoint.getSignature().getName();
        String args = Arrays.toString(joinPoint.getArgs());
        logger.info("Before {} args={}", method, args);
        aopTraceStore.addInfo(method, "Before args=" + args);
    }

    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String method = joinPoint.getSignature().getName();
        logger.info("After {} result={}", method, result);
        aopTraceStore.addInfo(method, "After result=" + String.valueOf(result));
    }

    @AfterThrowing(pointcut = "serviceMethods()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        String method = joinPoint.getSignature().getName();
        logger.error("Error {} message={}", method, ex.getMessage());
        aopTraceStore.addError(method, "Error message=" + ex.getMessage());
    }
}

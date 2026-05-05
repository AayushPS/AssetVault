package com.assetvault.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Profile("dev")
public class ServiceLoggingAspect {

    @Around("execution(public * com.assetvault.service..*(..)) && @within(org.springframework.stereotype.Service)")
    public Object logServiceEntryAndExit(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger logger = loggerFor(joinPoint);
        if (!logger.isDebugEnabled()) {
            return joinPoint.proceed();
        }

        String methodName = methodName(joinPoint);
        long startedAt = System.nanoTime();
        logger.debug("Entering {}", methodName);
        try {
            Object result = joinPoint.proceed();
            logger.debug("Exiting {} after {} ms", methodName, elapsedMillis(startedAt));
            return result;
        } catch (Throwable ex) {
            logger.debug("Exiting {} with {} after {} ms", methodName, ex.getClass().getSimpleName(), elapsedMillis(startedAt));
            throw ex;
        }
    }

    private Logger loggerFor(ProceedingJoinPoint joinPoint) {
        Object target = joinPoint.getTarget();
        Class<?> loggerClass = target == null ? joinPoint.getSignature().getDeclaringType() : target.getClass();
        return LoggerFactory.getLogger(loggerClass);
    }

    private String methodName(ProceedingJoinPoint joinPoint) {
        Object target = joinPoint.getTarget();
        Class<?> targetClass = target == null ? joinPoint.getSignature().getDeclaringType() : target.getClass();
        return "%s.%s".formatted(targetClass.getSimpleName(), joinPoint.getSignature().getName());
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}

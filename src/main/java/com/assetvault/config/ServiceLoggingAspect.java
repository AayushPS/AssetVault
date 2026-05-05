package com.assetvault.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

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
        logger.debug("Entering {} args={}", methodName, summarizeArguments(joinPoint.getArgs()));
        try {
            Object result = joinPoint.proceed();
            logger.debug("Exiting {} after {} ms result={}", methodName, elapsedMillis(startedAt), summarize(result));
            return result;
        } catch (Throwable ex) {
            logger.debug(
                    "Exiting {} with {} after {} ms message={}",
                    methodName,
                    ex.getClass().getSimpleName(),
                    elapsedMillis(startedAt),
                    ex.getMessage()
            );
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

    private String summarizeArguments(Object[] args) {
        return Arrays.stream(args)
                .map(this::summarize)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private String summarize(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Page<?> page) {
            return "Page{items=%d,total=%d}".formatted(page.getNumberOfElements(), page.getTotalElements());
        }
        if (value instanceof Collection<?> collection) {
            return "%s{size=%d}".formatted(value.getClass().getSimpleName(), collection.size());
        }
        if (isScalar(value)) {
            return String.valueOf(value);
        }
        Class<?> type = value.getClass();
        if (type.isRecord()) {
            return value.toString();
        }
        return type.getSimpleName();
    }

    private boolean isScalar(Object value) {
        return value instanceof CharSequence
                || value instanceof Number
                || value instanceof Boolean
                || value instanceof Enum<?>;
    }
}

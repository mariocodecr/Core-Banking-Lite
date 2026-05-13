package com.corebanking.audit;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * AOP aspect that logs every REST controller method invocation.
 *
 * Captures:
 * - Method signature (controller + method name)
 * - Execution time in milliseconds
 * - Success vs. exception outcome
 *
 * The correlationId is automatically included in every log line because
 * CorrelationIdFilter already set it in MDC before any controller is called.
 */
@Aspect
@Component
@Slf4j
public class AuditLogAspect {

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logControllerCall(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().toShortString();
        long start = System.currentTimeMillis();

        log.info("→ {}", method);

        try {
            Object result = pjp.proceed();
            log.info("← {} — {}ms", method, System.currentTimeMillis() - start);
            return result;
        } catch (Exception ex) {
            log.error("✗ {} — {}ms — {}: {}",
                    method,
                    System.currentTimeMillis() - start,
                    ex.getClass().getSimpleName(),
                    ex.getMessage());
            throw ex;
        }
    }
}

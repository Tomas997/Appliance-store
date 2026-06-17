package com.epam.rd.autocode.assessment.appliances.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingServices {

    @Around("@annotation(com.epam.rd.autocode.assessment.appliances.aspect.Loggable)")
    public Object logMethod(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().toShortString();
        log.debug("→ {} args={}", method, Arrays.toString(pjp.getArgs()));
        try {
            Object result = pjp.proceed();
            log.info("✓ {}", method);
            return result;
        } catch (Exception ex) {
            log.error("✗ {} failed: {}", method, ex.getMessage());
            throw ex;
        }
    }
}

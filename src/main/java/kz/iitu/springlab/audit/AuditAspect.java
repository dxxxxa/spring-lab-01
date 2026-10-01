package kz.iitu.springlab.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;

@Aspect
@Component
@Order(1)
public class AuditAspect {

    private static final Logger log =
            LoggerFactory.getLogger(AuditAspect.class);

    @Around("@annotation(audited)")
    public Object audit(
            ProceedingJoinPoint pjp,
            Audited audited) throws Throwable {

        String action = audited.action();

        if (audited.logArguments()) {
            log.info("[AUDIT] start {} args={} time={}",
                    action,
                    Arrays.toString(pjp.getArgs()),
                    Instant.now());
        } else {
            log.info("[AUDIT] start {} time={}",
                    action,
                    Instant.now());
        }

        try {
            Object result = pjp.proceed();

            log.info("[AUDIT] {} success", action);

            return result;

        } catch (Throwable ex) {

            log.info("[AUDIT] {} failure: {}",
                    action,
                    ex.getMessage());

            throw ex;
        }
    }
}
package com.project.msauction.config.logging;

import ch.qos.logback.classic.spi.EventArgUtil;
import com.project.msauction.exception.handler.GlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Pointcut(value = "within(com.project.msauction.auction.controller..*)")
    public void controllerPc() {}

    @Pointcut(value = "within(com.project.msauction.auction.service..*)")
    public void servicePc() {}

    @Pointcut(value = "within(com.project.msauction.client..*)")
    public void clientPc() {}

    @Pointcut(value = "within(com.project.msauction.scheduler..*)")
    public void schedulerPc() {}

    @Before(value = "controllerPc() || servicePc() || clientPc() || schedulerPc()")
    public void logBefore(JoinPoint joinPoint) {
        startTime.set(System.currentTimeMillis());
        log.info("Started: {}", joinPoint.getSignature().toShortString());
    }

    @AfterReturning(value = "controllerPc() || servicePc() || clientPc() || schedulerPc()")
    public void logAfter(JoinPoint joinPoint) {
        Long start = startTime.get();
        Long duration = start != null ? System.currentTimeMillis() - start : 0;

        log.info("Finished: {} in {} ms.",
                joinPoint.getSignature().toShortString(),
                duration);
        startTime.remove();
    }


    @AfterThrowing(value = "controllerPc() || servicePc() || clientPc() || schedulerPc()",
            throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        Long start = startTime.get();
        Long duration = start != null ? System.currentTimeMillis() - start : 0;

        log.error("Failed: {} after {} ms. Exception: {}",
                joinPoint.getSignature().toShortString(),
                duration,
                e.getMessage());

        startTime.remove();
    }

}

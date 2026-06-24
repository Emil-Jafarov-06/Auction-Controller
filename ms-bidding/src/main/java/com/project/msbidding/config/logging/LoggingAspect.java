package com.project.msbidding.config.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Pointcut(value = "within(com.project.msbidding.bid.controller..* || com.project.msbidding.bidder.controller..*)")
    public void controllerPc() {}

    @Pointcut(value = "within(com.project.msbidding.bid.service..* || com.project.msbidding.bid.service..*)")
    public void servicePc() {}

    @Pointcut(value = "within(com.project.msbidding.client..* || com.project.msbidding.client..*)")
    public void clientPc(){}

    @Before(value = "controllerPc() || servicePc() || clientPc()")
    public void logBefore(JoinPoint joinPoint) {
        startTime.set(System.currentTimeMillis());
        log.info("Started: {}", joinPoint.getSignature().toShortString());
    }

    @AfterReturning(value = "controllerPc() || servicePc() || clientPc()")
    public void logAfter(JoinPoint joinPoint) {
        Long start = startTime.get();
        Long duration = start != null ? System.currentTimeMillis() - start : 0;
        log.info("Finished: {} in {} ms.",
                joinPoint.getSignature().toShortString(),
                duration);

        startTime.remove();
    }

    @AfterThrowing(value = "controllerPc() || servicePc() || clientPc()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        Long start = startTime.get();
        Long duration = start != null ? System.currentTimeMillis() - start : 0;
        log.info("Failed: {} after {} ms. Exception : {}",
                joinPoint.getSignature().toShortString(),
                duration,
                e.getMessage(),
                e);

        startTime.remove();
    }

}

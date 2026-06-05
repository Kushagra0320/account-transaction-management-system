package com.atm.backend.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * AOP Logging Aspect — Cross-Cutting Concern.
 *
 * Responsibilities:
 * 1. Measures execution time of every strategy (deposit/withdraw/transfer) via @Around.
 * 2. Logs full result on success (timing + strategy name).
 * 3. Logs failure class name only — never logs balance amounts or PII.
 *
 * Pointcut targets the service.strategy sub-package in com.atm.backend.
 */
@Slf4j
@Aspect
@Component
public class TransactionLoggingAspect {

    @Pointcut("execution(* com.atm.backend.service.strategy.*.execute(..))")
    public void transactionStrategyExecution() {}

    @Around("transactionStrategyExecution()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String strategyName = joinPoint.getTarget().getClass().getSimpleName();
        long startTime = System.currentTimeMillis();
        log.info("[TXN-AUDIT] Starting strategy: {}", strategyName);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.info("[TXN-AUDIT] {} completed in {}ms | Result: {}", strategyName, duration, result);
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[TXN-AUDIT] {} FAILED after {}ms | Exception: {}",
                    strategyName, duration, ex.getClass().getSimpleName());
            throw ex;
        }
    }

    @AfterThrowing(pointcut = "transactionStrategyExecution()", throwing = "ex")
    public void logTransactionFailure(JoinPoint joinPoint, Throwable ex) {
        String strategyName = joinPoint.getTarget().getClass().getSimpleName();
        log.warn("[TXN-AUDIT] FAILURE RECORDED | Strategy: {} | Type: {}",
                strategyName, ex.getClass().getSimpleName());
    }
}

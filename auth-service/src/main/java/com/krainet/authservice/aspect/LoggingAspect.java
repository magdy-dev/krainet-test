package com.krainet.authservice.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* com.krainet.authservice.controller..*(..)) || " +
            "execution(* com.krainet.authservice.service..*(..))")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        if (log.isDebugEnabled()) {
            return logMethodWithDetails(joinPoint);
        } else if (log.isInfoEnabled()) {
            return logBasicInfo(joinPoint);
        } else {
            return joinPoint.proceed();
        }
    }

    private Object logMethodWithDetails(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();
        
        // Get method arguments
        Object[] args = joinPoint.getArgs();
        String[] argNames = methodSignature.getParameterNames();
        
        // Log method entry with parameters
        Map<String, Object> params = new HashMap<>();
        if (argNames != null && argNames.length > 0) {
            for (int i = 0; i < argNames.length; i++) {
                params.put(argNames[i], args[i]);
            }
        }
        
        log.debug("Entering: {}.{}() with arguments: {}", className, methodName, params);
        
        // Measure method execution time
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        try {
            // Execute the method
            Object result = joinPoint.proceed();
            stopWatch.stop();
            
            // Log method exit with return value
            log.debug("Exiting: {}.{}() with result: {} (execution time: {} ms)", 
                    className, methodName, result, stopWatch.getTotalTimeMillis());
            
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            log.error("Exception in {}.{}(): {} (execution time: {} ms)", 
                    className, methodName, e.getMessage(), stopWatch.getTotalTimeMillis(), e);
            throw e;
        }
    }
    
    private Object logBasicInfo(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();
        
        log.info("Executing: {}.{}", className, methodName);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();
            log.info("Completed: {}.{} (execution time: {} ms)", 
                    className, methodName, stopWatch.getTotalTimeMillis());
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            log.error("Error in {}.{}: {} (execution time: {} ms)", 
                    className, methodName, e.getMessage(), stopWatch.getTotalTimeMillis());
            throw e;
        }
    }
}

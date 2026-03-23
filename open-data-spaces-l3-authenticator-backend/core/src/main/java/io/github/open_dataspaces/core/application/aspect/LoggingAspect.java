/*
 * LoggingAspect.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is an aspect for logging method entry and exit points in the application.
 *
 * Date: 2025/08/04
 */

package io.github.open_dataspaces.core.application.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import io.github.open_dataspaces.core.common.consts.Const;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aspect for logging method entry and exit points in the application.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Before execution of all controller methods.
     *
     * @param joinPoint the join point
     */
    @Before("within(" + Const.LOGGING_ASPECT_CONTROLLER_PACKAGE + "..*)")
    public void beforeController(JoinPoint joinPoint) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Entering {} with args: {}", joinPoint.getSignature(), joinPoint.getArgs());
        } else {
            LOGGER.info("Entering {} ", joinPoint.getSignature());
        }
    }

    /**
     * After successful execution of all controller methods.
     *
     * @param joinPoint the join point
     * @param result    the result
     */
    @AfterReturning(pointcut = "within(" + Const.LOGGING_ASPECT_CONTROLLER_PACKAGE + "..*)", returning = "result")
    public void afterController(JoinPoint joinPoint, Object result) {
        if (LOGGER.isDebugEnabled()) {
            Object body = result;
            if (result instanceof org.springframework.http.ResponseEntity<?> responseEntity) {
                body = responseEntity.getBody();
            }
            LOGGER.debug("Exiting {} with return: {}", joinPoint.getSignature(), body);
        } else {
            LOGGER.info("Exiting {} ", joinPoint.getSignature());
        }
    }

    /**
     * When an exception occurs in any controller method.
     *
     * @param joinPoint the join point
     * @param error     the error
     */
    @AfterThrowing(pointcut = "within(" + Const.LOGGING_ASPECT_CONTROLLER_PACKAGE + "..*)", throwing = "error")
    public void afterThrowingController(JoinPoint joinPoint, Throwable error) {
        LOGGER.debug("Exception in {}: {}", joinPoint.getSignature(), error.getMessage(), error);
    }

    /**
     * Before execution of all service methods.
     *
     * @param joinPoint the join point
     */
    @Before("within(" + Const.LOGGING_ASPECT_SERVICE_PACKAGE + "..*)")
    public void beforeService(JoinPoint joinPoint) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Entering {} with args: {}", joinPoint.getSignature(), joinPoint.getArgs());
        } else {
            LOGGER.info("Entering {} ", joinPoint.getSignature());
        }
    }

    /**
     * After successful execution of all service methods.
     *
     * @param joinPoint the join point
     * @param result    the result
     */
    @AfterReturning(pointcut = "within(" + Const.LOGGING_ASPECT_SERVICE_PACKAGE + "..*)", returning = "result")
    public void afterService(JoinPoint joinPoint, Object result) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Exiting {} with return: {}", joinPoint.getSignature(), result);
        } else {
            LOGGER.info("Exiting {} ", joinPoint.getSignature());
        }
    }

    /**
     * When an exception occurs in any service method.
     *
     * @param joinPoint the join point
     * @param error     the error
     */
    @AfterThrowing(pointcut = "within(" + Const.LOGGING_ASPECT_SERVICE_PACKAGE + "..*)", throwing = "error")
    public void afterThrowingService(JoinPoint joinPoint, Throwable error) {
        LOGGER.trace("Exception in {}: {}", joinPoint.getSignature(), error.getMessage(), error);
    }

    /**
     * Before execution of all infrastructure methods.
     *
     * @param joinPoint the join point
     */
    @Before("within(" + Const.LOGGING_ASPECT_INFRASTRUCTURE_PACKAGE + "..*)")
    public void beforeInfrastructure(JoinPoint joinPoint) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Entering {} with args: {}", joinPoint.getSignature(), joinPoint.getArgs());
        } else {
            LOGGER.info("Entering {} ", joinPoint.getSignature());
        }
    }

    /**
     * After successful execution of all infrastructure methods.
     *
     * @param joinPoint the join point
     * @param result    the result
     */
    @AfterReturning(pointcut = "within(" + Const.LOGGING_ASPECT_INFRASTRUCTURE_PACKAGE + "..*)", returning = "result")
    public void afterInfrastructure(JoinPoint joinPoint, Object result) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Exiting {} with return: {}", joinPoint.getSignature(), result);
        } else {
            LOGGER.info("Exiting {} ", joinPoint.getSignature());
        }
    }

    /**
     * When an exception occurs in any infrastructure method.
     *
     * @param joinPoint the join point
     * @param error     the error
     */
    @AfterThrowing(pointcut = "within(" + Const.LOGGING_ASPECT_INFRASTRUCTURE_PACKAGE + "..*)", throwing = "error")
    public void afterThrowingInfrastructure(JoinPoint joinPoint, Throwable error) {
        LOGGER.trace("Exception in {}: {}", joinPoint.getSignature(), error.getMessage(), error);
    }
}

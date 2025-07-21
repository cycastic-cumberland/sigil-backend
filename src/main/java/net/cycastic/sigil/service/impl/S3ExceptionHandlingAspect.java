package net.cycastic.sigil.service.impl;

import net.cycastic.sigil.domain.exception.RequestException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Aspect
@Component
public class S3ExceptionHandlingAspect {
    @Around("@annotation(HandleS3Exception)")
    public Object handleS3Exception(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (S3Exception e) {
            var statusCode = e.statusCode();
            switch (statusCode){
                case 400:
                case 401:
                case 403:
                case 404: {
                    throw new RequestException(statusCode, e, e.getMessage());
                }
                default: {
                    throw new RequestException(500, e, "Internal server error");
                }
            }
        }
    }
}

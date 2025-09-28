package net.cycastic.sigil.service.impl;

import net.cycastic.sigil.domain.exception.RequestException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

@Aspect
@Component
public class AwsExceptionHandlingAspect {
    @Around("@annotation(HandleAwsException)")
    public Object handleAwsException(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (AwsServiceException e) {
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

package io.pp.arcade.global.trace.aspect;

import io.pp.arcade.global.trace.domain.TraceStatus;
import io.pp.arcade.global.trace.service.LogTrace;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@AllArgsConstructor
@Component
public class TraceAspect {
    private final LogTrace logTrace;

    @Pointcut("execution(* io.pp.arcade.domain..*(..))")
    public void allDomain(){}

    @Pointcut("execution(* io.pp.arcade.domain.security..*(..))")
    public void securityDomain(){}

    @Pointcut("execution(* io.pp.arcade.global.util..*(..))")
    public void utilDomain(){}

    @Pointcut("execution(* io.pp.arcade.global.scheduler..*(..))")
    public void schedulerDomain(){}

    @Around("(allDomain() || utilDomain() || schedulerDomain()) && !securityDomain()")
    public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
        TraceStatus status = null;
        MethodSignature method = (MethodSignature)joinPoint.getSignature();
        Object[] methodArgs = joinPoint.getArgs();
        try{
            status = logTrace.begin(method.getDeclaringType().getSimpleName() + "." + method.getName() + "(): arguments = " + Arrays.toString(methodArgs));
            Object result = joinPoint.proceed();
            logTrace.end(status);
            return result;
        } catch (Exception e) {
            logTrace.exception(status, e);
            throw e;
        }
    }
}

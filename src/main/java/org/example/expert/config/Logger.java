package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class Logger {

    @Around("@annotation(org.example.expert.config.AdminLogging)")
    public Object executionLogger(ProceedingJoinPoint joinPoint) throws Throwable {

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        String paramsString = buildParamsString(parameterNames, args);

        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes()).getRequest();

        Long userId = (Long) request.getAttribute("userId");
        String url = request.getRequestURI();

        log.info("[API 요청] userId = {}, url = {}, {}.{} | 파라미터: [{}]",
                userId, url, className, methodName, paramsString);

        long start = System.currentTimeMillis();

        Object proceed = joinPoint.proceed();

        long end = System.currentTimeMillis();

        log.info("[API 응답] userId = {}, {}.{} (수행시간: {}ms)",
                userId, className, methodName, (end - start));

        return proceed;
    }

    private String buildParamsString(String[] parameterNames, Object[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parameterNames.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(parameterNames[i]).append("=").append(args[i]);
        }
        return sb.toString();
    }
}

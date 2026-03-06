package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.LinkedHashMap;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class Logger {

    private final ObjectMapper mapper = new ObjectMapper();

    @Around("@annotation(org.example.expert.config.AdminLogging)")
    public Object executionLogger(ProceedingJoinPoint joinPoint) throws Throwable {

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        Map<String, Object> paramsMap = new LinkedHashMap<>();
        for (int i = 0; i < parameterNames.length; i++) {
            paramsMap.put(parameterNames[i], args[i]);
        }

        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes()).getRequest();

        Long userId = (Long) request.getAttribute("userId");
        String url = request.getRequestURI();

        String paramsJson = mapper.writeValueAsString(paramsMap);

        log.info("[API 요청] userId = {}, url = {}, {}.{} | 요청: [{}]",
                userId, url, className, methodName, paramsJson);

        long start = System.currentTimeMillis();

        Object proceed = joinPoint.proceed();

        String responseJson = mapper.writeValueAsString(proceed);

        long end = System.currentTimeMillis();

        log.info("[API 응답] userId = {}, {}.{} | 응답: {} (수행시간: {}ms)",
                userId, className, methodName, responseJson, (end - start));

        return proceed;
    }
}

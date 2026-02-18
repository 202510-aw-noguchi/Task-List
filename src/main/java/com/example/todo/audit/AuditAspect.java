package com.example.todo.audit;

import com.example.todo.entity.AppUser;
import com.example.todo.repository.UserRepository;
import com.example.todo.service.AuditService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuditAspect {
    private final AuditService auditService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    public AuditAspect(AuditService auditService,
                       UserRepository userRepository,
                       ObjectMapper objectMapper) {
        this.auditService = auditService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(auditable)")
    public Object aroundAuditable(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String oldValueJson = toJson(buildOldValue(joinPoint));
        Object result = null;
        Throwable thrown = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            thrown = ex;
            throw ex;
        } finally {
            String newValueJson = thrown == null ? toJson(result) : toJson(Map.of("error", thrown.getMessage()));
            Long entityId = resolveEntityId(joinPoint, auditable, result);
            auditService.record(
                    auditable.action(),
                    auditable.entityType(),
                    entityId,
                    resolveCurrentUserId(),
                    oldValueJson,
                    newValueJson,
                    resolveIpAddress()
            );
        }
    }

    private Map<String, Object> buildOldValue(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("method", signature.getDeclaringType().getSimpleName() + "." + signature.getName());
        if (paramNames == null || paramNames.length == 0) {
            map.put("args", Arrays.asList(args));
            return map;
        }
        for (int i = 0; i < paramNames.length; i++) {
            map.put(paramNames[i], i < args.length ? args[i] : null);
            map.put("p" + i, i < args.length ? args[i] : null);
        }
        return map;
    }

    private Long resolveEntityId(ProceedingJoinPoint joinPoint, Auditable auditable, Object result) {
        String exprText = auditable.entityIdExpression();
        if (exprText == null || exprText.isBlank()) {
            return null;
        }
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            StandardEvaluationContext context = new StandardEvaluationContext();
            for (int i = 0; i < args.length; i++) {
                context.setVariable("p" + i, args[i]);
                if (paramNames != null && i < paramNames.length) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }
            context.setVariable("result", result);
            Expression expression = expressionParser.parseExpression(exprText);
            Object value = expression.getValue(context);
            if (value == null) {
                return null;
            }
            if (value instanceof Number number) {
                return number.longValue();
            }
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }

    private Long resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = authentication.getName();
        Optional<AppUser> userOpt = userRepository.findByUsername(username);
        return userOpt.map(AppUser::getId).orElse(null);
    }

    private String resolveIpAddress() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes servletAttrs)) {
            return null;
        }
        String forwarded = servletAttrs.getRequest().getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return servletAttrs.getRequest().getRemoteAddr();
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "{\"serializationError\":\"" + ex.getMessage().replace("\"", "'") + "\"}";
        }
    }
}

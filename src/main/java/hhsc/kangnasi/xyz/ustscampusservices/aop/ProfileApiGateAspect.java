package hhsc.kangnasi.xyz.ustscampusservices.aop;


import hhsc.kangnasi.xyz.ustscampusservices.annotation.ProfileApiGate;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.core.env.Environment;
import org.aspectj.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import org.aspectj.lang.reflect.MethodSignature;


@Slf4j
@Aspect
@Component
public class ProfileApiGateAspect {
    private final Environment env;


    public ProfileApiGateAspect(Environment env) {
        this.env = env;
    }

    @Around("@annotation(hhsc.kangnasi.xyz.ustscampusservices.annotation.ProfileApiGate)")
    public Object guard(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        ProfileApiGate gate = AnnotationUtils.findAnnotation(method, ProfileApiGate.class);

        // 当前激活的 profiles（小写去重）
        Set<String> actives = Arrays.stream(env.getActiveProfiles())
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 注解要求的 profiles（小写去重）
        List<String> required = Arrays.stream(gate.value())
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toLowerCase)
                .distinct()
                .toList();

        boolean pass;
        if (gate.anyMatch()) {
            pass = required.stream().anyMatch(actives::contains);
        } else {
            pass = actives.containsAll(required);
        }

        if (!pass) {
            String requiredStr = String.join(",", gate.value());
            String activeStr = String.join(",", actives);
            String msg = gate.message().replace("{profiles}", requiredStr);
            log.warn("ProfileGate 拦截：method={}, required=[{}], active=[{}], anyMatch={}",
                    method.getName(), requiredStr, activeStr, gate.anyMatch());
            throw new IllegalStateException(msg);
        }

        return pjp.proceed();
    }
}

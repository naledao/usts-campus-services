package hhsc.kangnasi.xyz.ustscampusservices.aop;

import hhsc.kangnasi.xyz.ustscampusservices.annotation.APIRateLimiting;
import hhsc.kangnasi.xyz.ustscampusservices.exception.TooManyRequestsException;
import hhsc.kangnasi.xyz.ustscampusservices.util.SpelUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateLimiterConfig;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    // 注意：改成 final，才能被 @RequiredArgsConstructor 正确注入
    private final RedissonClient redissonClient;
    private final SpelUtil spelUtil;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint pjp, APIRateLimiting rateLimit) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();

        // 1) 计算 limiter 名称（前缀 + 方法签名 + 动态维度）
        String limiterName = buildLimiterName(rateLimit, pjp, method);

        // 2) 获取/配置限流器
        RRateLimiter limiter = redissonClient.getRateLimiter(limiterName);
        ensureLimiterConfig(limiter, rateLimit);

        // 3) 获取令牌
        boolean acquired = tryAcquire(limiter, rateLimit);
        if (!acquired) {
            throw new TooManyRequestsException("请求过于频繁，请稍后再试");
        }

        // 4) 放行
        return pjp.proceed();
    }

    private String buildLimiterName(APIRateLimiting anno, ProceedingJoinPoint pjp, Method method) {
        Signature signature = pjp.getSignature();
        String methodKey = signature.getDeclaringTypeName() + "#" + signature.getName();

        StringBuilder name = new StringBuilder();
        name.append(anno.name()).append(":").append(methodKey);

        switch (anno.mode()) {
            case PER_IP -> name.append(":ip:").append(resolveClientIp().orElse("unknown"));

            case PER_KEY -> {
                String keyExpr = anno.key();
                Object keyVal = null;

                if (StringUtils.hasText(keyExpr)) {
                    // 既支持普通 SpEL：@authUtil.getCurrentEmail() + ':limit'
                    // 也支持模板：    "limit-#{@authUtil.getCurrentEmail()}"
                    try {
                        if (isTemplate(keyExpr)) {
                            keyVal = spelUtil.parseTemplate(
                                    keyExpr, method, pjp.getArgs(), Map.of(), pjp.getTarget());
                        } else {
                            keyVal = spelUtil.parse(
                                    keyExpr, method, pjp.getArgs(), Map.of(), pjp.getTarget());
                        }
                    } catch (Exception e) {
                        // 为防止 SpEL 失败导致 500，这里降级记录日志并退化为 _empty
                        log.warn("RateLimit SpEL 解析失败, expr={}, err={}", keyExpr, e.toString());
                    }
                }

                String keyStr = (keyVal != null) ? String.valueOf(keyVal) : "";
                if (StringUtils.hasText(keyStr)) {
                    name.append(":key:").append(keyStr);
                } else {
                    name.append(":key:_empty"); // 退化到 OVERALL 的一个桶
                }
            }

            case OVERALL -> { /* 无额外维度 */ }
        }
        return name.toString();
    }

    private boolean isTemplate(String expr) {
        // 简单判定是否包含 #{...}
        return expr.contains("#{") && expr.contains("}");
    }

    private Optional<String> resolveClientIp() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs == null) return Optional.empty();
        Object req = attrs.resolveReference(RequestAttributes.REFERENCE_REQUEST);
        if (!(req instanceof HttpServletRequest r)) return Optional.empty();

        String xff = r.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            return Optional.of(xff.split(",")[0].trim());
        }
        String xri = r.getHeader("X-Real-IP");
        if (StringUtils.hasText(xri)) return Optional.of(xri);
        return Optional.ofNullable(r.getRemoteAddr());
    }

    private void ensureLimiterConfig(RRateLimiter limiter, APIRateLimiting anno) {
        RateType rateType = RateType.OVERALL; // 维度通过名称分桶
        long rate = anno.rate();
        long interval = anno.interval();
        RateIntervalUnit unit = toRateUnit(anno.intervalUnit());

        RateLimiterConfig current = limiter.getConfig();
        if (current == null) {
            limiter.trySetRate(rateType, rate, interval, unit);
            return;
        }

        try {
            // 新版支持动态更新
            limiter.setRate(rateType, rate, interval, unit);
            return;
        } catch (UnsupportedOperationException | IllegalArgumentException ignored) {
        }

        boolean diff = false;
        try {
            if (current.getRateType() != rateType) diff = true;
            if (current.getRate() != rate) diff = true;
            if (current.getRateInterval() != interval) diff = true;

            try {
                var m = current.getClass().getMethod("getRateIntervalUnit");
                Object curUnit = m.invoke(current);
                if (curUnit instanceof RateIntervalUnit && curUnit != unit) diff = true;
            } catch (NoSuchMethodException ignore) { }
        } catch (Exception e) {
            diff = true;
        }

        if (diff) {
            try { limiter.delete(); } catch (Exception ignored) { }
            limiter.trySetRate(rateType, rate, interval, unit);
        }
    }

    private RateIntervalUnit toRateUnit(TimeUnit unit) {
        return switch (unit) {
            case SECONDS -> RateIntervalUnit.SECONDS;
            case MINUTES -> RateIntervalUnit.MINUTES;
            case HOURS   -> RateIntervalUnit.HOURS;
            case DAYS    -> RateIntervalUnit.DAYS;
            default      -> RateIntervalUnit.SECONDS;
        };
    }

    private boolean tryAcquire(RRateLimiter limiter, APIRateLimiting anno) {
        int permits = Math.max(1, anno.permits());
        long timeout = anno.timeout();
        if (timeout <= 0) {
            return limiter.tryAcquire(permits);
        }
        long waitMs = anno.timeoutUnit().toMillis(timeout);
        return limiter.tryAcquire(permits, waitMs, TimeUnit.MILLISECONDS);
    }
}

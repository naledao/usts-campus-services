package hhsc.kangnasi.xyz.ustscampusservices.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redisson RRateLimiter 的接口限流
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface APIRateLimiting {

    /** 令牌发放速率（每个窗口最多允许的“许可数”） */
    long rate();

    /** 窗口长度 */
    long interval();

    /** 窗口时间单位（建议：SECONDS 或 MINUTES） */
    TimeUnit intervalUnit() default TimeUnit.SECONDS;

    /** 每次请求需要消耗的许可数 */
    int permits() default 1;

    /** 等待获取许可的超时时间（0 表示立即失败，>0 表示等待） */
    long timeout() default 0;

    /** 超时时间单位 */
    TimeUnit timeoutUnit() default TimeUnit.MILLISECONDS;

    /** 限流模式 */
    Mode mode() default Mode.OVERALL;

    /**
     * 自定义限流器名称前缀（可选）。
     * 最终限流器 key = 前缀:方法唯一签名[:动态key]
     */
    String name() default "rate:limiter";

    /**
     * 动态 key（SpEL），常用于“按用户/租户/业务ID”限流。
     * 例：'user:' + #userId  或  'tenant:' + #tenantId
     */
    String key() default "";

    enum Mode {
        /** 全局限流（所有请求共用一个桶） */
        OVERALL,
        /** 按 SpEL 计算后的 key 维度限流 */
        PER_KEY,
        /** 按请求 IP 限流（从 HttpServletRequest 解析） */
        PER_IP
    }
}

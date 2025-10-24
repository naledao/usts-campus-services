package hhsc.kangnasi.xyz.ustscampusservices.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProfileApiGate {

    /**
     * 允许访问的 profile 列表（大小写不敏感）
     * 例：{"local"} 或 {"dev","test"}
     */
    String[] value();

    /**
     * 匹配模式：true=任一匹配（默认）；false=全部匹配
     */
    boolean anyMatch() default true;

    /**
     * 拒绝时的自定义提示（{profiles} 会被替换为 value 内容）
     */
    String message() default "该接口仅允许在 {profiles} 环境访问";
}

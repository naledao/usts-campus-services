package hhsc.kangnasi.xyz.ustscampusservices.util;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.Map;

@Component
public class SpelUtil {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer pnd = new DefaultParameterNameDiscoverer();
    private final BeanFactory beanFactory;

    public SpelUtil(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 解析普通 SpEL（支持 @bean、#param）
     */
    public Object parse(String spel, @Nullable Method method, @Nullable Object[] args,
                        @Nullable Map<String, Object> vars, @Nullable Object root) {
        if (spel == null || spel.isEmpty()) return null;

        StandardEvaluationContext ctx = new StandardEvaluationContext(root);
        // 关键：让 @beanName 可用
        ctx.setBeanResolver(new BeanFactoryResolver(beanFactory));

        // 方法参数名 -> 值：可用 #userId 这类
        if (method != null && args != null) {
            String[] names = pnd.getParameterNames(method);
            if (names != null) {
                for (int i = 0; i < names.length; i++) {
                    ctx.setVariable(names[i], args[i]);
                }
            }
            // 也可按 Spring 习惯同时提供 #p0/#a0
            for (int i = 0; i < args.length; i++) {
                ctx.setVariable("p" + i, args[i]);
                ctx.setVariable("a" + i, args[i]);
            }
        }

        // 额外自定义变量
        if (vars != null) vars.forEach(ctx::setVariable);

        return parser.parseExpression(spel).getValue(ctx);
    }

    /**
     * 解析模板字符串：如 "key-#{@authUtil.getCurrentEmail()}"
     */
    public String parseTemplate(String template, @Nullable Method method, @Nullable Object[] args,
                                @Nullable Map<String, Object> vars, @Nullable Object root) {
        if (template == null || template.isEmpty()) return null;
        StandardEvaluationContext ctx = new StandardEvaluationContext(root);
        ctx.setBeanResolver(new BeanFactoryResolver(beanFactory));
        if (method != null && args != null) {
            String[] names = pnd.getParameterNames(method);
            if (names != null) {
                for (int i = 0; i < names.length; i++) ctx.setVariable(names[i], args[i]);
            }
            for (int i = 0; i < args.length; i++) {
                ctx.setVariable("p" + i, args[i]);
                ctx.setVariable("a" + i, args[i]);
            }
        }
        if (vars != null) vars.forEach(ctx::setVariable);
        return parser.parseExpression(template, new TemplateParserContext())
                .getValue(ctx, String.class);
    }
}

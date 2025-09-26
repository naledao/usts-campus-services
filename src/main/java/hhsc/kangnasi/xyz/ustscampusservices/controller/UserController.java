package hhsc.kangnasi.xyz.ustscampusservices.controller;

import hhsc.kangnasi.xyz.ustscampusservices.messaging.AppMessage;
import hhsc.kangnasi.xyz.ustscampusservices.messaging.MessageProducer;
import hhsc.kangnasi.xyz.ustscampusservices.util.EmailUtil;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.time.Duration;

@RequestMapping("/user")
@RestController
public class UserController {

    private final RedissonClient redissonClient;
    private final EmailUtil emailUtil;
    private final MessageProducer messageProducer;

    public UserController(RedissonClient redissonClient, EmailUtil emailUtil, MessageProducer messageProducer) {
        this.redissonClient = redissonClient;
        this.emailUtil = emailUtil;
        this.messageProducer = messageProducer;
    }

    /**
     * 发送登录邮件验证码：先通过 RabbitMQ 发送消息，再发送邮件，并将验证码存入 Redis。
     * 提示：生产环境建议限流、防刷与图形验证码等保护措施。
     */
    @GetMapping("/send-login-code")
    public ResponseEntity<String> sendLoginCode(@RequestParam("email") String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return ResponseEntity.badRequest().body("邮箱格式不正确");
        }

        // 生成 6 位数字验证码
        String code = generateNumericCode(6);

        // 2) 发送邮件验证码
        String subject = "登录验证码";
        int minutes = 5;
        String content = "您的登录验证码为：" + code + "，" + minutes + "分钟内有效。";
        emailUtil.sendText(email, subject, content);

        // 3) 将验证码存入 Redis，设置过期时间
        String key = "login:code:" + email;
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(code, Duration.ofMinutes(minutes));

        return ResponseEntity.ok("验证码已发送");
    }

    private static String generateNumericCode(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}

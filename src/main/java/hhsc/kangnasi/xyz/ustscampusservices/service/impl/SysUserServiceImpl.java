package hhsc.kangnasi.xyz.ustscampusservices.service.impl;

import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.SysUserEntity;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.SysUserMapper;
import hhsc.kangnasi.xyz.ustscampusservices.service.SysUserService;
import hhsc.kangnasi.xyz.ustscampusservices.util.EmailUtil;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class SysUserServiceImpl implements SysUserService {

    private final RedissonClient redissonClient;
    private final EmailUtil emailUtil;
    private final SysUserMapper sysUserMapper;

    public SysUserServiceImpl(RedissonClient redissonClient, EmailUtil emailUtil, SysUserMapper sysUserMapper) {
        this.redissonClient = redissonClient;
        this.emailUtil = emailUtil;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public ResponseEntity<String> sendLoginCode(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return ResponseEntity.badRequest().body("邮箱格式不正确");
        }

        String code = generateNumericCode(6);

        String subject = "登录验证码";
        int minutes = 5;
        String content = "您的登录验证码为：" + code + "，" + minutes + "分钟内有效。";
        emailUtil.sendText(email, subject, content);

        String key = "login:code:" + email;
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(code, Duration.ofMinutes(minutes));

        return ResponseEntity.ok("验证码已发送");
    }

    @Override
    public ResponseEntity<?> login(String email, String code) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return ResponseEntity.badRequest().body("邮箱格式不正确");
        }
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body("验证码不能为空");
        }

        String codeKey = "login:code:" + email;
        RBucket<String> codeBucket = redissonClient.getBucket(codeKey);
        String cachedCode = codeBucket.get();

        if (cachedCode == null || !cachedCode.equals(code)) {
            return ResponseEntity.status(401).body("验证码错误或已过期");
        }

        codeBucket.delete();

        String token = UUID.randomUUID().toString().replace("-", "");
        String tokenKey = "auth:token:" + token;
        RBucket<String> tokenBucket = redissonClient.getBucket(tokenKey);
        tokenBucket.set(email);

        // 查看数据库是否有该用户
        SysUserEntity user = sysUserMapper.selectByEmail(email);
        if (user == null) {
            // 如果数据库中没有该用户，创建一个新用户
            SysUserEntity newUser = new SysUserEntity();
            newUser.setEmail(email);
            newUser.setNickName("用户");
            Date date = new Date();
            newUser.setCreateTime(date);
            newUser.setUpdateTime(date);
            newUser.setIsDel(0);
            sysUserMapper.insert(newUser);
        }
        return ResponseEntity.ok(Map.of("token", token));
    }

    @Override
    public ResponseEntity<?> validateToken(String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body("token不能为空");
        }

        String tokenKey = "auth:token:" + token;
        RBucket<String> bucket = redissonClient.getBucket(tokenKey);
        String email = bucket.get();
        if (email != null) {
            String nickname = "admin";
            try {
                SysUserEntity user = sysUserMapper.selectByEmail(email);
                if (user != null && user.getNickName() != null && !user.getNickName().isBlank()) {
                    nickname = user.getNickName();
                }
            } catch (Exception ignored) {
            }
            return ResponseEntity.ok(Map.of("valid", 1, "nickname", nickname));
        }
        return ResponseEntity.status(401).body("token无效");
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

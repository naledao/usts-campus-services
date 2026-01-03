package hhsc.kangnasi.xyz.ustscampusservices.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hhsc.kangnasi.xyz.ustscampusservices.config.ApplicationContextProvider;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.SysUserEntity;
import hhsc.kangnasi.xyz.ustscampusservices.domain.request.SmsRequest;
import hhsc.kangnasi.xyz.ustscampusservices.domain.vo.CommonServiceVo;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.SysUserMapper;
import hhsc.kangnasi.xyz.ustscampusservices.service.SysUserService;
import hhsc.kangnasi.xyz.ustscampusservices.util.EmailUtil;
import hhsc.kangnasi.xyz.ustscampusservices.websocket.WsSessionHub;
import org.apache.commons.text.StringSubstitutor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static hhsc.kangnasi.xyz.ustscampusservices.config.AuthInterceptor.CURRENT_USER_EMAIL;
import static hhsc.kangnasi.xyz.ustscampusservices.util.EmailUtil.SMSTEMPLATE;
import static hhsc.kangnasi.xyz.ustscampusservices.websocket.SMSWebSocket.SMSKEY;

@Service
public class SysUserServiceImpl implements SysUserService {

    private final RedissonClient redissonClient;
    private final EmailUtil emailUtil;
    private final SysUserMapper sysUserMapper;
    private final WsSessionHub wsSessionHub;
    private final ObjectMapper objectMapper = new ObjectMapper();
    public SysUserServiceImpl(RedissonClient redissonClient, EmailUtil emailUtil, SysUserMapper sysUserMapper, WsSessionHub wsSessionHub) {
        this.redissonClient = redissonClient;
        this.emailUtil = emailUtil;
        this.sysUserMapper = sysUserMapper;
        this.wsSessionHub = wsSessionHub;
    }

    @Override
    public ResponseEntity<String> sendLoginCode(String email) throws JsonProcessingException {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return ResponseEntity.badRequest().body("邮箱格式不正确");
        }

        String code = generateNumericCode(6);

        String subject = "登录验证码";
        int minutes = 5;
        String content = "您的登录验证码为：" + code + "，" + minutes + "分钟内有效。";
        emailUtil.sendText(email, subject, content,true);

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

    @Override
    public ResponseEntity<?> updateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return ResponseEntity.badRequest().body("昵称不能为空");
        }

        SysUserEntity user = sysUserMapper.selectByEmail(CURRENT_USER_EMAIL.get());
        if (user == null) {
            return ResponseEntity.status(404).body("用户不存在");
        }
        user.setNickName(nickname);
        user.setUpdateTime(new Date());
        // 保持现有 isDel 值
        if (user.getIsDel() == null) {
            user.setIsDel(0);
        }
        int rows = sysUserMapper.update(user);
        if (rows > 0) {
            return ResponseEntity.ok(Map.of("nickname", nickname));
        }
        return ResponseEntity.internalServerError().body("更新失败");
    }

    @Override
    public String getCurrentUserEmail() {
        String email = CURRENT_USER_EMAIL.get();
        if (email == null || email.isBlank()) {
            throw new RuntimeException("当前用户不存在");
        }
        SysUserEntity sysUserEntity = sysUserMapper.selectByEmail(email);
        if (sysUserEntity == null || sysUserEntity.getIsDel() == 1) {
            throw new RuntimeException("当前用户不存在");
        }
        return email;
    }

    @Override
    public Integer bindPhoneNumber(String phoneNumber, String code) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return 0;
        }
        if (code == null || code.isBlank()) {
            return 0;
        }
        String key = "bind:phone:code:" + phoneNumber;
        RBucket<String> bucket = redissonClient.getBucket(key);
        String cachedCode = bucket.get();
        if (cachedCode == null || !cachedCode.equals(code)) {
            return 0;
        }
        bucket.delete();
        // 绑定手机号
        SysUserEntity user = sysUserMapper.selectByEmail(CURRENT_USER_EMAIL.get());
        if (user == null) {
            return 0;
        }
        user.setPhoneNumber(phoneNumber);
        user.setUpdateTime(new Date());
        // 保持现有 isDel 值
        if (user.getIsDel() == null) {
            user.setIsDel(0);
        }
        int rows = sysUserMapper.update(user);
        if (rows > 0) {
            return 1;
        }
        return 0;
    }

    @Override
    public String sendPhoneCode(String phoneNumber) throws JsonProcessingException {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return "";
        }
        // 对手机号码进行验证
        if (!phoneNumber.matches("^1[3-9]\\d{9}$")) {
            return "";
        }
        SysUserEntity user = sysUserMapper.selectByEmail(CURRENT_USER_EMAIL.get());
        if (user == null) {
            return "";
        }
        // 生成验证码
        String code = generateNumericCode(6);
        // 缓存验证码，过期时间为 5 分钟
        RBucket<String> bucket = redissonClient.getBucket("bind:phone:code:" + phoneNumber);
        bucket.set(code, 5, TimeUnit.MINUTES);
        Map<String, String> values = new HashMap<>();
        values.put("msg", "您的手机号绑定验证码为：" + code + "，5分钟内有效");
        StringSubstitutor sub = new StringSubstitutor(values);
        String result = sub.replace(SMSTEMPLATE);
        SmsRequest smsRequest=new SmsRequest(phoneNumber, result);
        String jsonString = objectMapper.writeValueAsString(smsRequest);
        wsSessionHub.send(SMSKEY, jsonString);
        return code;
    }

    @Override
    public Map<String, String> getCurrentUserMsg(String token) {
        if (token == null || token.isBlank()) {
            return new HashMap<>();
        }

        String tokenKey = "auth:token:" + token;
        RBucket<String> bucket = redissonClient.getBucket(tokenKey);
        String email = bucket.get();
        if (email != null) {
            String nickname = "";
            String phone="";
            try {
                SysUserEntity user = sysUserMapper.selectByEmail(email);
                if (user != null && user.getNickName() != null && !user.getNickName().isBlank()) {
                    nickname = user.getNickName();
                    phone=user.getPhoneNumber()==null?"":user.getPhoneNumber();
                }
            } catch (Exception ignored) {
            }
            if(nickname.isEmpty()){
                nickname = "用户";
            }
            return Map.of("account", email, "nickname", nickname,"phone", phone);
        }
        return Map.of();
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

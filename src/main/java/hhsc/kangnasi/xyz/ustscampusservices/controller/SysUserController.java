package hhsc.kangnasi.xyz.ustscampusservices.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import hhsc.kangnasi.xyz.ustscampusservices.service.SysUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/user")
@RestController
public class SysUserController {

    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @GetMapping("/send-login-code")
    public ResponseEntity<String> sendLoginCode(@RequestParam("email") String email) throws JsonProcessingException {
        return sysUserService.sendLoginCode(email);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam("email") String email,
                                   @RequestParam("code") String code) {
        return sysUserService.login(email, code);
    }

    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam("token") String token) {
        return sysUserService.validateToken(token);
    }

    @PostMapping("/update-nickname")
    public ResponseEntity<?> updateNickname(@RequestBody Map<String, String> body) {
        String nickname = body.get("nickname");
        if (nickname == null || nickname.isBlank()) {
            return ResponseEntity.badRequest().body("昵称不能为空");
        }
        return sysUserService.updateNickname(nickname);
    }

    @PostMapping("/bind-phone-number")
    public ResponseEntity<String> bindPhoneNumber(@RequestBody Map<String, String> body) {
        String phoneNumber = body.get("phoneNumber");
        String code = body.get("code");
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body("验证码不能为空");
        }
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return ResponseEntity.badRequest().body("手机号不能为空");
        }
        Integer i = sysUserService.bindPhoneNumber(phoneNumber, code);
        return i==1?ResponseEntity.ok("绑定成功"):ResponseEntity.badRequest().body("绑定失败");
    }

    @GetMapping("/send-phone-code")
    public ResponseEntity<String> sendPhoneCode(@RequestParam("phoneNumber") String phoneNumber) throws JsonProcessingException {
        String s = sysUserService.sendPhoneCode(phoneNumber);
        return ResponseEntity.ok(s);
    }

    @GetMapping("/get-user-msg")
    public ResponseEntity<Map<String, String>> getUserMsg(@RequestParam("token") String token) {
        Map<String, String> map = sysUserService.getCurrentUserMsg(token);
        return ResponseEntity.ok(map);
    }
}

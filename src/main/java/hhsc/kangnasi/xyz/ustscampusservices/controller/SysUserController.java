package hhsc.kangnasi.xyz.ustscampusservices.controller;

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
    public ResponseEntity<String> sendLoginCode(@RequestParam("email") String email) {
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
}

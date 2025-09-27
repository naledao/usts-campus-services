package hhsc.kangnasi.xyz.ustscampusservices.service;

import org.springframework.http.ResponseEntity;

public interface SysUserService {

    ResponseEntity<String> sendLoginCode(String email);

    ResponseEntity<?> login(String email, String code);

    ResponseEntity<?> validateToken(String token);

    ResponseEntity<?> updateNickname(String token, String nickname);
}

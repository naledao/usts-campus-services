package hhsc.kangnasi.xyz.ustscampusservices.service;

import hhsc.kangnasi.xyz.ustscampusservices.domain.vo.CommonServiceVo;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface SysUserService {

    ResponseEntity<String> sendLoginCode(String email);

    ResponseEntity<?> login(String email, String code);

    ResponseEntity<?> validateToken(String token);

    ResponseEntity<?> updateNickname(String nickname);

}

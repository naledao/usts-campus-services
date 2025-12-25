package hhsc.kangnasi.xyz.ustscampusservices.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import hhsc.kangnasi.xyz.ustscampusservices.domain.vo.CommonServiceVo;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface SysUserService {

    ResponseEntity<String> sendLoginCode(String email) throws JsonProcessingException;

    ResponseEntity<?> login(String email, String code);

    ResponseEntity<?> validateToken(String token);

    ResponseEntity<?> updateNickname(String nickname);

    String getCurrentUserEmail();

}

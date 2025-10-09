package hhsc.kangnasi.xyz.ustscampusservices.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

public interface CommonService {
    ResponseEntity<?> allService(String email);

    ResponseEntity<?> startService(String email,String serviceTableName) throws JsonProcessingException;

    ResponseEntity<?> stopService(String email,String serviceTableName);

    ResponseEntity<?> deleteService(String email, String tableName);
}

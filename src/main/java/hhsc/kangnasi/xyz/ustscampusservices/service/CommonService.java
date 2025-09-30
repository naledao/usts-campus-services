package hhsc.kangnasi.xyz.ustscampusservices.service;

import org.springframework.http.ResponseEntity;

public interface CommonService {
    ResponseEntity<?> allService(String email);

    ResponseEntity<?> startService(String email,String serviceTableName);

    ResponseEntity<?> stopService(String email,String serviceTableName);

    ResponseEntity<?> deleteService(String email, String tableName);
}

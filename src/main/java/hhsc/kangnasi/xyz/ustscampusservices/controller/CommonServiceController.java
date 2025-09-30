package hhsc.kangnasi.xyz.ustscampusservices.controller;

import hhsc.kangnasi.xyz.ustscampusservices.service.CommonService;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import static hhsc.kangnasi.xyz.ustscampusservices.config.AuthInterceptor.CURRENT_USER_EMAIL;

@RequestMapping("/common-service")
@RestController
public class CommonServiceController {

    private final CommonService commonService;

    public CommonServiceController(CommonService commonService) {
        this.commonService = commonService;
    }

    @GetMapping
    public ResponseEntity<?> allService() {
        String email = CURRENT_USER_EMAIL.get();
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(401).body("未登录");
        }
        return commonService.allService(email);
    }

    @PostMapping("/start")
    public ResponseEntity<?> startService(@RequestParam("serviceTag")String serviceTag) {
        String email = CURRENT_USER_EMAIL.get();
        if(serviceTag==null || serviceTag.isBlank()) {
            return ResponseEntity.badRequest().body("服务不能为空");
        }
        String tableName = convertServiceTagToTableName(serviceTag);
        if(tableName.isBlank()) {
            return ResponseEntity.badRequest().body("服务不存在");
        }
        return commonService.startService(email,tableName);
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stopService(@RequestParam("serviceTag")String serviceTag) {
        String email = CURRENT_USER_EMAIL.get();
        if(serviceTag==null || serviceTag.isBlank()) {
            return ResponseEntity.badRequest().body("服务不能为空");
        }
        String tableName = convertServiceTagToTableName(serviceTag);
        if(tableName.isBlank()) {
            return ResponseEntity.badRequest().body("服务不存在");
        }
        return commonService.stopService(email,tableName);
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteService(@RequestParam("serviceTag")String serviceTag) {
        String email = CURRENT_USER_EMAIL.get();
        if(serviceTag==null || serviceTag.isBlank()) {
            return ResponseEntity.badRequest().body("服务不能为空");
        }
        String tableName = convertServiceTagToTableName(serviceTag);
        if(tableName.isBlank()) {
            return ResponseEntity.badRequest().body("服务不存在");
        }
        return commonService.deleteService(email,tableName);
    }

    private String convertServiceTagToTableName(String serviceTag) {
        switch (serviceTag.toLowerCase()) {
            case "校园网自动登录":
                return "service_campus_net_login";
            default:
                return "";
        }
    }

}
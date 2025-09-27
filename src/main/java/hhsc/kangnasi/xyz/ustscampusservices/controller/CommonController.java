package hhsc.kangnasi.xyz.ustscampusservices.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static hhsc.kangnasi.xyz.ustscampusservices.config.AuthInterceptor.CURRENT_USER_EMAIL;

@RequestMapping("/common-service")
public class CommonController {


    @GetMapping
    public String allService() {
        String email=CURRENT_USER_EMAIL.get();
    }
}

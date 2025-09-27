package hhsc.kangnasi.xyz.ustscampusservices.controller;

import hhsc.kangnasi.xyz.ustscampusservices.domain.vo.CommonServiceVo;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static hhsc.kangnasi.xyz.ustscampusservices.config.AuthInterceptor.CURRENT_USER_EMAIL;

@RequestMapping("/common-service")
@RestController
public class CommonController {

    // 仅扫描 service.impl 包下、类名以 Service 开头的 @Service Bean
    private final String serviceRootPkg = "hhsc.kangnasi.xyz.ustscampusservices.service";
    private final String implSuffix = ".impl";

    private final ApplicationContext applicationContext;

    public CommonController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @GetMapping
    public ResponseEntity<?> allService() {
        String email = CURRENT_USER_EMAIL.get();
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(401).body("未登录");
        }

        List<CommonServiceVo> result = new ArrayList<>();

        Map<String, Object> serviceBeans = applicationContext.getBeansWithAnnotation(Service.class);
        for (Object bean : serviceBeans.values()) {
            Class<?> targetClass = bean.getClass();

            String pkgName = targetClass.getPackageName();
            String simpleName = targetClass.getSimpleName();

            boolean inImplPackage = pkgName.startsWith(serviceRootPkg) && pkgName.endsWith(implSuffix);
            boolean nameStartsWithService = simpleName.startsWith("Service");
            if (!inImplPackage || !nameStartsWithService) {
                continue;
            }

            try {
                Method m = bean.getClass().getMethod("allService", String.class);
                Object ret = m.invoke(bean, email);
                if (ret instanceof List<?> list) {
                    for (Object o : list) {
                        if (o instanceof CommonServiceVo csv) {
                            result.add(csv);
                        }
                    }
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ResponseEntity.ok(result);
    }
}
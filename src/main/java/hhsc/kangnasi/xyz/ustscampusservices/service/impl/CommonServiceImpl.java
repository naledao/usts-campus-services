package hhsc.kangnasi.xyz.ustscampusservices.service.impl;

import hhsc.kangnasi.xyz.ustscampusservices.domain.vo.CommonServiceVo;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.CommonServiceMapper;
import hhsc.kangnasi.xyz.ustscampusservices.service.CommonService;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CommonServiceImpl implements CommonService {
    // 仅扫描 service.impl 包下、类名以 Service 开头的 @Service Bean
    private final String serviceRootPkg = "hhsc.kangnasi.xyz.ustscampusservices.service";
    private final String implSuffix = ".impl";
    private final ApplicationContext applicationContext;
    private final CommonServiceMapper commonServiceMapper;

    public CommonServiceImpl(ApplicationContext applicationContext, CommonServiceMapper commonServiceMapper) {
        this.applicationContext = applicationContext;
        this.commonServiceMapper = commonServiceMapper;
    }

    @Override
    public ResponseEntity<?> allService(String email) {
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

    @Override
    public ResponseEntity<?> startService(String email, String serviceTableName) {
        int row=commonServiceMapper.startService(email, serviceTableName);
        if(row==0){
            return ResponseEntity.badRequest().body("启动服务失败");
        }
        return ResponseEntity.ok("启动服务成功");
    }

    @Override
    public ResponseEntity<?> stopService(String email, String serviceTableName) {
        int row=commonServiceMapper.stopService(email, serviceTableName);
        if(row==0){
            return ResponseEntity.badRequest().body("停止服务失败");
        }
        return ResponseEntity.ok("停止服务成功");
    }

    @Override
    public ResponseEntity<?> deleteService(String email, String tableName) {
        int row=commonServiceMapper.deleteService(email, tableName);
        if(row==0){
            return ResponseEntity.badRequest().body("删除服务失败");
        }
        return ResponseEntity.ok("删除服务成功");
    }
}

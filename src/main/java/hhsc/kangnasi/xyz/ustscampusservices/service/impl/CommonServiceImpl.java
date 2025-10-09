package hhsc.kangnasi.xyz.ustscampusservices.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceCampusNetLoginEntity;
import hhsc.kangnasi.xyz.ustscampusservices.domain.vo.CommonServiceVo;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.CommonServiceMapper;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.ServiceCampusNetLoginMapper;
import hhsc.kangnasi.xyz.ustscampusservices.service.CommonService;
import hhsc.kangnasi.xyz.ustscampusservices.service.ServiceCampusNetLoginService;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static hhsc.kangnasi.xyz.ustscampusservices.config.AuthInterceptor.CURRENT_USER_EMAIL;

@Service
public class CommonServiceImpl implements CommonService {
    // 仅扫描 service.impl 包下、类名以 Service 开头的 @Service Bean
    private final String serviceRootPkg = "hhsc.kangnasi.xyz.ustscampusservices.service";
    private final String implSuffix = ".impl";
    private final ApplicationContext applicationContext;
    private final CommonServiceMapper commonServiceMapper;
    private final ServiceCampusNetLoginMapper serviceCampusNetLoginMapper;
    private final ServiceCampusNetLoginService serviceCampusNetLoginService;

    public CommonServiceImpl(ApplicationContext applicationContext, CommonServiceMapper commonServiceMapper, ServiceCampusNetLoginMapper serviceCampusNetLoginMapper, ServiceCampusNetLoginService serviceCampusNetLoginService) {
        this.applicationContext = applicationContext;
        this.commonServiceMapper = commonServiceMapper;
        this.serviceCampusNetLoginMapper = serviceCampusNetLoginMapper;
        this.serviceCampusNetLoginService = serviceCampusNetLoginService;
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
    public ResponseEntity<?> startService(String email, String serviceTableName) throws JsonProcessingException {
        commonServiceMapper.startService(email, serviceTableName);
        processService(serviceTableName);
        return ResponseEntity.ok("启动服务成功");
    }

    private void processService(String serviceTableName) throws JsonProcessingException {
        switch (serviceTableName){
            case "service_campus_net_login":
                String email=CURRENT_USER_EMAIL.get();
                ServiceCampusNetLoginEntity serviceCampusNetLoginEntity = serviceCampusNetLoginMapper.selectById(email);
                String[] times = serviceCampusNetLoginEntity.getRefreshTime().split("-");
                serviceCampusNetLoginService.setRunningTime(serviceCampusNetLoginEntity, times[0], times[1]);
                break;
        }
    }

    @Override
    public ResponseEntity<?> stopService(String email, String serviceTableName) {
        int row=commonServiceMapper.stopService(email, serviceTableName);
        if(row==0){
            return ResponseEntity.badRequest().body("停止服务失败");
        }
        return ResponseEntity.ok("停止服务成功");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResponseEntity<?> deleteService(String email, String tableName) {
        int row=commonServiceMapper.deleteService(email, tableName);
        if(row==0){
            return ResponseEntity.badRequest().body("删除服务失败");
        }
        return ResponseEntity.ok("删除服务成功");
    }
}

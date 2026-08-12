package hhsc.kangnasi.xyz.ustscampusservices.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceCampusNetLoginEntity;
import hhsc.kangnasi.xyz.ustscampusservices.domain.vo.CommonServiceVo;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.CommonServiceMapper;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.ServiceCampusNetLoginMapper;
import hhsc.kangnasi.xyz.ustscampusservices.service.CommonService;
import hhsc.kangnasi.xyz.ustscampusservices.service.CommonServiceProvider;
import hhsc.kangnasi.xyz.ustscampusservices.service.ServiceCampusNetLoginService;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static hhsc.kangnasi.xyz.ustscampusservices.config.AuthInterceptor.CURRENT_USER_EMAIL;

@Service
@RegisterReflectionForBinding(CommonServiceVo.class)
public class CommonServiceImpl implements CommonService {
    private final List<CommonServiceProvider> serviceProviders;
    private final CommonServiceMapper commonServiceMapper;
    private final ServiceCampusNetLoginMapper serviceCampusNetLoginMapper;
    private final ServiceCampusNetLoginService serviceCampusNetLoginService;

    public CommonServiceImpl(List<CommonServiceProvider> serviceProviders, CommonServiceMapper commonServiceMapper, ServiceCampusNetLoginMapper serviceCampusNetLoginMapper, ServiceCampusNetLoginService serviceCampusNetLoginService) {
        this.serviceProviders = serviceProviders;
        this.commonServiceMapper = commonServiceMapper;
        this.serviceCampusNetLoginMapper = serviceCampusNetLoginMapper;
        this.serviceCampusNetLoginService = serviceCampusNetLoginService;
    }

    @Override
    public ResponseEntity<?> allService(String email) {
        List<CommonServiceVo> result = new ArrayList<>();
        for (CommonServiceProvider serviceProvider : serviceProviders) {
            result.addAll(serviceProvider.allService(email));
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

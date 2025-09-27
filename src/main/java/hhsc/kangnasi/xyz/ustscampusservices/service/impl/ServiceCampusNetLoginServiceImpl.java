package hhsc.kangnasi.xyz.ustscampusservices.service.impl;

import hhsc.kangnasi.xyz.ustscampusservices.config.AuthInterceptor;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceCampusNetLoginEntity;
import hhsc.kangnasi.xyz.ustscampusservices.domain.vo.CommonServiceVo;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.ServiceCampusNetLoginMapper;
import hhsc.kangnasi.xyz.ustscampusservices.service.ServiceCampusNetLoginService;
import hhsc.kangnasi.xyz.ustscampusservices.util.TimeUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ServiceCampusNetLoginServiceImpl implements ServiceCampusNetLoginService {

    private final ServiceCampusNetLoginMapper mapper;

    public ServiceCampusNetLoginServiceImpl(ServiceCampusNetLoginMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<?> create(ServiceCampusNetLoginEntity body) {
        String email = AuthInterceptor.CURRENT_USER_EMAIL.get();
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(401).body("未登录");
        }

        if (body == null) {
            return ResponseEntity.badRequest().body("请求体不能为空");
        }

        if (body.getNetAccount() == null || body.getNetAccount().isBlank()) {
            return ResponseEntity.badRequest().body("netAccount不能为空");
        }
        if (body.getCarrier() == null || body.getCarrier().isBlank()) {
            return ResponseEntity.badRequest().body("carrier不能为空");
        }
        if (body.getNetPassword() == null || body.getNetPassword().isBlank()) {
            return ResponseEntity.badRequest().body("netPassword不能为空");
        }

        if (mapper.selectById(email) != null) {
            return ResponseEntity.status(409).body("当前校园网账号已存在，无法新增!");
        }

        // 检查 net_account 是否已存在
        if (mapper.selectByNetByEmail(email) > 0) {
            return ResponseEntity.status(409).body("当前账户下已存在关联的校园网账号，无法新增!");
        }

        body.setEmail(email);
        Date now = new Date();
        body.setCreateTime(now);
        body.setUpdateTime(now);
        if (body.getIsDel() == null) {
            body.setIsDel((short) 0);
        }
        body.setRunStatus((short) 0);
        body.setWlanUserMac("000000000000");
        body.setWlanAcIp("221.178.235.146");
        body.setWlanAcName("JSSUZ-MC-CMNET-BRAS-KEDA_ME60X8");
        int rows = mapper.insert(body);
        if (rows > 0) {
            return ResponseEntity.ok(Map.of(
                    "email", body.getEmail(),
                    "netAccount", body.getNetAccount(),
                    "carrier", body.getCarrier()
            ));
        }
        return ResponseEntity.internalServerError().body("创建失败");
    }

    @Override
    public List<CommonServiceVo> allService(String email) {
        ServiceCampusNetLoginEntity serviceCampusNetLoginEntity = mapper.selectById(email);
        if (serviceCampusNetLoginEntity == null) {
            return List.of();
        }
        CommonServiceVo commonServiceVo = new CommonServiceVo();
        commonServiceVo.setTag("校园网自动登录");
        commonServiceVo.setName(serviceCampusNetLoginEntity.getNetAccount());
        commonServiceVo.setRunStatus(serviceCampusNetLoginEntity.getRunStatus() == 1 ? "运行中" : "已停止");
        commonServiceVo.setRunningTime(TimeUtil.formatDiff(serviceCampusNetLoginEntity.getUpdateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), LocalDateTime.now()));
        return List.of(commonServiceVo);
    }
}


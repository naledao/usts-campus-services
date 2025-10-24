package hhsc.kangnasi.xyz.ustscampusservices.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceCampusNetLoginEntity;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceLogEntity;
import hhsc.kangnasi.xyz.ustscampusservices.domain.vo.CommonServiceVo;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.ServiceCampusNetLoginMapper;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.ServiceLogMapper;
import hhsc.kangnasi.xyz.ustscampusservices.mq.DelayedMessageSender;
import hhsc.kangnasi.xyz.ustscampusservices.service.ServiceCampusNetLoginService;
import hhsc.kangnasi.xyz.ustscampusservices.util.TimeUtil;
import hhsc.kangnasi.xyz.ustscampusservices.websocket.WsSessionHub;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static hhsc.kangnasi.xyz.ustscampusservices.config.AuthInterceptor.CURRENT_USER_EMAIL;
import static hhsc.kangnasi.xyz.ustscampusservices.websocket.CampusNetworkAutoLoginWebSocket.campusNetworkAutoLoginKey;

@Service
public class ServiceCampusNetLoginServiceImpl implements ServiceCampusNetLoginService {

    private final ServiceCampusNetLoginMapper mapper;
    private final WsSessionHub wsSessionHub;
    private final ObjectMapper objectMapper;
    private final ServiceLogMapper serviceLogMapper;
    private final DelayedMessageSender delayedMessageSender;


    public ServiceCampusNetLoginServiceImpl(ServiceCampusNetLoginMapper mapper, WsSessionHub wsSessionHub, ObjectMapper objectMapper, ServiceLogMapper serviceLogMapper, DelayedMessageSender delayedMessageSender) {
        this.mapper = mapper;
        this.wsSessionHub = wsSessionHub;
        this.objectMapper = objectMapper;
        this.serviceLogMapper = serviceLogMapper;
        this.delayedMessageSender = delayedMessageSender;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResponseEntity<?> create(ServiceCampusNetLoginEntity body) throws JsonProcessingException {
        String email = CURRENT_USER_EMAIL.get();
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
            return ResponseEntity.status(409).body("当前邮箱下已存在校园网账号，无法新增!");
        }

        // 检查 net_account 是否已存在,删除和不删除的都要取出来
        ServiceCampusNetLoginEntity existing = mapper.selectByNetByEmail(email,0);
        if(existing==null){
            existing= mapper.selectByNetByEmail(email,1);
        }
        if (existing != null && existing.getIsDel()==0) { // 当前仅存的服务没有被删除
            return ResponseEntity.status(409).body("当前邮箱下已存在校园网账号，无法新增!");
        }

        if(existing!=null && existing.getIsDel()==1){
            existing.setIsDel((short) 0);// 当前仅存的服务被删除了
            existing.setNetAccount(body.getNetAccount());
            existing.setCarrier(body.getCarrier());
            existing.setNetPassword(body.getNetPassword());
            existing.setWlanUserIp(body.getWlanUserIp());
            existing.setUpdateTime(new Date());
            mapper.update(existing);
            return ResponseEntity.ok(Map.of(
                    "email", email,
                    "netAccount", body.getNetAccount(),
                    "carrier", body.getCarrier()
            ));
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
        // 添加到延迟队列
        int rows = mapper.insert(body);
        if (rows > 0) {
            ServiceCampusNetLoginEntity serviceCampusNetLoginEntity = mapper.selectById(email);
            setRunningTime(serviceCampusNetLoginEntity,"03","00");
            return ResponseEntity.ok(Map.of(
                    "email", email,
                    "netAccount", body.getNetAccount(),
                    "carrier", body.getCarrier()
            ));
        }
        return ResponseEntity.internalServerError().body("创建失败");
    }

    @Transactional(rollbackFor = Exception.class)
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

    @Override
    public int edit(ServiceCampusNetLoginEntity body) throws JsonProcessingException {
        String email = CURRENT_USER_EMAIL.get();
        ServiceCampusNetLoginEntity serviceCampusNetLoginEntity = mapper.selectById(email);
        if(serviceCampusNetLoginEntity==null){
            throw new RuntimeException("当前邮箱下不存在校园网账号，无法编辑!");
        }
        serviceCampusNetLoginEntity.setNetAccount(body.getNetAccount());
        serviceCampusNetLoginEntity.setCarrier(body.getCarrier());
        serviceCampusNetLoginEntity.setNetPassword(body.getNetPassword());
        serviceCampusNetLoginEntity.setWlanUserIp(body.getWlanUserIp());
        serviceCampusNetLoginEntity.setUpdateTime(new Date());
        int update = mapper.update(serviceCampusNetLoginEntity);
        if(update<=0){
            throw new RuntimeException("编辑失败");
        }else{
            String[] refreshTime = serviceCampusNetLoginEntity.getRefreshTime().split("-");
            setRunningTime(serviceCampusNetLoginEntity,refreshTime[0],refreshTime[1]);
            return 1;
        }
    }

    @Override
    public void connect(ServiceCampusNetLoginEntity serviceCampusNetLoginEntity) throws JsonProcessingException {
        serviceCampusNetLoginEntity.setNetAccount(serviceCampusNetLoginEntity.getNetAccount()+"@"+serviceCampusNetLoginEntity.getCarrier());
        JsonNode node = objectMapper.valueToTree(serviceCampusNetLoginEntity);
        ObjectNode objectNode = (ObjectNode) node;
        objectNode.put("type", "login");
        String json=objectMapper.writeValueAsString(objectNode);
        boolean send = wsSessionHub.send(campusNetworkAutoLoginKey, json);
        if (!send) {
            ServiceLogEntity serviceLog=new ServiceLogEntity();
            serviceLog.setRelationTable("service_campus_net_login");
            serviceLog.setEmail(serviceCampusNetLoginEntity.getEmail());
            serviceLog.setCreateTime(new Date());
            serviceLog.setOperationName("登录");
            serviceLog.setOperationStatus(0);
            serviceLog.setRemarks("登录失败，请联系管理员QQ\n+2419646091");
            serviceLogMapper.insert(serviceLog);
        }
    }

    @Override
    public List<ServiceLogEntity> logs(String email) {
        List<ServiceLogEntity> serviceLogEntities = serviceLogMapper.selectByEmail(email);
        return serviceLogEntities==null?List.of():serviceLogEntities;
    }

    @Override
    public void logout(ServiceCampusNetLoginEntity serviceCampusNetLoginEntity) throws JsonProcessingException {
        serviceCampusNetLoginEntity.setNetAccount(serviceCampusNetLoginEntity.getNetAccount()+"@"+serviceCampusNetLoginEntity.getCarrier());
        JsonNode node = objectMapper.valueToTree(serviceCampusNetLoginEntity);
        ObjectNode objectNode = (ObjectNode) node;
        objectNode.put("type", "logout");
        String json=objectMapper.writeValueAsString(objectNode);
        boolean send = wsSessionHub.send(campusNetworkAutoLoginKey, json);
        if (!send) {
            ServiceLogEntity serviceLog=new ServiceLogEntity();
            serviceLog.setRelationTable("service_campus_net_login");
            serviceLog.setEmail(serviceCampusNetLoginEntity.getEmail());
            serviceLog.setCreateTime(new Date());
            serviceLog.setOperationName("下线");
            serviceLog.setOperationStatus(0);
            serviceLog.setRemarks("下线失败，请联系管理员QQ\n+2419646091");
            serviceLogMapper.insert(serviceLog);
        }
    }

    @Override
    public void setRunningTime(ServiceCampusNetLoginEntity serviceCampusNetLoginEntity, String hour, String minute) throws JsonProcessingException {
        long intervalMillis = TimeUtil.getIntervalMillis(hour, minute);
        serviceCampusNetLoginEntity.setRefreshTime(hour+"-"+minute);
        serviceCampusNetLoginEntity.setUpdateTime(new Date());
        mapper.update(serviceCampusNetLoginEntity);
        String json = toJson(serviceCampusNetLoginEntity);
        delayedMessageSender.send(json, intervalMillis);
    }

    @Override
    public String toJson(ServiceCampusNetLoginEntity serviceCampusNetLoginEntity) throws JsonProcessingException {
        serviceCampusNetLoginEntity.setNetAccount(serviceCampusNetLoginEntity.getNetAccount()+"@"+serviceCampusNetLoginEntity.getCarrier());
        JsonNode node = objectMapper.valueToTree(serviceCampusNetLoginEntity);
        ObjectNode objectNode = (ObjectNode) node;
        objectNode.put("serviceName", "service_campus_net_login");
        objectNode.put("type", "all");
        return objectMapper.writeValueAsString(objectNode);
    }
}


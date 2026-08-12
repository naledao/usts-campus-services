package hhsc.kangnasi.xyz.ustscampusservices.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dianfei.DianFeiServiceGrpc;
import dianfei.Dianfei;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceDormElectricityAlertEntity;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceDormElectricityAlertRoomEntity;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceLogEntity;
import hhsc.kangnasi.xyz.ustscampusservices.domain.vo.CommonServiceVo;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.ServiceDormElectricityAlertMapper;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.ServiceDormElectricityAlertRoomMapper;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.ServiceLogMapper;
import hhsc.kangnasi.xyz.ustscampusservices.service.ServiceDormElectricityAlertService;
import hhsc.kangnasi.xyz.ustscampusservices.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.annotation.RegisterReflection;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static hhsc.kangnasi.xyz.ustscampusservices.config.AuthInterceptor.CURRENT_USER_EMAIL;

@Service
@RegisterReflection(
        classes = {
                Dianfei.QueryRequest.class,
                Dianfei.QueryReply.class
        },
        memberCategories = MemberCategory.INVOKE_PUBLIC_METHODS
)
public class ServiceDormElectricityAlertServiceImpl implements ServiceDormElectricityAlertService {
    private static final Logger log = LoggerFactory.getLogger(ServiceDormElectricityAlertServiceImpl.class);

    private final ServiceDormElectricityAlertRoomMapper serviceDormElectricityAlertRoomMapper;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final ServiceDormElectricityAlertMapper serviceDormElectricityAlertMapper;
    private final ServiceLogMapper serviceLogMapper;
    private final DianFeiServiceGrpc.DianFeiServiceBlockingStub dianFeiService;
    private final long dianFeiTimeoutMs;

    public ServiceDormElectricityAlertServiceImpl(
            ServiceDormElectricityAlertRoomMapper serviceDormElectricityAlertRoomMapper,
            ResourceLoader resourceLoader,
            ObjectMapper objectMapper,
            ServiceDormElectricityAlertMapper serviceDormElectricityAlertMapper,
            ServiceLogMapper serviceLogMapper,
            DianFeiServiceGrpc.DianFeiServiceBlockingStub dianFeiService,
            @Value("${app.dianfei.timeout-ms:5000}") long dianFeiTimeoutMs
    ) {
        this.serviceDormElectricityAlertRoomMapper = serviceDormElectricityAlertRoomMapper;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
        this.serviceDormElectricityAlertMapper = serviceDormElectricityAlertMapper;
        this.serviceLogMapper = serviceLogMapper;
        this.dianFeiService = dianFeiService;
        this.dianFeiTimeoutMs = dianFeiTimeoutMs;
    }

    @Override
    public void addRooms() throws IOException {
        List<ServiceDormElectricityAlertRoomEntity> roomEntities = new ArrayList<>();
        Resource resource = resourceLoader.getResource("classpath:rooms_all.json");
        try (InputStream inputStream = resource.getInputStream()) {
            JsonNode allRoomNode = objectMapper.readTree(inputStream);
            for (JsonNode roomNode : allRoomNode) {
                ServiceDormElectricityAlertRoomEntity roomEntity = new ServiceDormElectricityAlertRoomEntity();
                roomEntity.setFeeitemid(roomNode.get("feeitemid").asInt());
                roomEntity.setType(roomNode.get("type").asText());
                roomEntity.setLevel(roomNode.get("level").asInt());
                roomEntity.setCampus(roomNode.get("campus").asText());
                roomEntity.setBuilding(roomNode.get("building").asInt());
                roomEntity.setRoom(roomNode.get("room").asText());
                roomEntity.setRoomName(roomNode.get("name").asText());
                roomEntities.add(roomEntity);
            }
        }
        serviceDormElectricityAlertRoomMapper.insertBatch(roomEntities);
    }

    @Override
    public List<Map<String,String>> getRoom(String campusId, Integer buildingId) {
        List<ServiceDormElectricityAlertRoomEntity> roomEntities = serviceDormElectricityAlertRoomMapper.selectByCampusAndBuilding(campusId, buildingId);
        if(roomEntities == null || roomEntities.isEmpty()) {
            return List.of();
        }
        List<Map<String,String>> result = new ArrayList<>();
        for(ServiceDormElectricityAlertRoomEntity roomEntity : roomEntities) {
            Map<String,String> map = new HashMap<>();
            map.put("roomId",roomEntity.getRoom());
            map.put("roomName",roomEntity.getRoomName());
            result.add(map);
        }
        return result;
    }

    @Override
    public void bindRoom(String campus,String campusName, String building, String buildingName,String room, String roomName) {
        ServiceDormElectricityAlertRoomEntity serviceDormElectricityAlertRoomEntity = serviceDormElectricityAlertRoomMapper.selectByCampusAndBuildingAndRoom(campus, Integer.parseInt(building), room);
        if(serviceDormElectricityAlertRoomEntity==null){
            throw new RuntimeException("不存在该房间信息");
        }
        // 判断当前账号下面是否有其他房间绑定
        ServiceDormElectricityAlertEntity serviceDormElectricityAlertEntity = serviceDormElectricityAlertMapper.selectByEmail(CURRENT_USER_EMAIL.get(),null);
        if(serviceDormElectricityAlertEntity!=null && serviceDormElectricityAlertEntity.getIsDel()==0){
            throw new RuntimeException("当前账号下面已经绑定了其他房间");
        }
        else if(serviceDormElectricityAlertEntity!=null && serviceDormElectricityAlertEntity.getIsDel()==1){
            serviceDormElectricityAlertEntity.setCampus(campus);
            serviceDormElectricityAlertEntity.setCampusName(campusName);
            serviceDormElectricityAlertEntity.setBuilding(building);
            serviceDormElectricityAlertEntity.setBuildingName(buildingName);
            serviceDormElectricityAlertEntity.setRoom(room);
            serviceDormElectricityAlertEntity.setRoomName(roomName);
            serviceDormElectricityAlertEntity.setIsDel(0);
            serviceDormElectricityAlertEntity.setRunStatus(1);
            updateById(serviceDormElectricityAlertEntity);
        }else{
            ServiceDormElectricityAlertEntity defaultNew = createDefaultNew();
            String email = CURRENT_USER_EMAIL.get();
            defaultNew.setEmail(email);
            defaultNew.setCampus(campus);
            defaultNew.setCampusName(campusName);
            defaultNew.setBuilding(building);
            defaultNew.setBuildingName(buildingName);
            defaultNew.setRoom(room);
            defaultNew.setRoomName(roomName);
            serviceDormElectricityAlertMapper.insert(defaultNew);
        }
    }

    @Override
    public ServiceDormElectricityAlertEntity createDefaultNew() {
        ServiceDormElectricityAlertEntity serviceDormElectricityAlertEntity=new ServiceDormElectricityAlertEntity();
        serviceDormElectricityAlertEntity.setFeeItemId("409");
        serviceDormElectricityAlertEntity.setType("IEC");
        serviceDormElectricityAlertEntity.setLevel("3");
        serviceDormElectricityAlertEntity.setRunStatus(1);
        serviceDormElectricityAlertEntity.setThreshold(20D);
        serviceDormElectricityAlertEntity.setIsDel(0);
        serviceDormElectricityAlertEntity.setCreateTime(LocalDateTime.now());
        serviceDormElectricityAlertEntity.setUpdateTime(LocalDateTime.now());
        return serviceDormElectricityAlertEntity;
    }

    @Override
    public void updateById(ServiceDormElectricityAlertEntity serviceDormElectricityAlertEntity) {
        serviceDormElectricityAlertEntity.setUpdateTime(LocalDateTime.now());
        serviceDormElectricityAlertMapper.updateById(serviceDormElectricityAlertEntity);
    }

    @Override
    public List<CommonServiceVo> allService(String email) {
        ServiceDormElectricityAlertEntity serviceDormElectricityAlertEntity = serviceDormElectricityAlertMapper.selectByEmail(email, 0);
        if (serviceDormElectricityAlertEntity == null) {
            return List.of();
        }
        CommonServiceVo commonServiceVo = new CommonServiceVo();
        commonServiceVo.setTag("宿舍电量预警");
        commonServiceVo.setName(serviceDormElectricityAlertEntity.getRoomName());
        commonServiceVo.setRunStatus(serviceDormElectricityAlertEntity.getRunStatus() == 1 ? "运行中" : "已停止");
        commonServiceVo.setRunningTime(TimeUtil.formatDiff(serviceDormElectricityAlertEntity.getUpdateTime().atZone(ZoneId.systemDefault()).toLocalDateTime(), LocalDateTime.now()));
        Double electricity = queryCurrentElectricity(email);
        if(electricity==null){
            electricity=-1d;
        }
        commonServiceVo.setName(commonServiceVo.getName()+"（"+electricity+"度）");
        return List.of(commonServiceVo);
    }

    @Override
    public void updateRoom(String campus,String campusName ,String building,String buildingName, String room, String roomName) {
        String email=CURRENT_USER_EMAIL.get();
        ServiceDormElectricityAlertEntity serviceDormElectricityAlertEntity = serviceDormElectricityAlertMapper.selectByEmail(email, 0);
        if(serviceDormElectricityAlertEntity==null){
            throw new RuntimeException("无法修改");
        }
        serviceDormElectricityAlertEntity.setCampus(campus);
        serviceDormElectricityAlertEntity.setCampusName(campusName);
        serviceDormElectricityAlertEntity.setBuilding(building);
        serviceDormElectricityAlertEntity.setBuildingName(buildingName);
        serviceDormElectricityAlertEntity.setRoom(room);
        serviceDormElectricityAlertEntity.setRoomName(roomName);
        updateById(serviceDormElectricityAlertEntity);
    }

    @Override
    public ServiceDormElectricityAlertEntity viewRoom(String email) {
        return serviceDormElectricityAlertMapper.selectByEmail(email, 0);
    }

    @Override
    public Double queryCurrentElectricity(String email)  {
        ServiceLogEntity serviceLog=new ServiceLogEntity();
        serviceLog.setRelationTable("service_dorm_electricity_alert");
        serviceLog.setEmail(email);
        serviceLog.setCreateTime(new Date());
        serviceLog.setOperationName("查询宿舍当前电量");
        try {
            ServiceDormElectricityAlertEntity serviceDormElectricityAlertEntity = serviceDormElectricityAlertMapper.selectByEmail(email,0);
            if(serviceDormElectricityAlertEntity==null){
                return -1d;
            }
            Dianfei.QueryRequest req = Dianfei.QueryRequest.newBuilder()
                    .setCampus(serviceDormElectricityAlertEntity.getCampus())
                    .setBuilding(serviceDormElectricityAlertEntity.getBuilding())
                    .setRoom(serviceDormElectricityAlertEntity.getRoom())
                    .setFeeitemid(serviceDormElectricityAlertEntity.getFeeItemId())
                    .setType(serviceDormElectricityAlertEntity.getType())
                    .setLevel(serviceDormElectricityAlertEntity.getLevel())
                    .build();
            Dianfei.QueryReply reply = dianFeiService
                    .withDeadlineAfter(dianFeiTimeoutMs, TimeUnit.MILLISECONDS)
                    .queryCurrentElectricity(req);
            serviceLog.setOperationStatus(1);
            serviceLog.setRemarks("操作成功，当前电量为"+reply.getValue()+"度");
            serviceLogMapper.insert(serviceLog);
            return reply.getValue();
        }catch (Exception e){
            log.error("获取宿舍电量失败，email={}", email, e);
            serviceLog.setOperationStatus(0);
            serviceLog.setRemarks("获取电量失败\n" + e);
            serviceLogMapper.insert(serviceLog);
            return -1d;
        }
    }

    @Override
    public List<ServiceLogEntity> logs(String email) {
        return serviceLogMapper.selectByEmail(email, "service_dorm_electricity_alert");
    }
}

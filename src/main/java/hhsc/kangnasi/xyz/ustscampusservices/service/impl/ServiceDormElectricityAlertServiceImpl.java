package hhsc.kangnasi.xyz.ustscampusservices.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import hhsc.kangnasi.xyz.ustscampusservices.domain.dto.RoomChargeDTO;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceDormElectricityAlertEntity;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceDormElectricityAlertRoomEntity;
import hhsc.kangnasi.xyz.ustscampusservices.domain.vo.CommonServiceVo;
import hhsc.kangnasi.xyz.ustscampusservices.dubbo.api.DianFeiService;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.ServiceDormElectricityAlertMapper;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.ServiceDormElectricityAlertRoomMapper;
import hhsc.kangnasi.xyz.ustscampusservices.service.ServiceDormElectricityAlertService;
import hhsc.kangnasi.xyz.ustscampusservices.util.TimeUtil;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static hhsc.kangnasi.xyz.ustscampusservices.config.AuthInterceptor.CURRENT_USER_EMAIL;

@Service
public class ServiceDormElectricityAlertServiceImpl implements ServiceDormElectricityAlertService {
    private final ServiceDormElectricityAlertRoomMapper serviceDormElectricityAlertRoomMapper;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final ServiceDormElectricityAlertMapper serviceDormElectricityAlertMapper;

    @DubboReference(
            interfaceClass = DianFeiService.class,
            protocol = "tri",
            url = "${dubbo.reference.dianfei-service.url}", // 与你原来的 URL 一致
            timeout = 5000
    )
    private DianFeiService dianFeiService;

    public ServiceDormElectricityAlertServiceImpl(ServiceDormElectricityAlertRoomMapper serviceDormElectricityAlertRoomMapper, ResourceLoader resourceLoader, ObjectMapper objectMapper, ServiceDormElectricityAlertMapper serviceDormElectricityAlertMapper) {
        this.serviceDormElectricityAlertRoomMapper = serviceDormElectricityAlertRoomMapper;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
        this.serviceDormElectricityAlertMapper = serviceDormElectricityAlertMapper;
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
        commonServiceVo.setTag("宿舍电费预警");
        commonServiceVo.setName(serviceDormElectricityAlertEntity.getRoomName());
        commonServiceVo.setRunStatus(serviceDormElectricityAlertEntity.getRunStatus() == 1 ? "运行中" : "已停止");
        commonServiceVo.setRunningTime(TimeUtil.formatDiff(serviceDormElectricityAlertEntity.getUpdateTime().atZone(ZoneId.systemDefault()).toLocalDateTime(), LocalDateTime.now()));
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
        ServiceDormElectricityAlertEntity serviceDormElectricityAlertEntity = serviceDormElectricityAlertMapper.selectByEmail(email,0);
        if(serviceDormElectricityAlertEntity==null){
            return -1d;
        }
        RoomChargeDTO roomChargeDTO=new RoomChargeDTO();
        roomChargeDTO.setLevel(serviceDormElectricityAlertEntity.getLevel());
        roomChargeDTO.setCampus(serviceDormElectricityAlertEntity.getCampus());
        roomChargeDTO.setFeeitemid(serviceDormElectricityAlertEntity.getFeeItemId());
        roomChargeDTO.setType(serviceDormElectricityAlertEntity.getType());
        roomChargeDTO.setBuilding(serviceDormElectricityAlertEntity.getBuilding());
        roomChargeDTO.setRoom(serviceDormElectricityAlertEntity.getRoom());
        Gson gson=new Gson();
        String payload = gson.toJson(roomChargeDTO);
        String responseJson = dianFeiService.query_current_electricity(payload);
        System.out.println(responseJson);
        return 1.0;
    }
}

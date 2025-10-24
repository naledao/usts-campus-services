package hhsc.kangnasi.xyz.ustscampusservices.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceDormElectricityAlertRoomEntity;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.ServiceDormElectricityAlertRoomMapper;
import hhsc.kangnasi.xyz.ustscampusservices.service.ServiceDormElectricityAlertRoomService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServiceDormElectricityAlertRoomServiceImpl implements ServiceDormElectricityAlertRoomService {
    private final ServiceDormElectricityAlertRoomMapper serviceDormElectricityAlertRoomMapper;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    public ServiceDormElectricityAlertRoomServiceImpl(ServiceDormElectricityAlertRoomMapper serviceDormElectricityAlertRoomMapper, ResourceLoader resourceLoader, ObjectMapper objectMapper) {
        this.serviceDormElectricityAlertRoomMapper = serviceDormElectricityAlertRoomMapper;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
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
    public void bindRoom(String campus, String building, String room) {
        ServiceDormElectricityAlertRoomEntity serviceDormElectricityAlertRoomEntity = serviceDormElectricityAlertRoomMapper.selectByCampusAndBuildingAndRoom(campus, Integer.parseInt(building), room);
        if(serviceDormElectricityAlertRoomEntity==null){
            throw new RuntimeException("不存在该房间信息");
        }
        System.out.println(campus + " " + building + " " + room);
    }
}

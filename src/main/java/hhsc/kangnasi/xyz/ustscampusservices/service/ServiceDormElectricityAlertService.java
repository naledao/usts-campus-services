package hhsc.kangnasi.xyz.ustscampusservices.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceDormElectricityAlertEntity;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceLogEntity;
import hhsc.kangnasi.xyz.ustscampusservices.domain.vo.CommonServiceVo;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ServiceDormElectricityAlertService extends CommonServiceProvider {
    void addRooms() throws IOException;

    List<Map<String,String>> getRoom(String campusId, Integer buildingId);

    void bindRoom(String campus,String campusName, String building,String buildingName,String room, String roomName);

    ServiceDormElectricityAlertEntity createDefaultNew();

    void updateById(ServiceDormElectricityAlertEntity serviceDormElectricityAlertEntity);

    @Override
    List<CommonServiceVo> allService(String email);

    void updateRoom(String campus,String campusName, String building, String buildingName,String room, String roomName);

    ServiceDormElectricityAlertEntity viewRoom(String email);

    Double queryCurrentElectricity(String email) throws JsonProcessingException;

    List<ServiceLogEntity> logs(String email);
}

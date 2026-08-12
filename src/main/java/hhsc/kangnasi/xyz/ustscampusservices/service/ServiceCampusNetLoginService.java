package hhsc.kangnasi.xyz.ustscampusservices.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceCampusNetLoginEntity;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceLogEntity;
import hhsc.kangnasi.xyz.ustscampusservices.domain.vo.CommonServiceVo;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

public interface ServiceCampusNetLoginService extends CommonServiceProvider {

    ResponseEntity<?> create(ServiceCampusNetLoginEntity body) throws JsonProcessingException;

    @Override
    List<CommonServiceVo> allService(String email);

    int edit(ServiceCampusNetLoginEntity body) throws JsonProcessingException;

    void connect(ServiceCampusNetLoginEntity serviceCampusNetLoginEntity) throws JsonProcessingException;

    List<ServiceLogEntity> logs(String email);

    void logout(ServiceCampusNetLoginEntity serviceCampusNetLoginEntity) throws JsonProcessingException;

    void setRunningTime(ServiceCampusNetLoginEntity serviceCampusNetLoginEntity, String hour, String minute) throws JsonProcessingException;

    void scheduleNextRun(ServiceCampusNetLoginEntity serviceCampusNetLoginEntity) throws JsonProcessingException;

    void scheduleNextRunAfter(ServiceCampusNetLoginEntity serviceCampusNetLoginEntity, Instant completedScheduledAt) throws JsonProcessingException;

    String toJson(ServiceCampusNetLoginEntity serviceCampusNetLoginEntity) throws JsonProcessingException;
}

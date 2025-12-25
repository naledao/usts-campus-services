package hhsc.kangnasi.xyz.ustscampusservices.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import hhsc.kangnasi.xyz.ustscampusservices.annotation.APIRateLimiting;
import hhsc.kangnasi.xyz.ustscampusservices.annotation.ProfileApiGate;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceCampusNetLoginEntity;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceDormElectricityAlertEntity;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceLogEntity;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.ServiceDormElectricityAlertMapper;
import hhsc.kangnasi.xyz.ustscampusservices.service.ServiceDormElectricityAlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static hhsc.kangnasi.xyz.ustscampusservices.config.AuthInterceptor.CURRENT_USER_EMAIL;

@RestController
@RequestMapping("/service-dorm-electricity-alert")
public class ServiceDormElectricityAlertController {

    private final ServiceDormElectricityAlertService serviceDormElectricityAlertService;
    private final ServiceDormElectricityAlertMapper serviceDormElectricityAlertMapper;



    public ServiceDormElectricityAlertController(ServiceDormElectricityAlertService serviceDormElectricityAlertService, ServiceDormElectricityAlertMapper serviceDormElectricityAlertMapper) {
        this.serviceDormElectricityAlertService = serviceDormElectricityAlertService;
        this.serviceDormElectricityAlertMapper = serviceDormElectricityAlertMapper;
    }

    @ProfileApiGate("local")
    @GetMapping("/init-rooms")
    public void initRooms() throws IOException {
        serviceDormElectricityAlertService.addRooms();
    }


    @GetMapping("/room/{campusId}/{buildingId}")
    public ResponseEntity<List<Map<String, String>>> getRooms(@PathVariable("campusId") String campusId, @PathVariable("buildingId") Integer buildingId) {
        if(campusId == null || buildingId == null) {
            return ResponseEntity.badRequest().body(List.of());
        }
        return ResponseEntity.ok(serviceDormElectricityAlertService.getRoom(campusId, buildingId));
    }

    @PostMapping("/bind-room")
    public ResponseEntity<String> bindRoom(@RequestBody Map<String, String> body) {
        if(body==null || body.isEmpty()) {
            return ResponseEntity.badRequest().body("");
        }
        String campus = body.get("campus");
        String campusName = body.get("campusName");
        String building = body.get("building");
        String buildingName = body.get("buildingName");
        String room = body.get("room");
        String roomName = body.get("roomName");
        if(campus==null || building==null || room == null || roomName == null || campusName==null || buildingName==null) {
            return ResponseEntity.badRequest().body("");
        }
        serviceDormElectricityAlertService.bindRoom(campus,campusName,building,buildingName,room,roomName);
        return ResponseEntity.ok("绑定成功");
    }

    @PostMapping("/update-room")
    public ResponseEntity<String> updateRoom(@RequestBody Map<String, String> body) {
        if(body==null || body.isEmpty()) {
            return ResponseEntity.badRequest().body("");
        }
        String campus = body.get("campus");
        String campusName = body.get("campusName");
        String building = body.get("building");
        String buildingName = body.get("buildingName");
        String room = body.get("room");
        String roomName = body.get("roomName");
        if(campus==null || building==null || room == null || roomName == null || campusName==null || buildingName==null) {
            return ResponseEntity.badRequest().body("");
        }
        serviceDormElectricityAlertService.updateRoom(campus,campusName,building,buildingName,room,roomName);
        return ResponseEntity.ok("修改成功");
    }

    @GetMapping("/view-room")
    public ResponseEntity<ServiceDormElectricityAlertEntity> viewRoom(){
        String email=CURRENT_USER_EMAIL.get();
        return ResponseEntity.ok(serviceDormElectricityAlertService.viewRoom(email));
    }


    @APIRateLimiting(
            rate = 23,
            interval = 1,
            intervalUnit = TimeUnit.DAYS,
            permits = 1,
            mode = APIRateLimiting.Mode.PER_KEY,
            key = "'api:service-dorm-electricity-alert:current-electricity:by-email:' + @sysUserServiceImpl.getCurrentUserEmail()"
    )
    @GetMapping("/current-electricity")
    public ResponseEntity<Double> queryCurrentElectricity() throws JsonProcessingException {
        String email=CURRENT_USER_EMAIL.get();
        return ResponseEntity.ok(serviceDormElectricityAlertService.queryCurrentElectricity(email));
    }

    @GetMapping("/view")
    public ResponseEntity<?> view() {
        String email= CURRENT_USER_EMAIL.get();
        ServiceDormElectricityAlertEntity serviceDormElectricityAlertEntity = serviceDormElectricityAlertMapper.selectByEmail(email, 0);
        if(serviceDormElectricityAlertEntity==null){
            throw new RuntimeException("请先创建服务");
        }
        return ResponseEntity.ok(serviceDormElectricityAlertEntity);
    }

    @GetMapping("/logs")
    public ResponseEntity<List<ServiceLogEntity>> logs()  {
        String email= CURRENT_USER_EMAIL.get();
        return ResponseEntity.ok(serviceDormElectricityAlertService.logs(email));
    }

    @PostMapping("/set-threshold/{threshold}")
    public ResponseEntity<String> setThreshold(@PathVariable("threshold") Double threshold) {
        if(threshold==null) {
            return ResponseEntity.badRequest().body("");
        }
        String email= CURRENT_USER_EMAIL.get();
        ServiceDormElectricityAlertEntity serviceDormElectricityAlertEntity = serviceDormElectricityAlertMapper.selectByEmail(email, 0);
        if(serviceDormElectricityAlertEntity==null){
            throw new RuntimeException("请先创建服务");
        }
        serviceDormElectricityAlertEntity.setThreshold(threshold);
        serviceDormElectricityAlertMapper.updateById(serviceDormElectricityAlertEntity);
        return ResponseEntity.ok("设置成功");
    }
}

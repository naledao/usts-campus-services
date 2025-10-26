package hhsc.kangnasi.xyz.ustscampusservices.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import hhsc.kangnasi.xyz.ustscampusservices.annotation.ProfileApiGate;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceDormElectricityAlertEntity;
import hhsc.kangnasi.xyz.ustscampusservices.service.ServiceDormElectricityAlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.util.List;
import java.util.Map;

import static hhsc.kangnasi.xyz.ustscampusservices.config.AuthInterceptor.CURRENT_USER_EMAIL;

@RestController
@RequestMapping("/service-dorm-electricity-alert")
public class ServiceDormElectricityAlertController {

    private final ServiceDormElectricityAlertService serviceDormElectricityAlertRoomService;



    public ServiceDormElectricityAlertController(ServiceDormElectricityAlertService serviceDormElectricityAlertRoomService) {
        this.serviceDormElectricityAlertRoomService = serviceDormElectricityAlertRoomService;
    }

    @ProfileApiGate("local")
    @GetMapping("/init-rooms")
    public void initRooms() throws IOException {
        serviceDormElectricityAlertRoomService.addRooms();
    }


    @GetMapping("/room/{campusId}/{buildingId}")
    public ResponseEntity<List<Map<String, String>>> getRooms(@PathVariable("campusId") String campusId, @PathVariable("buildingId") Integer buildingId) {
        if(campusId == null || buildingId == null) {
            return ResponseEntity.badRequest().body(List.of());
        }
        return ResponseEntity.ok(serviceDormElectricityAlertRoomService.getRoom(campusId, buildingId));
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
        serviceDormElectricityAlertRoomService.bindRoom(campus,campusName,building,buildingName,room,roomName);
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
        serviceDormElectricityAlertRoomService.updateRoom(campus,campusName,building,buildingName,room,roomName);
        return ResponseEntity.ok("修改成功");
    }

    @GetMapping("/view-room")
    public ResponseEntity<ServiceDormElectricityAlertEntity> viewRoom(){
        String email=CURRENT_USER_EMAIL.get();
        return ResponseEntity.ok(serviceDormElectricityAlertRoomService.viewRoom(email));
    }

    @GetMapping("/charge")
    public ResponseEntity<Double> charge() throws JsonProcessingException {
        String email=CURRENT_USER_EMAIL.get();
        serviceDormElectricityAlertRoomService.getCharge(email);
        return ResponseEntity.ok(1.0);
    }
}

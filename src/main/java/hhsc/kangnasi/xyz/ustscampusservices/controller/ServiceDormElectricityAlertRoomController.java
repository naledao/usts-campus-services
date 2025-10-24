package hhsc.kangnasi.xyz.ustscampusservices.controller;

import hhsc.kangnasi.xyz.ustscampusservices.annotation.ProfileApiGate;
import hhsc.kangnasi.xyz.ustscampusservices.service.ServiceDormElectricityAlertRoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/service-dorm-electricity-alert-room")
public class ServiceDormElectricityAlertRoomController {

    private final ServiceDormElectricityAlertRoomService serviceDormElectricityAlertRoomService;



    public ServiceDormElectricityAlertRoomController(ServiceDormElectricityAlertRoomService serviceDormElectricityAlertRoomService) {
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
        String building = body.get("building");
        String room = body.get("room");
        if(campus==null || building==null || room == null) {
            return ResponseEntity.badRequest().body("");
        }
        serviceDormElectricityAlertRoomService.bindRoom(campus,building,room);
        return ResponseEntity.ok("绑定成功");
    }

}

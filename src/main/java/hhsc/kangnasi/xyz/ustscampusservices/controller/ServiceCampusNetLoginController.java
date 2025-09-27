package hhsc.kangnasi.xyz.ustscampusservices.controller;

import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceCampusNetLoginEntity;
import hhsc.kangnasi.xyz.ustscampusservices.service.ServiceCampusNetLoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/service-campus-net-login")
@RestController
public class ServiceCampusNetLoginController {

    private final ServiceCampusNetLoginService serviceCampusNetLoginService;

    public ServiceCampusNetLoginController(ServiceCampusNetLoginService serviceCampusNetLoginService) {
        this.serviceCampusNetLoginService = serviceCampusNetLoginService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody ServiceCampusNetLoginEntity serviceCampusNetLoginEntity) {
        return serviceCampusNetLoginService.create(serviceCampusNetLoginEntity);
    }
}

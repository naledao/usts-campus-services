package hhsc.kangnasi.xyz.ustscampusservices.controller;

import hhsc.kangnasi.xyz.ustscampusservices.domain.vo.AppUpdateInfo;
import hhsc.kangnasi.xyz.ustscampusservices.service.AppUpdateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app-update")
public class AppUpdateController {

    private final AppUpdateService appUpdateService;

    public AppUpdateController(AppUpdateService appUpdateService) {
        this.appUpdateService = appUpdateService;
    }

    @GetMapping("/android/latest")
    public ResponseEntity<AppUpdateInfo> androidLatest(
            @RequestParam(value = "currentVersionCode", defaultValue = "0") int currentVersionCode) {
        return ResponseEntity.ok(appUpdateService.getAndroidLatest(currentVersionCode));
    }
}

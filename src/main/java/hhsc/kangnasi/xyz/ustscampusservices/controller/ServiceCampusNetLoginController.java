package hhsc.kangnasi.xyz.ustscampusservices.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceCampusNetLoginEntity;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.ServiceCampusNetLoginMapper;
import hhsc.kangnasi.xyz.ustscampusservices.service.ServiceCampusNetLoginService;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RedissonClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static hhsc.kangnasi.xyz.ustscampusservices.config.AuthInterceptor.CURRENT_USER_EMAIL;

@RequestMapping("/service-campus-net-login")
@RestController
public class ServiceCampusNetLoginController {

    private final ServiceCampusNetLoginService serviceCampusNetLoginService;
    private final ServiceCampusNetLoginMapper serviceCampusNetLoginMapper;
    private final RedissonClient redissonClient;

    public ServiceCampusNetLoginController(ServiceCampusNetLoginService serviceCampusNetLoginService, ServiceCampusNetLoginMapper serviceCampusNetLoginMapper, RedissonClient redissonClient) {
        this.serviceCampusNetLoginService = serviceCampusNetLoginService;
        this.serviceCampusNetLoginMapper = serviceCampusNetLoginMapper;
        this.redissonClient = redissonClient;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody ServiceCampusNetLoginEntity serviceCampusNetLoginEntity) throws JsonProcessingException {
        return serviceCampusNetLoginService.create(serviceCampusNetLoginEntity);
    }

    @PostMapping("/edit")
    public ResponseEntity<?> edit(@RequestBody ServiceCampusNetLoginEntity serviceCampusNetLoginEntity) throws JsonProcessingException {
        if(serviceCampusNetLoginEntity==null){
            throw new RuntimeException("服务信息为空");
        }
        int edit = serviceCampusNetLoginService.edit(serviceCampusNetLoginEntity);
        if(edit==0){
            throw new RuntimeException("编辑失败");
        }
        return ResponseEntity.ok("编辑成功");
    }

    @GetMapping("/view")
    public ResponseEntity<?> view() {
        String email= CURRENT_USER_EMAIL.get();
        ServiceCampusNetLoginEntity serviceCampusNetLoginEntity = serviceCampusNetLoginMapper.selectById(email);
        if(serviceCampusNetLoginEntity==null){
            throw new RuntimeException("请先创建服务");
        }
        return ResponseEntity.ok(serviceCampusNetLoginEntity);
    }

    @GetMapping("/connect")
    public ResponseEntity<?> connect() throws JsonProcessingException {
        String email= CURRENT_USER_EMAIL.get();
        ServiceCampusNetLoginEntity serviceCampusNetLoginEntity = serviceCampusNetLoginMapper.selectById(email);
        if(serviceCampusNetLoginEntity==null){
            throw new RuntimeException("请先创建服务");
        }
        serviceCampusNetLoginService.connect(serviceCampusNetLoginEntity);
        return ResponseEntity.ok("连接操作已执行，请查看日志结果。");
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout() throws JsonProcessingException {
        String email= CURRENT_USER_EMAIL.get();
        ServiceCampusNetLoginEntity serviceCampusNetLoginEntity = serviceCampusNetLoginMapper.selectById(email);
        if(serviceCampusNetLoginEntity==null){
            throw new RuntimeException("请先创建服务");
        }
        serviceCampusNetLoginService.logout(serviceCampusNetLoginEntity);
        return ResponseEntity.ok("下线操作已执行，请查看日志结果。");
    }

    @GetMapping("/logs")
    public ResponseEntity<?> logs()  {
        String email= CURRENT_USER_EMAIL.get();
        return ResponseEntity.ok(serviceCampusNetLoginService.logs(email));
    }

    @GetMapping("/set-running-time")
    public ResponseEntity<?> setRunningTime(@RequestParam("hour") String hour,@RequestParam("minute") String minute) throws JsonProcessingException {
        String email= CURRENT_USER_EMAIL.get();
        ServiceCampusNetLoginEntity serviceCampusNetLoginEntity = serviceCampusNetLoginMapper.selectById(email);
        if(serviceCampusNetLoginEntity==null){
            throw new RuntimeException("请先创建服务");
        }
        if(serviceCampusNetLoginEntity.getIsDel()==1){
            throw new RuntimeException("服务已删除");
        }
        if(serviceCampusNetLoginEntity.getRunStatus()==0){
            throw new RuntimeException("服务未启动");
        }
        if(hour==null || minute==null || hour.isEmpty() || minute.isEmpty()){
            throw new RuntimeException("请输入正确的时间");
        }
        if(Integer.parseInt(hour)<0 || Integer.parseInt(hour)>23){
            throw new RuntimeException("请输入正确的时间");
        }
        if(Integer.parseInt(minute)<0 || Integer.parseInt(minute)>59){
            throw new RuntimeException("请输入正确的时间");
        }
        serviceCampusNetLoginService.setRunningTime(serviceCampusNetLoginEntity,hour,minute);
        return ResponseEntity.ok("设置成功");
    }
}

package hhsc.kangnasi.xyz.ustscampusservices.schedule_task;

import com.fasterxml.jackson.core.JsonProcessingException;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceDormElectricityAlertEntity;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceLogEntity;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.ServiceDormElectricityAlertMapper;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.ServiceLogMapper;
import hhsc.kangnasi.xyz.ustscampusservices.service.ServiceDormElectricityAlertService;
import hhsc.kangnasi.xyz.ustscampusservices.util.EmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class QueryPowerTask implements SchedulingConfigurer {

    private final TaskScheduler queryPowerTaskScheduler;
    private final ServiceDormElectricityAlertMapper serviceDormElectricityAlertMapper;
    private final ServiceDormElectricityAlertService serviceDormElectricityAlertService;
    private final  EmailUtil emailUtil;
    private final ServiceLogMapper serviceLogMapper;

    public QueryPowerTask(TaskScheduler queryPowerTaskScheduler, ServiceDormElectricityAlertService serviceDormElectricityAlertService, ServiceDormElectricityAlertMapper serviceDormElectricityAlertMapper, EmailUtil emailUtil, ServiceLogMapper serviceLogMapper) {
        this.queryPowerTaskScheduler = queryPowerTaskScheduler;
        this.serviceDormElectricityAlertMapper = serviceDormElectricityAlertMapper;
        this.serviceDormElectricityAlertService = serviceDormElectricityAlertService;
        this.emailUtil = emailUtil;
        this.serviceLogMapper = serviceLogMapper;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(queryPowerTaskScheduler);
        taskRegistrar.addCronTask(this::runTask, "0 0 0/6 * * ?");
    }

//    @PostConstruct
//    public void runOnStartup() {
//        runTask();
//    }

    private void runTask()  {
        List<ServiceDormElectricityAlertEntity> serviceDormElectricityAlertEntities = serviceDormElectricityAlertMapper.selectByRunStatus(1);
        serviceDormElectricityAlertEntities.forEach(serviceDormElectricityAlertEntity -> {
            Double threshold = serviceDormElectricityAlertEntity.getThreshold();
            String email = serviceDormElectricityAlertEntity.getEmail();
            ServiceLogEntity serviceLogEntity = new ServiceLogEntity();
            serviceLogEntity.setRelationTable("service_dorm_electricity_alert");
            serviceLogEntity.setEmail(email);
            serviceLogEntity.setCreateTime(new Date());
            try {
                Double electricity = serviceDormElectricityAlertService.queryCurrentElectricity(email);
                if(electricity<=threshold){
                    emailUtil.sendText(email, "电量预警", "您的宿舍电量已低于阈值"+threshold+"，当前电量为"+electricity+",请及时充值");
                    serviceLogEntity.setOperationName("发送电量预警邮件");
                    serviceLogEntity.setOperationStatus(1);
                    serviceLogEntity.setRemarks("发送电量预警成功，您当前电量为"+electricity+",已经低于阈值"+threshold);
                    serviceLogMapper.insert(serviceLogEntity);
                }
            } catch (JsonProcessingException e) {
                log.error("执行电量查询任务出错\n{}", (Object) e.getStackTrace());
                serviceLogEntity.setOperationName("查询宿舍当前电量");
                serviceLogEntity.setOperationStatus(0);
                serviceLogEntity.setRemarks("查询宿舍当前电量失败，"+e.getMessage());
                serviceLogMapper.insert(serviceLogEntity);
            }finally {
                try {
                    Thread.sleep(3000);
                }catch (Exception e){
                    log.error("查询电量定时任务出错\n{}", (Object) e.getStackTrace());
                }
            }
        });
    }
}

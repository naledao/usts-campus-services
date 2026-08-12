package hhsc.kangnasi.xyz.ustscampusservices.mq;

import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceCampusNetLoginEntity;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.ServiceCampusNetLoginMapper;
import hhsc.kangnasi.xyz.ustscampusservices.service.ServiceCampusNetLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "mq.delay.enabled", havingValue = "true")
@Slf4j
public class CampusNetLoginScheduleInitializer {

    private final ServiceCampusNetLoginMapper serviceCampusNetLoginMapper;
    private final ServiceCampusNetLoginService serviceCampusNetLoginService;

    public CampusNetLoginScheduleInitializer(
            ServiceCampusNetLoginMapper serviceCampusNetLoginMapper,
            ServiceCampusNetLoginService serviceCampusNetLoginService
    ) {
        this.serviceCampusNetLoginMapper = serviceCampusNetLoginMapper;
        this.serviceCampusNetLoginService = serviceCampusNetLoginService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void rescheduleRunningCampusNetLoginServices() {
        List<ServiceCampusNetLoginEntity> services = serviceCampusNetLoginMapper.selectAll();
        int scheduledCount = 0;
        for (ServiceCampusNetLoginEntity service : services) {
            if (!isRunnable(service)) {
                continue;
            }
            try {
                serviceCampusNetLoginService.scheduleNextRun(service);
                scheduledCount++;
                log.info("已重建校园网自动登录延迟任务，email={}, refreshTime={}", service.getEmail(), service.getRefreshTime());
            } catch (AmqpException error) {
                log.error("重建校园网自动登录延迟任务失败，email={}", service.getEmail(), error);
            } catch (Exception error) {
                log.error("重建校园网自动登录延迟任务异常，email={}", service.getEmail(), error);
            }
        }
        log.info("校园网自动登录延迟任务重建完成，count={}", scheduledCount);
    }

    private boolean isRunnable(ServiceCampusNetLoginEntity service) {
        if (service == null || service.getRunStatus() == null || service.getRunStatus() != 1) {
            return false;
        }
        String refreshTime = service.getRefreshTime();
        if (refreshTime == null || refreshTime.isBlank()) {
            log.warn("跳过缺少 refreshTime 的校园网自动登录服务，email={}", service.getEmail());
            return false;
        }
        String[] parts = refreshTime.split("-");
        if (parts.length != 2) {
            log.warn("跳过 refreshTime 格式错误的校园网自动登录服务，email={}, refreshTime={}", service.getEmail(), refreshTime);
            return false;
        }
        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59;
        } catch (NumberFormatException error) {
            log.warn("跳过 refreshTime 不是数字的校园网自动登录服务，email={}, refreshTime={}", service.getEmail(), refreshTime);
            return false;
        }
    }
}

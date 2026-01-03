package hhsc.kangnasi.xyz.ustscampusservices.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hhsc.kangnasi.xyz.ustscampusservices.contant.MqConstant;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceCampusNetLoginEntity;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.ServiceCampusNetLoginMapper;
import hhsc.kangnasi.xyz.ustscampusservices.service.ServiceCampusNetLoginService;
import hhsc.kangnasi.xyz.ustscampusservices.util.EmailUtil;
import hhsc.kangnasi.xyz.ustscampusservices.util.TimeUtil;
import hhsc.kangnasi.xyz.ustscampusservices.websocket.WsSessionHub;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static hhsc.kangnasi.xyz.ustscampusservices.websocket.CampusNetworkAutoLoginWebSocket.campusNetworkAutoLoginKey;

@Component
@ConditionalOnProperty(name = "mq.delay.enabled", havingValue = "true")
@Slf4j
public class DelayedMessageListener {

    private final ObjectMapper objectMapper;
    private final WsSessionHub wsSessionHub;
    private final ServiceCampusNetLoginMapper serviceCampusNetLoginMapper;
    private final DelayedMessageSender delayedMessageSender;
    private final ServiceCampusNetLoginService serviceCampusNetLoginService;
    private final EmailUtil emailUtil;
    private final RedissonClient redissonClient;


    public DelayedMessageListener(ObjectMapper objectMapper, WsSessionHub wsSessionHub, ServiceCampusNetLoginMapper serviceCampusNetLoginMapper, DelayedMessageSender delayedMessageSender, ServiceCampusNetLoginService serviceCampusNetLoginService, EmailUtil emailUtil, RedissonClient redissonClient) {
        this.objectMapper = objectMapper;
        this.wsSessionHub = wsSessionHub;
        this.serviceCampusNetLoginMapper = serviceCampusNetLoginMapper;
        this.delayedMessageSender = delayedMessageSender;
        this.serviceCampusNetLoginService = serviceCampusNetLoginService;
        this.emailUtil = emailUtil;
        this.redissonClient = redissonClient;
    }

    @RabbitListener(queues = MqConstant.BIZ_QUEUE)
    public void onMessage(String payload) throws JsonProcessingException {
        JsonNode serviceNode = objectMapper.readTree(payload);
        String serviceName=serviceNode.get("serviceName").asText();
        processService(payload,serviceName,serviceNode);
    }

    private void processService(String json,String serviceName,JsonNode serviceNode) throws JsonProcessingException {
        switch (serviceName) {
            case "service_campus_net_login":
                log.info("执行自动登录校园网服务");
                try {
                    String[] currentHourMinuteArray = TimeUtil.getCurrentHourMinuteArray();
                    String email=serviceNode.get("email").asText();
                    log.info("当前执行的的账号为：{}",email);
                    ServiceCampusNetLoginEntity serviceCampusNetLoginEntity = serviceCampusNetLoginMapper.selectById(email);
                    String[] refreshTime = serviceNode.get("refreshTime").asText().split("-");
                    String hour=refreshTime[0];
                    String minute=refreshTime[1];
                    String[] dbRefreshTime = serviceCampusNetLoginEntity.getRefreshTime().split("-");
                    log.info("开始判断队列中的json信息是否和数据库里面的一样");
                    if(serviceCampusNetLoginEntity!=null
                    && serviceCampusNetLoginEntity.getRunStatus()==1
                    && serviceCampusNetLoginEntity.getIsDel()==0
                    && dbRefreshTime[0].equals(hour)
                    && dbRefreshTime[1].equals(minute)
                    && serviceCampusNetLoginEntity.getWlanUserIp().equals(serviceNode.get("wlanUserIp").asText())
                    && (serviceCampusNetLoginEntity.getNetAccount()+"@"+serviceCampusNetLoginEntity.getCarrier()).equals(serviceNode.get("netAccount").asText())
                    && serviceCampusNetLoginEntity.getCarrier().equals(serviceNode.get("carrier").asText())
                    && serviceCampusNetLoginEntity.getNetPassword().equals(serviceNode.get("netPassword").asText())){
                        log.info("开始判断当前账号今天是否已经执行过了");
                        // 判断今天有没有发送过
                        RBucket<Object> bucket = redissonClient.getBucket("campusNetLoginServiceSend:" + TimeUtil.getToday() + email);
                        if(bucket.get()!=null){
                            // 今天已经发送过了，所以直接跳过
                            log.info("当前账号今天已经执行过了，所以直接跳过");
                            return;
                        }
                        boolean send = wsSessionHub.send(campusNetworkAutoLoginKey, json);
                        log.info("开始判断发送websocket是否成功");
                        if(!send){
                            // 没有发送成功，添加到消息队列中（延迟12分钟），并发送邮件告知我
                            // 发送邮件告知我
                            emailUtil.sendText("2419646091@qq.com","发送校园网操作失败","websocket已断开连接",true);
                            delayedMessageSender.send(serviceCampusNetLoginService.toJson(serviceCampusNetLoginEntity),7200000);
                            log.info("没有发送websocket成功，添加到消息队列中（延迟12分钟），并发送邮件告知我");
                        }else{
                            log.info("当前刷新校园网的的操作执行成功，开始放到下一天的消息队列");
                            // 发送成功，将今天的发送记录添加到redis中（过期时间设置为24小时）
                            bucket.set("yes", Duration.ofDays(3));
                            log.info("开始判断执行的时间是否是json里面的时间");
                            if(hour.equals(currentHourMinuteArray[0]) && minute.equals(currentHourMinuteArray[1])){ // 是当前时间，直接24小时之后
                                delayedMessageSender.send(serviceCampusNetLoginService.toJson(serviceCampusNetLoginEntity),86400000);
                                log.info("是当前时间，直接24小时之后");
                            }
                            else{
                                long intervalMillis=TimeUtil.getIntervalMillis(hour,minute);
                                delayedMessageSender.send(serviceCampusNetLoginService.toJson(serviceCampusNetLoginEntity),intervalMillis);
                                log.info("不是当前时间，计算出距离当前时间的毫秒数，放到消息队列中");
                            }
                        }
                    }
                    else{
                        log.info("和数据库里面的信息不一样");
                        log.info(json);
                    }
                    log.info("执行自动登录校园网操作结束");
                    log.info("================================================");
                    break;
                }catch (Exception e){
                    e.printStackTrace();
                }
            case "login":
                // 处理登录服务
                break;
            default:
                // 处理其他服务
                break;
        }
    }
}


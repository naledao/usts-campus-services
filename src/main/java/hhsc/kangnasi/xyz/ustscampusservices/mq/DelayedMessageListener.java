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
                try {
                    String[] currentHourMinuteArray = TimeUtil.getCurrentHourMinuteArray();
                    String email=serviceNode.get("email").asText();
                    ServiceCampusNetLoginEntity serviceCampusNetLoginEntity = serviceCampusNetLoginMapper.selectById(email);
                    String[] refreshTime = serviceNode.get("refreshTime").asText().split("-");
                    String hour=refreshTime[0];
                    String minute=refreshTime[1];
                    String[] dbRefreshTime = serviceCampusNetLoginEntity.getRefreshTime().split("-");
                    if(serviceCampusNetLoginEntity!=null && serviceCampusNetLoginEntity.getRunStatus()==1 && serviceCampusNetLoginEntity.getIsDel()==0 && dbRefreshTime[0].equals(hour) && dbRefreshTime[1].equals(minute)
                    && serviceCampusNetLoginEntity.getWlanUserIp().equals(serviceNode.get("wlanUserIp").asText())){
                        // 判断今天有没有发送过
                        RBucket<Object> bucket = redissonClient.getBucket("campusNetLoginServiceSend:" + TimeUtil.getToday() + email);
                        if(bucket.get()!=null){
                            // 今天已经发送过了，所以直接跳过
                            return;
                        }
                        boolean send = wsSessionHub.send(campusNetworkAutoLoginKey, json);
                        if(!send){
                            // 没有发送成功，添加到消息队列中（延迟12分钟），并发送邮件告知我
                            // 发送邮件告知我
                            emailUtil.sendText("2419646091@qq.com","发送校园网操作失败","websocket已断开连接");
                            delayedMessageSender.send(serviceCampusNetLoginService.toJson(serviceCampusNetLoginEntity),7200000);
                        }
                        // 发送成功，将今天的发送记录添加到redis中（过期时间设置为24小时）
                        bucket.set("yes", Duration.ofDays(3));
                        // 判断当前json的刷新时间是否和数据库里面的一样
                        if(dbRefreshTime[0].equals(hour) && dbRefreshTime[1].equals(minute)){
                            // 是一样的，所以可以自动再将消息放入到延迟队列中去
                            if(hour.equals(currentHourMinuteArray[0]) && minute.equals(currentHourMinuteArray[1])){ // 是当前时间，直接24小时之后
                                delayedMessageSender.send(serviceCampusNetLoginService.toJson(serviceCampusNetLoginEntity),86400000);
                            }
                            else{
                                long intervalMillis=TimeUtil.getIntervalMillis(hour,minute);
                                delayedMessageSender.send(serviceCampusNetLoginService.toJson(serviceCampusNetLoginEntity),intervalMillis);
                            }
                        }
                    }
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


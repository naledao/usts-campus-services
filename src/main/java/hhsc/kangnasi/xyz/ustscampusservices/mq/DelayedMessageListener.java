package hhsc.kangnasi.xyz.ustscampusservices.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.time.Instant;

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
    private static final long WEBSOCKET_RETRY_DELAY_MILLIS = 7200000L;


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
                    String email=serviceNode.get("email").asText();
                    log.info("当前执行的的账号为：{}",email);
                    JsonNode scheduledAtNode = serviceNode.get("scheduledAtEpochMillis");
                    if (scheduledAtNode == null || !scheduledAtNode.canConvertToLong()) {
                        log.warn("跳过旧格式校园网自动登录延迟消息，email={}", email);
                        return;
                    }
                    long scheduledAtEpochMillis = scheduledAtNode.asLong();
                    long nowEpochMillis = Instant.now().toEpochMilli();
                    if (scheduledAtEpochMillis - nowEpochMillis > Duration.ofMinutes(1).toMillis()) {
                        log.warn("校园网自动登录消息提前投递，继续消费，email={}, scheduledAtEpochMillis={}, earlyMillis={}",
                                email, scheduledAtEpochMillis, scheduledAtEpochMillis - nowEpochMillis);
                    }
                    ServiceCampusNetLoginEntity serviceCampusNetLoginEntity = serviceCampusNetLoginMapper.selectById(email);
                    if (serviceCampusNetLoginEntity == null) {
                        log.info("数据库中不存在该校园网自动登录服务，email={}", email);
                        return;
                    }
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
                            // 没有发送成功，添加到消息队列中（延迟2小时），并发送邮件告知我
                            // 发送邮件告知我
                            emailUtil.sendText("2419646091@qq.com","发送校园网操作失败","websocket已断开连接",true);
                            delayedMessageSender.send(toScheduledJson(serviceCampusNetLoginEntity, WEBSOCKET_RETRY_DELAY_MILLIS), WEBSOCKET_RETRY_DELAY_MILLIS);
                            log.info("没有发送websocket成功，添加到消息队列中（延迟2小时），并发送邮件告知我");
                        }else{
                            log.info("当前刷新校园网的的操作执行成功，开始放到下一天的消息队列");
                            // 发送成功，将今天的发送记录添加到redis中（过期时间设置为24小时）
                            bucket.set("yes", Duration.ofDays(3));
                            serviceCampusNetLoginService.scheduleNextRunAfter(
                                    serviceCampusNetLoginEntity,
                                    Instant.ofEpochMilli(scheduledAtEpochMillis)
                            );
                            log.info("已按本次计划时间之后的下一执行时间重新排队");
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
                break;
            case "login":
                // 处理登录服务
                break;
            default:
                // 处理其他服务
                break;
        }
    }

    private String toScheduledJson(ServiceCampusNetLoginEntity serviceCampusNetLoginEntity, long delayMillis) throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(serviceCampusNetLoginService.toJson(serviceCampusNetLoginEntity));
        ObjectNode objectNode = (ObjectNode) node;
        objectNode.put("scheduledAtEpochMillis", Instant.now().plusMillis(delayMillis).toEpochMilli());
        objectNode.put("scheduledZone", TimeUtil.BUSINESS_ZONE.getId());
        return objectMapper.writeValueAsString(objectNode);
    }
}

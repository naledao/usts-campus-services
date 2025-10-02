package hhsc.kangnasi.xyz.ustscampusservices.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hhsc.kangnasi.xyz.ustscampusservices.contant.MqConstant;
import hhsc.kangnasi.xyz.ustscampusservices.websocket.WsSessionHub;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static hhsc.kangnasi.xyz.ustscampusservices.websocket.CampusNetworkAutoLoginWebSocket.campusNetworkAutoLoginKey;

@Component
@ConditionalOnProperty(name = "mq.delay.enabled", havingValue = "true")
public class DelayedMessageListener {

    private final ObjectMapper objectMapper;
    private final WsSessionHub wsSessionHub;

    public DelayedMessageListener(ObjectMapper objectMapper, WsSessionHub wsSessionHub) {
        this.objectMapper = objectMapper;
        this.wsSessionHub = wsSessionHub;
    }

    @RabbitListener(queues = MqConstant.BIZ_QUEUE)
    public void onMessage(String payload) throws JsonProcessingException {
        JsonNode serviceNode = objectMapper.readTree(payload);
        String serviceName=serviceNode.get("serviceName").asText();
        processService(payload,serviceName);
    }

    private void processService(String json,String serviceName) {
        switch (serviceName) {
            case "service_campus_net_login":
                wsSessionHub.send(campusNetworkAutoLoginKey,json);
                break;
            case "login":
                // 处理登录服务
                break;
            default:
                // 处理其他服务
                break;
        }
    }
}


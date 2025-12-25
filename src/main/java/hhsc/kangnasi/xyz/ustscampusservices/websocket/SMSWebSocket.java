package hhsc.kangnasi.xyz.ustscampusservices.websocket;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hhsc.kangnasi.xyz.ustscampusservices.config.ApplicationContextProvider;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceLogEntity;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.ServiceLogMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@ServerEndpoint("/sms/{secretKey}")
@Component
public class SMSWebSocket {

    @Setter
    private static WsSessionHub hub;

    public static final String  SMSKEY="bgcjhdsgcfjsdgkuiweyqwcdgksjabvuydfvgbfiahhdc87236rfhdw7";

    private ServiceLogMapper serviceLogMapper= ApplicationContextProvider.getBean(ServiceLogMapper.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session, @PathParam("secretKey") String secretKey) throws IOException {
        if(secretKey==null){
            send(session, "密钥错误");
            session.close();
            return;
        }
        if(!SMSKEY.equals(secretKey)) {
            send(session, "密钥错误");
            session.close();
            return;
        }
        hub.add(SMSKEY, session);
        session.getAsyncRemote().sendText("连接成功");
    }

    @OnMessage
    public void onMessage(String msg, Session session) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(msg);
        String result = jsonNode.get("result").asText();
        ServiceLogEntity serviceLog=new ServiceLogEntity();
        serviceLog.setRelationTable("NO_TABLE");
        serviceLog.setEmail("2419646091@qq.com");
        serviceLog.setCreateTime(new Date());
        serviceLog.setOperationName("发送短信");
        serviceLog.setOperationStatus(result.equals("success")?1:0);
        serviceLog.setRemarks("发送短信失败");
        serviceLogMapper.insert(serviceLog);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        hub.remove(SMSKEY);
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        // 记录日志即可
    }

    private void send(Session session, String text) {
        try { session.getBasicRemote().sendText(text); } catch (Exception ignored) {}
    }
}

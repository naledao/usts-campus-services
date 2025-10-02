package hhsc.kangnasi.xyz.ustscampusservices.websocket;

import hhsc.kangnasi.xyz.ustscampusservices.config.ApplicationContextProvider;
import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceLogEntity;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.ServiceLogMapper;
import jakarta.websocket.OnOpen;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.Setter;
import org.springframework.stereotype.Component;
import jakarta.websocket.*;

import java.io.IOException;
import java.util.Date;


@ServerEndpoint("/campus-network-auto-login/{secretKey}")
@Component
public class CampusNetworkAutoLoginWebSocket {

    @Setter
    private static WsSessionHub hub;

    public static final String  campusNetworkAutoLoginKey="d1e56wf48sfv15489rt4es2dc57svd84f5c1289sfdv4c1s56489rs6f48r6egr489s65rd4f98r64s5vdf845esrd";

    private ServiceLogMapper serviceLogMapper= ApplicationContextProvider.getBean(ServiceLogMapper.class);


    @OnOpen
    public void onOpen(Session session, @PathParam("secretKey") String secretKey) throws IOException {
        if(secretKey==null){
            send(session, "密钥错误");
            session.close();
            return;
        }
        if(!campusNetworkAutoLoginKey.equals(secretKey)) {
            send(session, "密钥错误");
            session.close();
            return;
        }
        hub.add(campusNetworkAutoLoginKey, session);
        session.getAsyncRemote().sendText("连接成功");
    }

    @OnMessage
    public void onMessage(String msg, Session session) {
        if(msg.contains("======")){
            try {
                String[] split = msg.split("======");
                String[] splitMsg = split[0].split(":");
                // 0-【login或者logout】，1-【0-操作失败，1-操作成功】，2-【邮箱】,3-【校园网账号======可能的错误信息】
                ServiceLogEntity serviceLog=new ServiceLogEntity();
                serviceLog.setRelationTable("service_campus_net_login");
                serviceLog.setEmail(splitMsg[2]);
                serviceLog.setCreateTime(new Date());
                serviceLog.setOperationName(splitMsg[0].equals("login")?"登录":"下线");
                serviceLog.setOperationStatus(Integer.parseInt(splitMsg[1]));
                serviceLog.setRemarks(split[1]);
                serviceLogMapper.insert(serviceLog);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        hub.remove(campusNetworkAutoLoginKey);
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        // 记录日志即可
    }

    private void send(Session session, String text) {
        try { session.getBasicRemote().sendText(text); } catch (Exception ignored) {}
    }
}

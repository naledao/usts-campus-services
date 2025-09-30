package hhsc.kangnasi.xyz.ustscampusservices.websocket;

import jakarta.websocket.OnOpen;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.Setter;
import org.springframework.stereotype.Component;
import jakarta.websocket.*;

import java.io.IOException;



@ServerEndpoint("/campus-network-auto-login/{secretKey}")
@Component
public class CampusNetworkAutoLoginWebSocket {

    @Setter
    private static WsSessionHub hub;

    private static final String key="d1e56wf48sfv15489rt4es2dc57svd84f5c1289sfdv4c1s56489rs6f48r6egr489s65rd4f98r64s5vdf845esrd";

    @OnOpen
    public void onOpen(Session session, @PathParam("secretKey") String secretKey) throws IOException {
        if(!key.equals(secretKey)) {
            send(session, "密钥错误");
            session.close();
            return;
        }
        hub.add(key, session);
        session.getAsyncRemote().sendText("连接成功");
    }

    @OnMessage
    public void onMessage(String msg, Session session) {
        send(session, "服务器收到: " + msg);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        hub.remove(key);
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        // 记录日志即可
    }

    private void send(Session session, String text) {
        try { session.getBasicRemote().sendText(text); } catch (Exception ignored) {}
    }
}

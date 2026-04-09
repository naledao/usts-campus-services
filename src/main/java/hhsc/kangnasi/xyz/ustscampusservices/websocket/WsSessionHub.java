package hhsc.kangnasi.xyz.ustscampusservices.websocket;

import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WsSessionHub {
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public void add(String key, Session s) { sessions.put(key, s); }
    public void remove(String key) { sessions.remove(key); }

    public boolean send(String key, String text) {
        Session s = sessions.get(key);
        if (s == null) {
            log.warn("WsSessionHub.send: session not found, key={}", key);
            return false;
        }
        if(!s.isOpen()){
            log.warn("WsSessionHub.send: session is closed, key={}", key);
            return false;
        }
        s.getAsyncRemote().sendText(text);
        log.info("WsSessionHub.send: sent message to session, key={}", key);
        return true;
    }

    public int broadcast(String text) {
        int cnt = 0;
        for (Session s : sessions.values()) {
            if (s.isOpen()) { s.getAsyncRemote().sendText(text); cnt++; }
        }
        return cnt;
    }
}
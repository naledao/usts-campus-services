package hhsc.kangnasi.xyz.ustscampusservices.websocket;

import jakarta.websocket.Session;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WsSessionHub {
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public void add(String key, Session s) { sessions.put(key, s); }
    public void remove(String key) { sessions.remove(key); }

    public boolean send(String key, String text) {
        Session s = sessions.get(key);
        if (s == null || !s.isOpen()) return false;
        s.getAsyncRemote().sendText(text); // 异步更保险
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
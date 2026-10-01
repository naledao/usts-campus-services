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

    public void add(String key, Session session) {
        Session previous = sessions.put(key, session);
        log.info(
                "WsSessionHub.add: registered session, sessionId={}, replacedSessionId={}",
                session.getId(),
                previous == null ? "none" : previous.getId()
        );
    }

    public boolean remove(String key, Session session) {
        boolean removed = sessions.remove(key, session);
        if (removed) {
            log.info("WsSessionHub.remove: removed current session, sessionId={}", session.getId());
        } else {
            log.info("WsSessionHub.remove: ignored stale session close, sessionId={}", session.getId());
        }
        return removed;
    }

    public boolean send(String key, String text) {
        Session s = sessions.get(key);
        if (s == null) {
            log.warn("WsSessionHub.send: session not found");
            return false;
        }
        if(!s.isOpen()){
            log.warn("WsSessionHub.send: session is closed, sessionId={}", s.getId());
            return false;
        }
        s.getAsyncRemote().sendText(text);
        log.info("WsSessionHub.send: sent message to session, sessionId={}", s.getId());
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

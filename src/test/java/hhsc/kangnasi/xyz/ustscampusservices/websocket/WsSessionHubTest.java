package hhsc.kangnasi.xyz.ustscampusservices.websocket;

import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WsSessionHubTest {

    @Test
    void closingReplacedSessionDoesNotRemoveCurrentSession() {
        WsSessionHub hub = new WsSessionHub();
        AtomicReference<String> sentText = new AtomicReference<>();
        RemoteEndpoint.Async currentRemote = asyncRemote(sentText);
        Session oldSession = session("old-session", true, null);
        Session currentSession = session("current-session", true, currentRemote);

        hub.add("service-key", oldSession);
        hub.add("service-key", currentSession);

        assertFalse(hub.remove("service-key", oldSession));
        assertTrue(hub.send("service-key", "payload"));
        assertEquals("payload", sentText.get());
    }

    @Test
    void closingCurrentSessionRemovesIt() {
        WsSessionHub hub = new WsSessionHub();
        Session currentSession = session("current-session", true, null);

        hub.add("service-key", currentSession);

        assertTrue(hub.remove("service-key", currentSession));
        assertFalse(hub.send("service-key", "payload"));
    }

    private static Session session(String id, boolean open, RemoteEndpoint.Async asyncRemote) {
        return (Session) Proxy.newProxyInstance(
                Session.class.getClassLoader(),
                new Class<?>[]{Session.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getId" -> id;
                    case "isOpen" -> open;
                    case "getAsyncRemote" -> asyncRemote;
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "Session[" + id + "]";
                    default -> null;
                }
        );
    }

    private static RemoteEndpoint.Async asyncRemote(AtomicReference<String> sentText) {
        return (RemoteEndpoint.Async) Proxy.newProxyInstance(
                RemoteEndpoint.Async.class.getClassLoader(),
                new Class<?>[]{RemoteEndpoint.Async.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("sendText") && args.length == 1 && args[0] instanceof String text) {
                        sentText.set(text);
                        return CompletableFuture.completedFuture(null);
                    }
                    if (method.getName().equals("equals")) {
                        return proxy == args[0];
                    }
                    if (method.getName().equals("hashCode")) {
                        return System.identityHashCode(proxy);
                    }
                    return null;
                }
        );
    }
}

package hhsc.kangnasi.xyz.ustscampusservices.websocket;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class WsBootstrap {
    private final WsSessionHub hub;

    @PostConstruct
    public void wire() {
        CampusNetworkAutoLoginWebSocket.setHub(hub);
    }
}
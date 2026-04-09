package hhsc.kangnasi.xyz.ustscampusservices.config;

import hhsc.kangnasi.xyz.ustscampusservices.handler.MusicWsHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class SpringWsBridgeConfig implements WebSocketConfigurer {

    private final MusicWsHandler musicWsHandler;

    public SpringWsBridgeConfig(MusicWsHandler musicWsHandler) {
        this.musicWsHandler = musicWsHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(musicWsHandler, "/ws/music")
                .setAllowedOrigins("*");
    }
}

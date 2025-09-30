package hhsc.kangnasi.xyz.ustscampusservices.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSocketConfig {
    @Bean
    public org.springframework.web.socket.server.standard.ServerEndpointExporter serverEndpointExporter() {
        return new org.springframework.web.socket.server.standard.ServerEndpointExporter();
    }
}
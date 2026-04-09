package hhsc.kangnasi.xyz.ustscampusservices.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
public class WsBufferConfig {

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        // 你现在 8.5KB 就炸，直接拉到 1MB 以上
        container.setMaxTextMessageBufferSize(1024 * 1024);   // 1MB
        container.setMaxBinaryMessageBufferSize(10 * 1024 * 1024); // 10MB（下载走二进制）
        return container;
    }
}

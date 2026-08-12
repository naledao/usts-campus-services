package hhsc.kangnasi.xyz.ustscampusservices.config;

import hhsc.kangnasi.xyz.ustscampusservices.websocket.CampusNetworkAutoLoginWebSocket;
import hhsc.kangnasi.xyz.ustscampusservices.websocket.SMSWebSocket;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.annotation.RegisterReflection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RegisterReflection(
        classes = {
                CampusNetworkAutoLoginWebSocket.class,
                SMSWebSocket.class
        },
        memberCategories = {
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_METHODS
        }
)
public class WebSocketConfig {
    @Bean
    public org.springframework.web.socket.server.standard.ServerEndpointExporter serverEndpointExporter() {
        return new org.springframework.web.socket.server.standard.ServerEndpointExporter();
    }
}

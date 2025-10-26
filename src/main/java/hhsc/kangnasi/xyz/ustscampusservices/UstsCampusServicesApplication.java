package hhsc.kangnasi.xyz.ustscampusservices;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableTransactionManagement
@EnableWebSocket
@EnableDubbo
public class UstsCampusServicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(UstsCampusServicesApplication.class, args);
    }

}

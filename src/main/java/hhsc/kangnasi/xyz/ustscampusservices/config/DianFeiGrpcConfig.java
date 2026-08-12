package hhsc.kangnasi.xyz.ustscampusservices.config;

import dianfei.DianFeiServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class DianFeiGrpcConfig {

    @Bean(destroyMethod = "shutdownNow")
    ManagedChannel dianFeiManagedChannel(
            @Value("${app.dianfei.host:127.0.0.1}") String host,
            @Value("${app.dianfei.port:55051}") int port
    ) {
        return OkHttpChannelBuilder.forAddress(host, port)
                .proxyDetector(targetAddress -> null)
                .usePlaintext()
                .build();
    }

    @Bean
    DianFeiServiceGrpc.DianFeiServiceBlockingStub dianFeiServiceBlockingStub(
            ManagedChannel dianFeiManagedChannel
    ) {
        return DianFeiServiceGrpc.newBlockingStub(dianFeiManagedChannel);
    }
}

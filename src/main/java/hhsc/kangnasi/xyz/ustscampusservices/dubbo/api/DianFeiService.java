package hhsc.kangnasi.xyz.ustscampusservices.dubbo.api;


import org.apache.dubbo.config.annotation.DubboService;


public interface DianFeiService {
    String query_current_electricity(String payloadJson);
}

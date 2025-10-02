package hhsc.kangnasi.xyz.ustscampusservices;

import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceCampusNetLoginEntity;
import hhsc.kangnasi.xyz.ustscampusservices.domain.vo.CommonServiceVo;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.ServiceCampusNetLoginMapper;
import hhsc.kangnasi.xyz.ustscampusservices.mq.DelayedMessageSender;
import hhsc.kangnasi.xyz.ustscampusservices.service.ServiceCampusNetLoginService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class Test {


    @InjectMocks
    private DelayedMessageSender delayedMessageSender;

    @org.junit.jupiter.api.Test
    public void testCreate() {
        delayedMessageSender.send(System.currentTimeMillis(),10000);
    }


}

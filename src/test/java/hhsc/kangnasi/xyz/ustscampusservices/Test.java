package hhsc.kangnasi.xyz.ustscampusservices;

import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceCampusNetLoginEntity;
import hhsc.kangnasi.xyz.ustscampusservices.domain.vo.CommonServiceVo;
import hhsc.kangnasi.xyz.ustscampusservices.mapper.ServiceCampusNetLoginMapper;
import hhsc.kangnasi.xyz.ustscampusservices.service.ServiceCampusNetLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class Test {

    @Autowired
    ServiceCampusNetLoginService serviceCampusNetLoginService;
    @Autowired
    ServiceCampusNetLoginMapper mapper;

    @org.junit.jupiter.api.Test
    public void testCreate() {
        ServiceCampusNetLoginEntity serviceCampusNetLoginEntity = mapper.selectById("2419646091@qq.com");
        System.out.println(serviceCampusNetLoginEntity);
    }


}

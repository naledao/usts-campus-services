package hhsc.kangnasi.xyz.ustscampusservices.config;

import org.mybatis.spring.boot.autoconfigure.SqlSessionFactoryBeanCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration(proxyBeanMethods = false)
public class MybatisNativeConfig {

    private static final String[] MAPPER_XML_LOCATIONS = {
            "classpath:hhsc/kangnasi/xyz/ustscampusservices/mapper/xml/ServiceCampusNetLoginMapper.xml",
            "classpath:hhsc/kangnasi/xyz/ustscampusservices/mapper/xml/ServiceDormElectricityAlertMapper.xml",
            "classpath:hhsc/kangnasi/xyz/ustscampusservices/mapper/xml/ServiceDormElectricityAlertRoomMapper.xml",
            "classpath:hhsc/kangnasi/xyz/ustscampusservices/mapper/xml/ServiceLogMapper.xml",
            "classpath:hhsc/kangnasi/xyz/ustscampusservices/mapper/xml/SysUserMapper.xml"
    };

    @Bean
    SqlSessionFactoryBeanCustomizer nativeMapperLocationsCustomizer(ApplicationContext applicationContext) {
        return factoryBean -> {
            Resource[] mapperResources = new Resource[MAPPER_XML_LOCATIONS.length];
            for (int i = 0; i < MAPPER_XML_LOCATIONS.length; i++) {
                mapperResources[i] = applicationContext.getResource(MAPPER_XML_LOCATIONS[i]);
            }
            factoryBean.setMapperLocations(mapperResources);
        };
    }
}

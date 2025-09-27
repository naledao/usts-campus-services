package hhsc.kangnasi.xyz.ustscampusservices.service;

import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceCampusNetLoginEntity;
import hhsc.kangnasi.xyz.ustscampusservices.domain.vo.CommonServiceVo;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ServiceCampusNetLoginService {

    ResponseEntity<?> create(ServiceCampusNetLoginEntity body);

    List<CommonServiceVo> allService(String email);
}


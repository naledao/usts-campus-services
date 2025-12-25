package hhsc.kangnasi.xyz.ustscampusservices.mapper;

import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ServiceLogMapper {
    int insert(ServiceLogEntity serviceLog);

    int update(ServiceLogEntity serviceLog);

    int deleteById(Integer id);


    ServiceLogEntity selectById(Integer id);

    List<ServiceLogEntity> selectAll();

    List<ServiceLogEntity> selectByEmail(@Param("email") String email, @Param("relationTable") String relationTable);

    List<ServiceLogEntity> selectByRelationTable(String relationTable);
}

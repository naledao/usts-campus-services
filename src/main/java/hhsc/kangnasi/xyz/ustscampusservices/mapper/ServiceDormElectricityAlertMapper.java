package hhsc.kangnasi.xyz.ustscampusservices.mapper;

import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceDormElectricityAlertEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ServiceDormElectricityAlertMapper {
    int insert(ServiceDormElectricityAlertEntity record);

    int insertSelective(ServiceDormElectricityAlertEntity record); // 仅对非空字段插入

    int updateById(ServiceDormElectricityAlertEntity record);      // 动态更新（按主键）

    int deleteById(@Param("id") Integer id);                 // 物理删除

    int markDeleteById(@Param("id") Integer id);             // 逻辑删除 is_del=1

    ServiceDormElectricityAlertEntity selectById(@Param("id") Integer id);

    ServiceDormElectricityAlertEntity selectByEmail(@Param("email") String email,@Param("isDel") Integer isDel);

    List<ServiceDormElectricityAlertEntity> selectAll();           // 简单全量查询
}

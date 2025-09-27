package hhsc.kangnasi.xyz.ustscampusservices.mapper;


import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.SysUserEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysUserMapper {

    SysUserEntity selectByEmail(String email);

    List<SysUserEntity> selectAll();

    int insert(SysUserEntity user);

    int update(SysUserEntity user);

    int deleteByEmail(String email);
}

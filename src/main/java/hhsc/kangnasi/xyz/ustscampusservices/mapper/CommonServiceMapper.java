package hhsc.kangnasi.xyz.ustscampusservices.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CommonServiceMapper {

    @Update("UPDATE ${serviceTableName} SET run_status = 1 , update_time = now() WHERE email = #{email} and is_del=0")
    int startService(@Param("email") String email, @Param("serviceTableName") String serviceTableName);

    @Update("UPDATE ${serviceTableName} SET run_status = 0, update_time = now() WHERE email = #{email} and is_del=0")
    int stopService(@Param("email") String email, @Param("serviceTableName") String serviceTableName);

    @Update("UPDATE ${tableName} SET is_del = 1 , update_time = now() WHERE email = #{email} and is_del=0")
    int deleteService(@Param("email") String email, @Param("tableName") String tableName);
}

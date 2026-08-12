package hhsc.kangnasi.xyz.ustscampusservices.mapper;

import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceCampusNetLoginEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ServiceCampusNetLoginMapper {

    @Select("SELECT * FROM service_campus_net_login WHERE email = #{email} and is_del = 0 for update")
    @Results({
            @Result(property = "email", column = "email", id = true),
            @Result(property = "netAccount", column = "net_account"),
            @Result(property = "carrier", column = "carrier"),
            @Result(property = "netPassword", column = "net_password"),
            @Result(property = "wlanUserIp", column = "wlan_user_ip"),
            @Result(property = "wlanUserMac", column = "wlan_user_mac"),
            @Result(property = "wlanAcIp", column = "wlan_ac_ip"),
            @Result(property = "wlanAcName", column = "wlan_ac_name"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "isDel", column = "is_del"),
            @Result(property = "runStatus", column = "run_status"),
            @Result(property = "refreshTime", column = "refresh_time")
    })
    ServiceCampusNetLoginEntity selectById(String email);

    @Select("SELECT * FROM service_campus_net_login WHERE is_del = 0")
    @Results({
            @Result(property = "email", column = "email", id = true),
            @Result(property = "netAccount", column = "net_account"),
            @Result(property = "carrier", column = "carrier"),
            @Result(property = "netPassword", column = "net_password"),
            @Result(property = "wlanUserIp", column = "wlan_user_ip"),
            @Result(property = "wlanUserMac", column = "wlan_user_mac"),
            @Result(property = "wlanAcIp", column = "wlan_ac_ip"),
            @Result(property = "wlanAcName", column = "wlan_ac_name"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "isDel", column = "is_del"),
            @Result(property = "runStatus", column = "run_status"),
            @Result(property = "refreshTime", column = "refresh_time")
    })
    List<ServiceCampusNetLoginEntity> selectAll();

    @Insert("INSERT INTO service_campus_net_login " +
            "(email, net_account, carrier, net_password, wlan_user_ip, wlan_user_mac, wlan_ac_ip, wlan_ac_name, create_time, update_time, is_del, run_status, refresh_time) " +
            "VALUES (#{email}, #{netAccount}, #{carrier}, #{netPassword}, #{wlanUserIp}, #{wlanUserMac}, #{wlanAcIp}, #{wlanAcName}, #{createTime}, #{updateTime}, #{isDel}, #{runStatus}, #{refreshTime})")
    int insert(ServiceCampusNetLoginEntity record);

    @Update("UPDATE service_campus_net_login SET " +
            "net_account=#{netAccount}, carrier=#{carrier}, net_password=#{netPassword}, wlan_user_ip=#{wlanUserIp}, " +
            "wlan_user_mac=#{wlanUserMac}, wlan_ac_ip=#{wlanAcIp}, wlan_ac_name=#{wlanAcName}, update_time=#{updateTime}, is_del=#{isDel}, run_status=#{runStatus}, refresh_time=#{refreshTime} " +
            "WHERE email=#{email}")
    int update(ServiceCampusNetLoginEntity record);

    @Delete("DELETE FROM service_campus_net_login WHERE email = #{email}")
    int deleteById(String email);

    @Select("select * from service_campus_net_login where email = #{email} and is_del = #{isDel}")
    @Results({
            @Result(property = "email", column = "email", id = true),
            @Result(property = "netAccount", column = "net_account"),
            @Result(property = "carrier", column = "carrier"),
            @Result(property = "netPassword", column = "net_password"),
            @Result(property = "wlanUserIp", column = "wlan_user_ip"),
            @Result(property = "wlanUserMac", column = "wlan_user_mac"),
            @Result(property = "wlanAcIp", column = "wlan_ac_ip"),
            @Result(property = "wlanAcName", column = "wlan_ac_name"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "isDel", column = "is_del"),
            @Result(property = "runStatus", column = "run_status"),
            @Result(property = "refreshTime", column = "refresh_time")
    })
    ServiceCampusNetLoginEntity selectByNetByEmail(@Param("email") String email,@Param("isDel") int isDel);
}

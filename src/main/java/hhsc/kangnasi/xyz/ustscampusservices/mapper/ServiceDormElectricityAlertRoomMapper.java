package hhsc.kangnasi.xyz.ustscampusservices.mapper;

import hhsc.kangnasi.xyz.ustscampusservices.domain.entity.ServiceDormElectricityAlertRoomEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ServiceDormElectricityAlertRoomMapper {
    int insert(ServiceDormElectricityAlertRoomEntity entity);

    int insertBatch(@Param("list") List<ServiceDormElectricityAlertRoomEntity> list);

    int deleteById(@Param("id") Integer id);

    int updateById(ServiceDormElectricityAlertRoomEntity entity);

    ServiceDormElectricityAlertRoomEntity selectById(@Param("id") Integer id);

     List<ServiceDormElectricityAlertRoomEntity> selectByCampusAndBuilding(
            @Param("campusId") String campusId,
            @Param("buildingId") Integer buildingId
    );

    ServiceDormElectricityAlertRoomEntity selectByCampusAndBuildingAndRoom(
            @Param("campusId") String campusId,
            @Param("buildingId") Integer buildingId,
            @Param("roomId") String roomId
    );

    /**
     * 条件列表查询（均为可选条件）
     */
    List<ServiceDormElectricityAlertRoomEntity> selectList(
            @Param("feeitemid") Integer feeitemid,
            @Param("type") String type,
            @Param("level") Integer level,
            @Param("campus") String campus,
            @Param("building") Integer building,
            @Param("room") Integer room,
            @Param("roomName") String roomName
    );
}

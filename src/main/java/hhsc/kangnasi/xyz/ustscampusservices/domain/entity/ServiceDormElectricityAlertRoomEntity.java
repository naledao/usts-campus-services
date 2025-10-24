package hhsc.kangnasi.xyz.ustscampusservices.domain.entity;

import lombok.Data;

@Data
public class ServiceDormElectricityAlertRoomEntity {
    private Integer id;          // 主键
    private Integer feeitemid;   // 费用项ID
    private String type;         // 类型
    private Integer level;       // 级别
    private String campus;       // 属于哪个小区
    private Integer building;    // 属于哪栋楼
    private String room;        // 房间号
    private String roomName;     // 房间名称
}

package hhsc.kangnasi.xyz.ustscampusservices.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServiceDormElectricityAlertEntity {
    private Integer id;                 // 主键
    private String  email;              // 邮箱
    private String  feeItemId;          // feeitemid
    private String  type;               // 类型
    private String  level;              // 等级
    private String  campus;             // 校区
    private String  campusName;         // 校区名称
    private String  building;           // 楼栋
    private String buildingName;        // 楼栋名称
    private String  room;               // 房间
    private String roomName;            // 房间名称
    private Integer runStatus;          // 运行状态
    private Double  threshold;          // 阈值
    private Integer isDel;              // 是否删除
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 更新时间
}

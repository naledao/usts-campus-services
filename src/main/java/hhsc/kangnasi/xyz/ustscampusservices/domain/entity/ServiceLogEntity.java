package hhsc.kangnasi.xyz.ustscampusservices.domain.entity;

import java.util.Date;

public class ServiceLogEntity {
    private Integer id;                 // id
    private String relationTable;       // 关联服务
    private String email;               // 邮箱
    private Date createTime;            // 日志产生时间
    private String operationName;       // 操作名称
    private Integer operationStatus;    // 操作状态（1-成功，0-失败）
    private String remarks;             // 备注

    // getter & setter
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getRelationTable() {
        return relationTable;
    }
    public void setRelationTable(String relationTable) {
        this.relationTable = relationTable;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreateTime() {
        return createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getOperationName() {
        return operationName;
    }
    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public Integer getOperationStatus() {
        return operationStatus;
    }
    public void setOperationStatus(Integer operationStatus) {
        this.operationStatus = operationStatus;
    }

    public String getRemarks() {
        return remarks;
    }
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}

package hhsc.kangnasi.xyz.ustscampusservices.domain.entity;

import java.util.Date;

public class ServiceCampusNetLoginEntity {
    private String email;        // 邮箱 (主键)
    private String netAccount;   // 校园网账号
    private String carrier;      // 运营商
    private String netPassword;  // 校园网密码
    private String wlanUserIp;   // 网络ip
    private String wlanUserMac;  // mac地址
    private String wlanAcIp;     // 公网ip
    private String wlanAcName;   // 网格提供者
    private Date createTime;     // 创建时间
    private Date updateTime;     // 更新时间
    private Short isDel;         // 是否删除
    private Short runStatus;     // 运行状态

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNetAccount() {
        return netAccount;
    }

    public void setNetAccount(String netAccount) {
        this.netAccount = netAccount;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getNetPassword() {
        return netPassword;
    }

    public void setNetPassword(String netPassword) {
        this.netPassword = netPassword;
    }

    public String getWlanUserIp() {
        return wlanUserIp;
    }

    public void setWlanUserIp(String wlanUserIp) {
        this.wlanUserIp = wlanUserIp;
    }

    public String getWlanUserMac() {
        return wlanUserMac;
    }

    public void setWlanUserMac(String wlanUserMac) {
        this.wlanUserMac = wlanUserMac;
    }

    public String getWlanAcIp() {
        return wlanAcIp;
    }

    public void setWlanAcIp(String wlanAcIp) {
        this.wlanAcIp = wlanAcIp;
    }

    public String getWlanAcName() {
        return wlanAcName;
    }

    public void setWlanAcName(String wlanAcName) {
        this.wlanAcName = wlanAcName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Short getIsDel() {
        return isDel;
    }

    public void setIsDel(Short isDel) {
        this.isDel = isDel;
    }

    public Short getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(Short runStatus) {
        this.runStatus = runStatus;
    }
}

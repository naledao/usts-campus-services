package hhsc.kangnasi.xyz.ustscampusservices.domain.entity;

import java.util.Date;

public class SysUserEntity {
    private String email;       // 登录账号
    private String phoneNumber;
    private String nickName;    // 昵称
    private Date createTime;    // 创建时间
    private Date updateTime;    // 更新时间
    private Integer isDel;      // 是否删除;

    // Getter & Setter
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    public Integer getIsDel() { return isDel; }
    public void setIsDel(Integer isDel) { this.isDel = isDel; }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}

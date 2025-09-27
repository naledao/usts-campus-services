package hhsc.kangnasi.xyz.ustscampusservices.domain.vo;

public class CommonServiceVo {
    private String tag;  //服务标签
    private String name; //服务名称
    private String runStatus; //运行状态
    private String runningTime; //运行总时间

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    public String getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(String runningTime) {
        this.runningTime = runningTime;
    }
}

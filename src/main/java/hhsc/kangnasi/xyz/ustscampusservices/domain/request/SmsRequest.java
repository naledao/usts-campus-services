package hhsc.kangnasi.xyz.ustscampusservices.domain.request;

import java.util.List;

public class SmsRequest {
    private String mobiles; // 对应 JSON 中的数组 ["..."]
    private String message;       // 对应 JSON 中的字符串 "..."

    // 构造方法
    public SmsRequest(String mobiles, String message) {
        this.mobiles = mobiles;
        this.message = message;
    }

    public String getMobiles() { return mobiles; }
    public void setMobiles(String mobiles) { this.mobiles = mobiles; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

package com.example.broadcasttest.Bean;

public class DataStructure {
    //协议类型
    private String broadType;
    //GSM开关
    private String powerSet;
    //上报频率
    private String repFre;
    //电量阈值
    private String powerAlarm;
    //移动信号阈值
    private String signAlarm;

    public DataStructure(String broadType, String powerSet, String repFre, String powerAlarm, String signAlarm) {
        this.broadType = broadType;
        this.powerSet = powerSet;
        this.repFre = repFre;
        this.powerAlarm = powerAlarm;
        this.signAlarm = signAlarm;
    }

    public String getBroadType() {
        return broadType;
    }

    public void setBroadType(String broadType) {
        this.broadType = broadType;
    }

    public String getPowerSet() {
        return powerSet;
    }

    public void setPowerSet(String powerSet) {
        this.powerSet = powerSet;
    }

    public String getRepFre() {
        return repFre;
    }

    public void setRepFre(String repFre) {
        this.repFre = repFre;
    }

    public String getPowerAlarm() {
        return powerAlarm;
    }

    public void setPowerAlarm(String powerAlarm) {
        this.powerAlarm = powerAlarm;
    }

    public String getSignAlarm() {
        return signAlarm;
    }

    public void setSignAlarm(String signAlarm) {
        this.signAlarm = signAlarm;
    }
}

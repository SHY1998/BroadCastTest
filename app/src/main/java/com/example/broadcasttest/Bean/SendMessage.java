package com.example.broadcasttest.Bean;

public class SendMessage {
    //设备名
    private String mac;
    //数据编号
    private int crNum;
    //命令类型
    private int comType;
    //数据段
    private DataStructure dataStructure;

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getCrNum() {
        return crNum;
    }

    public void setCrNum(int crNum) {
        this.crNum = crNum;
    }

    public int getComType() {
        return comType;
    }

    public void setComType(int comType) {
        this.comType = comType;
    }

    public DataStructure getDataStructure() {
        return dataStructure;
    }

    public void setDataStructure(DataStructure dataStructure) {
        this.dataStructure = dataStructure;
    }

    public SendMessage(String mac, int crNum, int comType, DataStructure dataStructure) {
        this.mac = mac ;
        this.crNum = crNum % 0XFF;
        this.comType = comType;
        this.dataStructure = dataStructure;
    }
}



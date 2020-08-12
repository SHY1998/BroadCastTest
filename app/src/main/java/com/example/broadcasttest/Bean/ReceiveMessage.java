package com.example.broadcasttest.Bean;

import android.util.Log;

import com.example.broadcasttest.util.BlueToothUtil;
import com.example.broadcasttest.util.BlueToothUtil;

public class ReceiveMessage {
    //设备mac
    private String mac;
    //数据编号
    private int curNum;
    //命令类型
    private int comType;
    //开关机
    private String powerSet;
    //协议类型
    private String broadType;
    //上报频率
    private String repFre;
    //电量阈值
    private String powerAlarm;
    //移动信号告警阈值
    private String signAlarm;
    //版本号长度
    private String editLen;
    //版本号
    private String edition;
    //执行结果
    private int exeResult;
    //电池电量
    private String battery;


    public int getExeResult() {
        return exeResult;
    }

    public void setExeResult(int exeResult) {
        this.exeResult = exeResult;
    }

    public String getEditLen() {
        return editLen;
    }

    public void setEditLen(String editLen) {
        this.editLen = editLen;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getCurNum() {
        return curNum;
    }

    public void setCurNum(int curNum) {
        this.curNum = curNum;
    }

    public int getComType() {
        return comType;
    }

    public void setComType(int comType) {
        this.comType = comType;
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

    public String getBroadType() {
        return broadType;
    }

    public void setBroadType(String broadType) {
        this.broadType = broadType;
    }

    @Override
    public String toString() {
        return "ReceiveMessage{" +
                "mac='" + mac + '\'' +
                ", curNum=" + curNum +
                ", comType=" + comType +
                ", powerSet='" + powerSet + '\'' +
                ", broadType='" + broadType + '\'' +
                ", repFre='" + repFre + '\'' +
                ", powerAlarm='" + powerAlarm + '\'' +
                ", signAlarm='" + signAlarm + '\'' +
                ", editLen='" + editLen + '\'' +
                ", edition='" + edition + '\'' +
                ", exeResult=" + exeResult +
                ", battery='" + battery + '\'' +
                '}';
    }

    public ReceiveMessage(String msg) {
        msg = msg.replace(" ","");
        //第一部分长度
        int fstLen = Integer.parseInt(msg.substring(0,2),16)+1;
        //第一部分数据
        String fstData =  msg.substring(0,fstLen*2);
        //第二部分长度
        int secLen = Integer.parseInt(msg.substring(fstLen*2,fstLen*2+2),16)+1;
        String secData = msg.substring(fstLen*2,fstLen*2+secLen*2);
        //第二部分数据
        Log.d("213", "解析中" + msg.substring(4,28));
        Log.d("213", "secData: " + secData);
        if (secLen >= 5) {

            this.mac = BlueToothUtil.hexStringToString(fstData.substring(4,fstLen*2));
            //数据编号
            this.curNum = Integer.parseInt(secData.substring(4,6),16);
            //命令类型
            this.comType = Integer.parseInt(secData.substring(6,8),16);
            //执行结果
            this.exeResult = Integer.parseInt(secData.substring(8,10),16);
            //最后数据段
            String lastData = secData.substring(10);
            Log.d("asd", "lastData " + lastData);
            if (this.exeResult == 0) {
                if (!lastData.equals("")){
                    switch (this.comType) {
                        case 1:
                            break;
                        case 2:
                            switch (Integer.parseInt(lastData.substring(0,2),16)) {
                                case 0:
                                    this.broadType = "MQTT";
                                    break;
                                case 1:
                                    this.broadType = "JTT808";
                                    break;
                                default:
                                    this.broadType = "MQTT";
                                    break;
                            }
                            switch (Integer.parseInt(lastData.substring(2,4),16)) {
                                case 0:
                                    this.powerSet = "关机";
                                    break;
                                case 1:
                                    this.powerSet = "开机";
                                    break;
                                default:
                                    break;
                            }
                            this.repFre = String.valueOf(Integer.parseInt(lastData.substring(4,8),16));
                            this.powerAlarm = String.valueOf(Integer.parseInt(lastData.substring(8,10),16));
                            this.signAlarm = String.valueOf(Integer.parseInt(lastData.substring(10,12),16));
                            break;
                        case 3:
                            this.editLen = lastData.substring(0,2);
                            this.edition = BlueToothUtil.hexStringToString(lastData.substring(2));
                            break;
                        case 4:
                            this.battery = String.valueOf(Integer.parseInt(lastData,16));
                            break;
                        case 0XFF:
                            break;
                        default:
                            break;
                    }
                }
            }
        }



    }

    public ReceiveMessage() {
    }
}

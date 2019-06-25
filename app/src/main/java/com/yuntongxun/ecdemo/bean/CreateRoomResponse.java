package com.yuntongxun.ecdemo.bean;

/**
 * Created by luhuashan on 17/5/16.
 */
public class CreateRoomResponse {



    public String statusCode;
    public String statusMsg;
    public String roomId;

    public String getStatusCode(){
        return statusCode;
    }

    public void setStatusCode(String statusCode){
        this.statusCode = statusCode;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}

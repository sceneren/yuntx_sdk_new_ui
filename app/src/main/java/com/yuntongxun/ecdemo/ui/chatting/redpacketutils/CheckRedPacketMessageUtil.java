package com.yuntongxun.ecdemo.ui.chatting.redpacketutils;

import android.util.Log;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecsdk.ECMessage;

import utils.RedPacketConstant;

public class CheckRedPacketMessageUtil {


    public static JSONObject isRedPacketMessage(ECMessage message) {
        JSONObject rpJSON = null;
        if (message.getType() == ECMessage.Type.TXT) {
            // 设置内容
            String extraData = message.getUserData();
            if (extraData != null) {
                try {
                    JSONObject jsonObject = JSONObject.parseObject(extraData);
                    if (jsonObject != null && jsonObject.containsKey(RedPacketConstant.MESSAGE_ATTR_IS_RED_PACKET_MESSAGE)
                            && jsonObject.getBoolean(RedPacketConstant.MESSAGE_ATTR_IS_RED_PACKET_MESSAGE)) {
                        rpJSON = jsonObject;
                    }
                } catch (JSONException e) {
                    Log.e("JSONException", e.toString());
                }
            }
        }
        return rpJSON;
    }

    public static JSONObject isRedPacketAckMessage(ECMessage message) {
        JSONObject jsonRedPacketAck = null;
        if (message.getType() == ECMessage.Type.TXT) {
            // 设置内容
            String extraData = message.getUserData();
            if (extraData != null) {
                try {
                    JSONObject jsonObject = JSONObject.parseObject(extraData);
                    if (jsonObject != null && jsonObject.containsKey(RedPacketConstant.MESSAGE_ATTR_IS_RED_PACKET_ACK_MESSAGE) && jsonObject.getBoolean(RedPacketConstant.MESSAGE_ATTR_IS_RED_PACKET_ACK_MESSAGE)) {
                        jsonRedPacketAck = jsonObject;
                    }
                } catch (JSONException e) {
                    Log.e("JSONException", e.toString());
                }
            }
        }
        return jsonRedPacketAck;
    }
    public static boolean isRedAckMessage(ECMessage message) {
        boolean is =false;
        if (message.getType() == ECMessage.Type.TXT) {
            // 设置内容
            String extraData = message.getUserData();
            if (extraData != null) {
                try {
                    JSONObject jsonObject = JSONObject.parseObject(extraData);
                    if (jsonObject != null && jsonObject.containsKey(RedPacketConstant.MESSAGE_ATTR_IS_RED_PACKET_ACK_MESSAGE) && jsonObject.getBoolean(RedPacketConstant.MESSAGE_ATTR_IS_RED_PACKET_ACK_MESSAGE)) {
                        is=true;
                        return is;
                    }
                } catch (JSONException e) {

                }
            }
        }
        return is;
    }
    public static boolean isRedAckMessage2(ECMessage message) {
        boolean is =false;
        if (message.getType() == ECMessage.Type.TXT) {
            // 设置内容
            String extraData = message.getUserData();
            if (extraData != null) {
                try {
                    JSONObject jsonObject = JSONObject.parseObject(extraData);
                    if (jsonObject != null && jsonObject.containsKey(RedPacketConstant.MESSAGE_ATTR_IS_RED_PACKET_ACK_MESSAGE) && jsonObject.getBoolean(RedPacketConstant.MESSAGE_ATTR_IS_RED_PACKET_ACK_MESSAGE)) {
                           String a = jsonObject.getString(RedPacketConstant.EXTRA_RED_PACKET_SENDER_ID);
                           String b = jsonObject.getString(RedPacketConstant.EXTRA_RED_PACKET_RECEIVER_ID);
                        if(!CCPAppManager.getClientUser().getUserId().equalsIgnoreCase(a)&&!CCPAppManager.getClientUser().getUserId().equalsIgnoreCase(b)){
                            is =true;
                        }
                        return  is;

                    }
                } catch (JSONException e) {

                }
            }
        }
        return is;
    }
    public static boolean isRedAckSelfMessage(ECMessage message) {
        boolean is =false;
        if (message.getType() == ECMessage.Type.TXT) {
            // 设置内容
            String extraData = message.getUserData();
            if (extraData != null) {
                try {
                    JSONObject jsonObject = JSONObject.parseObject(extraData);
                    if (jsonObject != null && jsonObject.containsKey(RedPacketConstant.MESSAGE_ATTR_IS_RED_PACKET_ACK_MESSAGE) && jsonObject.getBoolean(RedPacketConstant.MESSAGE_ATTR_IS_RED_PACKET_ACK_MESSAGE)) {
                        return is;
                    }
                } catch (JSONException e) {

                }
            }
        }
        return is;
    }
    public static boolean isRedPacketAckOtherMessage(ECMessage message) {
        boolean isToSelf =false;
        String  userId ="";
        JSONObject jsonRedPacketAck = null;
        if (message.getType() == ECMessage.Type.TXT) {
            // 设置内容
            String extraData = message.getUserData();
            if (extraData != null) {
                try {
                    JSONObject jsonObject = JSONObject.parseObject(extraData);
                    if (jsonObject != null && jsonObject.containsKey(RedPacketConstant.MESSAGE_ATTR_IS_RED_PACKET_ACK_MESSAGE) && jsonObject.getBoolean(RedPacketConstant.MESSAGE_ATTR_IS_RED_PACKET_ACK_MESSAGE)) {
                        jsonRedPacketAck = jsonObject;
                    }
                    if(jsonRedPacketAck!=null&&jsonRedPacketAck.containsKey(RedPacketConstant.EXTRA_RED_PACKET_SENDER_ID)){
                        userId =jsonRedPacketAck.getString(RedPacketConstant.EXTRA_RED_PACKET_SENDER_ID);
                    }
                    if(CCPAppManager.getClientUser().getUserId().equalsIgnoreCase(userId)){
                        isToSelf =true;
                    }
                } catch (JSONException e) {
                    return  false;
                }
            }
        }
        return isToSelf;
    }

    public static boolean isMyAckMessage(ECMessage message) {
        boolean isMyselfAckMsg = false;
        JSONObject jsonObject = isRedPacketAckMessage(message);
        if (jsonObject != null) {
            String receiverId = jsonObject.getString(RedPacketConstant.EXTRA_RED_PACKET_RECEIVER_ID);//红包接收者id
            String senderId = jsonObject.getString(RedPacketConstant.EXTRA_RED_PACKET_SENDER_ID);//红包发送者id
            //发送者和领取者都不是自己
            if (receiverId.equalsIgnoreCase(senderId)&&!receiverId.equalsIgnoreCase(CCPAppManager.getClientUser().getUserId())) {
                isMyselfAckMsg = true;
            }
        }
        return isMyselfAckMsg;
    }
    public static boolean isGroupAckMessage(ECMessage message) {
        boolean isMyselfAckMsg = false;
        JSONObject jsonObject = isRedPacketAckMessage(message);
        if (jsonObject != null) {
            String receiverId = jsonObject.getString(RedPacketConstant.EXTRA_RED_PACKET_RECEIVER_ID);//红包接收者id
            String senderId = jsonObject.getString(RedPacketConstant.EXTRA_RED_PACKET_SENDER_ID);//红包发送者id
            //发送者和领取者都不是自己
            if (isPeerChat(message)&&receiverId.equalsIgnoreCase(senderId)&&receiverId.equalsIgnoreCase(CCPAppManager.getClientUser().getUserId())) {
                isMyselfAckMsg = true;
            }
        }
        return isMyselfAckMsg;
    }

    public static boolean isPeerChat(ECMessage msg) {
        return msg != null && msg.getSessionId().toLowerCase().startsWith("g");
    }


}

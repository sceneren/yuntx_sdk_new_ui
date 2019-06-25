package com.yuntongxun.ecdemo.pojo;

import android.support.annotation.NonNull;

/**
 * Created by luhuashan on 17/8/25.
 * email huashan2007@sina.cn
 */
public class FriendMessage implements Comparable<FriendMessage>{



          private String   friendUseracc;//好友账号
          private String     message;//请求的信息
          private String  source;//好友来源
          private String        isInvited;//0：邀请1：被邀请
          private String    dealState;//0：非好友 1:好友


    public String getFriendUseracc() {
        return friendUseracc;
    }

    public void setFriendUseracc(String friendUseracc) {
        this.friendUseracc = friendUseracc;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getIsInvited() {
        return isInvited;
    }

    public void setIsInvited(String isInvited) {
        this.isInvited = isInvited;
    }

    public String getDealState() {
        return dealState;
    }

    public void setDealState(String dealState) {
        this.dealState = dealState;
    }


    @Override
    public int compareTo(@NonNull FriendMessage friendMessage) {

        if(this==friendMessage){
            return 0;
        }
        int state  = Integer.parseInt(this.getDealState());
        int invite  = Integer.parseInt(this.getIsInvited());

        int state2  = Integer.parseInt(friendMessage.getDealState());
        int invite2  = Integer.parseInt(friendMessage.getIsInvited());

        if(state>state2){
            return 1;
        }else if(state<state2){
            return -1;
        }
         else if(state==state2){
            if(invite>invite2){
                return -1;
            }else if(invite==invite2){
                return 0;
            }else {
                return 1;
            }
        }
        return 0;
    }
}

package com.yuntongxun.ecdemo.pojo;

import com.yuntongxun.ecsdk.PersonInfo;

/**
 * Created by luhuashan on 17/8/18.
 * email huashan2007@sina.cn
 */
public class Friend {

    private String useracc;  //好友账号
    private String remarkName;//备注名
    private String nickName;//昵称
    private String avatar;//头像
    private String friendState;//0:非好友 1:好友
    private String sign;
    private PersonInfo.Sex sex;


    public boolean isFriendLy() {
        return "1".equalsIgnoreCase(this.friendState);
    }

    public PersonInfo.Sex getSex() {
        return sex;
    }

    public void setSex(PersonInfo.Sex sex) {
        this.sex = sex;
    }

    private String age;

    public String getUseracc() {
        return useracc;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public void setUseracc(String useracc) {
        this.useracc = useracc;
    }


    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getRemarkName() {
        return remarkName;
    }

    public void setRemarkName(String remarkName) {
        this.remarkName = remarkName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getFriendState() {
        return friendState;
    }

    public void setFriendState(String friendState) {
        this.friendState = friendState;
    }
}

package com.yuntongxun.ecdemo.ui.videomeeting;

import com.yuntongxun.ecsdk.meeting.ECVoiceMeetingMember;

/**
 * Created by smileklvens on 2017/9/14.
 * 存储时间
 */

public class CommonECMeetingMember extends ECVoiceMeetingMember {

    private  String time ;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

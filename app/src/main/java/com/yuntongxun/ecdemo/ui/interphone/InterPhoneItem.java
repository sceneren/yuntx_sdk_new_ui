package com.yuntongxun.ecdemo.ui.interphone;

import android.view.View;
import android.widget.TextView;

import com.yuntongxun.ecdemo.AvatorUtil;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecsdk.meeting.intercom.ECInterPhoneMeetingMember;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 处理实时对讲成员状态图标显示
 * 以及加入、未加入、正在说话等状态
 * com.yuntongxun.ecdemo.ui.interphone in ECDemo_Android
 * Created by Jorstin on 2015/7/16.
 */
public class InterPhoneItem {

    /**
     * 实时对讲加入成员状态
     */
    private TextView inter_phone_time;
    /**
     * 实时对讲加入成员昵称
     */
    private TextView mUsernameView;
    /**
     * 实时对讲成员状态描述
     */
    private TextView mUserActionView;

    public InterPhoneItem(View view) {
        initView(view);
    }

    /**
     * 初始化界面资源
     */
    private void initView(View view) {
        inter_phone_time = (TextView) view.findViewById(R.id.inter_phone_time);
        mUsernameView = (TextView) view.findViewById(R.id.name);
        mUserActionView = (TextView) view.findViewById(R.id.action_tips);
    }

    /**
     * 设置实时对讲成员信息
     *
     * @param member 实时对讲成员信息
     */
//    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setInterMember(ECInterPhoneMeetingMember member) {
        if (member == null || member.getMember() == null) {
            return;
        }
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        String t = format.format(new Date());
        inter_phone_time.setVisibility(View.GONE);

        mUsernameView.setText(AvatorUtil.getInstance().getMarkName(member.getMember()));
        // 如果实时对讲成员不在线，则不处理控麦等状态
        if (setInterMemberOnline(member.getOnline())) {
            // 处理控麦等状态
            setInterMemberMic(member.getMic());
        }

    }

    /**
     * 初始化实时对讲在线加入状态
     *
     * @param online 实时对讲成员加入状态
     */
    private boolean setInterMemberOnline(ECInterPhoneMeetingMember.Online online) {
        if (online != ECInterPhoneMeetingMember.Online.ONLINE) {
            // 实时对讲成员状态显示未加入
            mUserActionView.setText(R.string.str_join_wait);
            return false;
        }
        // 实时对讲成员在线
        return true;
    }

    /**
     * 设置实时对讲成员控麦状态
     *
     * @param mic 实时对讲成员控麦状态
     */
    private void setInterMemberMic(ECInterPhoneMeetingMember.Mic mic) {
        if (mic == ECInterPhoneMeetingMember.Mic.MIC_CONTROLLER) {
            // 实时对讲成员控麦
            mUserActionView.setText(R.string.str_join_speaking);
            return;
        }
        mUserActionView.setText(R.string.str_join_success);
    }
}

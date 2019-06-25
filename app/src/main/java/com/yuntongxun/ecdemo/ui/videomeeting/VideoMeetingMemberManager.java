package com.yuntongxun.ecdemo.ui.videomeeting;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECMeetingManager;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.meeting.ECVideoMeetingMember;
import com.yuntongxun.ecsdk.meeting.voice.ECVoiceMeetingMsg;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.yuntongxun.ecdemo.ui.videomeeting.MultiVideoconference.VIDEOCONFERENCEID;

/**
 * 视屏会议成员管理界面
 * Created by Jorstin on 2015/7/26.
 */
public class VideoMeetingMemberManager extends VideoconferenceBaseActivity implements
        View.OnClickListener {

    private static final String TAG = "ECSDK_Demo.VoiceMeetingMemberManager";

    private ListView mListView;
    /**
     * 会议成员列表
     */
    private MeetingMemberAdapter mListAdapter;
    /**
     * 是否移除过成员
     */
    private boolean mRemoveMember;
    /**
     * 会议成员
     */
    private List<MultiVideoMember> mulitMembers;

    private String mVideoConferenceId;
    private String creator;

    @Override
    protected int getLayoutId() {
        return R.layout.voice_meeting_members;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVideoConferenceId = getIntent().getStringExtra(VIDEOCONFERENCEID);
        creator = getIntent().getStringExtra(VideoconferenceConversation.CONFERENCE_CREATOR);

        if (mVideoConferenceId == null) {
            finish();
            return;
        }
        getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt, -1, R.string.meeting_member_mgr_title, this);
        initView();
        // 查询会议成员
        queryMeetingMembers(mVideoConferenceId);
    }

    private void queryMeetingMembers(String meetingNo) {
        // 获取一个会议管理接口对象
        ECMeetingManager meetingManager = ECDevice.getECMeetingManager();
// 发起查询视频会议成员请求

        meetingManager.queryMeetingMembersByType(meetingNo,
                ECMeetingManager.ECMeetingType.MEETING_MULTI_VIDEO,
                new ECMeetingManager.OnQueryMeetingMembersListener<ECVideoMeetingMember>() {
                    @Override
                    public void onQueryMeetingMembers(ECError reason, List<ECVideoMeetingMember> members) {
                        if (SdkErrorCode.REQUEST_SUCCESS == reason.errorCode) {

                            if (mulitMembers == null) {
                                mulitMembers = new ArrayList<MultiVideoMember>();
                            }
                            mulitMembers.clear();

                            if (members == null || members.size() <= 0) {
                                return;
                            }

                            ArrayList<ECVideoMeetingMember> membersNew = (ArrayList<ECVideoMeetingMember>) members;
                            for (ECVideoMeetingMember member : membersNew) {
                                MultiVideoMember mulitMember = new MultiVideoMember(
                                        member);
                                final boolean ismobile = ((member.getPort()==0));
                                if(ismobile){
                                    mulitMember.setNumber("m"+member.getNumber());

                                }
                                mulitMembers.add(mulitMember);
                            }

                            Iterator<MultiVideoMember> iterator = mulitMembers.iterator();
                            while (iterator.hasNext()) {
                                MultiVideoMember next = iterator.next();
                                if (TextUtils.equals(creator, next.getNumber())) {
                                    iterator.remove();
                                }
                            }
                            mListAdapter.setMembers(mulitMembers);
                            mListAdapter.notifyDataSetChanged();
                        } else {
                            ToastUtil.showMessage("weew");
                        }

                    }
                });
    }


    /**
     * 初始化界面资源
     */

    private void initView() {
        mListView = (ListView) findViewById(R.id.meeting_member_lv);
        View emptyView = findViewById(R.id.empty_tip_recommend_bind_tv);
        mListAdapter = new MeetingMemberAdapter(this);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemLongClickListener(null);
        mListView.setEmptyView(emptyView);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_left:
                hideSoftKeyboard();
                setResultOk();
                break;
        }
    }

    /**
     * 设置返回更新
     */
    private void setResultOk() {
//        Intent intent = new Intent(VideoMeetingMemberManager.this, MultiVideoconference.class);
//        intent.putExtra("isKicked", mRemoveMember);
//        intent.putParcelableArrayListExtra("removedmembers",removedMembers);
//        setResult(RESULT_OK, intent);

        EventBus.getDefault().post(new EventRemovedMembers(removedMembers));
        this.finish();
    }

    /**
     * 处理移除会议成员操作
     *
     * @param position 成员所在列表位置
     */

    private ArrayList<MultiVideoMember> removedMembers = new ArrayList<MultiVideoMember>();

    private void doRemoveMeetingMember(final int position) {
        ECMeetingManager meetingManager = ECDevice.getECMeetingManager();
        if (meetingManager == null) {
            return;
        }
        if (mListAdapter != null) {
            final MultiVideoMember meetingMember = mListAdapter.getItem(position);

            if (meetingMember == null) {
                return;
            }


            showProcessDialog();

            final boolean ismobile = ((meetingMember.getPort()==0));


            String mem = meetingMember.getNumber();
            if(mem.startsWith("m")){
                mem = mem.substring(1,mem.length());
            }
            meetingManager.removeMemberFromMultiMeetingByType(ECMeetingManager.ECMeetingType.MEETING_MULTI_VIDEO,
                    mVideoConferenceId, mem, ismobile, new ECMeetingManager.OnRemoveMemberFromMeetingListener() {
                        @Override
                        public void onRemoveMemberFromMeeting(ECError reason, String member) {
                            dismissPostingDialog();
                            if (SdkErrorCode.REQUEST_SUCCESS == reason.errorCode) {
                                if (mulitMembers == null) {
                                    return;
                                }
                                if(ismobile){
                                    meetingMember.setNumber("m"+meetingMember.getNumber());
                                }
                                removedMembers.add(meetingMember);

                                mulitMembers.remove(position);
                                if (mListAdapter != null) {
                                    mListAdapter.setMembers(mulitMembers);
                                    mListAdapter.notifyDataSetChanged();
                                }
                                return;
                            }
                            ToastUtil.showMessage("移除会议成员失败[" + reason.errorCode + "]");
                        }
                    });
        }
    }

    public class MeetingMemberAdapter extends ArrayAdapter<MultiVideoMember> {

        public MeetingMemberAdapter(Context context) {
            super(context, 0, new ArrayList<MultiVideoMember>());
        }

        public void setMembers(List<MultiVideoMember> members) {
            clear();
            if (members != null) {
                for (MultiVideoMember member : members) {
                    super.add((MultiVideoMember) member);
                }
            }
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder mViewHolder;
            if (convertView == null || convertView.getTag() == null) {
                view = View.inflate(getContext(), R.layout.meeting_member_item, null);

                mViewHolder = new ViewHolder();
                mViewHolder.mAvatar = (TextView) view.findViewById(R.id.tv_icon);
                mViewHolder.mNikeName = (TextView) view.findViewById(R.id.meeting_contact_item_nick_tv);
                mViewHolder.mPermission = (TextView) view.findViewById(R.id.meeting_contact_item_digest_tv);
                mViewHolder.chatroom_contact_del_btn = (Button) view.findViewById(R.id.chatroom_contact_del_btn);

                view.setTag(mViewHolder);
            } else {
                view = convertView;
                mViewHolder = (ViewHolder) view.getTag();
            }
            final MultiVideoMember item = getItem(position);
            if (item != null) {

                mViewHolder.mAvatar.setText(item.getNumber());
                if (item.isMobile()) {
                    mViewHolder.mNikeName.setText("m" + item.getNumber());
                } else {
                    mViewHolder.mNikeName.setText(item.getNumber());
                }

                mViewHolder.mPermission.setText(item.getNumber());


                final ECVoiceMeetingMsg.ForbidOptions options = item.getForbid();


                mViewHolder.chatroom_contact_del_btn.setVisibility(View.VISIBLE);
                mViewHolder.chatroom_contact_del_btn.setText("删除");
                mViewHolder.chatroom_contact_del_btn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        doRemoveMeetingMember(position);
                    }
                });
            }
            return view;

        }


        class ViewHolder {
            TextView mAvatar;
            TextView mNikeName;
            TextView mPermission;
            Button chatroom_contact_del_btn;
        }
    }
}

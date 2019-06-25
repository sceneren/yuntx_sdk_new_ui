package com.yuntongxun.ecdemo.ui.livechatroom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yuntongxun.ecdemo.ECApplication;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.dialog.ECAlertDialog;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.common.view.DivergeView;
import com.yuntongxun.ecdemo.ui.ECSuperActivity;
import com.yuntongxun.ecdemo.ui.SDKCoreHelper;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECLiveCallBack;
import com.yuntongxun.ecsdk.ECLiveCallBack.OnSendLiveChatRoomMessageListener;
import com.yuntongxun.ecsdk.ECLiveChatRoom;
import com.yuntongxun.ecsdk.ECLiveChatRoomManager;
import com.yuntongxun.ecsdk.ECLiveChatRoomMember;
import com.yuntongxun.ecsdk.ECLiveChatRoomMemberInfoBuilder;
import com.yuntongxun.ecsdk.ECLiveChatRoomModifyBuilder;
import com.yuntongxun.ecsdk.ECLiveControlOption;
import com.yuntongxun.ecsdk.ECLiveEnums;
import com.yuntongxun.ecsdk.ECLiveNotifyWrapper;
import com.yuntongxun.ecsdk.ECLiveSearchMembersConditionBuilder;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.OnLiveChatRoomListener;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.im.ECTextMessageBody;
import com.yuntongxun.ecsdk.livechatroom.ECLiveChatRoomNotification;

import java.util.ArrayList;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.VideoView;

/**
 * Created by luhuashan on 17/5/19.
 * 直播
 */
public class LiveChatUI extends ECSuperActivity implements View.OnClickListener, LiveEditText.LiveInputListener, TestDialog.onTestListener, LIveFragment.LWListener, FragmentGiftDialog.OnGridViewClickListener {


    private static final String TAG = "LiveChatUI";
    private ArrayList<Bitmap> mList;

    private DivergeView mDivergeView;

    private LIveFragment fragment;

    private RecyclerView hRecyclerView;
    private RecyclerView lv;

    private int mIndex = 0;

    private ImageView iv;

    private TextView tvOnline;

    private LinkedList<ECMessage> linkedList = new LinkedList<ECMessage>();
    private LinkedList<ECLiveChatRoomMember> membersList = new LinkedList<ECLiveChatRoomMember>();

    private RelativeLayout tvCreate;

//    private String path = "";


    BSRGiftView imageButton;
    BSRGiftLayout giftLayout;
    GiftAnmManager giftAnmManager;


    @Override
    protected boolean isEnableSwipe() {
        return false;
    }

    private TextView tvDesc;

    @Override
    protected int getLayoutId() {
        return R.layout.live_chat_ui;
    }


    @Override
    public int getTitleLayout() {
        return -1;
    }

    @Override
    public void onClick(View v) {


        switch (v.getId()) {

            case R.id.btn_close:

                handleLogout();
                break;
            case R.id.tv_modify:

                break;


        }
    }


    private void showAlertDialog() {
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setView(LayoutInflater.from(this).inflate(R.layout.alert_dialog_ui, null));
        dialog.show();
        dialog.getWindow().setContentView(R.layout.alert_dialog_ui);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        Button btnPositive = (Button) dialog.findViewById(R.id.btn_add);
        Button btnNegative = (Button) dialog.findViewById(R.id.btn_cancel);
        final EditText etContent = (EditText) dialog.findViewById(R.id.et_content);
        final EditText etContentLast = (EditText) dialog.findViewById(R.id.et_content_last);
        btnPositive.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String str = etContent.getText().toString();
                String strLast = etContentLast.getText().toString();
                if (isNullEmptyBlank(str) || isNullEmptyBlank(strLast)) {

                    ToastUtil.showMessage("输入内容为空");

                } else {
                    dialog.dismiss();

                    ECLiveChatRoomMemberInfoBuilder modifySelfInfo = new ECLiveChatRoomMemberInfoBuilder(str, strLast);

                    ECLiveChatRoomManager roomManager = ECDevice.getECLiveChatRoomManager();
                    if (roomManager == null) {
                        return;
                    }

                    roomManager.modifyLiveChatRoomSelfInfo(room.roomId, modifySelfInfo, new ECLiveCallBack.OnUpdateSelfInfoListener() {
                        @Override
                        public void onResult(ECError ecError) {

                            showToast(ecError);
                        }
                    });


                }
            }
        });


        btnNegative.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });
    }

    private static boolean isNullEmptyBlank(String str) {
        if (str == null || "".equals(str) || "".equals(str.trim())) {
            return true;
        }
        return false;
    }


    private void handleLogout() {


        ECAlertDialog buildAlert = ECAlertDialog.buildAlert(this, R.string.settings_logout_warning_tip_live, null, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ECLiveChatRoomManager roomManager = ECDevice.getECLiveChatRoomManager();
                if (roomManager == null) {
                    return;
                }
                roomManager.exitLiveChatRoom(room.roomId, SDKCoreHelper.buildNotify(), new ECLiveCallBack.OnExitLiveChatRoomListener() {
                    @Override
                    public void onResult(ECError ecError) {
                        if (ecError.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
//                            path=null;
                            finish();
                        } else {
                            ToastUtil.showMessage("退出失败:" + ecError.errorCode);
                        }
                    }
                });
            }
        });
        buildAlert.setTitle(R.string.settings_logout);
        buildAlert.show();


    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            if (iv_aixin != null) {
                iv_aixin.setVisibility(View.GONE);
            }

        }
    };

    @Override
    public void onSendClick(String text) {
        if (text == null) {
            return;
        }
        if (text.toString().trim().length() <= 0) {
            return;
        }

        if(text.length()>=2048){
            ToastUtil.showMessage("您当前输入的文字长度超出了最大限制");
            return;
        }

        final ECMessage msg = ECMessage.createECMessage(ECMessage.Type.TXT);
        msg.setFrom(CCPAppManager.getUserId());

        msg.setTo(room.roomId);
        ECTextMessageBody msgBody = new ECTextMessageBody(text.toString());
        msg.setBody(msgBody);

        ECLiveChatRoomManager roomManager = ECDevice.getECLiveChatRoomManager();
        if (roomManager == null) {
            return;
        }
        roomManager.sendLiveChatRoomMessage(msg, new OnSendLiveChatRoomMessageListener() {
            @Override
            public void onSendResult(ECError error, ECMessage message) {
                if (error.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
                    linkedList.add(msg);
                    lv.getLayoutManager().smoothScrollToPosition(lv, null, linkedList.size() - 1);
                    chatAdapter.notifyDataSetChanged();
                } else if (error.errorCode == 620008) {
                    msg.setVersion(620008);
                    chatAdapter.notifyDataSetChanged();
                }
            }
        });
    }


    public void sendLWMsg(String text, String data) {
        if (text == null) {
            return;
        }
        if (text.toString().trim().length() <= 0) {
            return;
        }
        final ECMessage msg = ECMessage.createECMessage(ECMessage.Type.TXT);
        msg.setFrom(CCPAppManager.getUserId());

        msg.setTo(room.roomId);
        ECTextMessageBody msgBody = new ECTextMessageBody(text.toString());

        msg.setUserData(data);

        msg.setBody(msgBody);

        ECLiveChatRoomManager roomManager = ECDevice.getECLiveChatRoomManager();
        if (roomManager == null) {
            return;
        }
        roomManager.sendLiveChatRoomMessage(msg, new OnSendLiveChatRoomMessageListener() {
            @Override
            public void onSendResult(ECError error, ECMessage message) {
                if (error.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
                    linkedList.add(msg);
                    lv.getLayoutManager().smoothScrollToPosition(lv, null, linkedList.size() - 1);
                    chatAdapter.notifyDataSetChanged();
                } else if (error.errorCode == 620008) {
                    msg.setVersion(620008);
                    chatAdapter.notifyDataSetChanged();
                }
            }
        });


    }


    @Override
    public void onTestListener(int uniqueIdentifier, String strName, String strHigh, String ext, final boolean isMute) {

        isCheck = isMute;
        this.ext = ext;
        mstrName = strName;
        mstrHigh = strHigh;

        ECLiveEnums.ECAllMuteMode mode = isMute ? ECLiveEnums.ECAllMuteMode.ECAllMuteMode_ON : ECLiveEnums.ECAllMuteMode.ECAllMuteMode_OFF;

        ECLiveChatRoomModifyBuilder builder = new ECLiveChatRoomModifyBuilder(room.roomId, strName, strHigh, ext, mode);

        ECLiveChatRoomManager roomManager = ECDevice.getECLiveChatRoomManager();
        if (roomManager == null) {
            return;
        }

        roomManager.modifyLiveChatRoomInfo(builder, SDKCoreHelper.buildNotify(), new ECLiveCallBack.OnUpdateLiveChatRoomListener() {
            @Override
            public void onResult(ECError ecError) {

                if(ecError.errorCode==SdkErrorCode.REQUEST_SUCCESS){
                    mIsMute = !mIsMute;
                }
                showToast(ecError);
            }
        });


    }

    private FragmentGiftDialog editNameDialog;

    @Override
    public void onClickCallBack() {
        LogUtil.e("onClickCallBack");

        FragmentManager fm = getSupportFragmentManager();
        editNameDialog = FragmentGiftDialog.newInstance();

        editNameDialog.setOnGridViewClickListener(this);
        isClick = 0;
        editNameDialog.show(fm, "fragment_bottom_dialog");


    }

    @Override
    public void onInfoClick() {
        showAlertDialog();
    }

    private ArrayList<Gift> gifts;

    public int isClick = 0;


    private Gift item;

    @Override
    public void click(Gift gift) {

        item = gift;

        isClick++;

    }

    @Override
    public void onSend() {
        if (isClick < 1) {
            return;
        }
        String name = item.name;
        if ("1333".equalsIgnoreCase(name)) {
            giftAnmManager.showCarOne();
            sendLWMsg("我送了一个跑车", "{\"EC_LiveRoom_SendRacingCarGift\":\"paoche\"}");

            if (editNameDialog != null) {
                editNameDialog.dismiss();
            }
            isClick = 0;
            return;
        } else if ("666".equalsIgnoreCase(name)) {
            sendLWMsg("我送了一个爱心", "{\"EC_LiveRoom_SendLoveGift\":\"520\"}");
            iv_aixin.setVisibility(View.VISIBLE);
            Glide.with(LiveChatUI.this).load(R.drawable.aixin_g).into(iv_aixin);
            iv_aixin.invalidate();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Message msg = new Message();
                    handler.sendMessage(msg);
                }
            }, 3000);
            if (editNameDialog != null) {
                editNameDialog.dismiss();
            }
            isClick = 0;
            return;
        }
        item.giftName = "送你一个小礼物";
        if (!gifts.contains(item)) {
            gifts.add(item);
            giftNumView.setGift(item);
        }
        giftNumView.addNum(1);


        LogUtil.e("onsend--");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isClick = 0;
        room = null;

    }

    class Provider implements DivergeView.DivergeViewProvider {

        @Override
        public Bitmap getBitmap(Object obj) {
            return mList == null ? null : mList.get((Integer) obj);
        }
    }

    public void setFullScreen(boolean isFullScreen) {
        if (isFullScreen) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
    }

    private ECLiveChatRoom room;


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    fragment.hide();
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }


    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] location = {0, 0};
            v.getLocationOnScreen(location);
            int left = location[0];
            int top = location[1];

            View sendV = fragment.getL().getSendView();

            int[] location2 = {0, 0};
            sendV.getLocationOnScreen(location2);
            int left2 = location2[0];
            int top2 = location2[1];

            if (event.getRawX() >= left2 && event.getRawX() <= left2 + sendV.getWidth() && event.getRawY() >= top2 && event.getRawY() <= top2 + sendV.getHeight()) {
                return false;
            }
            Log.d(TAG, "getLocationOnScreen(): left = " + location[0] + "  top=" + location[1]);

            if (event.getRawX() < left || (event.getRawX() > left2 + sendV.getWidth())
                    || event.getRawY() < top ) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    private String mstrName = "";
    private String mstrHigh = "";
    private String ext = "";
    private boolean isCheck = false;

    private VideoView mVideoView;

    @BindView(R.id.live_gift_num)
    public GiftItemView giftNumView;

    @BindView(R.id.iv_aixin)
    public ImageView iv_aixin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);

        room = (ECLiveChatRoom) getIntent().getParcelableExtra("item");

        ECApplication.URL = room.pullUrl;

        if (room == null) {
            finish();
            return;
        }

        imageButton = (BSRGiftView) findViewById(R.id.gift_view);
        giftLayout = (BSRGiftLayout) findViewById(R.id.gift_layout);

        giftAnmManager = new GiftAnmManager(giftLayout, this);

        iv = (ImageView) findViewById(R.id.btn_close);

        gifts = new ArrayList<Gift>();

        iv.setOnClickListener(this);

        if (!LibsChecker.checkVitamioLibs(this)) {
            return;
        }

        if (SDKCoreHelper.getLiveManager() != null) {
            SDKCoreHelper.getLiveManager().setOnLiveChatRoomListener(liveChatRoomListener);
        }

        tvOnline = (TextView) findViewById(R.id.tv_online);

        tvCreate = (RelativeLayout) findViewById(R.id.live_zhubo);


        tvCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selfRole == 1) {
                    TestDialog dialog = new TestDialog().newInstance("请输入", 1,
                            mstrName, mstrHigh, ext, mIsMute);
                    dialog.show(getSupportFragmentManager(), "TestDialog");
                }
            }
        });
        /**构建进入聊天室透传信息对象、设置昵称与透传信息、别的用户可以通过调用查询聊天室成员信息接口进行获取*/
        ECLiveChatRoomMemberInfoBuilder infoBuilder = new ECLiveChatRoomMemberInfoBuilder(CCPAppManager.getClientUser().getUserName(), CCPAppManager.getUserId() + ":infoext");

        ECLiveChatRoomManager roomManager = ECDevice.getECLiveChatRoomManager();
        if (roomManager == null) {
            LogUtil.e(TAG,"livemanager is null");
            return;
        }
        roomManager.joinLiveChatRoom(room.roomId, infoBuilder, SDKCoreHelper.buildNotify(), new ECLiveCallBack.OnEnterLiveChatRoomListener() {
            @Override
            public void onEnterResult(ECError error, ECLiveChatRoom liveChatRoom, ECLiveChatRoomMember member) {

                int code = error.errorCode;
                if (error.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
                    if (member != null) {
                        selfRole = member.getRole().value();
                    }
                    handlerGetRoomInfo();
                } else {
                    finish();
                    if(code==620005){
                        ToastUtil.showMessage("抱歉,当前房间被关闭");
                    }else if(code==620025){
                        ToastUtil.showMessage("抱歉,您被禁止进入当前房间");
                    }else if(code==620030){
                        ToastUtil.showMessage("抱歉,房间人数已达上限");
                    }else {
                        ToastUtil.showMessage("加入失败:" + error.errorCode);
                    }
                }
            }
        });

        fragment = (LIveFragment) getSupportFragmentManager().findFragmentById(R.id.live_bar);

        fragment.getL().setListener(this);

        ImageView iv = (ImageView) fragment.getView().findViewById(R.id.btn_love);

        mList = new ArrayList<Bitmap>();
        mList.add(((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.heart_one, null)).getBitmap());
        mList.add(((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.heart_two, null)).getBitmap());
        mList.add(((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.heart_three, null)).getBitmap());
        mList.add(((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.heart_four, null)).getBitmap());
        mList.add(((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.heart_five, null)).getBitmap());
        mList.add(((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.heart_zero, null)).getBitmap());
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIndex == 5) {
                    mIndex = 0;
                }
                mDivergeView.startDiverges(mIndex);
                mIndex++;

            }
        });
        mDivergeView = (DivergeView) findViewById(R.id.divergeView);
        mDivergeView.post(new Runnable() {
            @Override
            public void run() {
                mDivergeView.setEndPoint(new PointF(mDivergeView.getMeasuredWidth() / 2, 0));
                mDivergeView.setDivergeViewProvider(new Provider());
            }
        });


        hRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_horizontal);
        lv = (RecyclerView) findViewById(R.id.live_listview);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        hRecyclerView.setLayoutManager(linearLayoutManager);


        LinearLayoutManager vManager = new LinearLayoutManager(this);
        vManager.setOrientation(LinearLayoutManager.VERTICAL);
        lv.setLayoutManager(vManager);

        lv.setAdapter(chatAdapter);


        tvDesc = (TextView) findViewById(R.id.tv_desc);
        tvDesc.setText(room.roomId);

        mVideoView = (VideoView) findViewById(R.id.surface_view);

        adapter = new RoomMemberAdapter(membersList);
        hRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.setOnRecyclerViewListener(listener);
        if (TextUtils.isEmpty(ECApplication.URL)){
//            return;
        } else {
            if (!TextUtils.isEmpty(ECApplication.URL)) {
                mVideoView.setVideoPath(ECApplication.URL);
                mVideoView.requestFocus();
                mVideoView.setOnPreparedListener(
                        new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mediaPlayer) {
                                mediaPlayer.setPlaybackSpeed(1.0f);
                            }
                        });
            }

        }

        fragment.setListener(this);
        final ECMessage msg = ECMessage.createECMessage(ECMessage.Type.TXT);
        msg.setFrom("系统通知");
        msg.setTo(room.roomId);
        ECTextMessageBody msgBody = new ECTextMessageBody(getResources().getString(R.string.live_warning));
        msg.setBody(msgBody);

        linkedList.add(msg);

    }

    private RoomMemberAdapter adapter;

    private ECLiveChatRoomMember findPosition(String userId) {

        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        for (ECLiveChatRoomMember member : membersList) {
            if (member != null && userId.equalsIgnoreCase(member.userId)) {
                return member;
            }
        }
        return null;

    }


    private OnLiveChatRoomListener liveChatRoomListener = new OnLiveChatRoomListener() {


        @Override
        public void OnReceiveLiveChatRoomMessage(ECMessage message) {
            if (linkedList != null && message != null) {
                linkedList.add(message);
                lv.getLayoutManager().smoothScrollToPosition(lv, null, linkedList.size() - 1);
                chatAdapter.notifyDataSetChanged();

                String userdata = message.getUserData();

                if (userdata != null && userdata.contains("520")) {
                    iv_aixin.setVisibility(View.VISIBLE);
                    Glide.with(LiveChatUI.this).load(R.drawable.aixin_g).into(iv_aixin);
                    iv_aixin.invalidate();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Message msg = new Message();
                            handler.sendMessage(msg);

                        }
                    }, 3000);
                } else if (userdata != null && userdata.contains("paoche")) {
                    if (giftAnmManager != null) {
                        giftAnmManager.showCarOne();
                    }

                }

            }

        }


        @Override
        public void OnReceiveLiveChatRoomNotification(ECLiveChatRoomNotification notice) {

            if (notice == null) {
                return;
            }
            ECLiveEnums.ECNoticeType type = notice.noticeType;
            String rece = notice.member;
            LogUtil.e(TAG, "OnReceiveLiveChatRoomNotification " + notice.noticeType.value());
            LogUtil.e(TAG, "OnReceiveLiveChatRoomNotification " + rece);


            if (type == ECLiveEnums.ECNoticeType.Join) {
                ECLiveChatRoomMember sMmember = new ECLiveChatRoomMember();
                sMmember.userId = notice.member;
                if (!isExist(notice.member)) {
                    membersList.add(sMmember);
                }
                adapter.notifyDataSetChanged();
                personSize++;
                if (tvOnline != null) {
                    tvOnline.setText(personSize + "人在观看");
                }

            } else if (type == ECLiveEnums.ECNoticeType.Exit) {
                ECLiveChatRoomMember item = findPosition(notice.member);
                if (item != null) {
                    boolean result = membersList.remove(item);
                    if (result) {
                        adapter.notifyDataSetChanged();
                    }
                    personSize--;
                    if (tvOnline != null) {
                        tvOnline.setText(personSize + "人在观看");
                    }
                }


            } else if (type == ECLiveEnums.ECNoticeType.AllMute) {

                ToastUtil.showMessage("聊天室被设置为全员禁言");
            } else if (type == ECLiveEnums.ECNoticeType.CancelAllMute) {
                ToastUtil.showMessage("聊天室取消全员禁言");
            } else if (type == ECLiveEnums.ECNoticeType.MemberMute) {

                if (CCPAppManager.getUserId().equalsIgnoreCase(rece)) {
                    ToastUtil.showMessage("你被设置为禁言");
                }

            } else if (type == ECLiveEnums.ECNoticeType.CancelMemberMute) {

                if (CCPAppManager.getUserId().equalsIgnoreCase(rece)) {
                    ToastUtil.showMessage("你被设置为取消禁言");
                }

            } else if (type == ECLiveEnums.ECNoticeType.MemberBlack) {

                if (CCPAppManager.getUserId().equalsIgnoreCase(rece)) {
                    ToastUtil.showMessage("你被加入到黑名单");
                    finish();
                } else {
                    ECLiveChatRoomMember item = findPosition(notice.member);
                    if (item != null) {
                        boolean result = membersList.remove(item);
                        if (result) {
                            adapter.notifyDataSetChanged();
                        }
                        personSize--;
                        if (tvOnline != null) {
                            tvOnline.setText(personSize + "人在观看");
                        }
                    }
                }

            } else if (type == ECLiveEnums.ECNoticeType.CancelMemberBlack) {

                if (CCPAppManager.getUserId().equalsIgnoreCase(rece)) {
                    ToastUtil.showMessage("你被从黑名单移除");
                }

            } else if (type == ECLiveEnums.ECNoticeType.KickMember) {
                if (CCPAppManager.getUserId().equalsIgnoreCase(rece)) {
                    ToastUtil.showMessage("你被管理员踢出");
                    finish();
                } else {
                    ECLiveChatRoomMember item = findPosition(notice.member);
                    if (item != null) {
                        boolean result = membersList.remove(item);
                        if (result) {
                            adapter.notifyDataSetChanged();
                        }
                        personSize--;
                        if (tvOnline != null) {
                            tvOnline.setText(personSize + "人在观看");
                        }
                    }

                }

            } else if (type == ECLiveEnums.ECNoticeType.ModifyRoomInfo) {

                ToastUtil.showMessage("房间信息被修改");

            } else if (type == ECLiveEnums.ECNoticeType.SetMemberRole) {
                String role = notice.role == ECLiveEnums.ECRole.ECRole_Manager ? "管理员" : "成员";
                if (CCPAppManager.getUserId().equalsIgnoreCase(notice.getMember())) {
                    ToastUtil.showMessage("你的权限被设置为" + role);
                    selfRole = notice.role.value();
                } else {
                    ToastUtil.showMessage(notice.getMember() + "的权限被设置为" + role);
                }
            }
        }
    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return false;
    }

    private int personSize = 0;

    private void handlerGetRoomInfo() {

        if (!TextUtils.isEmpty(ECApplication.URL)) {

            mVideoView.setVideoPath(ECApplication.URL);
        }

        ECLiveChatRoomManager roomManager = ECDevice.getECLiveChatRoomManager();
        if (roomManager == null) {
            return;
        }
        ECDevice.getECLiveChatRoomManager().queryLiveChatRoomInfo(room.roomId, new ECLiveCallBack.OnQueryLiveChatRoomInfoListener() {
            @Override
            public void onQueryLiveChatRoom(ECError error, ECLiveChatRoom liveChatRoom) {
                if (error.errorCode == SdkErrorCode.REQUEST_SUCCESS) {

                         ECLiveEnums.ECAllMuteMode mode =  liveChatRoom.getMode();
                         if(mode== ECLiveEnums.ECAllMuteMode.ECAllMuteMode_ON){
                             mIsMute =  true;
                         }else {
                             mIsMute = false;
                         }
                    LogUtil.e(TAG, error.toString());
                    if (liveChatRoom != null) {
                        personSize = liveChatRoom.onLineCount;
                        tvOnline.setText(personSize + "人在观看");
                    }
                } else {

                }
            }
        });


        ECLiveSearchMembersConditionBuilder builder = new ECLiveSearchMembersConditionBuilder(room.roomId, "", ECLiveEnums.ECRole.ECRole_CREATE, 10);
        ECDevice.getECLiveChatRoomManager().queryLiveChatRoomMembers(builder, new ECLiveCallBack.OnQueryLiveChatRoomMembersListener() {

            @Override
            public void onQueryResult(ECError error, ArrayList<ECLiveChatRoomMember> list) {
                if (error.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
                    if (list == null || list.size() < 1) {
                        return;
                    }
                    for (ECLiveChatRoomMember item : list) {
                        if (membersList != null && !membersList.contains(item)) {
                            membersList.add(item);
                        }
                    }
                    adapter.notifyDataSetChanged();
                } else {
                }
            }
        });
    }

    private boolean mIsMute = false;

    private RecyclerView.Adapter chatAdapter = new RecyclerView.Adapter() {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.live_chat_list_item, null);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(lp);
            return new ChatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {


            ChatViewHolder vholder = (ChatViewHolder) holder;
            vholder.position = i;
            ECMessage person = linkedList.get(i);
            String from = person.getForm();

            if (person.getForm().equalsIgnoreCase(CCPAppManager.getUserId())) {

                vholder.ageTv.setText("LV2:" + ECApplication.nick + ":" + ((ECTextMessageBody) person.getBody()).getMessage());
            } else {

                if (!TextUtils.isEmpty(from) && from.equalsIgnoreCase("系统通知")) {

                    vholder.ageTv.setText("系统通知" + ":" + ((ECTextMessageBody) person.getBody()).getMessage());
                } else {

                }
                String s = person.getNickName() == null ? "" : person.getNickName();

                vholder.ageTv.setText("LV1:" + s + ":" + ((ECTextMessageBody) person.getBody()).getMessage());

            }


            if (person.getVersion() == 620008) {
                Drawable drawable = getResources().getDrawable(R.drawable.msg_state_fail_resend);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                vholder.ageTv.setCompoundDrawables(drawable, null, null, null);
            }

        }

        @Override
        public int getItemCount() {
            return linkedList.size();
        }
    };

    class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public View rootView;
        public TextView ageTv;
        public int position;

        public ChatViewHolder(View itemView) {
            super(itemView);
            ageTv = (TextView) itemView.findViewById(R.id.live_list_item_tv);
            rootView = itemView.findViewById(R.id.live_parent_item);
            rootView.setOnClickListener(this);
            rootView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
        }

        @Override
        public boolean onLongClick(View v) {

            return false;
        }
    }

    private int selfRole;

    private boolean isExist(String memberId) {

        boolean isExist = false;
        for (ECLiveChatRoomMember item : membersList) {
            if (item != null) {
                if (item.getUserId().equalsIgnoreCase(memberId)) {
                    isExist = true;
                    break;
                }
            }
        }
        return isExist;
    }

    private void dialog(int position) {
        ECLiveChatRoomMember member = membersList.get(position);

        final ECLiveNotifyWrapper wrapper = SDKCoreHelper.buildNotify();
        final String userId = member.userId;

        final View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (v.getId()) {

                    case R.id.btn_kick:


                        ECLiveChatRoomManager roomManager = ECDevice.getECLiveChatRoomManager();
                        if (roomManager == null) {
                            return;
                        }
                        roomManager.kickLiveChatRoomMember(room.roomId, userId, wrapper, new ECLiveCallBack.OnLiveChatRoomKickMemberListener() {
                            @Override
                            public void onResult(ECError ecError) {

                                if (ecError.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
                                    ECLiveChatRoomMember item = findPosition(userId);
                                    if (item != null) {
                                        boolean result = membersList.remove(item);
                                        if (result) {
                                            adapter.notifyDataSetChanged();
                                        }
                                        personSize--;
                                        if (tvOnline != null) {
                                            tvOnline.setText(personSize + "人在观看");
                                        }
                                    }
                                }
                                showToast(ecError);
                            }
                        });


                        break;
                    case R.id.btn_mute:

                        ECLiveControlOption option = new ECLiveControlOption(4000, ECLiveEnums.ECState.EC_STATE_Mute);

                        SDKCoreHelper.getLiveManager().forbidLiveChatRoomMember(room.roomId, userId, option, wrapper, new ECLiveCallBack.OnControlMemberStateListener() {
                            @Override
                            public void onResult(ECError ecError) {
                                showToast(ecError);
                            }
                        });

                        break;
                    case R.id.btn_mute_out:
                        ECLiveControlOption optionM = new ECLiveControlOption(0, ECLiveEnums.ECState.EC_STATE_Cancel_Mute);

                        SDKCoreHelper.getLiveManager().forbidLiveChatRoomMember(room.roomId, userId, optionM, wrapper, new ECLiveCallBack.OnControlMemberStateListener() {
                            @Override
                            public void onResult(ECError ecError) {
                                showToast(ecError);
                            }
                        });

                        break;
                    case R.id.btn_black:


                        SDKCoreHelper.getLiveManager().dfriendLiveChatRoomMember(room.roomId, userId, ECLiveEnums.ECState.EC_STATE_Add_Black, wrapper, new ECLiveCallBack.OnControlMemberStateListener() {
                            @Override
                            public void onResult(ECError ecError) {

                                if (ecError.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
                                    ECLiveChatRoomMember item = findPosition(userId);
                                    if (item != null) {
                                        boolean result = membersList.remove(item);
                                        if (result) {
                                            adapter.notifyDataSetChanged();
                                        }
                                        personSize--;
                                        if (tvOnline != null) {
                                            tvOnline.setText(personSize + "人在观看");
                                        }
                                    }
                                }

                                showToast(ecError);
                            }
                        });

                        break;
                    case R.id.btn_black_out:


                        SDKCoreHelper.getLiveManager().dfriendLiveChatRoomMember(room.roomId, userId, ECLiveEnums.ECState.EC_STATE_Cancel_Black, wrapper, new ECLiveCallBack.OnControlMemberStateListener() {
                            @Override
                            public void onResult(ECError ecError) {
                                showToast(ecError);
                            }
                        });

                        break;
                    case R.id.btn_add_manager:

                        ECLiveChatRoomManager roomManager2 = ECDevice.getECLiveChatRoomManager();
                        if (roomManager2 == null) {
                            return;
                        }
                        ECLiveNotifyWrapper notifyWrapper = new ECLiveNotifyWrapper(CCPAppManager.getUserId(), ECLiveEnums.ECNotifyOption.ECNotifyOption_ON);
                        roomManager2.modifyLiveChatRoomMemberRole(room.roomId, userId, ECLiveEnums.ECRole.ECRole_Manager, notifyWrapper, new ECLiveCallBack.OnControlMemberRoleListener() {
                            @Override
                            public void onResult(ECError ecError) {
                                showToast(ecError);
                            }
                        });


                        break;
                    case R.id.btn_add_member:

                        SDKCoreHelper.getLiveManager().modifyLiveChatRoomMemberRole(room.roomId, userId, ECLiveEnums.ECRole.ECRole_Member, SDKCoreHelper.buildNotify(), new ECLiveCallBack.OnControlMemberRoleListener() {
                            @Override
                            public void onResult(ECError ecError) {
                                showToast(ecError);
                            }
                        });

                        break;
                }
            }
        };
        SDKCoreHelper.getLiveManager().queryLiveChatRoomMember(room.roomId, member.userId, new ECLiveCallBack.OnQuerySingleLiveChatRoomMembersListener() {
            @Override
            public void onQueryResult(ECError error, ECLiveChatRoomMember member) {

                if (member == null) {
                    return;
                }

                final AlertDialog dialog = new AlertDialog.Builder(LiveChatUI.this)
                        .create();
                dialog.show();
                Window window = dialog.getWindow();
                // 设置布局
                window.setContentView(R.layout.alertdialog);
                // 设置宽高
                window.setLayout(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                // 设置弹出的动画效果
                window.setWindowAnimations(R.style.AnimBottom);
                // 设置监听

                EditText infoED = (EditText) window.findViewById(R.id.dialog_tv);
                String nick = ((member.getNickName() == null) ? "" : member.getNickName());
                String info = ((member.getInfoExt() == null) ? "" : member.getInfoExt());
                String text = "昵称:" + nick + ",签名:" + info;
                infoED.setText(text);


                TextView tvKick = (TextView) window.findViewById(R.id.btn_kick);
                TextView tvMute = (TextView) window.findViewById(R.id.btn_mute);
                TextView tvMute_Out = (TextView) window.findViewById(R.id.btn_mute_out);
                TextView tvBlack = (TextView) window.findViewById(R.id.btn_black);
                TextView tvBlack_out = (TextView) window.findViewById(R.id.btn_black_out);
                TextView manager = (TextView) window.findViewById(R.id.btn_add_manager);
                TextView members = (TextView) window.findViewById(R.id.btn_add_member);


                infoED.setEnabled(false);
                infoED.setFocusable(false);

                int otherRole = member.getRole().value();
                if (selfRole < otherRole) {
                    tvKick.setEnabled(true);
                    tvMute.setEnabled(true);
                    tvMute_Out.setEnabled(true);
                    tvBlack.setEnabled(true);
                    tvBlack_out.setEnabled(true);
                    tvKick.setOnClickListener(clickListener);
                    tvMute.setOnClickListener(clickListener);
                    tvMute_Out.setOnClickListener(clickListener);
                    tvBlack.setOnClickListener(clickListener);
                    tvBlack_out.setOnClickListener(clickListener);
                    manager.setOnClickListener(clickListener);
                    members.setOnClickListener(clickListener);
                } else {
                    tvKick.setEnabled(false);
                    tvMute.setEnabled(false);
                    tvMute_Out.setEnabled(false);
                    tvBlack.setEnabled(false);
                    tvBlack_out.setEnabled(false);

                }


            }
        });


    }

    private void showToast(ECError error){
        if (error.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
            ToastUtil.showMessage("设置成功");
        } else {
            if(error.errorCode==620032){
                ToastUtil.showMessage("设置失败,已达最大上限");
            }else {
                ToastUtil.showMessage("设置失败");
            }

        }
    }


    private RoomMemberAdapter.OnRecyclerViewListener listener = new RoomMemberAdapter.OnRecyclerViewListener() {
        @Override
        public void onItemClick(int position) {

            dialog(position);
        }

        @Override
        public boolean onItemLongClick(int position) {
            return false;
        }
    };


}

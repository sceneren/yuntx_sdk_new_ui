package com.yuntongxun.ecdemo.ui.personcenter;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yuntongxun.ecdemo.ECApplication;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.ExceptionHandler;
import com.yuntongxun.ecdemo.common.utils.DateUtil;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.common.utils.DensityUtil;
import com.yuntongxun.ecdemo.common.utils.GetImageUtils;
import com.yuntongxun.ecdemo.common.utils.ImageLoader;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.pojo.Friend;
import com.yuntongxun.ecdemo.storage.FriendMessageSqlManager;
import com.yuntongxun.ecdemo.ui.BaseActivity;
import com.yuntongxun.ecdemo.ui.RestServerDefines;
import com.yuntongxun.ecdemo.ui.SDKCoreHelper;
import com.yuntongxun.ecdemo.ui.phonemodel.HttpMethods;
import com.yuntongxun.ecsdk.PersonInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Created by luhuashan on 17/8/22.
 * email huashan2007@sina.cn
 */
public class FriendInfoUI extends BaseActivity {


    public final static String RAW_ID = "raw_id";
    public final static String MOBILE = "mobile";
    public final static String DISPLAY_NAME = "display_name";
    private static final int RQ_REMARK = 0x2a;

    @BindView(R.id.title_bar)
    TitleBar titleBar;
    @BindView(R.id.iv_my_header)
    ImageView ivMyHeader;
    @BindView(R.id.tv_remark)
    TextView tvRemark;
    @BindView(R.id.tv_my_phone)
    TextView tvMyPhone;
    @BindView(R.id.tv_my_nick)
    TextView tvMyNick;
    @BindView(R.id.p_touxiang)
    LinearLayout pTouxiang;
    @BindView(R.id.tv_my_beizhu)
    TextView tvFriendBeizhu;
    @BindView(R.id.tv_my_age)
    TextView tvFriendAge;
    @BindView(R.id.tv_my_sign)
    TextView tvFriendSign;

    @BindView(R.id.btn_add)
    Button btnAdd;
    @BindView(R.id.phone_chat)
    Button phoneChat;
    @BindView(R.id.phone_voip)
    Button phoneVoip;
    @BindView(R.id.p_nickname)
    RelativeLayout re_beizhu;


    private String mobile;
    private String displayname;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void runOnUiThread(Friend friend, boolean enable) {
        if (!canShowUI() || TextUtils.equals(CCPAppManager.getUserId(), mobile)) {
            return;
        }
        if (friend == null || !enable) {
            return;
        }

        String nick = friend.getNickName();
        if (!TextUtils.isEmpty(nick)) {
            displayname = nick;
            tvMyNick.setText("昵称:" + nick);
            String sign = friend.getSign();
            if (!TextUtils.isEmpty(sign)) {
                tvFriendSign.setText(friend.getSign());
            }

            String remark = friend.getRemarkName();
            if (!TextUtils.isEmpty(remark)) {
                displayname = remark;
                tvFriendBeizhu.setText(remark);
                tvRemark.setText(remark);
            } else {
                tvRemark.setText(nick);
            }

            String age = friend.getAge();
            if (!TextUtils.isEmpty(age)) {
                tvFriendAge.setText(CCPAppManager.calculateDatePoor(age));
            }

            PersonInfo.Sex sex = friend.getSex();
            Drawable woman = getResources().getDrawable(R.drawable.woman);
            Drawable man = getResources().getDrawable(R.drawable.man);
            man.setBounds(0, 0, man.getMinimumWidth(), man.getMinimumHeight());
            woman.setBounds(0, 0, woman.getMinimumWidth(), woman.getMinimumHeight());
            if (sex == PersonInfo.Sex.FEMALE) {
                tvRemark.setCompoundDrawables(null, null, woman, null);
            } else {
                tvRemark.setCompoundDrawables(null, null, man, null);

            }
            String photo = friend.getAvatar();
            if (!TextUtils.isEmpty(photo)) {
                ImageLoader.getInstance().displayCricleImage(
                        mContext, photo
                        , ivMyHeader);
            }
        }

        if (friend.isFriendLy()) {
            titleBar.setMySettingIcon(R.drawable.more)
                    .setSettingIconOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(FriendInfoUI.this, DeleteFriendUI.class).putExtra("userId", mobile));
                        }
                    });

            titleBar.getRightBtn().setPadding(DensityUtil.dip2px(10), DensityUtil.dip2px(10)
                    , DensityUtil.dip2px(10), DensityUtil.dip2px(10));
            re_beizhu.setVisibility(View.VISIBLE);
            btnAdd.setVisibility(View.GONE);
            phoneChat.setVisibility(View.VISIBLE);
            phoneVoip.setVisibility(View.VISIBLE);
        } else {
            btnAdd.setVisibility(View.VISIBLE);
            phoneChat.setVisibility(View.GONE);
            phoneVoip.setVisibility(View.GONE);
            re_beizhu.setVisibility(View.GONE);
        }
    }

    private void handSelfInfo() {
        PersonInfo info = CCPAppManager.getPersonInfo();
        tvMyNick.setText("昵称:" + info.getNickName());
        tvFriendSign.setText(info.getSign());
        tvRemark.setVisibility(View.GONE);

        String age = info.getBirth();
        if (TextUtils.isEmpty(age)) {
            tvFriendAge.setText(CCPAppManager.calculateDatePoor("28岁"));
        } else {
            tvFriendAge.setText(CCPAppManager.calculateDatePoor(info.getBirth() + "岁"));
        }
        PersonInfo.Sex sex = info.getSex();
        Drawable woman = getResources().getDrawable(R.drawable.woman);
        Drawable man = getResources().getDrawable(R.drawable.man);
        man.setBounds(0, 0, man.getMinimumWidth(), man.getMinimumHeight());
        woman.setBounds(0, 0, woman.getMinimumWidth(), woman.getMinimumHeight());
        if (sex == PersonInfo.Sex.FEMALE) {
            tvRemark.setCompoundDrawables(null, null, woman, null);
        } else {
            tvRemark.setCompoundDrawables(null, null, man, null);
        }
        if (!TextUtils.isEmpty(ECApplication.photoUrl)) {
            ImageLoader.getInstance().displayCricleImage(
                    mContext, ECApplication.photoUrl
                    , ivMyHeader);
        } else {
            if (GetImageUtils.isFileExist()) {
                ImageLoader.getInstance().displayCricleImage(this, GetImageUtils.getPicFile(), ivMyHeader);
            } else {
                ImageLoader.getInstance().displayCricleImage(this, R.drawable.header_woman, ivMyHeader);
            }
        }
        re_beizhu.setVisibility(View.GONE);
        btnAdd.setVisibility(View.GONE);
        phoneChat.setVisibility(View.VISIBLE);
        phoneVoip.setVisibility(View.GONE);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEventMainThread(FirstEvent event) {
        finish();
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void initView(Bundle savedInstanceState) {

        EventBus.getDefault().register(this);

        mobile = getIntent().getStringExtra(MOBILE);
        displayname = getIntent().getStringExtra(DISPLAY_NAME);


        tvMyPhone.setText(mobile);

        if (!SDKCoreHelper.getInstance().isSupportMedia()) {
            phoneVoip.setVisibility(View.GONE);
        }

        if (RestServerDefines.IM) {
            phoneVoip.setVisibility(View.GONE);
        }

        initTooleBar(titleBar, true, "详细资料");

        if (TextUtils.equals(CCPAppManager.getUserId(), mobile)) {
            handSelfInfo();
        } else {
            Friend friend = FriendMessageSqlManager.queryFriendById(mobile);
            runOnUiThread(friend, true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (TextUtils.isEmpty(mobile)) {
            mobile = getIntent().getStringExtra(MOBILE);
            displayname = getIntent().getStringExtra(DISPLAY_NAME);
            tvMyPhone.setText(mobile);

            if (!SDKCoreHelper.getInstance().isSupportMedia()) {
                phoneVoip.setVisibility(View.GONE);
            }

            if (RestServerDefines.IM) {
                phoneVoip.setVisibility(View.GONE);
            }

            if (TextUtils.equals(CCPAppManager.getUserId(), mobile)) {
                handSelfInfo();
            } else {
                Friend friend = FriendMessageSqlManager.queryFriendById(mobile);
                runOnUiThread(friend, true);
            }
        }
    }

    private void getFriendInfo(final String friendId) {
        Observer<Object> subscriber = new Observer<Object>() {
            @Override
            public void onComplete() {
                LogUtil.e("onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                LogUtil.e(e.toString());
            }

            @Override
            public void onSubscribe(Disposable d) {
            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onNext(Object movieEntity) {
                if (movieEntity != null) {
                    LogUtil.e(movieEntity.toString());
                    ResponseBody body = (ResponseBody) movieEntity;
                    try {
                        String s = new String(body.bytes());
                        ExceptionHandler.logHttpResp(s);
                        String error = "";
                        int code = 0;
                        JSONObject j = new JSONObject(s);
                        if (j != null && j.has("statusMsg")) {
                            error = j.getString("statusMsg");
                        }
                        if (j != null && j.has("statusCode")) {
                            code = Integer.parseInt(j.getString("statusCode"));
                        }
                        if (DemoUtils.isTrue(s)) {
                            Friend friend = DemoUtils.getFriendInfo(s);
                            runOnUiThread(friend, true);

                            FriendMessageSqlManager.updateAll(friendId, friend.getNickName()
                                    , friend.getFriendState(), friend.getAvatar(), friend.getRemarkName());
                        } else {
                            ToastUtil.showMessage(DemoUtils.getErrMsg(s));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        String time = DateUtil.formatNowDate(new Date());
        String url = getSig(time);
        JSONObject map = HttpMethods.buildGetFriendInfo(CCPAppManager.getUserId(), friendId);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), map.toString());
        HttpMethods.getInstance(time).getPersonInfo(subscriber, RestServerDefines.APPKER, url, body);
    }


    public String getSig(String stime) {
        String s = RestServerDefines.APPKER + CCPAppManager.getAppToken() + stime;
        return getMessageDigest(s.getBytes());
    }

    public static String getMessageDigest(byte[] input) {
        char[] source = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(input);
            byte[] digest = mDigest.digest();
            int length = digest.length;
            char[] result = new char[length * 2];
            int j = 0;
            for (byte l : digest) {
                result[(j++)] = source[(l >>> 4 & 0xF)];
                result[(j++)] = source[(l & 0xF)];
            }
            return new String(result);
        } catch (Exception e) {
        }
        return null;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.friend_ui;
    }

    @Override
    protected void initWidgetAciotns() {
        getFriendInfo(mobile);
    }


    @OnClick({R.id.p_nickname, R.id.p_age, R.id.p_sign, R.id.phone_chat, R.id.phone_voip, R.id.btn_add})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.p_nickname:

                Intent intent = new Intent(this, SetInfoUI.class);
                intent.putExtra(SetInfoUI.Type, 2);
                intent.putExtra("friendId", mobile);
                intent.putExtra("mark", tvFriendBeizhu.getText().toString().trim());
                startActivityForResult(intent, RQ_REMARK);
                break;
            case R.id.phone_chat:
                CCPAppManager.startChattingAction(this, mobile, displayname, true);
                break;
            case R.id.phone_voip:
                CCPAppManager.showCallMenu(this, displayname, mobile);
                break;

            case R.id.btn_add:
                requestAddFriend(mobile);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RQ_REMARK
                && resultCode == RESULT_OK
                && data != null) {
            String remark = data.getStringExtra("remark");
            tvFriendBeizhu.setText(remark);
            tvRemark.setText(remark);
        }

    }


    private void requestAddFriend(String mobile) {
        if (TextUtils.isEmpty(mobile)) {
            return;
        }
        showCommonProcessDialog();
        Observer<Object> subscriber = new Observer<Object>() {
            @Override
            public void onComplete() {
                dismissCommonPostingDialog();
            }

            @Override
            public void onError(Throwable e) {
                ToastUtil.showMessage("添加请求失败");
                dismissCommonPostingDialog();
            }

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Object movieEntity) {
                if (movieEntity != null) {
                    LogUtil.e(movieEntity.toString());
                    ResponseBody body = (ResponseBody) movieEntity;
                    try {
                        String s = new String(body.bytes());
                        String error = "";
                        JSONObject j = new JSONObject(s);
                        if (j != null && j.has("statusMsg")) {
                            error = j.getString("statusMsg");
                        }
                        if (DemoUtils.isTrue(s)) {
                            ToastUtil.showMessage("添加请求成功");
                            finish();
                        } else {
                            ToastUtil.showMessage(error);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        String time = DateUtil.formatNowDate(new Date());
        String url = getSig(time);
        JSONObject map = HttpMethods.buildAddFriendOther(CCPAppManager.getUserId(), mobile);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), map.toString());
        HttpMethods.getInstance(time).addFriend(subscriber, RestServerDefines.APPKER, url, body);
    }
}

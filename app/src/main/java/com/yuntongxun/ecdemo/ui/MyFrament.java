package com.yuntongxun.ecdemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.melink.bqmmsdk.ui.store.EmojiPackageList;
import com.yuntongxun.ecdemo.ECApplication;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.utils.DateUtil;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.common.utils.GetImageUtils;
import com.yuntongxun.ecdemo.common.utils.ImageLoader;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.SharedPreferencesUtils;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.core.ClientUser;
import com.yuntongxun.ecdemo.ui.personcenter.PersonInfoUI;
import com.yuntongxun.ecdemo.ui.phonemodel.HttpMethods;
import com.yuntongxun.ecdemo.ui.settings.AboutActivity;
import com.yuntongxun.ecdemo.ui.settings.SettingsActivity;
import com.yuntongxun.ecdemo.ui.settings.SuggestActivity;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.PersonInfo;
import com.yuntongxun.ecsdk.SdkErrorCode;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import utils.RedPacketUtil;

/**
 * Created by zlk on 2017/7/24.
 */

public class MyFrament extends LazyFrament implements PlatformActionListener {
    @BindView(R.id.title_bar)
    TitleBar titleBar;
    @BindView(R.id.iv_my_header)
    ImageView ivMyHeader;
    @BindView(R.id.tv_my_name)
    TextView tvMyName;
    @BindView(R.id.tv_my_phone)
    TextView tvMyPhone;
    @BindView(R.id.tv_my_wallte)
    TextView tvMyWallte;
    @BindView(R.id.tv_my_emo)
    TextView tvMyEmo;
    @BindView(R.id.tv_my_about)
    TextView tvMyAbout;
    @BindView(R.id.tv_my_recommend)
    TextView tvMySuggestion;
    @BindView(R.id.tv_my_setting)
    TextView tvMySetting;
    @BindView(R.id.self_info)
    RelativeLayout tvInfo;
    Unbinder unbinder;

    private UpdateAppManger updateAppManger;


    public static MyFrament newInstance() {
        return new MyFrament();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_my;
    }

    private MainAct getUI() {
        return (MainAct) getActivity();
    }


    @Override
    protected void initView(Bundle savedInstanceState) {
        titleBar.setMyCenterTitle("我的");
        ShareSDK.initSDK(mContext);
        updateAppManger = new UpdateAppManger(getActivity());


        tvMyPhone.setText(CCPAppManager.getUserId());

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

            @Override
            public void onNext(Object movieEntity) {
                if (movieEntity != null) {
                    LogUtil.e(movieEntity.toString());
                    ResponseBody body = (ResponseBody) movieEntity;
                    try {
                        String s = new String(body.bytes());
                        LogUtil.e("picurl", "" + s);
                        String error = "";
                        JSONObject j = new JSONObject(s);
                        if (j != null && j.has("avatar")) {
                            String url = j.getString("avatar");
                            if (!TextUtils.isEmpty(url)) {
                                ECApplication.photoUrl = url;
                                ImageLoader.getInstance().displayCricleImage(
                                        mContext, url
                                        , ivMyHeader);
                            }

                        }

                        if (j != null && j.has("statusMsg")) {
                            error = j.getString("statusMsg");
                        }
                        if (DemoUtils.isTrue(s)) {
                        } else {
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        String time = DateUtil.formatNowDate(new Date());
        String url = getSig(time);
        JSONObject map = HttpMethods.buildGetPersonPic(CCPAppManager.getUserId());
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), map.toString());
        HttpMethods.getInstance(time).getPersonPic(subscriber, RestServerDefines.APPKER, url, body);


        /********************999******/

//        getFriends();


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
    protected void initWidgetActions() {
        final ClientUser clientUser = CCPAppManager.getClientUser();
        ECDevice.getPersonInfo(clientUser.getUserId(), new ECDevice.OnGetPersonInfoListener() {
            @Override
            public void onGetPersonInfoComplete(ECError e, PersonInfo p) {
                if (e.errorCode == SdkErrorCode.REQUEST_SUCCESS && p != null) {
                    clientUser.setpVersion(p.getVersion());
                    clientUser.setSex(p.getSex().ordinal());
                    clientUser.setUserName(p.getNickName());
                    clientUser.setSignature(p.getSign());

                    ECApplication.nick = p.getNickName();
                    ECApplication.sign = p.getSign();
                    if (!TextUtils.isEmpty(p.getBirth())) {
                        clientUser.setBirth(p.getBirth());
                    } else {
                        clientUser.setBirth("1989-01-21");
                    }

                    CCPAppManager.setClientUser(clientUser);

                    tvMyName.setText(CCPAppManager.getClientUser().getUserName());
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        enable =true;
        tvMyName.setText(CCPAppManager.getClientUser().getUserName());


        if (!TextUtils.isEmpty(ECApplication.photoUrl)) {
            ImageLoader.getInstance().displayCricleImage(
                    mContext, ECApplication.photoUrl
                    , ivMyHeader);
        } else {
            if (GetImageUtils.isFileExist()) {
                ImageLoader.getInstance().displayCricleImage(getActivity(), GetImageUtils.getPicFile(), ivMyHeader);
            } else {
                ImageLoader.getInstance().displayCricleImage(getActivity(), R.drawable.header_woman, ivMyHeader);
            }

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        enable = true;
    }

    @Override
    public void fetchData() {

    }

    @OnClick({R.id.iv_my_header, R.id.self_info, R.id.tv_my_wallte, R.id.tv_my_emo, R.id.tv_my_about
            , R.id.tv_my_recommend, R.id.tv_my_setting, R.id.tv_my_app_recommend, R.id.tv_my_official_accounts
    ,R.id.tv_my_update})
    public void onViewClicked(final View view) {
        switch (view.getId()) {
            case R.id.iv_my_header:
                break;
            case R.id.tv_my_wallte:
                String fromNickname = ECApplication.nick;
                String fromAvatarUrl = "none";
                fromAvatarUrl = TextUtils.isEmpty(fromAvatarUrl) ? "none" : fromAvatarUrl;
                fromNickname = TextUtils.isEmpty(fromNickname) ? CCPAppManager.getClientUser().getUserId() : fromNickname;
                RedPacketUtil.startChangeActivity(getActivity(), fromNickname, fromAvatarUrl, CCPAppManager.getClientUser().getUserId());
                break;
            case R.id.tv_my_emo:
                startActivity(EmojiPackageList.class);
                break;
            case R.id.tv_my_about:
                startActivity(AboutActivity.class);
                break;
            case R.id.tv_my_recommend:
                startActivity(SuggestActivity.class);
                break;
            case R.id.tv_my_setting:
                startActivity(SettingsActivity.class);
                break;
            case R.id.self_info:
                startActivity(new Intent(getContext(), PersonInfoUI.class));
                break;

            case R.id.tv_my_app_recommend:

//                getActivity().findViewById(R.id.tv_my_app_recommend).setClickable(false);
//                getActivity().findViewById(R.id.tv_my_app_recommend).setClickable(false);
//                getActivity().findViewById(R.id.tv_my_app_recommend).setFocusable(false);
//                view.setClickable(false);
//                view.setEnabled(false);

                if(enable){
                    shareToWeChat("容联快聊","国内顶级云通讯平台","http://www.yuntongxun.com/front/images/im_img4.png","http://m.yuntongxun.com/qrcode/tiyan/tiyan.html?m_im");
                }
                break;
            case R.id.tv_my_official_accounts:
//                shareToWeChatFriend("快聊", "国内顶级云通讯平台，关注公众号第一时间了解最新资讯", "http://www.yuntongxun.com/front/images/im_img4.png", "https://imwechat.yuntongxun.com/2017-10-19/Application/20150314000000110000000000000010/wx/jumpPage.shtml?pageName=download.jsp&code=071rJQ2a1hUTqS1XXs5a1Pe13a1rJQ29&state=1");

                shareToWeChatFriend("快聊", "国内顶级云通讯平台，关注公众号第一时间了解最新资讯", "http://www.yuntongxun.com/front/images/im_img4.png", "https://imwechat.yuntongxun.com/2017-10-19/Application/20150314000000110000000000000010/wx/jumpPage.shtml?pageName=download.jsp&code=071rJQ2a1hUTqS1XXs5a1Pe13a1rJQ29&state=1");
                break;
            case R.id.tv_my_update:

                String pwd  = (String)SharedPreferencesUtils.getParam(CCPAppManager.getContext(), "pwd", "");
                handleUpdate(CCPAppManager.getUserId(),pwd);
                break;
        }
    }


    private void handleUpdate(final String phone, final String pwd) {


        if(ECApplication.NEW_VERSION){
            updateAppManger.showNoticeDialog("有新版本更新!",UpdateAppManger.URL);
        }else {
            ToastUtil.showMessage("当前没有新版本!");
        }


//        getUI().showCommonProcessDialog();
//        final Observer<Object> subscriber = new Observer<Object>() {
//            @Override
//            public void onComplete() {
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                getUI().dismissCommonPostingDialog();
//                if (e instanceof ConnectException) {
//                    ToastUtil.showMessage("网络异常请检查重试");
//                } else {
//                    ToastUtil.showMessage("检测失败");
//                }
//            }
//
//            @Override
//            public void onSubscribe(Disposable d) {
//            }
//
//            @Override
//            public void onNext(Object movieEntity) {
//                getUI().dismissCommonPostingDialog();
//                if (movieEntity != null) {
//                    ResponseBody body = (ResponseBody)movieEntity;
//                    try {
//                        String s = new String(body.bytes());
//                        LogUtil.e("update", s);
//                        if(DemoUtils.isTrue(s)) {
//                            JSONObject object = new JSONObject(s);
//                            if(object!=null&&object.has("downLoadUrl")){
//                               String url = object.getString("downLoadUrl");
//                                if(!TextUtils.isEmpty(url)){
//                                    updateAppManger.showNoticeDialog("有新版本更新!",url);
//                                }else {
//                                    ToastUtil.showMessage("当前已是最新版本");
//                                }
//                            }
//                        } else {
//                            ToastUtil.showMessage(DemoUtils.getErrMsg(s));
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        };
//        String time = DateUtil.formatNowDate(new Date());
//        String url = getSig(time);
//        JSONObject map = HttpMethods.buildLogin(phone, pwd);
//        RequestBody body = RequestBody.create(MediaType.parse("application/json"), map.toString());
//        HttpMethods.getInstance(time).login(subscriber, RestServerDefines.APPKER, url, body);
    }

    private boolean enable = true;

    private void  shareToWeChat(String title,String text,String imgurl,String url){

        enable = false;

        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setShareType(Platform.SHARE_WEBPAGE);//非常重要：一定要设置分享属性
        sp.setTitle(title);  //分享标题
        sp.setText(text);   //分享文本
        sp.setImageUrl(imgurl);//网络图片rul
        sp.setUrl(url);   //网友点进链接后，可以看到分享的详情

        //3、非常重要：获取平台对象
        Platform wechat = ShareSDK.getPlatform(Wechat.NAME);
        wechat.setPlatformActionListener(this); // 设置分享事件回调
        // 执行分享
        wechat.share(sp);

    }
    private void  shareToWeChatFriend(String title,String text,String imgurl,String url){

        enable =false;

        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setShareType(Platform.SHARE_WEBPAGE);//非常重要：一定要设置分享属性
        sp.setTitle(title);  //分享标题
        sp.setText(text);   //分享文本
        sp.setImageUrl(imgurl);//网络图片rul
        sp.setUrl(url);   //网友点进链接后，可以看到分享的详情

        //3、非常重要：获取平台对象
        Platform wechat = ShareSDK.getPlatform(WechatMoments.NAME);
        wechat.setPlatformActionListener(this); // 设置分享事件回调
        // 执行分享
        wechat.share(sp);

    }

    @Override
    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
        if (platform.getName().equals(Wechat.NAME)) {
            handler.sendEmptyMessage(1);
        } else if (platform.getName().equals(WechatMoments.NAME)) {
            handler.sendEmptyMessage(3);
        }
    }

    @Override
    public void onError(Platform platform, int i, Throwable throwable) {
        throwable.printStackTrace();
        Message msg = new Message();
        msg.what = 6;
        msg.obj = throwable.getMessage();
        handler.sendMessage(msg);
    }

    @Override
    public void onCancel(Platform platform, int i) {
        handler.sendEmptyMessage(5);

    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            enable = true;
            switch (msg.what) {


                case 2:
                    Toast.makeText(CCPAppManager.getContext(), "微信分享成功", Toast.LENGTH_LONG).show();
                    break;
                case 3:
                    Toast.makeText(CCPAppManager.getContext(), "微信朋友圈分享成功", Toast.LENGTH_LONG).show();
                    break;
                case 4:
                    break;

                case 5:
                    Toast.makeText(CCPAppManager.getContext(), "取消分享", Toast.LENGTH_LONG).show();
                    break;
                case 6:
                    Toast.makeText(CCPAppManager.getContext(), "分享失败" + msg.obj, Toast.LENGTH_LONG).show();
                    break;

                default:
                    break;
            }
        }

    };

}

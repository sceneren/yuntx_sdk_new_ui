/*
 *  Copyright (c) 2015 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.yuntongxun.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.yuntongxun.ecdemo.common;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.dialog.ECListDialog;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.common.utils.ECPreferenceSettings;
import com.yuntongxun.ecdemo.common.utils.ECPreferences;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.MimeTypesTools;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.core.ClientUser;
import com.yuntongxun.ecdemo.pojo.FsRobot;
import com.yuntongxun.ecdemo.ui.BaseActivity;
import com.yuntongxun.ecdemo.ui.ECSuperActivity;
import com.yuntongxun.ecdemo.ui.LocationInfo;
import com.yuntongxun.ecdemo.ui.RestServerDefines;
import com.yuntongxun.ecdemo.ui.ShowBaiDuMapActivity;
import com.yuntongxun.ecdemo.ui.chatting.ChattingActivity;
import com.yuntongxun.ecdemo.ui.chatting.ChattingFragment;
import com.yuntongxun.ecdemo.ui.chatting.FileDisplayActivity;
import com.yuntongxun.ecdemo.ui.chatting.ImageGalleryActivity;
import com.yuntongxun.ecdemo.ui.chatting.ImageGralleryPagerActivity;
import com.yuntongxun.ecdemo.ui.chatting.ImageMsgInfoEntry;
import com.yuntongxun.ecdemo.ui.chatting.ViewImageInfo;
import com.yuntongxun.ecdemo.ui.chatting.view.ChatFooterPanel;
import com.yuntongxun.ecdemo.ui.smallwindow.VoiceMeetingService;
import com.yuntongxun.ecdemo.ui.voip.VideoActivity;
import com.yuntongxun.ecdemo.ui.voip.VoIPCallActivity;
import com.yuntongxun.ecdemo.ui.voip.VoIPCallHelper;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.PersonInfo;
import com.yuntongxun.ecsdk.im.ECGroupMember;
import com.yuntongxun.ecsdk.im.ECLocationMessageBody;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.InvalidClassException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yuntongxun.ecdemo.ui.chatting.IMChattingHelper.INTENT_ACTION_ADD_GROUP_MEMBER;
import static com.yuntongxun.ecdemo.ui.chatting.IMChattingHelper.INTENT_ACTION_CHANGE_ADMIN;

/**
 * 存储SDK一些全局性的常量
 * Created by Jorstin on 2015/3/17.
 */
public class CCPAppManager {

    public static Md5FileNameGenerator md5FileNameGenerator = new Md5FileNameGenerator();
    /**
     * Android 应用上下文
     */
    private static Context mContext = null;
    /**
     * 包名
     */
    public static String pkgName = "com.yuntongxun.ecdemo";
    /**
     * SharedPreferences 存储名字前缀
     */
    public static final String PREFIX = "com.yuntongxun.ecdemo_";
    public static final int FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT = 0x10000000;
    /**
     * IM功能UserData字段默认文字
     */
    public static final String USER_DATA = "yuntongxun.ecdemo";
    public static HashMap<String, Integer> mPhotoCache = new HashMap<String, Integer>();

    public static ArrayList<ECSuperActivity> activities = new ArrayList<ECSuperActivity>();
    public static ArrayList<BaseActivity> activitiesBase = new ArrayList<BaseActivity>();

    public static HashMap<String, String> map = new HashMap<String, String>();


    /**
     * IM聊天更多功能面板
     */
    private static ChatFooterPanel mChatFooterPanel;

    public static String getPackageName() {
        return pkgName;
    }

    public static ClientUser mClientUser;


    public static void put(String key, String v) {
        map.put(key, v);
    }

    public static String get(String k) {
        return map.get(k);
    }

    /**
     * 返回SharePreference配置文件名称
     *
     * @return
     */
    public static String getSharePreferenceName() {
        return pkgName + "_preferences";
    }

    public static SharedPreferences getSharePreference() {
        if (mContext != null) {
            return mContext.getSharedPreferences(getSharePreferenceName(), 0);
        }
        return null;
    }

    public static String pwd = "";

    /**
     * 返回上下文对象
     *
     * @return
     */
    public static Context getContext() {
        return mContext;
    }

    public static void sendRemoveMemberBR() {

        getContext().sendBroadcast(new Intent("com.yuntongxun.ecdemo.removemember"));
    }


    public static void sendAddBR(ECGroupMember member) {

        Intent intent = new Intent(INTENT_ACTION_ADD_GROUP_MEMBER);
        intent.putExtra("addmember", member);
        CCPAppManager.getContext().sendBroadcast(intent);
    }

    private static FsRobot robot;

    public static String PHONE = "";

    public static FsRobot getRobot() {
        return robot;
    }

    public static void setRobot(FsRobot robot) {
        CCPAppManager.robot = robot;
    }

    /**
     * 设置上下文对象
     *
     * @param context
     */
    public static void setContext(Context context) {
        mContext = context;
        pkgName = context.getPackageName();
        LogUtil.d(LogUtil.getLogUtilsTag(CCPAppManager.class),
                "setup application context for package: " + pkgName);
    }


    public static ChatFooterPanel getChatFooterPanel(Context context) {
        return mChatFooterPanel;
    }

    /**
     * 缓存账号注册信息
     *
     * @param user
     */
    public static void setClientUser(ClientUser user) {
        mClientUser = user;
    }

    public static void setPversion(int version) {
        if (mClientUser != null) {
            mClientUser.setpVersion(version);
        }
    }

    /**
     * 保存注册账号信息
     *
     * @return 客户登录信息
     */
    public static ClientUser getClientUser() {
        if (mClientUser != null) {
            return mClientUser;
        }
        String registerAccount = getAutoRegisterAccount();
        if (!TextUtils.isEmpty(registerAccount)) {
            mClientUser = new ClientUser("");
            return mClientUser.from(registerAccount);
        }
        return null;
    }

    public static String getUserId() {
        if (getClientUser() == null) {
            return "";
        } else {
            return getClientUser().getUserId();
        }
    }

    private static String getAutoRegisterAccount() {
        SharedPreferences sharedPreferences = ECPreferences.getSharedPreferences();
        ECPreferenceSettings registerAuto = ECPreferenceSettings.SETTINGS_REGIST_AUTO;
        String registerAccount = sharedPreferences.getString(registerAuto.getId(), (String) registerAuto.getDefaultValue());
        return registerAccount;
    }
    public static String getHttpToken() {

        return "17E24E5AFDB6D0C1EF32F3533494502B";
//        SharedPreferences sharedPreferences = ECPreferences.getSharedPreferences();
//        ECPreferenceSettings registerAuto = ECPreferenceSettings.SETTINGS_REGIST_AUTO_TOKEN;
//        String registerAccount = sharedPreferences.getString(registerAuto.getId(), (String) registerAuto.getDefaultValue());
//        return registerAccount;
    }



    public static void putHttpToken(String token) {
        try {
            ECPreferences.savePreference(
                    ECPreferenceSettings.SETTINGS_REGIST_AUTO_TOKEN,
                    token, true);
        } catch (InvalidClassException e) {
            e.printStackTrace();
        }
    }

    public static String getAppKey(){

        String appid =  RestServerDefines.QR_APK?RestServerDefines.APPKER_CODE:RestServerDefines.APPKER;

        return appid;


    }
    public static String getAppToken(){

        String token =  RestServerDefines.QR_APK?CCPAppManager.getHttpToken():RestServerDefines.TOKEN;

        return token;


    }

    /**
     * @param context
     * @param path
     */
    public static void doViewFilePrevieIntent(Context context, String path) {

        if(DemoUtils.isOffice(path)){
            FileDisplayActivity.show(context, path);
            return;
        }


        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(android.content.Intent.ACTION_VIEW);
            String type = MimeTypesTools.getMimeType(context, path);
            File file = new File(path);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (file != null) {
                Uri uri = null;
                if (Build.VERSION.SDK_INT >= 24) {

                    uri = FileProvider.getUriForFile(context, context.getPackageName()+".fileprovider", file);
                } else {
                    uri = Uri.fromFile(file);
                }
                if (uri != null) {
                    intent.setDataAndType(uri, type);
                    context.startActivity(intent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showMessage("手机上无可打开此格式的app");
            LogUtil.e(LogUtil.getLogUtilsTag(CCPAppManager.class), "do view file error " + e.getLocalizedMessage());
        }
    }

    /**
     * @param cotnext
     * @param value
     */
    public static void startChattingImageViewAction(Context cotnext, ImageMsgInfoEntry value) {
        Intent intent = new Intent(cotnext, ImageGralleryPagerActivity.class);
        intent.putExtra(ImageGalleryActivity.CHATTING_MESSAGE, value);
        cotnext.startActivity(intent);
    }

    /**
     * 批量查看图片
     *
     * @param ctx
     * @param position
     * @param session
     */
    public static void startChattingImageViewAction(Context ctx, int position, ArrayList<ViewImageInfo> session, String msgId) {
        Intent intent = new Intent(ctx, ImageGralleryPagerActivity.class);
        intent.putExtra(ImageGralleryPagerActivity.EXTRA_IMAGE_INDEX, position);
        intent.putParcelableArrayListExtra(ImageGralleryPagerActivity.EXTRA_IMAGE_URLS, session);
        intent.putExtra(ImageGralleryPagerActivity.EXTRA_IMAGE_URLS_ID, msgId);
        ctx.startActivity(intent);
    }

    /**
     * 获取应用程序版本名称
     *
     * @return
     */
    public static String getVersion() {
        String version = "0.0.0";
        if (mContext == null) {
            return version;
        }
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(
                    getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return version;
    }

    /**
     * 获取应用版本号
     *
     * @return 版本号
     */
    public static int getVersionCode() {
        int code = 1;
        if (mContext == null) {
            return code;
        }
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(
                    getPackageName(), 0);
            code = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return code;
    }


    public static void addActivity(ECSuperActivity activity) {
        activities.add(activity);
    }

    public static void addActivityBase(BaseActivity activity) {
        activitiesBase.add(activity);
    }

    public static void clearActivity() {
        for (ECSuperActivity activity : activities) {
            if (activity != null) {
                activity.finish();
                activity = null;
            }
            activities.clear();
        }
        for (BaseActivity activity : activitiesBase) {
            if (activity != null) {
                activity.finish();
                activity = null;
            }
            activitiesBase.clear();
        }


    }


    /**
     * 打开浏览器下载新版本
     *
     * @param context
     */
    public static void startUpdater(Context context) {
        Uri uri = Uri.parse("http://dwz.cn/F8Amj");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }

    public static HashMap<String, Object> prefValues = new HashMap<String, Object>();

    /**
     * @param key
     * @param value
     */
    public static void putPref(String key, Object value) {
        prefValues.put(key, value);
    }

    public static Object getPref(String key) {
        return prefValues.remove(key);
    }

    public static void removePref(String key) {
        prefValues.remove(key);
    }

    /**
     * 开启在线客服
     *
     * @param context
     * @param contactid
     */
    public static void startCustomerServiceAction(Context context, String contactid) {
        Intent intent = new Intent(context, ChattingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ChattingFragment.RECIPIENTS, contactid);
        intent.putExtra(ChattingFragment.CONTACT_USER, "在线客服");
        intent.putExtra(ChattingActivity.CONNECTIVITY_SERVICE, true);
        context.startActivity(intent);
    }

    /**
     * 聊天界面
     *
     * @param context
     * @param contactid
     * @param username
     */
    public static void startChattingAction(Context context, String contactid, String username) {
        startChattingAction(context, contactid, username, false);
    }

    public static void startChattingAction2(Context context, String contactid, String username) {
        startChattingAction2(context, contactid, username, false);
    }

    /**
     * @param context
     * @param contactid
     * @param username
     * @param clearTop
     */
    public static void startChattingAction(Context context, String contactid, String username, boolean clearTop) {
        Intent intent = new Intent(context, ChattingActivity.class);
        if (clearTop) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        intent.putExtra(ChattingFragment.RECIPIENTS, contactid);
        intent.putExtra(ChattingFragment.CONTACT_USER, username);
        intent.putExtra(ChattingFragment.CUSTOMER_SERVICE, false);
        context.startActivity(intent);
    }

    public static void startChattingAction2(Context context, String contactid, String username, boolean clearTop) {
        Intent intent = new Intent(context, ChattingActivity.class);
        if (clearTop) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        intent.putExtra(ChattingFragment.RECIPIENTS, contactid);
        intent.putExtra(ChattingFragment.CONTACT_USER, username);
        intent.putExtra(ChattingFragment.CUSTOMER_SERVICE, false);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//        context.startActivityForResult( intent,2);

        context.startActivity(intent);

        EventBus.getDefault().post("111");
    }

    /**
     * VoIP呼叫
     *
     * @param nickname  昵称
     * @param contactId 呼出号码
     */
    public static void callVoIPAction(Context ctx, String nickname, String contactId) {
        // VoIP呼叫
        callVoIPAction(ctx, ECVoIPCallManager.CallType.VOICE, nickname, contactId, false);
    }


    public static final String calculateDatePoor(String birthday) {
        try {

            if (TextUtils.isEmpty(birthday)) {
                return "0";
            }


            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date birthdayDate = sdf.parse(birthday);
            String currTimeStr = sdf.format(new Date());
            Date currDate = sdf.parse(currTimeStr);
            if (birthdayDate.getTime() > currDate.getTime()) {
                return "0";
            }
            long age = (currDate.getTime() - birthdayDate.getTime())
                    / (24 * 60 * 60 * 1000) + 1;
            String year = new DecimalFormat("0.00").format(age / 365f);
            if (TextUtils.isEmpty(year)) {
                return "0";
            }
            return String.valueOf(new Double(year).intValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0";
    }


    public static PersonInfo getPersonInfo() {


        PersonInfo personInfo = new PersonInfo();

        ClientUser clientUser = CCPAppManager.getClientUser();

        String bir =     clientUser.getBirth();
        personInfo.setBirth(TextUtils.isEmpty(bir)?"1989-01-21":bir);
        personInfo.setNickName(clientUser.getUserName());
        personInfo.setSex(clientUser.getSex() == 1 ? PersonInfo.Sex.FEMALE : PersonInfo.Sex.MALE);
        personInfo.setSign(clientUser.getSignature());
        personInfo.setUserId(clientUser.getUserId());


        return personInfo;

    }

    /**
     * 根据呼叫类型通话
     *
     * @param ctx       上下文
     * @param callType  呼叫类型
     * @param nickname  昵称
     * @param contactId 号码
     */
    public static void callVoIPAction(Context ctx, ECVoIPCallManager.CallType callType, String nickname, String contactId, boolean flag) {
        // VoIP呼叫
        Intent callAction = new Intent(ctx, VoIPCallActivity.class);
        if (callType == ECVoIPCallManager.CallType.VIDEO) {
            callAction = new Intent(ctx, VideoActivity.class);
            VoIPCallHelper.mHandlerVideoCall = true;
        } else {
            VoIPCallHelper.mHandlerVideoCall = false;
        }

        callAction.putExtra(VoIPCallActivity.EXTRA_CALL_NAME, nickname);
        callAction.putExtra(VoIPCallActivity.EXTRA_CALL_NUMBER, contactId);
        callAction.putExtra(ECDevice.CALLTYPE, callType);
        callAction.putExtra(VoIPCallActivity.EXTRA_OUTGOING_CALL, true);

        //赋值


        VoiceMeetingService.getInstance().nickname = nickname;
        VoiceMeetingService.getInstance().contactId = contactId;
        VoiceMeetingService.getInstance().callType = callType;
        VoiceMeetingService.getInstance().outgoingCall = false;
        VoiceMeetingService.getInstance().callbackCall = flag;

        if (flag) {
            callAction.putExtra(VoIPCallActivity.ACTION_CALLBACK_CALL, true);
        }
        ctx.startActivity(callAction);
    }

    /**
     * 根据呼叫类型通话
     *
     * @param ctx       上下文
     * @param callType  呼叫类型
     * @param nickname  昵称
     * @param contactId 号码
     */
    public static void callVideoAction(Context ctx, ECVoIPCallManager.CallType callType, String nickname, String contactId, String callid) {
        // VoIP呼叫
        Intent callAction = new Intent(ctx, VoIPCallActivity.class);
        if (callType == ECVoIPCallManager.CallType.VIDEO) {
            callAction = new Intent(ctx, VideoActivity.class);
            VoIPCallHelper.mHandlerVideoCall = true;
        } else {
            VoIPCallHelper.mHandlerVideoCall = false;
        }
        callAction.putExtra(VoIPCallActivity.EXTRA_CALL_NAME, nickname);
        callAction.putExtra(VoIPCallActivity.EXTRA_CALL_NUMBER, contactId);
        callAction.putExtra(ECDevice.CALLTYPE, callType);
        callAction.putExtra(ECDevice.CALLID, callid);
        callAction.putExtra(VoIPCallActivity.EXTRA_OUTGOING_CALL, true);
        ctx.startActivity(callAction);
    }

    /**
     * 多选呼叫菜单
     *
     * @param ctx       上下文
     * @param nickname  昵称
     * @param contactId 号码
     */
    public static void showCallMenu(final Context ctx, final String nickname, final String contactId) {
        ECListDialog dialog = new ECListDialog(ctx, R.array.chat_call);
        ;
        dialog.setOnDialogItemClickListener(new ECListDialog.OnDialogItemClickListener() {
            @Override
            public void onDialogItemClick(Dialog d, int position) {
                LogUtil.d("onDialogItemClick", "position " + position);

                if (position == 3) {
                    callVoIPAction(ctx, ECVoIPCallManager.CallType.VOICE, nickname, contactId, true);
                    return;
                }

                callVoIPAction(ctx, ECVoIPCallManager.CallType.values()[position], nickname, contactId, false);
            }
        });
        dialog.setTitle(R.string.ec_talk_mode_select);
        dialog.show();
    }

    static List<Integer> mChecks;

    /**
     * 提示选择呼叫编码设置
     *
     * @param ctx 上下文
     */
    public static void showCodecConfigMenu(final Context ctx) {
        final ECListDialog multiDialog = new ECListDialog(ctx, R.array.Codec_call);


        multiDialog.setOnDialogItemClickListener(false, new ECListDialog.OnDialogItemClickListener() {
            @Override
            public void onDialogItemClick(Dialog d, int position) {
                LogUtil.d("onDialogItemClick", "position " + position);
            }
        });
        if (mChecks == null) {
            mChecks = new ArrayList<Integer>();
//            mChecks.add(ECVoIPSetupManager.Codec.Codec_iLBC.ordinal());
            mChecks.add(ECVoIPSetupManager.Codec.Codec_G729.ordinal());
            mChecks.add(ECVoIPSetupManager.Codec.Codec_PCMU.ordinal());
//            mChecks.add(ECVoIPSetupManager.Codec.Codec_PCMA.ordinal());
            mChecks.add(ECVoIPSetupManager.Codec.Codec_H264.ordinal());
//            mChecks.add(ECVoIPSetupManager.Codec.Codec_SILK8K.ordinal());
//            mChecks.add(ECVoIPSetupManager.Codec.Codec_AMR.ordinal());
            mChecks.add(ECVoIPSetupManager.Codec.Codec_VP8.ordinal());
//            mChecks.add(ECVoIPSetupManager.Codec.Codec_SILK16K.ordinal());
//            mChecks.add(ECVoIPSetupManager.Codec.Codec_OPUS48.ordinal());
            mChecks.add(ECVoIPSetupManager.Codec.Codec_OPUS16.ordinal());
            mChecks.add(ECVoIPSetupManager.Codec.Codec_OPUS8.ordinal());
        }
        multiDialog.setChecks(mChecks);
        multiDialog.setTitle(R.string.ec_talk_codec_select);
        multiDialog.setButton(ECListDialog.BUTTON_POSITIVE, R.string.dialog_ok_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mChecks = multiDialog.getCheck();
                if (mChecks == null) {
                    ToastUtil.showMessage("设置失败，未选择任何编码");
                    return;
                }
                ECVoIPSetupManager setupManager = ECDevice.getECVoIPSetupManager();
                if (setupManager == null) {
                    ToastUtil.showMessage("设置失败，请先初始化SDK");
                    return;
                }
                Map<Integer, Object> map = new HashMap<Integer, Object>();
                map.put(0, ECVoIPSetupManager.Codec.Codec_G729);
                map.put(1, ECVoIPSetupManager.Codec.Codec_PCMU);
                map.put(2, ECVoIPSetupManager.Codec.Codec_H264);
                map.put(3, ECVoIPSetupManager.Codec.Codec_VP8);
                map.put(4, ECVoIPSetupManager.Codec.Codec_OPUS16);
                map.put(5, ECVoIPSetupManager.Codec.Codec_OPUS8);
                for (ECVoIPSetupManager.Codec code : ECVoIPSetupManager.Codec.values()) {
                    boolean enable = mChecks.contains(code.ordinal());

                    ECVoIPSetupManager.Codec c = (ECVoIPSetupManager.Codec) map.get(code.ordinal());
                    setupManager.setCodecEnabled(c, enable);
                }
                ToastUtil.showMessage("设置成功");
            }
        });
        multiDialog.show();
    }


    public static void startShowBaiDuMapAction(ChattingActivity mContext2,
                                               ECMessage iMessage) {

        if (iMessage == null || mContext2 == null) {
            return;
        }

        Intent intent = new Intent(mContext2, ShowBaiDuMapActivity.class);

        ECLocationMessageBody body = (ECLocationMessageBody) iMessage.getBody();
        LocationInfo locationInfo = new LocationInfo();
        locationInfo.setLat(body.getLatitude());
        locationInfo.setLon(body.getLongitude());
        locationInfo.setAddress(body.getTitle());
        intent.putExtra("location", locationInfo);

        mContext2.startActivity(intent);

    }

    public static void sendChangeAdminBR() {
        Intent intent = new Intent(INTENT_ACTION_CHANGE_ADMIN);
        CCPAppManager.getContext().sendBroadcast(intent);

    }
}

package com.yuntongxun.ecdemo.ui.chatting;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.yuntongxun.ecdemo.AvatorUtil;
import com.yuntongxun.ecdemo.ECApplication;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.ExceptionHandler;
import com.yuntongxun.ecdemo.common.dialog.ECListDialog;
import com.yuntongxun.ecdemo.common.utils.DateUtil;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.common.utils.FileAccessor;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.common.view.InfoItem;
import com.yuntongxun.ecdemo.common.view.SettingItem;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.pojo.FsRobot;
import com.yuntongxun.ecdemo.storage.ConversationSqlManager;
import com.yuntongxun.ecdemo.storage.IMessageSqlManager;
import com.yuntongxun.ecdemo.ui.BaseActivity;
import com.yuntongxun.ecdemo.ui.RestServerDefines;
import com.yuntongxun.ecdemo.ui.SDKCoreHelper;
import com.yuntongxun.ecdemo.ui.group.GroupInfoActivity;
import com.yuntongxun.ecdemo.ui.phonemodel.HttpMethods;
import com.yuntongxun.ecsdk.ECChatManager;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.platformtools.ECHandlerHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;


/**
 * Created by smileklvens on 2017/10/30.
 */

public class ChatingDetailAct extends BaseActivity {
    private static final int REQUESTCODE_GET_BG = 0x1;
    public static final String KEY_PICTUREPATH = "key_picturepath";
    public static final String KEY_PICTUREURI = "key_pictureuri";

    @BindView(R.id.tv_avatar)
    TextView mTvAvatar;
    @BindView(R.id.tv_name)
    TextView mTvName;
    @BindView(R.id.tv_robot)
    TextView mRobot;
    @BindView(R.id.set_top)
    SettingItem mSetTop;
    @BindView(R.id.no_disturbing)
    SettingItem mNoDisturbing;
    @BindView(R.id.set_bg)
    InfoItem mSetBg;
    @BindView(R.id.tv_clear_histroy)
    TextView mTvClearHistroy;
    @BindView(R.id.title_bar)
    TitleBar mTitleBar;
    private String sessionId;

    private boolean mClearChatmsg = false;

    /**
     * 选择图片拍照路径
     */
    private String mFilePath;
    public static final int REQUEST_CODE_TAKE_PICTURE = 0x3;
    public static final String EXTRA_SESSIONID = "extra_sessionid";
    private boolean mIsNoDisturbing;//是否已经设置免打扰

    private View vRot;
    private View full;

    @Override
    protected void initView(Bundle savedInstanceState) {
        initTooleBar(mTitleBar, true, "聊天详情");


          vRot=  findViewById(R.id.sv_rot);
          full=  findViewById(R.id.ll_full);





        mTitleBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        sessionId = getIntent().getStringExtra(EXTRA_SESSIONID);
        mSetBg.setLeftTitle("设置当前聊天背景");

        if (TextUtils.equals(RestServerDefines.FILE_ASSISTANT, sessionId)) {
            mTvAvatar.setBackgroundResource(R.drawable.file);
            mTvName.setText("文件助手");
        } else {
            AvatorUtil.getInstance().setAvatorPhoto(mTvAvatar, R.drawable.memer_bg, sessionId);
            mTvName.setText(AvatorUtil.getInstance().getMarkName(sessionId));
        }

        if(ConversationSqlManager.queryIsNoticeBySessionId(sessionId)){
            mNoDisturbing.setChecked(true);
            mIsNoDisturbing = true;
        }else {
            mIsNoDisturbing = false;
            mNoDisturbing.setChecked(false);
        }
        if(RestServerDefines.ROBOT.equalsIgnoreCase(sessionId)){
            full.setVisibility(View.GONE);
            vRot.setVisibility(View.VISIBLE);
            mTvName.setText("详情");
            getRobotInfo();
        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.chat_detail_act;
    }

    @Override
    protected void initWidgetAciotns() {
//        getDisturb(sessionId,"1","1");
        boolean isTop = ConversationSqlManager.querySessionisTopBySessionId(sessionId);//支持单人、群组
        if (isTop) {
            mSetTop.setChecked(true);
        } else {
            mSetTop.setChecked(false);
        }

        mSetTop.getCheckedTextView().setOnClickListener(new View.OnClickListener() {//置顶
            @Override
            public void onClick(View v) {
                setSessionToTop();
            }
        });



        mNoDisturbing.getCheckedTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//免打扰
                isNoDisturbing(!mIsNoDisturbing);





            }
        });
    }

    /**
     * 设置免打扰
     *
     * @param isNoDisturbing
     */
    private void isNoDisturbing(final boolean isNoDisturbing) {
        showCommonProcessDialog();

        ECDevice.setMuteNotification(sessionId, isNoDisturbing, new ECDevice.OnSetDisturbListener() {
            @Override
            public void onResult(ECError ecError) {
                dismissCommonPostingDialog();
                if(ecError.errorCode==SdkErrorCode.REQUEST_SUCCESS){
                            mNoDisturbing.toggle();
                            ToastUtil.showMessage("设置成功");
                            ConversationSqlManager.updateSessionIdNotify(isNoDisturbing ? "2" : "1", sessionId);
                            if(!isNoDisturbing){
                            }
                            mIsNoDisturbing =! mIsNoDisturbing;
                        } else {
                            ToastUtil.showMessage("设置失败");
                        }
            }
        });

    }

    private void setSessionToTop() {
        showCommonProcessDialog();
        final boolean isTop = ConversationSqlManager.querySessionisTopBySessionId(sessionId);
        ECChatManager chatManager = SDKCoreHelper.getECChatManager();
        if (chatManager == null) {
            return;
        }
        chatManager.setSessionToTop(sessionId, !isTop, new ECChatManager.OnSetContactToTopListener() {
            @Override
            public void onSetContactResult(ECError error, String contact) {

                dismissCommonPostingDialog();
                if (error.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
                    mSetTop.toggle();
                    ConversationSqlManager.updateSessionToTop(sessionId, !isTop);
                    ToastUtil.showMessage("设置成功");
                } else {
                    ToastUtil.showMessage("设置失败");
                }
            }
        });

    }


    @OnClick({R.id.set_bg, R.id.tv_clear_histroy})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.set_bg:
                final String[] menu = new String[]{"从相册选择", "拍照","清除聊天背景"};
                ;

                ECListDialog dialog = new ECListDialog(mContext, menu);
                dialog.setOnDialogItemClickListener(new ECListDialog.OnDialogItemClickListener() {
                    @Override
                    public void onDialogItemClick(Dialog d, int position) {
                        if (position == 0) {
                            handleSelectImageIntent();
                        } else if(position==1) {
                            handleTackPicture();
                        }else {
                            ConversationSqlManager.updateBg(sessionId);
                        }
                    }
                });
                dialog.setTitle(AvatorUtil.getInstance().getMarkName(sessionId));
                dialog.show();
                break;
            case R.id.tv_clear_histroy:
                clearMsg();
                break;
        }
    }


    public static final int REQUEST_CODE_LOAD_IMAGE = 0x4;

    private void handleSelectImageIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra("crop", true);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, REQUESTCODE_GET_BG);
    }


    private void clearMsg() {
        showCommonProcessDialog();
        ECHandlerHelper handlerHelper = new ECHandlerHelper();
        handlerHelper.postRunnOnThead(new Runnable() {
            @Override
            public void run() {
                IMessageSqlManager.deleteChattingMessage(sessionId);
                ToastUtil.showMessage(R.string.clear_msg_success);
                mClearChatmsg = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissCommonPostingDialog();
                    }
                });
            }
        });
    }


    private void goBack() {
        Intent intent = new Intent();
        intent.putExtra(GroupInfoActivity.EXTRA_RELOAD, mClearChatmsg);
        setResult(RESULT_OK, intent);
        finish();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        if (requestCode == REQUESTCODE_GET_BG) {
            if (data == null) {
                return;
            }
            try {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    withBgBack(getRealPathFromUri(mContext, selectedImage));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("TAG-->Error", e.toString());
            }
        }

        if (requestCode == REQUEST_CODE_TAKE_PICTURE) {//拍照
            if (!TextUtils.isEmpty(mFilePath)) {
                withBgBack(mFilePath);
            }
        }
    }


    private void handleTackPicture() {
        if (!FileAccessor.isExistExternalStore()) {
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        File file = FileAccessor.getTackPicFilePath();

        if (file != null) {
            Uri uri = null;
            if (Build.VERSION.SDK_INT >= 24) {
                uri = FileProvider.getUriForFile(ECApplication.getInstance(), ECApplication.getInstance().getPackageName() + ".fileprovider", file);
            } else {
                uri = Uri.fromFile(file);
            }
            if (uri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            }
            mFilePath = file.getAbsolutePath();
        }
        intent.putExtra("return-data", true);
        startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
    }


    private void withBgBack(String picturePath) {
        Intent intent = new Intent();
        intent.putExtra(KEY_PICTUREPATH, picturePath);
        setResult(RESULT_OK, intent);
        finish();
    }

    //获取路径

    /**
     * 根据Uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    public static String getRealPathFromUri(Context context, Uri uri) {
        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion >= 19) { // api >= 19
            return getRealPathFromUriAboveApi19(context, uri);
        } else { // api < 19
            return getRealPathFromUriBelowAPI19(context, uri);
        }
    }

    /**
     * 适配api19以下(不包括api19),根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    private static String getRealPathFromUriBelowAPI19(Context context, Uri uri) {
        return getDataColumn(context, uri, null, null);
    }

    /**
     * 适配api19及以上,根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    @SuppressLint("NewApi")
    private static String getRealPathFromUriAboveApi19(Context context, Uri uri) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是document类型的 uri, 则通过document id来进行处理
            String documentId = DocumentsContract.getDocumentId(uri);
            if (isMediaDocument(uri)) { // MediaProvider
                // 使用':'分割
                String id = documentId.split(":")[1];

                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = {id};
                filePath = getDataColumn(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                filePath = getDataColumn(context, contentUri, null, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是 content 类型的 Uri
            filePath = getDataColumn(context, uri, null, null);
        } else if ("file".equals(uri.getScheme())) {
            // 如果是 file 类型的 Uri,直接获取图片对应的路径
            filePath = uri.getPath();
        }
        return filePath;
    }

    /**
     * 获取数据库表中的 _data 列，即返回Uri对应的文件路径
     *
     * @return
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String path = null;

        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is MediaProvider
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is DownloadsProvider
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }


    /**
     * 免打扰状态获取
     */
    private void getDisturb(final String sessionId, String pageNo, String pageSize) {

        Observer<Object> subscriber = new Observer<Object>() {
            @Override
            public void onComplete() {
                LogUtil.e("onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                ToastUtil.showMessage("获取失败");
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
                        String error = "";
                        JSONObject j = new JSONObject(s);
                        if (j != null && j.has("statusMsg")) {
                            error = j.getString("statusMsg");
                        }

                        int code = 0;
                        if (j != null && j.has("statusCode")) {
                            code = Integer.parseInt(j.getString("statusCode"));
                        }

                        if (DemoUtils.isTrue(s)) {
                            if (j != null && j.has("result")) {
                                JSONArray result = j.getJSONArray("result");
                                ArrayList<String> disturbs = new ArrayList<>();
                                for (int i = 0; i <result.length() ; i++) {
                                    String re = (String) result.get(0);
                                    disturbs.add(re);
                                }

                                if (!disturbs.isEmpty()) {
                                    mIsNoDisturbing = disturbs.contains(sessionId);
                                    mNoDisturbing.setChecked(mIsNoDisturbing);
                                }
                            }
                        } else {
                            ExceptionHandler.converToastMsg(code, error);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        String time = DateUtil.formatNowDate(new Date());
        String url = getSig(time);
        JSONObject map = HttpMethods.buildGetDisturb(CCPAppManager.getUserId(), "1", "20");
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), map.toString());
        HttpMethods.getInstance(time).getDisturb(subscriber, RestServerDefines.APPKER, url, body);

    }
    private void getRobotInfo() {

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
                        String error = "";
                        JSONObject j = new JSONObject(s);
                        if (j != null && j.has("statusMsg")) {
                            error = j.getString("statusMsg");
                        }

                        int code = 0;
                        if (j != null && j.has("statusCode")) {
                            code = Integer.parseInt(j.getString("statusCode"));
                        }

                        if (DemoUtils.isTrue(s)) {
                              FsRobot robot =  CCPAppManager.getRobot();
                             if(robot!=null){
                                 mRobot.setText(robot.getDesc());
                             }
                        } else {
                            ExceptionHandler.converToastMsg(code, error);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        String time = DateUtil.formatNowDate(new Date());
        String url = getSig(time);
//        JSONObject map = HttpMethods.buildGetDisturb(CCPAppManager.getUserId(), "1", "20");
//        RequestBody body = RequestBody.create(MediaType.parse("application/json"), map.toString());
        HttpMethods.getInstance(time).GetRobotsInfo(subscriber, RestServerDefines.APPKER, url,sessionId.substring(6));

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


}

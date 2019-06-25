package com.yuntongxun.ecdemo.ui.personcenter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yuntongxun.ecdemo.ECApplication;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.utils.Base64;
import com.yuntongxun.ecdemo.common.utils.DateUtil;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.common.utils.GetImageUtils;
import com.yuntongxun.ecdemo.common.utils.ImageLoader;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.SharedPreferencesUtils;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.common.view.SettingItem;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.ui.BaseActivity;
import com.yuntongxun.ecdemo.ui.RestServerDefines;
import com.yuntongxun.ecdemo.ui.phonemodel.HttpMethods;
import com.yuntongxun.ecsdk.PersonInfo;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
 * Created by luhuashan on 17/8/21.
 * email huashan2007@sina.cn
 */
public class PersonInfoUI extends BaseActivity {

    @BindView(R.id.title_bar)
    TitleBar titleBar;
    @BindView(R.id.iv_my_header)
    ImageView ivMyHeader;
    @BindView(R.id.tv_my_name)
    TextView tvMyName;
    @BindView(R.id.iv_goo)
    ImageView ivGoo;
    @BindView(R.id.p_touxiang)
    RelativeLayout pTouxiang;
    @BindView(R.id.tv_my_nick)
    TextView tvMyNick;
    @BindView(R.id.iv_go)
    ImageView ivGo;
    @BindView(R.id.p_nickname)
    RelativeLayout pNickname;
    @BindView(R.id.tv_my_sign)
    TextView tvMySign;
    @BindView(R.id.iv_go2)
    ImageView ivGo2;
    @BindView(R.id.p_sign)
    RelativeLayout pSign;
    @BindView(R.id.tv_my_sex)
    TextView tvMySex;
    @BindView(R.id.iv_go3)
    ImageView ivGo3;
    @BindView(R.id.p_sex)
    RelativeLayout pSex;
    @BindView(R.id.tv_my_age)
    TextView tvMyAge;
    @BindView(R.id.iv_go4)
    ImageView ivGo4;
    @BindView(R.id.p_age)
    RelativeLayout pAge;

    @BindView(R.id.info_msg_notify)
    SettingItem infoMsgNotify;


    @Override
    protected void onResume() {
        super.onResume();

        handPersonInfo();
    }

    private void handPersonInfo() {
        PersonInfo info = CCPAppManager.getPersonInfo();
        if (info != null) {
            tvMyNick.setText(info.getNickName());
            tvMySign.setText(info.getSign());
            tvMySex.setText(info.getSex() == PersonInfo.Sex.MALE ? "男" : "女");

            String age = info.getBirth();
            if (TextUtils.isEmpty(age)) {
                tvMyAge.setText(CCPAppManager.calculateDatePoor("28岁"));
            } else {
                tvMyAge.setText(CCPAppManager.calculateDatePoor(info.getBirth() + "岁"));
            }
        }
    }


    @Override
    protected void initView(Bundle savedInstanceState) {

        initTooleBar(titleBar, true, "个人信息");

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

        String s = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.FRIEND_TAG, "");
        if ("1".equalsIgnoreCase(s)) {
            infoMsgNotify.getCheckedTextView().setChecked(true);
        }

        infoMsgNotify.getCheckedTextView().setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateGroupNewMsgNotify();
                    }
                });
    }

    private void updateGroupNewMsgNotify() {
        showCommonProcessDialog();
        try {
            if (infoMsgNotify == null) {
                return;
            }
            infoMsgNotify.toggle();
            boolean checked = infoMsgNotify.isChecked();
            final String isNeed = checked ? "1" : "0";

            Observer<Object> subscriber = new Observer<Object>() {
                @Override
                public void onError(Throwable e) {
                    dismissCommonPostingDialog();
                    LogUtil.e(e.toString());
                    ToastUtil.showMessage("设置失败");
                }

                @Override
                public void onComplete() {
//                    LogUtil.e("onCompleted");
                    dismissCommonPostingDialog();
                }

                @Override
                public void onSubscribe(Disposable d) {
                }

                @Override
                public void onNext(Object movieEntity) {
                    dismissCommonPostingDialog();
                    if (movieEntity != null) {
                        LogUtil.e(movieEntity.toString());
                        ResponseBody body = (ResponseBody) movieEntity;
                        try {
                            String s = new String(body.bytes());
                            if (DemoUtils.isTrue(s)) {
                                ToastUtil.showMessage("设置成功");

                                SharedPreferencesUtils.setParam(PersonInfoUI.this, SharedPreferencesUtils.FRIEND_TAG, isNeed);

                            } else {
                                ToastUtil.showMessage("设置失败");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            String time = DateUtil.formatNowDate(new Date());
            String url = getSig(time);
            JSONObject map = HttpMethods.buildVeriInfo(CCPAppManager.getUserId(), isNeed);
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), map.toString());
            HttpMethods.getInstance(time).setUserVerify(subscriber, RestServerDefines.APPKER, url, body);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    @Override
    protected int getLayoutId() {
        return R.layout.person_info;
    }

    @Override
    protected void initWidgetAciotns() {

    }

    private String touxiangpath;


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

    public String getAuth(String time) {
        String s = RestServerDefines.APPKER + ":" + time;
        return Base64.encode(s.getBytes());
    }


    private final CallBack cb = new CallBack() {
        @Override
        public void onResult(int code, final Bitmap b) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ivMyHeader != null) {
//                        if (GetImageUtils.isFileExist()) {
//                            ImageLoader.getInstance().displayCricleImage(PersonInfoUI.this, GetImageUtils.getPicFile(), ivMyHeader);
//                        }

                        if (!TextUtils.isEmpty(ECApplication.photoUrl)) {
                            ImageLoader.getInstance().displayCricleImage(
                                    mContext, ECApplication.photoUrl
                                    , ivMyHeader);
                        } else {
                            if (GetImageUtils.isFileExist()) {
                                ImageLoader.getInstance().displayCricleImage(PersonInfoUI.this, GetImageUtils.getPicFile(), ivMyHeader);
                            } else {
                                ImageLoader.getInstance().displayCricleImage(PersonInfoUI.this, R.drawable.header_woman, ivMyHeader);
                            }
                        }


                        ivMyHeader.invalidate();
                    }
                }
            });
        }
    };


    interface CallBack {

        void onResult(int code, Bitmap b);
    }

    public void uploadFile(String urls, String filePath, String auth, CallBack cb, Bitmap b) {

        HttpURLConnection conn = null;
        try {
            // (HttpConst.uploadImage 上传到服务器的地址
            URL url = new URL(urls);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            // 设置请求方法
            conn.setRequestMethod("POST");

            // 设置header
            conn.setRequestProperty("Accept", "application/json");
//            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Content-Type",
                    "application/octet-stream;; charset=utf-8");
            conn.setRequestProperty("Authorization", auth);
            File file = new File(filePath);
            conn.setRequestProperty("Authorization", auth);
            // 获取写输入流
            OutputStream out = new DataOutputStream(conn.getOutputStream());
            // 获取上传文件

            // 要上传的数据
            StringBuffer strBuf = null;


            // 获取文件流
            FileInputStream fileInputStream = new FileInputStream(file);
            DataInputStream inputStream = new DataInputStream(fileInputStream);

            // 每次上传文件的大小(文件会被拆成几份上传)
            int bytes = 0;
            // 计算上传进度
            float count = 0;
            // 获取文件总大小
            int fileSize = fileInputStream.available();
            // 每次上传的大小
            byte[] bufferOut = new byte[1024];
            // 上传文件
            while ((bytes = inputStream.read(bufferOut)) != -1) {
                // 上传文件(一份)
                out.write(bufferOut, 0, bytes);
                // 计算当前已上传的大小
                count += bytes;
                // 打印上传文件进度(已上传除以总大*100就是进度)
            }

            // 关闭文件流
//            inputStream.close();


            // 至此上传代码完毕

            // 总结上传数据的流程：preFix + payLoad(标识服务器表单接收文件的格式) + 文件(以流的形式) + preFix
            // 文本与图片的不同,仅仅只在payLoad那一处的后缀的不同而已。

            // 输出所有数据到服务器
//            out.flush();

            // 关闭网络输出流
//            out.close();

            // 重新构造一个StringBuffer,用来存放从服务器获取到的数据
            int cah = conn.getResponseCode();
            if (cah == 200) {
                InputStream isInputStream = conn.getInputStream();
                int ch;
                StringBuffer buffer = new StringBuffer();
                while ((ch = isInputStream.read()) != -1) {
                    buffer.append((char) ch);
                }
                Log.e("server", buffer.toString());
                if (DemoUtils.isTrue(buffer.toString())) {
                    JSONObject o = new JSONObject(buffer.toString());

                    if (o.has("avatar")) {
                        ECApplication.photoUrl = o.getString("avatar");
                    }
                }
                if (cb != null) {
                    cb.onResult(cah, b);
                }
            } else {
                Log.e("server", "server code = " + cah);
            }

            // 打印服务器返回的数据

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GetImageUtils.REQUEST_CODE_FROM_CAMERA:
                if (resultCode == Activity.RESULT_CANCELED) {
                    GetImageUtils.deleteImageUri(this, GetImageUtils.imageUriFromCamera);
                } else {
                    GetImageUtils.startPhotoZoom(this, GetImageUtils.imageUriFromCamera);
                }
                break;
            case GetImageUtils.REQUEST_CODE_FROM_ALBUM:
                if (resultCode == Activity.RESULT_CANCELED) {
                    return;
                } else {
                    GetImageUtils.startPhotoZoom(this, data.getData());
                }
                break;
            case GetImageUtils.REQUEST_CODE_FROM_CUTTING:
                if (resultCode == Activity.RESULT_CANCELED) {
                    return;
                }
                Bundle extras = data.getExtras();
                if (extras != null) {
                    final Bitmap photo = extras.getParcelable("data");
                    try {
                        String fileName = CCPAppManager.getUserId() + "touxiang.jpg";
                        touxiangpath = GetImageUtils.saveFile(this, photo, fileName);
                        final Bitmap bitmap = GetImageUtils.getSmallBitmap(touxiangpath, 100, 100);
                        if (touxiangpath == null) {
                            ToastUtil.showMessage("上传失败，请重新上传");
                            return;
                        }
                        String time = DateUtil.formatNowDate(new Date());
                        String sig = getSig(time);
                        final String auth = getAuth(time);

                        StringBuilder sb = new StringBuilder();

                        sb.append(RestServerDefines.Friend+"/2013-12-26/Application/" + RestServerDefines.APPKER + "/IM/uploadAvatar");

                        sb.append("?sig=");
                        sb.append(sig);
                        sb.append("&fileName=" + fileName);
                        sb.append("&useracc=");
                        sb.append(RestServerDefines.APPKER + "%23" + CCPAppManager.getUserId());

                        final String url = sb.toString();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                uploadFile(url, touxiangpath, auth, cb, bitmap);
                            }
                        }).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }


    @OnClick({R.id.p_touxiang, R.id.p_nickname, R.id.p_sex, R.id.p_age, R.id.p_sign})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.p_touxiang:

                GetImageUtils.showImagePickDialog(this, this, ivMyHeader);
                break;
            case R.id.p_nickname:

                startActivity(new Intent(this, SetInfoUI.class).putExtra(SetInfoUI.Type, 0));
                break;
            case R.id.p_sex:
                startActivity(new Intent(this, SetInfoUI.class).putExtra(SetInfoUI.Type, 3));
                break;
            case R.id.p_age:
                startActivity(new Intent(this, SetInfoUI.class).putExtra(SetInfoUI.Type, 4));

                break;
            case R.id.p_sign:
                startActivity(new Intent(this, SetInfoUI.class).putExtra(SetInfoUI.Type, 1));
                break;
        }
    }
}

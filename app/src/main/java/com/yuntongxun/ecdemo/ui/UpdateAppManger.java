package com.yuntongxun.ecdemo.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.dialog.ECAlertDialog;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class UpdateAppManger {
    private Context mContext;
    private static final String APK_NAME = "rongliankuailiao.apk";
    // 更新进度条
    private ProgressBar mUpdateProgressBar;
    // 下载对话框
    private AlertDialog mDownloadDialog;

    private String updateLog = "软件有新版本，要更新吗？";

    public static final String URL = "http://download.cloopen.net:8050/im/tiyan.apk";
    private TextView tv_percentage;

    public UpdateAppManger(Context context) {
        this.mContext = context;
    }

    /**
     * 显示软件更新对话框
     */
    ECAlertDialog buildAlert;

    public void showNoticeDialog(String params, final String url) {
        if (params == null || TextUtils.isEmpty(params)) {
            ToastUtil.showMessage("更新失败，请重试");
            return;
        }



            buildAlert = ECAlertDialog.buildAlert(mContext
                    , "有新版本了", "暂不更新", "立即更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (buildAlert != null) {
                                buildAlert.dismiss();
                            }
                            buildAlert.setCanceledOnTouchOutside(false);
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showDownloadDialog(url);
                        }
                    }
            );
            buildAlert.setTitle(R.string.app_tip);
            buildAlert.show();

    }


    /**
     * 显示软件下载对话框
     */
    private void showDownloadDialog(String url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("正在更新");
        //给对话框增加进度条
        builder.setOnKeyListener(keylistener).setCancelable(false);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.view_progress, null);
        mUpdateProgressBar = (ProgressBar) v.findViewById(R.id.progress);
        tv_percentage = (TextView) v.findViewById(R.id.tv_percentage);
        mDownloadDialog = builder.setView(v).create();
        mDownloadDialog.show();

        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            downloadApk(url);
        } else {
            ToastUtil.showMessage("SD卡不可用，请插入SD卡");
        }

    }

    /**
     * 安装APK
     */
    private void installApk() {
        File filePath = getFilePath();
        File file = new File(filePath, APK_NAME);

        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 24) {//判读版本是否在7.0以上
            Uri apkUri =
                    FileProvider.getUriForFile(CCPAppManager.getContext(),
                           CCPAppManager.getContext().getPackageName() + ".fileprovider",
                            file);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            installIntent.setDataAndType(Uri.fromFile(file),
                    "application/vnd.android.package-archive");
        }

        mContext.startActivity(installIntent);
        ((Activity) mContext).finish();
    }



    /**
     * 下载APK文件
     */
    private void downloadApk(String url) {
        new Thread(new DownloadFile(url)).start();
    }


    private File getFilePath() {
        File dir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath()
                + File.separator
                + mContext.getPackageName()
                + File.separator + "update");
        return dir;
    }

    private void savaFile(byte[] bs) {
        File file = new File(getFilePath(), APK_NAME);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            try {
                fileOutputStream.write(bs);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    OnKeyListener keylistener = new OnKeyListener() {
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                return true;
            } else {
                return false;
            }
        }
    };


    //++++++++++++++========下载apk带进度=============================
    public static final int UPGRADE_DONE = 0;
    public static final int UPGRADE_DOING = 1;
    public static final int UPGRADE_ERROR = -1;

    Handler upgradeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (!Thread.currentThread().isInterrupted()) {

                if (true) {
                    switch (msg.what) {
                        case UPGRADE_DOING:
                            mUpdateProgressBar.setProgress(msg.arg1);
                            tv_percentage.setText("已为您加载了：" + msg.arg1 + "%");
                            break;
                        case UPGRADE_DONE:
                            mDownloadDialog.dismiss();
                            ToastUtil.showMessage("下载完成");
                            installApk();
                            break;
                        case UPGRADE_ERROR:
                            mDownloadDialog.dismiss();
                            onUpdateError(msg);
                            break;
                    }
                } else {//fix hot

                }
            }
        }
    };

    public void onUpdateError(Message msg) {
        String error = msg.obj != null ? msg.obj.toString() : null;
        ToastUtil.showMessage(error);
    }


    private class DownloadFile implements Runnable {
        private String url = null;

        private DownloadFile(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(this.url);
                HttpResponse response = client.execute(get);
                int stateCode = response.getStatusLine().getStatusCode();
                if (stateCode != HttpStatus.SC_OK) {//网络异常
                    Message msg = upgradeHandler
                            .obtainMessage(UPGRADE_ERROR);
                    msg.obj = mContext.getResources().getString(
                            R.string.system_maintenance);
                    upgradeHandler.sendMessage(msg);
                    return;
                }
                HttpEntity entity = response.getEntity();
                float length = entity.getContentLength();
                InputStream is = entity.getContent();

                if (is == null) {
                    Message msg = upgradeHandler
                            .obtainMessage(UPGRADE_ERROR);
                    msg.obj = "返回数据为空";
                    upgradeHandler.sendMessage(msg);
                    return;
                }
                File dir = getFilePath();
                if (dir.exists()) {
                    deleteDir(dir);
                    dir.mkdirs();
                } else {
                    dir.mkdirs();
                }

                File file;
                if (false) {
                    file = new File(dir, "hotfix.apatch");
                } else {
                    file = new File(dir, APK_NAME);
                }

                FileOutputStream fileOutputStream = new FileOutputStream(file);
                byte[] buf = new byte[1024];
                int len;
                float count = 0;
                while ((len = is.read(buf)) != -1) {
                    fileOutputStream.write(buf, 0, len);
                    count += len;
                    Message msg = upgradeHandler.obtainMessage(UPGRADE_DOING);
                    msg.arg1 = (int) (count * 100 / length);
                    upgradeHandler.sendMessage(msg);
                }
                // 文件从内存存到sd卡文件中去
                fileOutputStream.flush();
                fileOutputStream.close();
                is.close();
                // 发送成功消息
                Message msg = upgradeHandler.obtainMessage(UPGRADE_DONE);
                msg.arg1 = 0;
                upgradeHandler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = upgradeHandler.obtainMessage(UPGRADE_ERROR);
                msg.obj = mContext.getResources().getString(
                        R.string.check_new_version_exception);
                upgradeHandler.sendMessage(msg);
            }
        }
    }

    public void deleteDir(File dir) {

        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                deleteDir(f);
            }
        }
    }

    private void downloadFixApk() {

    }

}

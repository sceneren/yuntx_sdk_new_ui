package com.yuntongxun.ecdemo.ui.personcenter;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.utils.DateUtil;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.storage.FriendMessageSqlManager;
import com.yuntongxun.ecdemo.ui.BaseActivity;
import com.yuntongxun.ecdemo.ui.RestServerDefines;
import com.yuntongxun.ecdemo.ui.phonemodel.HttpMethods;

import org.greenrobot.eventbus.EventBus;
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
 * Created by luhuashan on 17/8/28.
 * email huashan2007@sina.cn
 */
public class DeleteFriendUI extends BaseActivity {
    @BindView(R.id.title_bar)
    TitleBar titleBar;
    @BindView(R.id.bu_delete)
    Button buDelete;

    private String userId;

    @Override
    protected void initView(Bundle savedInstanceState) {

        userId = getIntent().getStringExtra("userId");

        initTooleBar(titleBar, true, "删除好友");


    }

    @Override
    protected int getLayoutId() {
        return R.layout.delete_friend;
    }

    @Override
    protected void initWidgetAciotns() {

    }



    @OnClick(R.id.bu_delete)
    public void onViewClicked(View v) {
        if(v.getId()==R.id.bu_delete){

            showCommonProcessDialog();

            Observer<Object> subscriber =new Observer<Object>(){
                @Override
                public void onComplete() {
                    dismissCommonPostingDialog();
                }
                @Override
                public void onError(Throwable e){
                    ToastUtil.showMessage("删除失败");
                    dismissCommonPostingDialog();
                }
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(Object movieEntity){
                    if(movieEntity!=null){
                        LogUtil.e(movieEntity.toString());
                        ResponseBody body = (ResponseBody)movieEntity;
                        try {
                            String s = new String(body.bytes());
                            String error ="";
                            JSONObject j = new JSONObject(s);
                            if(j!=null&&j.has("statusMsg")){
                                error = j.getString("statusMsg");
                            }
                            if(DemoUtils.isTrue(s)){
                                EventBus.getDefault().post(
                                        new FirstEvent("FirstEvent btn clicked"));
                                ToastUtil.showMessage("删除成功");
                                FriendMessageSqlManager.delFriend(userId);
                                finish();
                            }else {
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
            JSONObject map = HttpMethods.buildDeleteFriend(CCPAppManager.getUserId(), userId);
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), map.toString());
            HttpMethods.getInstance(time).delFriend(subscriber, RestServerDefines.APPKER, url, body);

        }

    }

    public  String getSig(String stime){
        String s = RestServerDefines.APPKER+CCPAppManager.getAppToken()+stime;
        return getMessageDigest(s.getBytes());
    }

    public static String getMessageDigest(byte[] input) {
        char[] source = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'a', 'b', 'c', 'd', 'e', 'f' };
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

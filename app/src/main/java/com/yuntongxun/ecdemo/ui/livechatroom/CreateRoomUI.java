package com.yuntongxun.ecdemo.ui.livechatroom;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.yuntongxun.ecdemo.ECCircumscription;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.bean.CreateRoomRequest;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.base.CCPClearEditText;
import com.yuntongxun.ecdemo.common.utils.Base64;
import com.yuntongxun.ecdemo.common.utils.DateUtil;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.httpUtil.interceptor.TokenInterceptor;
import com.yuntongxun.ecdemo.ui.ECSuperActivity;
import com.yuntongxun.ecdemo.ui.RestServerDefines;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by luhuashan on 17/5/16.
 */
public class CreateRoomUI extends ECSuperActivity implements View.OnClickListener{

    
    @Override
    protected int getLayoutId(){
        return R.layout.new_room;
    }




    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt,
                -1, null,
                null,
                getString(R.string.app_create_room), null, this);


        name = (CCPClearEditText)findViewById(R.id.room_name);
        notice = (CCPClearEditText)findViewById(R.id.room_notice);
        ext = (CCPClearEditText)findViewById(R.id.room_ext);




    }

    private CCPClearEditText name;
    private CCPClearEditText notice;
    private CCPClearEditText ext;



    private CreateRoomRequest build(){


        CreateRoomRequest req = new CreateRoomRequest();
        req.name = name.getText().toString().trim();
        req.declared = notice.getText().toString().trim();
        req.ext=notice.getText().toString().trim();

        return req;

    }


    private String time ;

    public  String getAuth(){
        String s = CCPAppManager.getClientUser().getAppKey()+":"+ time;
        return Base64.encode(s.getBytes());
    }

    public  String getSig(){
        String s =CCPAppManager.getClientUser().getAppKey()+CCPAppManager.getClientUser().getAppToken()+time;
        return getMessageDigest(s.getBytes());
    }
    static final MediaType JSON= MediaType.parse("application/json; charset=utf-8");

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

    private Context ctx;

    @Override
    public void onClick(View v){
        switch (v.getId()){

            case R.id.btn_left:
                hideSoftKeyboard();
                finish();
                break;

            case R.id.create:


                String rname = name.getText().toString().trim();
                String declared = notice.getText().toString().trim();
                String ext=notice.getText().toString().trim();


                if(TextUtils.isEmpty(rname)||TextUtils.isEmpty(declared)||TextUtils.isEmpty(ext)){

                    ToastUtil.showMessage("输入不能为空");
                    return;
                }
                if(rname.length()> ECCircumscription.GROUP_CARD||declared.length()> ECCircumscription.GROUP_CARD||ext.length()> ECCircumscription.GROUP_CARD){

                    ToastUtil.showMessage("抱歉、您输入的超过最大限制咯");
                    return;
                }

                if(!DemoUtils.isValidNormalAccount(rname)||!DemoUtils.isValidNormalAccount(declared)||!DemoUtils.isValidNormalAccount(ext)){

                    ToastUtil.showMessage("你的输入包含非法字符");
                    return;
                }

                showCommonProcessDialog("请稍等");

                time = DateUtil.formatNowDate(new Date());

                String u = RestServerDefines.SERVER+"/2013-12-26/Application/"+CCPAppManager.getClientUser().getAppKey()+"/IM/createChatRoom?sig="+getSig();
                Log.e("aa", u);


                CreateRoomRequest r =   build();


                OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
                        .addInterceptor(new TokenInterceptor(getAuth()))
                        .build();
                JSONObject object = new JSONObject();
                try {
                    object.put("name", r.name);
                    object.put("ext",r.ext);
                    object.put("declared", r.declared);
                    object.put("pushUrl",r.pullUrl);
                    object.put("pullUrl",r.pushUrl);
                    object.put("creator",CCPAppManager.getClientUser().getUserId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestBody formBody = RequestBody.create(JSON,object.toString());
                Request request = new Request.Builder()
                        .url(u)
                        .post(formBody)
                        .build();
                Call call = mOkHttpClient.newCall(request);
                call.enqueue(new Callback(){
                    @Override
                    public void onFailure(Call call, IOException e){


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissCommonPostingDialog();
                                Toast.makeText(getApplicationContext(), "创建失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException{
                        final String str = response.body().string();


                        runOnUiThread(new Runnable(){
                            @Override
                            public void run(){
                                dismissCommonPostingDialog();
                                if(isSuccess(str)){
                                    Toast.makeText(getApplicationContext(), "创建成功", Toast.LENGTH_SHORT).show();
                                    finish();
                                }else {
                                    if(str!=null&&str.contains("627999")){
                                        Toast.makeText(getApplicationContext(), "您的输入包含非法字符", Toast.LENGTH_SHORT).show();
                                    }else if(str!=null&&str.contains("620224")){
                                        Toast.makeText(getApplicationContext(), "房间数已达最大上限", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(), "创建失败:"+getCode(str), Toast.LENGTH_SHORT).show();
                                    }


                                }

                            }
                        });
                    }

                });



                break;



        }
    }



}

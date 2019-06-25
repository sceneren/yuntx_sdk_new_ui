package com.yuntongxun.ecdemo.ui.mvp.group;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.utils.Base64;
import com.yuntongxun.ecdemo.common.utils.DateUtil;
import com.yuntongxun.ecdemo.exception.ECEncoderQrException;
import com.yuntongxun.ecdemo.ui.ECSuperActivity;
import com.yuntongxun.ecsdk.im.ECGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by luhuashan on 17/4/7.
 */
public class GroupQRUI extends ECSuperActivity implements IGroupView, View.OnClickListener{

    private GroupQrPresenter presenter;

    private ImageView qrImg ;

    private ECGroup group;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_group_qr;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt,
                -1, null,
                null,
                getString(R.string.str_group_notice_erweima), null, this);

        group =(ECGroup)getIntent().getParcelableExtra("group");
        if(group ==null){
            finish();
        }

        presenter = new GroupQrPresenter(this,new GroupQrModelImpl());
        qrImg = (ImageView)findViewById(R.id.group_qr_img);

        try {
            Bitmap bitmap = presenter.getGroupQrCode(buildString());
            presenter.showGroupQrCode(bitmap);
        } catch (ECEncoderQrException e) {
            finish();
        }
    }



    @Override
    public void setGroupQrImg(Bitmap bitmap) {

        qrImg.setImageBitmap(bitmap);
    }



    public String buildString() throws ECEncoderQrException {


        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("groupid",group.getGroupId());
            jsonObject.put("creator",group.getOwner());
            jsonObject.put("name",group.getName());
            jsonObject.put("time", DateUtil.sFormatNowDate(new Date()));
            jsonObject.put("count",group.getCount());

            String data = Base64.encode(jsonObject.toString().getBytes());
            JSONObject obj = new JSONObject();
            obj.put("url","joinGroup");
            obj.put("data",data);
            return obj.toString();
        } catch (JSONException e) {
            throw new ECEncoderQrException(e);
        }

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.btn_left:
                hideSoftKeyboard();
                finish();
                break;



        }

    }
}

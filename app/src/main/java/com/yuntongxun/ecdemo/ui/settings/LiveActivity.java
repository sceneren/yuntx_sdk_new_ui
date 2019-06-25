//package com.yuntongxun.ecdemo.ui.settings;
//
//import android.os.Bundle;
//import android.view.View;
//import android.widget.EditText;
//
//import com.yuntongxun.ecdemo.R;
//import com.yuntongxun.ecdemo.ui.ECSuperActivity;
//import com.yuntongxun.ecsdk.ECDevice;
//import com.yuntongxun.ecsdk.voip.video.ECOpenGlView;
//
///**
// * Created by luhuashan on 17/4/20.
// * email huashan2007@sina.cn
// */
//public class LiveActivity extends ECSuperActivity implements View.OnClickListener {
//
//
//    private EditText ed ;
//    @Override
//    protected int getLayoutId() {
//        return R.layout.live;
//    }
//
//    private long roomId ;
//    private ECOpenGlView view ;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt,
//                -1, null,
//                null,
//                getString(R.string.app_company_live), null, this);
//        ed = (EditText) findViewById(R.id.et);
//        view = (ECOpenGlView) findViewById(R.id.ec_opengl);
//    }
//
//    public void onClick(View v){
//
//        switch (v.getId()){
//
//            case R.id.begin:
//                ECDevice.getECVoIPSetupManager().setGlDisplayWindow(view,null);
//                roomId =  ECDevice.getLiveStreamManager().createLiveStream(0);
//                ECDevice.getLiveStreamManager().setVideoProfileLiveStream(roomId, 1, 2);
//                ECDevice.getLiveStreamManager().pushLiveStream(roomId, ed.getText().toString().trim(), view);
//                break;
//            case R.id.destory:
//                ECDevice.getLiveStreamManager().releaseLiveStream(roomId);
//
//                break;
//            case R.id.play:
//
//                ECDevice.getECVoIPSetupManager().setGlDisplayWindow(null,view);
//
//                roomId = ECDevice.getLiveStreamManager().createLiveStream(0);
//
//                ECDevice.getLiveStreamManager().playLiveStream(roomId, ed.getText().toString().trim(), view);
//
//
//                break;
//            case R.id.stop:
//                ECDevice.getLiveStreamManager().stopLiveStream(roomId);
//                break;
//
//            case R.id.btn_left:
//                hideSoftKeyboard();
//                finish();
//                break;
//
//        }
//    }
//}

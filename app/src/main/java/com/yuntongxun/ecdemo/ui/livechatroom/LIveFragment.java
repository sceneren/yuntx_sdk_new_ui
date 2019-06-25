package com.yuntongxun.ecdemo.ui.livechatroom;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yuntongxun.ecdemo.R;

public class LIveFragment extends Fragment{

    private static final String TAG = "LIveUI";

    private ViewGroup livePanel;
    private ImageView img;
    private LiveEditText input;

    public ImageView liveIv ;

    public ImageView infoIv;


    public void hide(){
        if(livePanel!=null){
            livePanel.setVisibility(View.VISIBLE);
        }
        if(input!=null){
            input.setVisibility(View.GONE);
        }

    }

    public LiveEditText  getL(){
        return input;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.live_fragment, container);
        livePanel = (ViewGroup) view.findViewById(R.id.button_panel);
        img = (ImageView) view.findViewById(R.id.btn_chat);
        liveIv = (ImageView)view.findViewById(R.id.btn_liwi);

        infoIv = (ImageView)view.findViewById(R.id.btn_play);






        liveIv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(listener!=null){
                    listener.onClickCallBack();
                }
            }
        });
        infoIv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(listener!=null){
                    listener.onInfoClick();
                }
            }
        });

        img.setClickable(true);
        input =(LiveEditText)view.findViewById(R.id.input_panel);



        img.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (livePanel != null) {
                    livePanel.setVisibility(View.GONE);
                }
                if (input != null) {
                    input.setVisibility(View.VISIBLE);
                    input.requestFos();
                }
            }
        });
        return view;
    }

    private LWListener  listener ;

    interface  LWListener{

        void onClickCallBack();

        void onInfoClick();
    }

    public LWListener getListener() {
        return listener;
    }

    public void setListener(LWListener listener) {
        this.listener = listener;
    }
}
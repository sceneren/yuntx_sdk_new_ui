package com.yuntongxun.ecdemo.ui.livechatroom;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;

public class LiveEditText extends LinearLayout implements View.OnClickListener {

    private final static String TAG = "Live";

    private EditText ed;
    private TextView fasongBtn;

    private LiveInputListener listener;

    public View getSendView(){
        return fasongBtn;
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){

            case R.id.input_send_live:
                if (listener != null) {
                    listener.onSendClick(ed.getText().toString());
                }
                if(ed !=null){
                    ed.setText("");
                }
                break;
        }
    }

    public interface LiveInputListener {
        void onSendClick(String text);
    }

    public LiveEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData();
    }


    public void requestFos(){
        if(ed !=null){
            ed.requestFocus();
        }
        InputMethodManager imm = ( InputMethodManager ) ed.getContext( ).getSystemService( Context.INPUT_METHOD_SERVICE );
        imm.showSoftInput(ed, InputMethodManager.SHOW_FORCED);

    }

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence sequence, int s, int num, int end) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable et) {

            fasongBtn.setEnabled(!et.toString().isEmpty());
            int start = ed.getSelectionStart();
            int end = ed.getSelectionEnd();
            ed.removeTextChangedListener(this);
            ed.setSelection(start, end);
            ed.addTextChangedListener(this);
        }
    };

    public void initData(){
        LayoutInflater.from(getContext()).inflate(R.layout.live_chat_input, this);
        ed = (EditText) findViewById(R.id.input_live);
        fasongBtn = (TextView) findViewById(R.id.input_send_live);

        ed.addTextChangedListener(watcher);

        fasongBtn.setOnClickListener(this);
    }

    public void setListener(LiveInputListener l) {
        listener = l;
    }



}
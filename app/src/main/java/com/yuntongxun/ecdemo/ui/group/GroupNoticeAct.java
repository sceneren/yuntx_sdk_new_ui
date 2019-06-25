package com.yuntongxun.ecdemo.ui.group;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.common.utils.KeyBordUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.common.view.TitleBar;

import butterknife.BindView;


/**
 * Created by zlk on 2017/7/27.
 * 群公告
 */

public class GroupNoticeAct extends BaseGroupReceiveAct {
    @BindView(R.id.title_bar)
    TitleBar titleBar;
    @BindView(R.id.ed_notice)
    EditText edNotice;
    public static final String KEY_NOTICE = "key_notice";

    @Override
    protected void initView(Bundle savedInstanceState) {

        initTooleBar(titleBar, true, "群公告");
        titleBar.setMySettingText("保存").setSettingTextOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String noticeStr = edNotice.getText().toString().trim();

                if (!TextUtils.isEmpty(noticeStr) && noticeStr.length() > 100) {
                    ToastUtil.showMessage("你输入的数字太多咯");
                    return;
                }

                if (!DemoUtils.isDeclareValid(noticeStr)) {
                    ToastUtil.showMessage("群组名称不合法，非空且仅能输入中英文、ASCII码范围内的值");
                    return;
                }
                if (!TextUtils.isEmpty(noticeStr)) {
                    Intent intent = new Intent();
                    intent.putExtra(KEY_NOTICE, noticeStr);
                    setResult(RESULT_OK, intent);
                }
                KeyBordUtil.hideSoftKeyboard(edNotice);
                finish();
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.act_group_notice;
    }

    @Override
    protected void initWidgetAciotns() {
    }

}

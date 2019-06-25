package com.yuntongxun.ecdemo.ui.friend;

import android.os.Bundle;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.ui.BaseActivity;

import butterknife.BindView;

/**
 * Created by smileklvens on 2017/11/2.
 * <p>
 * 手机通讯录
 */

public class AddressBookListAct extends BaseActivity {
    @BindView(R.id.title_bar)
    TitleBar mTitleBar;

    @Override
    protected void initView(Bundle savedInstanceState) {
        initTooleBar(mTitleBar, true, "查看手机通讯录");

    }

    @Override
    protected int getLayoutId() {
        return R.layout.act_addressbook_list;
    }

    @Override
    protected void initWidgetAciotns() {

    }


}

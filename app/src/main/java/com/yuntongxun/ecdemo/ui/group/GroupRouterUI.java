package com.yuntongxun.ecdemo.ui.group;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.ui.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by luhuashan on 17/9/11.
 * email huashan2007@sina.cn
 */
public class GroupRouterUI extends BaseActivity {
    @BindView(R.id.title_bar)
    TitleBar titleBar;
    @BindView(R.id.p_list)
    RelativeLayout pList;
    @BindView(R.id.p_search)
    RelativeLayout pSearch;

    @Override
    protected void initView(Bundle savedInstanceState) {

        initTooleBar(titleBar, true, "群组");
    }

    @Override
    protected int getLayoutId() {
        return R.layout.group_router;
    }

    @Override
    protected void initWidgetAciotns() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @OnClick({R.id.title_bar, R.id.p_list, R.id.p_search})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.title_bar:
                break;
            case R.id.p_list:
                startActivity(GroupAct.class);
                break;
            case R.id.p_search:
                startActivity(BaseSearch.class);
                break;
        }
    }
}

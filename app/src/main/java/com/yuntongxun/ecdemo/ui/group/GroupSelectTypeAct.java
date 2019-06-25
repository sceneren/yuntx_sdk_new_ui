package com.yuntongxun.ecdemo.ui.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.ui.BaseActivity;
import com.yuntongxun.ecdemo.ui.adapter.GroupTypeAdapter;

import butterknife.BindView;


/**
 * Created by zlk on 2017/7/27.
 * 选择群组类型
 */

public class GroupSelectTypeAct extends BaseActivity {
    @BindView(R.id.title_bar)
    TitleBar titleBar;
    @BindView(R.id.lv_goup_Type)
    ListView lvGoupType;
    private String[] types;
    public static final String KEY_GROUP_TYPE = "key_group_type";
    public static final String KEY_GROUP_POSITION = "key_group_position";

    @Override
    protected void initView(Bundle savedInstanceState) {
        initTooleBar(titleBar, true, "选择群组类型");
    }

    @Override
    protected int getLayoutId() {
        return R.layout.act_select_type;
    }

    @Override
    protected void initWidgetAciotns() {
        types = getResources().getStringArray(R.array.create_group_type_content);
        lvGoupType.setAdapter(new GroupTypeAdapter(mContext, types));
        lvGoupType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra(KEY_GROUP_TYPE, types[position]);
                intent.putExtra(KEY_GROUP_POSITION, position);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }


}

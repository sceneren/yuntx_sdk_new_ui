package com.yuntongxun.ecdemo.ui.friend;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.SearchEditText;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.ui.BaseActivity;
import com.yuntongxun.ecdemo.ui.contact.ContactDetailActivity;
import com.yuntongxun.ecdemo.ui.personcenter.FriendInfoUI;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.PersonInfo;
import com.yuntongxun.ecsdk.SdkErrorCode;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by luhuashan on 17/8/4.
 */

public class AddFriendActivity extends BaseActivity {


    @BindView(R.id.title_bar)
    TitleBar titleBar;
    @BindView(R.id.search)
    SearchEditText mSearch;
    @BindView(R.id.rl_add_phone)
    RelativeLayout mRlAddContact;
    @BindView(R.id.tv_empty)
    TextView mTvEmpty;
    @BindView(R.id.iv_phone_tag)
    ImageView mIvPhoneTag;
    @BindView(R.id.tv_phone_tag)
    TextView mTvPhoneTag;

    @Override
    protected void initView(Bundle savedInstanceState) {
        initTooleBar(titleBar, true, "添加朋友");
    }

    @Override
    protected int getLayoutId() {
        return R.layout.add_friend;
    }

    @Override
    protected void initWidgetAciotns() {


        mSearch.setOnSearchTextWatch(new SearchEditText.OnSearchTextWatch() {
            @Override
            public void onTextChanged(CharSequence s) {
                if (TextUtils.isEmpty(s)) {
                    mRlAddContact.setVisibility(View.VISIBLE);

                    mTvEmpty.setVisibility(View.GONE);
                }
            }
        });
        mSearch.setOnSearchClickListener(new SearchEditText.OnSearchClickListener() {
            @Override
            public void onSearchClick(String s) {
                if (!TextUtils.isEmpty(s)) {
                    ECDevice.getPersonInfo(s, new ECDevice.OnGetPersonInfoListener() {
                        @Override
                        public void onGetPersonInfoComplete(ECError ecError, PersonInfo personInfo) {
                            if (ecError.errorCode == SdkErrorCode.REQUEST_SUCCESS && personInfo != null) {
                                Intent intent = new Intent(mContext, FriendInfoUI.class);
                                intent.putExtra(FriendInfoUI.MOBILE, personInfo.getUserId());
                                intent.putExtra(ContactDetailActivity.DISPLAY_NAME, personInfo.getNickName());
                                startActivity(intent);
                            } else {
                                mRlAddContact.setVisibility(View.GONE);
                                mTvEmpty.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        mTvEmpty.setVisibility(View.GONE);
    }

    @OnClick(R.id.rl_add_phone)
    public void onViewClicked() {
        startActivity(AddressBookListAct.class);
    }
}

package com.yuntongxun.ecdemo.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yuntongxun.ecdemo.ECApplication;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.dialog.ECAlertDialog;
import com.yuntongxun.ecdemo.common.utils.ImageLoader;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.storage.FriendMessageSqlManager;
import com.yuntongxun.ecdemo.storage.GroupMemberSqlManager;
import com.yuntongxun.ecdemo.ui.SDKCoreHelper;
import com.yuntongxun.ecdemo.ui.group.GroupMemberService;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECGroupManager;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.im.ECGroup;
import com.yuntongxun.ecsdk.im.ECGroupMember;
import com.yuntongxun.ecsdk.im.ESpeakStatus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zlk on 2017/8/19.
 */

public class GagMemberAdapter extends ArrayAdapter<ECGroupMember> {

    Context mContext;
    private ECGroup mGroup;

    public GagMemberAdapter(@NonNull Context context) {
        super(context, 0);
        mContext = context;
    }

    public void setData(List<ECGroupMember> data, ECGroup mGroup) {
        this.mGroup = mGroup;
        clear();
        addAll(data);
        notifyDataSetChanged();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        final ViewHolder mViewHolder;
        if (convertView == null || convertView.getTag() == null) {
            view = View.inflate(mContext, R.layout.item_member_gag, null);
            mViewHolder = new ViewHolder(view);
            view.setTag(mViewHolder);
        } else {
            view = convertView;
            mViewHolder = (ViewHolder) view.getTag();
        }

        final ECGroupMember item = getItem(position);
        if (item == null) {
            return view;
        }

        String headUrl = FriendMessageSqlManager.queryURLByID(item.getVoipAccount());
        if (!TextUtils.isEmpty(headUrl)) {
            mViewHolder. mIvAvatar.setVisibility(View.VISIBLE);
            mViewHolder.tvGagAvatar.setVisibility(View.GONE);
            ImageLoader.getInstance().displayCricleImage(mContext,headUrl,mViewHolder.mIvAvatar);
        } else {
            if(CCPAppManager.getUserId().equalsIgnoreCase(item.getVoipAccount())
                    &&!TextUtils.isEmpty(ECApplication.photoUrl)){
                mViewHolder.mIvAvatar.setVisibility(View.VISIBLE);
                mViewHolder.tvGagAvatar.setVisibility(View.GONE);
                ImageLoader.getInstance().displayCricleImage(mContext,ECApplication.photoUrl,  mViewHolder.mIvAvatar);
            }else{
                mViewHolder. mIvAvatar.setVisibility(View.GONE);
                mViewHolder.tvGagAvatar.setVisibility(View.VISIBLE);
                mViewHolder.tvGagAvatar.setText(TextUtils.isEmpty(item.getDisplayName()) ? item.getVoipAccount() : item.getDisplayName());
            }

        }
        mViewHolder.tvGagMemberName.setText(TextUtils.isEmpty(item.getDisplayName()) ? item.getVoipAccount() : item.getDisplayName());


        mViewHolder.btnClearGag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSetMemberSpeakStatus(item);
            }
        });
        return view;
    }


    /**
     * 解除禁言
     *
     * @param item
     */
    private void doSetMemberSpeakStatus(final ECGroupMember item) {
        String msg = mContext.getString(R.string.str_group_member_unspeak_tips,
                item.getDisplayName());

        ECAlertDialog buildAlert = ECAlertDialog.buildAlert(mContext, msg,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        forbidMemberSpeakStatus(mGroup.getGroupId(), item);
                    }
                });

        buildAlert.setTitle(R.string.app_tip);
        buildAlert.show();
    }

    public void forbidMemberSpeakStatus(final String groupId, final ECGroupMember item) {
        String member = item.getVoipAccount();
        final boolean enabled = !item.isBan();

        ESpeakStatus speakStatus = new ESpeakStatus();
        speakStatus.setOperation(enabled ? 2 : 1);
        SDKCoreHelper.getECGroupManager().forbidMemberSpeakStatus(groupId, member, speakStatus, new ECGroupManager.OnForbidMemberSpeakStatusListener() {
            @Override
            public void onForbidMemberSpeakStatusComplete(ECError error, String groupId, String member) {

                if (isSuccess(error)) {

                    GroupMemberSqlManager.updateMemberSpeakState(groupId, member, enabled);
                    GroupMemberService.synsGroupMember(mGroup.getGroupId());
//                    remove(item);
//                    notifyDataSetChanged();
                } else {
                    ToastUtil.showMessage("设置失败[" + error.errorCode + "]");
                }
            }

        });
    }

    private boolean isSuccess(ECError error) {
        if (error.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
            return true;
        }
        return false;
    }

    class ViewHolder {
        @BindView(R.id.tv_gag_avatar)
        TextView tvGagAvatar;
        @BindView(R.id.iv_avatar)
        ImageView mIvAvatar;


        @BindView(R.id.tv_gag_member_name)
        TextView tvGagMemberName;
        @BindView(R.id.tv_gag_time)
        TextView tvGagTime;
        @BindView(R.id.btn_clear_gag)
        Button btnClearGag;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


}




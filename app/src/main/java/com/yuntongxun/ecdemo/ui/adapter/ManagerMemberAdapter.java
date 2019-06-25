package com.yuntongxun.ecdemo.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yuntongxun.ecdemo.ECApplication;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.dialog.ECAlertDialog;
import com.yuntongxun.ecdemo.common.utils.ImageLoader;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.common.view.SwipeMenuLayout;
import com.yuntongxun.ecdemo.storage.FriendMessageSqlManager;
import com.yuntongxun.ecdemo.ui.SDKCoreHelper;
import com.yuntongxun.ecdemo.ui.group.GroupMemberService;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECGroupManager;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.im.ECGroup;
import com.yuntongxun.ecsdk.im.ECGroupMember;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zlk on 2017/8/19.
 */

public class ManagerMemberAdapter extends ArrayAdapter<ECGroupMember> {

    Context mContext;
    private ECGroup mGroup;

    public ManagerMemberAdapter(@NonNull Context context) {
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
            view = View.inflate(mContext, R.layout.item_manager, null);
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
            mViewHolder.mIvAvatar.setVisibility(View.VISIBLE);
            mViewHolder.tvGagAvatar.setVisibility(View.GONE);
            ImageLoader.getInstance().displayCricleImage(mContext,headUrl,mViewHolder.mIvAvatar);
        } else {
            if(CCPAppManager.getUserId().equalsIgnoreCase(item.getVoipAccount())
                    &&!TextUtils.isEmpty(ECApplication.photoUrl)){
                mViewHolder.mIvAvatar.setVisibility(View.VISIBLE);
                mViewHolder.tvGagAvatar.setVisibility(View.GONE);
                ImageLoader.getInstance().displayCricleImage(mContext,ECApplication.photoUrl,  mViewHolder.mIvAvatar);
            }else{
                mViewHolder.mIvAvatar.setVisibility(View.GONE);
                mViewHolder.tvGagAvatar.setVisibility(View.VISIBLE);
                mViewHolder.tvGagAvatar.setText(TextUtils.isEmpty(item.getDisplayName()) ? item.getVoipAccount() : item.getDisplayName());
            }

        }
        mViewHolder.tvGagMemberName.setText(TextUtils.isEmpty(item.getDisplayName()) ? item.getVoipAccount() : item.getDisplayName());


        mViewHolder.tvDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ECAlertDialog buildAlert = ECAlertDialog.buildAlert(mContext
                        , "移除管理员", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                doLogicSetMemberRole(mGroup.getGroupId(), item, ECGroupManager.ECGroupMemberRole.MEMBER, mViewHolder.swipeMenuLayout);
                            }
                        });
                buildAlert.setTitle(R.string.app_tip);
                buildAlert.show();
            }
        });

        return view;
    }


    public void doLogicSetMemberRole(final String groupId, final ECGroupMember member,
                                     final ECGroupManager.ECGroupMemberRole enRole, final SwipeMenuLayout swipeMenuLayout) {

        SDKCoreHelper.getECGroupManager().setGroupMemberRole(groupId, member.getVoipAccount(), enRole, new ECGroupManager.OnSetGroupMemberRoleListener() {

            @Override
            public void onSetGroupMemberRoleComplete(ECError error, String groupId) {

                if (isSuccess(error)) {
                    remove(member);
                    swipeMenuLayout.smoothClose();

                    GroupMemberService.synsGroupMember(groupId);

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

        @BindView(R.id.tv_del)
        TextView tvDel;

        @BindView(R.id.sml)
        SwipeMenuLayout swipeMenuLayout;



        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


}




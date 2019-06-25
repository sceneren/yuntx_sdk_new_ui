package com.yuntongxun.ecdemo.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yuntongxun.ecdemo.AvatorUtil;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.utils.DateUtil;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.pojo.FriendMessage;
import com.yuntongxun.ecdemo.storage.FriendMessageSqlManager;
import com.yuntongxun.ecdemo.ui.RestServerDefines;
import com.yuntongxun.ecdemo.ui.contact.NewFrendAct;
import com.yuntongxun.ecdemo.ui.phonemodel.HttpMethods;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Created by zlk on 2017/7/26.
 */

public class NewFrendAdapter extends BaseAdapter {

    public static final int TYPE_TITLE = 0x1;
    public static final int TYPE_CONTENT = 0x2;
    public static final List<String> titles = Arrays.asList(new String[]{"好友通知", "好友推荐"});
    private List<FriendMessage> datas;

    private Context context;
    private NewFrendAct act;

    public NewFrendAdapter(Context c, List<FriendMessage> datas, NewFrendAct ui) {
        this.datas = datas;
        this.context = c;
        this.act = ui;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        View view;
        ViewHolder mViewHolder;

        if (convertView == null || convertView.getTag() == null) {

            view = View.inflate(context, R.layout.friend_notice_item, null);
            mViewHolder = new ViewHolder(view);
            mViewHolder.ivMyHeader = (ImageView) view.findViewById(R.id.iv_my_header);
            mViewHolder.tvName = (TextView) view.findViewById(R.id.item_name);

            mViewHolder.tvPhone = (TextView) view.findViewById(R.id.item_phone);
            mViewHolder.buAdd = (Button) view.findViewById(R.id.item_bu);

            view.setTag(mViewHolder);
        } else {
            view = convertView;
            mViewHolder = (ViewHolder) view.getTag();
        }

        final FriendMessage item = (FriendMessage) getItem(position);
        if (item == null) {
            return view;
        }
        final String userId = DemoUtils.splite(item.getFriendUseracc());

        String state = item.getDealState();
        String invite = item.getIsInvited();


        if (TextUtils.equals(item.getSource(), "1")) {
            mViewHolder.tvPhone.setText("");
        } else {
            mViewHolder.tvPhone.setText("来源:搜索或者群查找");
        }
        if ("1".equalsIgnoreCase(state)) {
            mViewHolder.buAdd.setText("已添加");
            mViewHolder.buAdd.setClickable(false);
            mViewHolder.buAdd.setTextColor(context.getResources().getColor(R.color.hint_text_color_dark_bg));
            mViewHolder.buAdd.setBackground(null);
        } else if ("0".equalsIgnoreCase(state)) {
            if ("0".equalsIgnoreCase(invite)) {
                mViewHolder.buAdd.setText("待同意");
                mViewHolder.buAdd.setTextColor(context.getResources().getColor(R.color.hint_text_color_dark_bg));
                mViewHolder.buAdd.setBackground(null);

            } else if ("1".equalsIgnoreCase(invite)) {
                mViewHolder.buAdd.setText("同意");
                mViewHolder.buAdd.setTextColor(context.getResources().getColor(R.color.white));
                mViewHolder.buAdd.setBackground(context.getResources().getDrawable(R.drawable.blue_rect_bg));

                mViewHolder.buAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        agreeFriend(item.getFriendUseracc(), userId);
                    }
                });
            }
        }

        String number = DemoUtils.splite(item.getFriendUseracc());
        if (!TextUtils.isEmpty(number)) {
            AvatorUtil.getInstance().setAvatorPhoto(mViewHolder.ivMyHeader, R.drawable.header_woman, number);
            mViewHolder.tvName.setText(AvatorUtil.getInstance().getMarkName(number));
        }

        return view;

    }

    private void agreeFriend(String useracc, final String friendId) {
        act.showCommonProcessDialog();
        Observer<Object> subscriber = new Observer<Object>() {
            @Override
            public void onComplete() {
                LogUtil.e("onCompleted");
                act.dismissCommonPostingDialog();
            }

            @Override
            public void onError(Throwable e) {
                ToastUtil.showMessage("操作失败");
                LogUtil.e(e.toString());
                act.dismissCommonPostingDialog();
            }

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Object movieEntity) {
                if (movieEntity != null) {
                    LogUtil.e(movieEntity.toString());
                    ResponseBody body = (ResponseBody) movieEntity;
                    try {
                        String s = new String(body.bytes());
                        String error = "";
                        JSONObject j = new JSONObject(s);
                        if (j != null && j.has("statusMsg")) {
                            error = j.getString("statusMsg");
                        }
                        if (DemoUtils.isTrue(s)) {
                            ToastUtil.showMessage("已同意");
                            FriendMessageSqlManager.updateState(friendId, "1");
                            if (act != null) {
                                act.finish();
                            }
                        } else {
                            ToastUtil.showMessage(error);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        String time = DateUtil.formatNowDate(new Date());
        String url = getSig(time);
        JSONObject map = HttpMethods.buildAgreeFriend(CCPAppManager.getUserId(), friendId);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), map.toString());
        String s = RestServerDefines.QR_APK ? RestServerDefines.APPKER_CODE : RestServerDefines.APPKER;
        HttpMethods.getInstance(time).friendAgree(subscriber, s, url, body);
    }


    public String getSig(String stime) {
        String s = RestServerDefines.APPKER + CCPAppManager.getAppToken() + stime;
        return getMessageDigest(s.getBytes());
    }

    public static String getMessageDigest(byte[] input) {
        char[] source = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(input);
            byte[] digest = mDigest.digest();
            int length = digest.length;
            char[] result = new char[length * 2];
            int j = 0;
            for (byte l : digest) {
                result[(j++)] = source[(l >>> 4 & 0xF)];
                result[(j++)] = source[(l & 0xF)];
            }
            return new String(result);
        } catch (Exception e) {
        }
        return null;
    }


    class ViewHolder {

        @BindView(R.id.iv_my_header)
        ImageView ivMyHeader;

        @BindView(R.id.item_name)
        TextView tvName;

        @BindView(R.id.item_phone)
        TextView tvPhone;

        @BindView(R.id.item_bu)
        Button buAdd;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


}

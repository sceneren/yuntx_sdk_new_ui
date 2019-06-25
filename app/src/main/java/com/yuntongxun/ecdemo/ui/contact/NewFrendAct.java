package com.yuntongxun.ecdemo.ui.contact;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.ExceptionHandler;
import com.yuntongxun.ecdemo.common.utils.DateUtil;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.SharedPreferencesUtils;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.common.view.WrapListview;
import com.yuntongxun.ecdemo.core.ContactsCache;
import com.yuntongxun.ecdemo.core.ECArrayLists;
import com.yuntongxun.ecdemo.pojo.FriendMessage;
import com.yuntongxun.ecdemo.storage.FriendMessageSqlManager;
import com.yuntongxun.ecdemo.ui.BaseActivity;
import com.yuntongxun.ecdemo.ui.RestServerDefines;
import com.yuntongxun.ecdemo.ui.adapter.NewFrendAdapter;
import com.yuntongxun.ecdemo.ui.adapter.RecommandAdapter;
import com.yuntongxun.ecdemo.ui.phonemodel.HttpMethods;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Created by zlk on 2017/7/26.
 */

public class NewFrendAct extends BaseActivity {

    @BindView(R.id.title_bar)
    TitleBar titleBar;
    @BindView(R.id.tv_tag_notice)
    TextView tvTagNotice;
    @BindView(R.id.lv_friends_notice)
    WrapListview lvFriendsNotice;
    @BindView(R.id.tv_tag_recommend)
    TextView tvTagRecommend;
    @BindView(R.id.lv_friends_recommend)
    WrapListview lvFriendsRecommend;
    @BindView(R.id.tv_more)
    TextView mTvMore;


    private List<FriendMessage> gAllFriends = new ArrayList<>();

    private List<ECContacts> gAllContacts = new ArrayList<>();


    private NewFrendAdapter newFrendAdapter;
    private String timestamp;

    @Override
    protected void initView(Bundle savedInstanceState) {
        initTooleBar(titleBar, true, "新的好友");

        SharedPreferencesUtils.setParam(this, SharedPreferencesUtils.FRIEND_NUM, 0);

//        lvFriendsNotice.setPullRefreshEnable(false);
//        lvFriendsNotice.setPullLoadEnable(true);

        loadData("");

//        Collections.sort(gAllFriends);
        newFrendAdapter = new NewFrendAdapter(NewFrendAct.this, gAllFriends, NewFrendAct.this);
        lvFriendsNotice.setAdapter(newFrendAdapter);

        getRecommandContacts();

        RecommandAdapter newFrendAdapter = new RecommandAdapter(NewFrendAct.this, gAllContacts, NewFrendAct.this);
        lvFriendsRecommend.setAdapter(newFrendAdapter);
    }

    private void getRecommandContacts() {

        try {
            ECArrayLists<ECContacts> contacts = ContactsCache.getInstance().getContacts();
            if (contacts != null && contacts.size() != 0) {
                Iterator<ECContacts> iterator = contacts.iterator();
                while (iterator.hasNext()) {
                    ECContacts i = iterator.next();
                    if (!FriendMessageSqlManager.isFriend(i.getContactid())
                            && gAllContacts.size() <= 12) {
                        gAllContacts.add(i);
                    }
                }
            } else {
                tvTagRecommend.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    protected int getLayoutId() {
        return R.layout.act_new_friend;
    }

    boolean isLoad;

    @Override
    protected void initWidgetAciotns() {

//        lvFriendsNotice.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                if (isLoad && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
//                    loadData(timestamp);
//                }
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                isLoad = ((firstVisibleItem + visibleItemCount) == totalItemCount);
//            }
//        });
    }


    private void loadData(String times) {

        showCommonProcessDialog();
        Observer<Object> subscriber = new Observer<Object>() {
            @Override
            public void onComplete() {
                LogUtil.e("onCompleted");
                dismissCommonPostingDialog();
            }

            @Override
            public void onError(Throwable e) {
                onLoad();
                ToastUtil.showMessage("获取失败");
                LogUtil.e(e.toString());
                dismissCommonPostingDialog();
            }

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Object movieEntity) {
                dismissCommonPostingDialog();
                onLoad();
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
                        if (j != null && j.has("timestamp")) {
                            timestamp = j.getString("timestamp");
                        }
                        int code = 0;
                        if (j != null && j.has("statusCode")) {
                            code = Integer.parseInt(j.getString("statusCode"));
                        }

                        if (DemoUtils.isTrue(s)) {
                            List<FriendMessage> list = DemoUtils.getFriendsMessage(s);
                            if (list.size() < 6) {
                                mTvMore.setVisibility(View.GONE);
                            } else {
                                mTvMore.setVisibility(View.VISIBLE);
                            }
                            gAllFriends.addAll(list);
                            Collections.sort(gAllFriends);
                            newFrendAdapter.notifyDataSetChanged();
                        } else {
                            ExceptionHandler.converToastMsg(code, error);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        String time = DateUtil.formatNowDate(new Date());
        String url = getSig(time);
        JSONObject map = HttpMethods.buildFriendMessage(CCPAppManager.getUserId(), times);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), map.toString());
        HttpMethods.getInstance(time).friendMessage(subscriber, RestServerDefines.APPKER, url, body);

    }

    private void onLoad() {
//        lvFriendsNotice.stopRefresh();
//        lvFriendsNotice.stopLoadMore();
//        lvFriendsNotice.setRefreshTime("刚刚");
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


    @OnClick(R.id.tv_more)
    public void onViewClicked() {
        loadData(timestamp);
    }


}

package com.yuntongxun.ecdemo.ui.livechatroom;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.utils.Base64;
import com.yuntongxun.ecdemo.common.utils.DateUtil;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.httpUtil.interceptor.TokenInterceptor;
import com.yuntongxun.ecdemo.ui.ECSuperActivity;
import com.yuntongxun.ecdemo.ui.RestServerDefines;
import com.yuntongxun.ecsdk.ECLiveChatRoom;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.baidu.location.b.g.o;

/**
 * Created by luhuashan on 17/5/16.
 * 直播
 */
public class ECLIveChatRoomListUI extends ECSuperActivity implements RoomListAdapter.OnRecyclerViewListener, View.OnClickListener {


    @Override
    protected int getLayoutId() {
        return R.layout.ec_chatroom_list;
    }

    private RecyclerView recyclerView;
    private RoomListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt,
                R.drawable.btn_style_green, null,
                getString(R.string.ec_chatroom),
                getString(R.string.str_chatroom), null, this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        recyclerView.setHasFixedSize(true);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RoomListAdapter(list);
        adapter.setOnRecyclerViewListener(this);
        recyclerView.setAdapter(adapter);

        recyclerView.addItemDecoration(new DividerItemDecoration(
                this, LinearLayoutManager.HORIZONTAL, R.drawable.divider));


        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItem = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                int totalItemCount = layoutManager.getItemCount();
                //lastVisibleItem >= totalItemCount - 4 表示剩下4个item自动加载，各位自由选择
                // dy>0 表示向下滑动


//                LogUtil.e("aa", "底部--"+lastVisibleItem);
//                LogUtil.e("aa", "底部--"+totalItemCount);


                if (lastVisibleItem >= totalItemCount - 1 && dy > 0) {


//                    if (isLoadingMore) {
//                        Log.e("aa", "ignore manually update!");
//                    } else {
////                        loadPage();//这里多线程也要手动控制isLoadingMore
////                        isLoadingMore = false;
//                    }
                    if (isButtom) {
                        LogUtil.e("aa", "底部--");
                        loadMore();
                        isButtom = false;
                    }
                }
            }
        });
    }

    private void loadMore() {

        showCommonProcessDialog("正在努力加载...");

        ECLiveChatRoom room = (ECLiveChatRoom) list.getFirst();

        time = DateUtil.formatNowDate(new Date());

        String u = RestServerDefines.SERVER + "/2013-12-26/Application/" + CCPAppManager.getClientUser().getAppKey() + "/IM/getChatRoomList?sig=" + getSig();
        Log.e("aa", u);
        OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor(getAuth()))
                .build();
        JSONObject object = new JSONObject();
        try {
            object.put("limit", "20");
            object.put("order", "1");
            object.put("dateTime", room.dateCreated);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody formBody = RequestBody.create(JSON, object.toString());
        Request request = new Request.Builder()
                .url(u)
                .post(formBody)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissCommonPostingDialog();
                        isButtom = true;
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String str = response.body().string();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isSuccess(str)) {
                            try {
                                JSONObject jsonObject = new JSONObject(str);

                                if (jsonObject != null && !TextUtils.isEmpty(jsonObject.getString("chatRoomList"))) {

                                    JSONArray obj = jsonObject.getJSONArray("chatRoomList");

                                    if (obj == null || obj.length() < 1) {
                                        return;
                                    }
                                    for (int i = 0; i < obj.length(); i++) {
                                        JSONObject ob = (JSONObject) obj.get(i);

                                        ECLiveChatRoom room = new ECLiveChatRoom();
                                        room.roomId = ob.getString("roomId");
                                        room.creator = ob.getString("creator");
                                        room.roomName = ob.getString("name");
                                        if (ob.has("pullUrl")) {
                                            room.pullUrl = ob.getString("pullUrl");
                                        }
                                        if (ob.has("dateCreated")) {
                                            room.dateCreated = ob.getString("dateCreated");
                                        }
                                        if (ob.has("portrait")) {
                                            room.pic = ob.getString("portrait");
                                        }

                                        list.add(room);

                                    }

                                }
                                Collections.reverse(list);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Collections.sort(list, new Comparator<ECLiveChatRoom>() {
                                            @Override
                                            public int compare(ECLiveChatRoom o1, ECLiveChatRoom o2) {
                                                return (int)(getTime(o2.getDateCreated()) -getTime(o1.getDateCreated()));
                                            }
                                        });
                                        adapter.notifyDataSetChanged();
                                        dismissCommonPostingDialog();
                                        isButtom = true;
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                        }
                    }
                });
            }
        });

    }

    private boolean isButtom = true;

    @Override
    protected void onResume() {
        super.onResume();
        getRoomList();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if(list!=null){
//            list.clear();
//            list = null;
//        }


    }

    public String getAuth() {
        String s = CCPAppManager.getClientUser().getAppKey() + ":" + time;
        return Base64.encode(s.getBytes());
    }

    public String getSig() {
        String s = CCPAppManager.getClientUser().getAppKey() + CCPAppManager.getClientUser().getAppToken() + time;
        return getMessageDigest(s.getBytes());
    }

    static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

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

    private void getRoomList() {

        showCommonProcessDialog("正在努力加载...");

        time = DateUtil.formatNowDate(new Date());

        String u = RestServerDefines.SERVER + "/2013-12-26/Application/" + CCPAppManager.getClientUser().getAppKey() + "/IM/getChatRoomList?sig=" + getSig();
        Log.e("aa", u);
        OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor(getAuth()))
                .build();
        JSONObject object = new JSONObject();
        try {
            object.put("limit", "20");
            object.put("order", "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody formBody = RequestBody.create(JSON, object.toString());
        Request request = new Request.Builder()
                .url(u)
                .post(formBody)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissCommonPostingDialog();
                        isButtom = true;
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String str = response.body().string();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        isButtom = true;
                        dismissCommonPostingDialog();
                        if (isSuccess(str)) {
                            try {
                                JSONObject jsonObject = new JSONObject(str);

                                JSONArray obj = jsonObject.getJSONArray("chatRoomList");

                                if (obj == null || obj.length() < 1) {
                                    return;
                                }
                                if (list != null) {
                                    list.clear();
                                }

                                for (int i = 0; i < obj.length(); i++) {
                                    JSONObject ob = (JSONObject) obj.get(i);

                                    ECLiveChatRoom room = new ECLiveChatRoom();
                                    room.roomId = ob.getString("roomId");
                                    room.creator = ob.getString("creator");
                                    room.roomName = ob.getString("name");
                                    if (ob.has("pullUrl")) {
                                        room.pullUrl = ob.getString("pullUrl");
                                    }
                                    if (ob.has("dateCreated")) {
                                        room.dateCreated = ob.getString("dateCreated");
                                    }
                                    if (ob.has("portrait")) {
                                        room.pic = ob.getString("portrait");
                                    }
                                    list.add(room);
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Collections.sort(list, new Comparator<ECLiveChatRoom>() {
                                            @Override
                                            public int compare(ECLiveChatRoom o1, ECLiveChatRoom o2) {
                                                return (int)(getTime(o2.getDateCreated()) -getTime(o1.getDateCreated()));
                                            }
                                        });
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "获取列表失败:" + getCode(str), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });

    }


    public long getTime(String dataStr) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date d1 = df.parse(dataStr);
            return d1.getTime();
        } catch (Exception e) {
        }
        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }

    private LinkedList<ECLiveChatRoom> list = new LinkedList<ECLiveChatRoom>();


    private View inflate;
    private Button choosePhoto;
    private Button takePhoto;
    private Button cancel;
    private Dialog dialog;

    public void show(int position) {
        dialog = new Dialog(this, R.style.ActionSheetDialogStyle);


        inflate = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null);


        choosePhoto = (Button) inflate.findViewById(R.id.chooseStop);
        takePhoto = (Button) inflate.findViewById(R.id.chooseStart);

        ECLiveChatRoom room = list.get(position);
        boolean isManager = false;
        if (room != null) {

            isManager = CCPAppManager.getUserId().equalsIgnoreCase(room.creator);
        }
        if (!isManager) {
            choosePhoto.setVisibility(View.GONE);
            takePhoto.setVisibility(View.GONE);
        }


        cancel = (Button) inflate.findViewById(R.id.btn_cancel);
        choosePhoto.setOnClickListener(this);
        takePhoto.setOnClickListener(this);
        cancel.setOnClickListener(this);
        dialog.setContentView(inflate);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.y = 20;
        lp.width = -1;
        dialogWindow.setAttributes(lp);
        dialog.show();
    }


    private int item;

    @Override
    public void onItemClick(int position) {

        show(position);
        item = position;


    }

    @Override
    public boolean onItemLongClick(int position) {
        return false;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_left:
                hideSoftKeyboard();
                finish();
                break;

            case R.id.text_right:
                startActivity(new Intent(this, CreateRoomUI.class));
                break;

            case R.id.chooseEnter:

                if (dialog != null) {
                    dialog.dismiss();
                }
                Intent s = new Intent(this, LiveChatUI.class);
                s.putExtra("item", list.get(item));
                startActivity(s);
                break;
            case R.id.chooseStop:
                if (dialog != null) {
                    dialog.dismiss();
                }
                handleManagerRoom(item, 2);
                break;
            case R.id.chooseStart:
                if (dialog != null) {
                    dialog.dismiss();
                }
                handleManagerRoom(item, 1);

                break;
            case R.id.btn_cancel:
                if (dialog != null) {
                    dialog.dismiss();
                }
                break;
        }
    }


    private String time;


    private void handleManagerRoom(int item, int i) {


        time = DateUtil.formatNowDate(new Date());

        String u = RestServerDefines.SERVER + "/2013-12-26/Application/" + CCPAppManager.getClientUser().getAppKey() + "/IM/ToggleState?sig=" + getSig();

        OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor(getAuth()))
                .build();
        JSONObject object = new JSONObject();
        try {
            object.put("roomId", list.get(item).roomId);
            object.put("operator", list.get(item).creator);
            object.put("state", i);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody formBody = RequestBody.create(JSON, object.toString());
        Request request = new Request.Builder()
                .url(u)
                .post(formBody)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showMessage("设置失败");
                        dismissCommonPostingDialog();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String str = response.body().string();
                Log.e("aa", str);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        dismissCommonPostingDialog();
                        if (isSuccess(str)) {
                            Toast.makeText(getApplicationContext(), "设置成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "设置失败:" + getCode(str), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        });


    }
}

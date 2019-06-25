package com.yuntongxun.ecdemo.ui.phonemodel;

import android.content.Context;

import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.utils.Base64;
import com.yuntongxun.ecdemo.common.utils.DateUtil;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.httpUtil.interceptor.TokenInterceptor;
import com.yuntongxun.ecdemo.ui.RestServerDefines;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
//import rx.Subscriber;
//import rx.schedulers.Schedulers;

public class HttpMethods {

    private static final int DEFAULT_TIMEOUT = 20000;

    private Retrofit retrofit;

    private BaseApiService movieService;

    private static String time;

    //构造方法私有
    private HttpMethods() {
        //手动创建一个OkHttpClient并设置超时时间


    }


    public String getAuth() {
        String appid = RestServerDefines.QR_APK ? RestServerDefines.APPKER_CODE : RestServerDefines.APPKER;
        String s = appid + ":" + time;
        return Base64.encode(s.getBytes());
    }

    public String getTokenAuth() {
        String s = "yuntongxun" + ":" + time;
        return Base64.encode(s.getBytes());
    }

    //在访问HttpMethods时创建单例
    private static final HttpMethods INSTANCE = new HttpMethods();

    //获取单例
    public static HttpMethods getInstance(String sTime) {
        time = sTime;
        return INSTANCE;
    }


    public void postSms(final Observer subscriber, String appid, String url, RequestBody map) {

//        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(new TokenInterceptor(getAuth()));
//        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
//        String BASE_URL = RestServerDefines.SERVER_CODE + "/2016-08-15/Application/" + getAppKey() + "/IMPlus/";
//
//        retrofit = new Retrofit.Builder()
//                .client(httpClientBuilder.build())
//                .addConverterFactory(GsonConverterFactory.create())
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .baseUrl(BASE_URL)
//                .build();
//        movieService = retrofit.create(BaseApiService.class);
//        movieService.postSms(appid, url, map)
//                .subscribeOn(Schedulers.io())
//                .unsubscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(subscriber);
    }

    public void register(final Observer subscriber, String appid, String url, RequestBody map) {


//        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(new TokenInterceptor(getAuth()));
//        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
//        String BASE_URL = RestServerDefines.SERVER_CODE + "/2016-08-15/Application/" + getAppKey() + "/IMPlus/";
//
//        retrofit = new Retrofit.Builder()
//                .client(httpClientBuilder.build())
//                .addConverterFactory(GsonConverterFactory.create())
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .baseUrl(BASE_URL)
//                .build();
//
//        movieService = retrofit.create(BaseApiService.class);
//        movieService.register(appid, url, map)
//                .subscribeOn(Schedulers.io())
//                .unsubscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(subscriber);
    }

    public void login(final Observer subscriber, String appid, String url, RequestBody map) {


//        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(new TokenInterceptor(getAuth()));
//        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
//        String BASE_URL = RestServerDefines.SERVER_CODE + "/2016-08-15/Application/" + getAppKey() + "/IMPlus/";
//
//        retrofit = new Retrofit.Builder()
//                .client(httpClientBuilder.build())
//                .addConverterFactory(GsonConverterFactory.create())
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .baseUrl(BASE_URL)
//                .build();
//
//        movieService = retrofit.create(BaseApiService.class);
//
//        movieService.login(appid, url, map)
//                .subscribeOn(Schedulers.io())
//                .unsubscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(subscriber);
    }

    public void update(final Observer subscriber, String appid, String url, RequestBody map) {


//        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(new TokenInterceptor(getAuth()));
//        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
//        String BASE_URL = RestServerDefines.SERVER_CODE + "/2016-08-15/Application/" + getAppKey() + "/IMPlus/";
//
//        retrofit = new Retrofit.Builder()
//                .client(httpClientBuilder.build())
//                .addConverterFactory(GsonConverterFactory.create())
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .baseUrl(BASE_URL)
//                .build();
//
//        movieService = retrofit.create(BaseApiService.class);
//        movieService.modifyPwd(appid, url, map)
//                .subscribeOn(Schedulers.io())
//                .unsubscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(subscriber);
    }

    public void getToken(final Observer subscriber, String appid, String url, RequestBody map) {

//        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(new TokenInterceptor(getTokenAuth()));
//        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
//        String BASE_URL = RestServerDefines.SERVER_CODE + "/2013-12-26/Corp/" + "yuntongxun" + "/IM/";
//
//        retrofit = new Retrofit.Builder()
//                .client(httpClientBuilder.build())
//                .addConverterFactory(GsonConverterFactory.create())
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .baseUrl(BASE_URL)
//                .build();
//
//        movieService = retrofit.create(BaseApiService.class);
//        movieService.getToken(url, map)
//                .subscribeOn(Schedulers.io())
//                .unsubscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(subscriber);
    }

    public void addFriend(final Observer subscriber, String appid, String url, RequestBody map) {


        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(new TokenInterceptor(getAuth()));
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        String BASE_URL = RestServerDefines.Friend + "/2013-12-26/Application/" + getAppKey() + "/IM/";

        retrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();

        movieService = retrofit.create(BaseApiService.class);
        movieService.addFriend(appid, url, map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void delFriend(final Observer subscriber, String appid, String url, RequestBody map) {


        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(new TokenInterceptor(getAuth()));
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        String BASE_URL = RestServerDefines.Friend + "/2013-12-26/Application/" + getAppKey() + "/IM/";
        retrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();

        movieService = retrofit.create(BaseApiService.class);
        movieService.delFriend(appid, url, map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void getPersonInfo(final Observer subscriber, String appid, String url, RequestBody map) {


        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(new TokenInterceptor(getAuth()));
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        String BASE_URL = RestServerDefines.Friend + "/2013-12-26/Application/" + getAppKey() + "/IM/";

        retrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        movieService = retrofit.create(BaseApiService.class);
        movieService.getPersonInfo(appid, url, map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void getPersonPic(final Observer subscriber, String appid, String url, RequestBody map) {


        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(new TokenInterceptor(getAuth()));
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        String BASE_URL = RestServerDefines.Friend + "/2013-12-26/Application/" + getAppKey() + "/IM/";

        retrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        movieService = retrofit.create(BaseApiService.class);
        movieService.getPersonPic(appid, url, map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void getFriendInfo(final Observer subscriber, String appid, String url, RequestBody map) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(new TokenInterceptor(getAuth()));
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        String BASE_URL = RestServerDefines.Friend + "/2013-12-26/Application/" + getAppKey() + "/IM/";

        retrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        movieService = retrofit.create(BaseApiService.class);
        movieService.getFriendInfo(appid, url, map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void getFriends(final Observer subscriber, String appid, String url, RequestBody map) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(new TokenInterceptor(getAuth()));
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        String BASE_URL = RestServerDefines.Friend + "/2013-12-26/Application/" + getAppKey() + "/IM/";

        retrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        movieService = retrofit.create(BaseApiService.class);
        movieService.getFriends(appid, url, map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void getVerify(final Observer subscriber, String appid, String url, RequestBody map) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(new TokenInterceptor(getAuth()));
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        String BASE_URL = RestServerDefines.Friend + "/2013-12-26/Application/" + getAppKey() + "/IM/";

        retrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        movieService = retrofit.create(BaseApiService.class);
        movieService.getVerify(appid, url, map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void friendRefuse(final Observer subscriber, String appid, String url, RequestBody map) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(new TokenInterceptor(getAuth()));
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        String BASE_URL = RestServerDefines.Friend + "/2013-12-26/Application/" + getAppKey() + "/IM/";

        retrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        movieService = retrofit.create(BaseApiService.class);
        movieService.friendRefuse(appid, url, map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void friendAgree(final Observer subscriber, String appid, String url, RequestBody map) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(new TokenInterceptor(getAuth()));
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        String BASE_URL = RestServerDefines.Friend + "/2013-12-26/Application/" + getAppKey() + "/IM/";

        retrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        movieService = retrofit.create(BaseApiService.class);
        movieService.friendAgree(appid, url, map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void friendMessage(final Observer subscriber, String appid, String url, RequestBody map) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(new TokenInterceptor(getAuth()));
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        String BASE_URL = RestServerDefines.Friend + "/2013-12-26/Application/" + getAppKey() + "/IM/";

        retrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        movieService = retrofit.create(BaseApiService.class);
        movieService.friendMessage(appid, url, map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }
    public void robotNengLi(final Observer subscriber, String appid, String url, RequestBody map) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(new TokenInterceptor(getAuth()));
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        String BASE_URL = RestServerDefines.Friend + "/2013-12-26/Application/" + getAppKey() + "/Robot/";

        retrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        movieService = retrofit.create(BaseApiService.class);
//        movieService.GetRobotByID(appid, url, map)
//                .subscribeOn(Schedulers.io())
//                .unsubscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(subscriber);
    }
    public void setDisturb(final Observer subscriber, String appid, String url, RequestBody map) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(new TokenInterceptor(getAuth()));
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        String BASE_URL = RestServerDefines.Friend + "/2013-12-26/Application/" + getAppKey() + "/IM/";

        retrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        movieService = retrofit.create(BaseApiService.class);
        movieService.setDis(appid, url, map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }


    public void getDisturb(final Observer subscriber, String appid, String url, RequestBody map) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(new TokenInterceptor(getAuth()));
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        String BASE_URL = RestServerDefines.Friend + "/2013-12-26/Application/" + getAppKey() + "/IM/";

        retrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        movieService = retrofit.create(BaseApiService.class);
        movieService.getDisturb(appid, url, map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }
    public void GetRobots(final Observer subscriber, String appid, String url, RequestBody map) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(new TokenInterceptor(getAuth()));
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        String BASE_URL = RestServerDefines.Friend + "/2013-12-26/Application/" + getAppKey() + "/Robot/";

        retrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        movieService = retrofit.create(BaseApiService.class);
        movieService.GetRobots(appid, url, map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }
    public void GetRobotsInfo(final Observer subscriber, String appid, String url,String id) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(new TokenInterceptor(getAuth()));
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        String BASE_URL = RestServerDefines.Friend + "/2013-12-26/Application/" + getAppKey() + "/Robot/";

        retrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        movieService = retrofit.create(BaseApiService.class);
        movieService.GetRobotByID(appid, url,id)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void setFriendRemark(final Observer subscriber, String appid, String url, RequestBody map) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(new TokenInterceptor(getAuth()));
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        String BASE_URL = RestServerDefines.Friend + "/2013-12-26/Application/" + getAppKey() + "/IM/";

        retrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        movieService = retrofit.create(BaseApiService.class);
        movieService.setFriendRemark(appid, url, map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void setUserVerify(final Observer subscriber, String appid, String url, RequestBody map) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(new TokenInterceptor(getAuth()));
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        String BASE_URL = RestServerDefines.Friend + "/2013-12-26/Application/" + getAppKey() + "/IM/";

        retrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        movieService = retrofit.create(BaseApiService.class);
        movieService.setUserVerify(appid, url, map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }


    public static JSONObject buildSmsBody(String mobilenum, Context c, String time) {
        JSONObject map = new JSONObject();
        try {
            map.put("useragent", DemoUtils.buildSubDevice());
            map.put("mobilenum", mobilenum);
            map.put("countrycode", "+86");
            map.put("type", "0");
            map.put("mac", DemoUtils.getMacAddress(c));
            map.put("flag", "1");
            map.put("reqtime", time);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static JSONObject buildRegister(String mobilenum, Context c, String time, String sms, String pwd) {

        JSONObject map = new JSONObject();
        try {
            map.put("useragent", DemoUtils.buildSubDevice());
            map.put("mobilenum", mobilenum);
            map.put("countrycode", "+86");
            map.put("smsverifycode", sms);
            map.put("userpasswd", pwd);
            map.put("version", RestServerDefines.VERSION);
            map.put("completeCode", DemoUtils.getMacAddress(c));
            map.put("reqtime", time);
            map.put("deviceType", "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static JSONObject buildAddFriend(String self, String other) {

        JSONObject map = new JSONObject();
        try {
            map.put("message", "加你为好友");
            map.put("source", "1");
            map.put("useracc", getAppKey() + "#" + self);
            map.put("friendUseracc", getAppKey() + "#" + other);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }
    public static JSONObject buildGetDis() {

        JSONObject map = new JSONObject();
        try {
            map.put("userName", CCPAppManager.getUserId());
            map.put("pageNo", "1");
            map.put("pageSize", "20");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }
    public static JSONObject buildSetDis(String s, String other,String user) {

        JSONObject map = new JSONObject();
        try {
            map.put("type", s);
            map.put("setAccount", other);
            map.put("userName", user);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }
    public static JSONObject GetRobots() {

        JSONObject map = new JSONObject();
        try {
            map.put("pageSize", "10");
            map.put("page", "1");
            map.put("order", "1");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static JSONObject buildAddFriendOther(String self, String other) {

        JSONObject map = new JSONObject();
        try {
            map.put("message", "加你为好友");
            map.put("source", "2");
            map.put("useracc", getAppKey() + "#" + self);
            map.put("friendUseracc", getAppKey() + "#" + other);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }


    public static JSONObject buildDeleteFriend(String self, String other) {

        JSONObject map = new JSONObject();
        try {
            map.put("useracc", getAppKey() + "#" + self);
            map.put("friendUseracc", getAppKey() + "#" + other);
            map.put("allDel", "1");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static JSONObject buildGetPersonPic(String self) {

        JSONObject map = new JSONObject();
        try {
            map.put("useracc", getAppKey() + "#" + self);
            map.put("updateTime", DateUtil.sFormatNowDate2(new Date()));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }


    //获取个人信息
    public static JSONObject buildGetFriendInfo(String self, String other) {

        JSONObject map = new JSONObject();

        try {
            map.put("useracc", getAppKey() + "#" + self);
            map.put("searchContent", getAppKey() + "#" + other);
//            map.put("isSimpUseracc ", "1");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static JSONObject buildVeriInfo(String self, String other) {

        JSONObject map = new JSONObject();
        try {
            map.put("useracc", getAppKey() + "#" + self);
            map.put("addVerify", other);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static JSONObject buildGetToken(String appId) {

        JSONObject map = new JSONObject();
        try {
            map.put("appId", appId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static JSONObject buildAgreeFriend(String self, String other) {

        JSONObject map = new JSONObject();
        try {
            map.put("useracc", getAppKey() + "#" + self);
            map.put("friendUseracc", getAppKey() + "#"+other);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static JSONObject buildGetFriends(String self, String time) {
        JSONObject map = new JSONObject();
        try {
            map.put("useracc", getAppKey() + "#" + self);
            map.put("timestamp", time);
            map.put("size", "100");
            map.put("isUpdate", "0");
//            map.put("isSimpUseracc ", "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static JSONObject buildFriendRemark(String self, String other, String remark) {

        JSONObject map = new JSONObject();
        try {

            map.put("useracc", getAppKey() + "#" + self);
            map.put("friendUseracc", getAppKey() + "#" + other);
            map.put("remarkName", remark);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static JSONObject buildFriendMessage(String self, String time) {

        JSONObject map = new JSONObject();
        try {

            map.put("useracc", getAppKey() + "#" + self);
            map.put("timestamp", time);
            map.put("size", "6");
            map.put("order", "2");
//            map.put("isSimpUseracc", "1");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static JSONObject buildGetDisturb(String userName, String pageNo, String pageSize) {

        JSONObject map = new JSONObject();
        try {

            map.put("userName", userName);
            map.put("pageNo", pageNo);
            map.put("pageSize", pageSize);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static JSONObject buildLogin(String mobilenum, String pwd) {

        JSONObject map = new JSONObject();
        try {
            map.put("mobilenum", mobilenum);
            map.put("userpasswd", pwd);
            map.put("platform", "1");
            map.put("version", "5.3.2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }


    public static JSONObject buildNewPwd(String mobilenum, String pwd, String sms) {

        JSONObject map = new JSONObject();
        try {
            map.put("mobilenum", mobilenum);
            map.put("smsverifycode", sms);
            map.put("new_pwd", pwd);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static JSONObject buildGetVerify(String userId) {

        JSONObject map = new JSONObject();
        try {
            map.put("useracc", getAppKey() + "#" + userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;

    }


    public static String getAppKey() {
        if (RestServerDefines.QR_APK) {
            return RestServerDefines.APPKER_CODE;
        } else {
            return RestServerDefines.APPKER;
        }

    }
}
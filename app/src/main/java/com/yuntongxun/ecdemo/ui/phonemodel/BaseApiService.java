package com.yuntongxun.ecdemo.ui.phonemodel;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BaseApiService{




    @POST("https://imapp.yuntongxun.com:443/2013-12-26/Application/{appid}/IM/addFriend")
    Observable<ResponseBody> addFriend(@Path("appid") String appid,
                                       @Query("sig") String sig,
                                       @Body RequestBody paramsMap );


    @POST("https://imapp.yuntongxun.com:443/2013-12-26/Application/{appid}/IM/delFriend")
    Observable<ResponseBody> delFriend(@Path("appid") String appid,
                                       @Query("sig") String sig,
                                       @Body RequestBody paramsMap );


    @POST("https://imapp.yuntongxun.com:443/2013-12-26/Application/{appid}/IM/getPersonInfo")
    Observable<ResponseBody> getPersonInfo(@Path("appid") String appid,
                                           @Query("sig") String sig,
                                           @Body RequestBody paramsMap );


    @POST("https://imapp.yuntongxun.com:443/2013-12-26/Application/{appid}/IM/getUserAvatar")
    Observable<ResponseBody> getPersonPic(@Path("appid") String appid,
                                          @Query("sig") String sig,
                                          @Body RequestBody paramsMap );

    @POST("https://imapp.yuntongxun.com:443/2013-12-26/Application/{appid}/IM/getFriendInfo")
    Observable<ResponseBody> getFriendInfo(@Path("appid") String appid,
                                           @Query("sig") String sig,
                                           @Body RequestBody paramsMap );

    @POST("https://imapp.yuntongxun.com:443/2013-12-26/Application/{appid}/IM/setUserVerify")
    Observable<ResponseBody> setUserVerify(@Path("appid") String appid,
                                           @Query("sig") String sig,
                                           @Body RequestBody paramsMap );

    @POST("https://imapp.yuntongxun.com:443/2013-12-26/Application/{appid}/IM/friendMessage")//相关的
    Observable<ResponseBody> friendMessage(@Path("appid") String appid,
                                           @Query("sig") String sig,
                                           @Body RequestBody paramsMap );

    @POST("https://imapp.yuntongxun.com:443/2013-12-26/Application/{appid}/IM/GetDisturb")//
    Observable<ResponseBody> getDisturb(@Path("appid") String appid,
                                        @Query("sig") String sig,
                                        @Body RequestBody paramsMap );

    @POST("https://imapp.yuntongxun.com:443/2013-12-26/Application/{appid}/IM/setFriendRemark")
    Observable<ResponseBody> setFriendRemark(@Path("appid") String appid,
                                             @Query("sig") String sig,
                                             @Body RequestBody paramsMap );


    @POST("https://imapp.yuntongxun.com:443/2013-12-26/Application/{appid}/IM/friendAgree")
    Observable<ResponseBody> friendAgree(@Path("appid") String appid,
                                         @Query("sig") String sig,
                                         @Body RequestBody paramsMap );


    @POST("https://imapp.yuntongxun.com:443/2013-12-26/Application/{appid}/IM/friendRefuse")
    Observable<ResponseBody> friendRefuse(@Path("appid") String appid,
                                          @Query("sig") String sig,
                                          @Body RequestBody paramsMap );


    @POST("https://imapp.yuntongxun.com:443/2013-12-26/Application/{appid}/IM/getFriends")
    Observable<ResponseBody> getFriends(@Path("appid") String appid,
                                        @Query("sig") String sig,
                                        @Body RequestBody paramsMap );

    @POST("https://imapp.yuntongxun.com:443/2013-12-26/Application/{appid}/IM/getUserVerify")
    Observable<ResponseBody> getVerify(@Path("appid") String appid,
                                       @Query("sig") String sig,
                                       @Body RequestBody paramsMap );

    @POST("https://imapp.yuntongxun.com:443/2013-12-26/Application/{appid}/IM/SetDisturb")
    Observable<ResponseBody> setDis(@Path("appid") String appid,
                                    @Query("sig") String sig,
                                    @Body RequestBody paramsMap );

    @POST("https://imapp.yuntongxun.com:443/2013-12-26/Application/{appid}/Robot/GetRobots")
    Observable<ResponseBody> GetRobots(@Path("appid") String appid,
                                       @Query("sig") String sig,
                                       @Body RequestBody paramsMap );

    @GET("https://imapp.yuntongxun.com:443/2013-12-26/Application/{appid}/Robot/GetRobotByID")
    Observable<ResponseBody> GetRobotByID(@Path("appid") String appid,
                                          @Query("sig") String sig,@Query("id") String id
    );






}
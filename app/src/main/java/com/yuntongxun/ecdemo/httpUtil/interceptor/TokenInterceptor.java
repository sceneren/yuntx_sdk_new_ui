package com.yuntongxun.ecdemo.httpUtil.interceptor;


import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class TokenInterceptor implements Interceptor{



    public TokenInterceptor(String s){
        auth = s;
    }

    private String auth;

    @Override
    public Response intercept(Chain chain) throws IOException{
        Request originalRequest = chain.request();

        Request tokenRequest = originalRequest.newBuilder().header("accept", "application/json")
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Authorization",auth)
                .build();
        return chain.proceed(tokenRequest);
    }


}

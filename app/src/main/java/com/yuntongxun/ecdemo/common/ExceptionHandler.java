package com.yuntongxun.ecdemo.common;

import android.util.SparseArray;

import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;

/**
 * Created by luhuashan on 17/9/4.
 * email huashan2007@sina.cn
 */
public class ExceptionHandler{

    public static boolean RELEASE = false;

    public static final String TAG = "ExceptionHandler";


    private static SparseArray<String>  errorArr = new SparseArray<String>();


    static {
        errorArr.put(112191,"");
        errorArr.put(112218,"当前无好友请求列表");

    }

    public static String get(int k){
        return errorArr.get(k);
    }


    public static void logHttpResp(String body){
        if(RELEASE){
            return;
        }
        if(body==null){
            return;
        }
        LogUtil.e(TAG+"--------OKHTTP3::::RESPONSE",body);
    }


    public static void converToastMsg(int code ,String msg){
         Object v  = get(code);
         if(v==null){
             ToastUtil.showMessage(msg);
         }else {
             ToastUtil.showMessage(v.toString());
         }
    }
}

package com.yuntongxun.ecdemo.ui;

import com.yuntongxun.ecdemo.BuildConfig;
import com.yuntongxun.ecdemo.R;

/**
 * Created by luhuashan on 17/6/9.
 */
public class RestServerDefines {

    public static int APP_VERSION = 541;

    public static  final String SERVER = "https://imapp.yuntongxun.com:443";
        public static  final String Friend = "https://imapp.yuntongxun.com:443";

    public static final String VERSION = BuildConfig.VERSION_NAME;


    public static final boolean QR_APK = false;

    public static final boolean IM = false;
    public static final String APPKER = RestServerDefines.QR_APK ? RestServerDefines.APPKER_CODE : RestServerDefines.APPKER_WEB;

    public static final String APPKER_WEB = "20150314000000110000000000000010";//demo appkey

    //小军
    public static final String APPKER_CODE = "20150314000000110000000000000010";
    public static final String TOKEN = "17E24E5AFDB6D0C1EF32F3533494502B";


    public static final int[] arr = new int[]{R.drawable.def_usericon, R.drawable.def_usericon_two, R.drawable.def_usericon_three, R.drawable.def_usericon_four, R.drawable.def_usericon_five, R.drawable.def_usericon_six, R.drawable.def_usericon_seven, R.drawable.def_usericon_eight};

    public static final String FILE_ASSISTANT = "~ytxfa";
    public static  String ROBOT = "";
}

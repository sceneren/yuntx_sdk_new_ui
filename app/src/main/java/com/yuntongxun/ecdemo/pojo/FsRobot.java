package com.yuntongxun.ecdemo.pojo;

/**
 * Created by luhuashan on 17/12/1.
 * email huashan2007@sina.cn
 */

public class FsRobot {

    public String name ;
    public String age ;
    public String sex ;



    public String getDesc(){

        StringBuilder sb = new StringBuilder();
        sb.append("我的姓名:");
        sb.append(name);
        sb.append(",年龄:");
        sb.append(age);


        return sb.toString();
    }


}

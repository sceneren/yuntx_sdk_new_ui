package com.yuntongxun.ecdemo.exception;

/**
 * Created by luhuashan on 17/4/7.
 */
public class ECDemoException extends Exception {

    public ECDemoException(){

    }

    public ECDemoException(String message){
        super(message);
    }

    public ECDemoException(Throwable throwable){
        super(throwable);
    }


}

package com.yuntongxun.ecdemo.bean;



public class BaseJson<T>{

    private T data;

    private BaseStatus status;

    public BaseJson(BaseStatus status,T data) {
        this.data = data;
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public BaseStatus getStatus() {
        return status;
    }

    public void setStatus(BaseStatus status) {
        this.status = status;
    }
}

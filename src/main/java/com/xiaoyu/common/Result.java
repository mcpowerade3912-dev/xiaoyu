package com.xiaoyu.common;

public class Result{
    private String code;
    private String msg;
    private Object data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Result(String code, Object data) {
        this.code = code;
        this.data = data;
    }

    public Result(String code, Object data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }
}

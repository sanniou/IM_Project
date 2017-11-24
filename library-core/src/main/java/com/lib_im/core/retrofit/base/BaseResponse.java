package com.lib_im.core.retrofit.base;

/**
 * Created by songgx on 2017/9/26.
 * 网络请求基类
 */

public abstract class BaseResponse<T> {

    public int code;

    public String message;
    public T result;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}

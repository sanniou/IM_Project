package com.lib_im.pro.retrofit.base;

/**
 * Created by songgx on 2017/9/26.
 * 定义接口数据返回是object的基类
 */

public class BaseObjectResponse<T> extends BaseResponse {

    public T result;

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}

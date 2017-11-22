package com.lib_im.pro.im.listener;

/**
 * Created by songgx on 2016/6/15.
 * 消息发送成功与否的回调
 */
public interface MessageCallBack<T> {

    void onSuccess(T msg);

    void onError(T msg);

}

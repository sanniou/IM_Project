package com.lib_im.pro.im.listener;

/**
 * Created by songgx on 2017/2/8.
 * 发送消息成功与失败
 */

public interface MessageStateListener<T> {
    void stateSuccess(T msg);
    void stateFailed(T msg);
}

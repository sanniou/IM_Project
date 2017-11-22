package com.lib_im.pro.im.listener;

/**
 * Created by songgx on 2016/6/15.
 * 消息监听
 */
public interface IMMessageListener<T> {
    /**
     * 监听接收到消息
     */
    void onReceiveMessage(T chatMessage);

}

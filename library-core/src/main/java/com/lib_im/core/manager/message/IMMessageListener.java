package com.lib_im.core.manager.message;

/**
 * Created by songgx on 2016/6/15.
 * 消息监听
 */
public interface IMMessageListener {

    /**
     * 监听接收到消息
     */
    void onReceiveMessage(String chatMessage);

    void onReceiveGroupMessage(String chatMessage);

    void onReceiveReceipt(String receiptId);

}

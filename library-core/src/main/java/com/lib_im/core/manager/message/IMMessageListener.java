package com.lib_im.core.manager.message;

import com.lib_im.core.entity.ChatMessage;

/**
 * Created by songgx on 2016/6/15.
 * 消息监听
 */
public interface IMMessageListener {

    /**
     * 监听接收到消息
     */
    void onReceiveMessage(ChatMessage chatMessage);

    void onReceiveGroupMessage(ChatMessage chatMessage);

    void onReceiveReceipt(ChatMessage chatMessage);

}

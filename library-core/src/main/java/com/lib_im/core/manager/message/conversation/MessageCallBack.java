package com.lib_im.core.manager.message.conversation;

import com.lib_im.core.entity.ChatMessage;

/**
 * Created by songgx on 2016/6/15.
 * 消息发送成功与否的回调
 */
public interface MessageCallBack {

    void onSuccess(ChatMessage msg);

    void onError(ChatMessage msg);

}

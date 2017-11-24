package com.lib_im.core.manager.message.conversation;

import com.lib_im.core.entity.ChatMessage;

import java.util.List;

/**
 * Created by songgx on 2016/8/23.
 * 历史消息刷新接口
 */
public interface OnRoomChatRecordListener {

    /**
     * @param t 类型
     * @descript 群组历史消息回调
     */
    void onRoomChatRecorder(List<ChatMessage> t);
}

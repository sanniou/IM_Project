package com.lib_im.core.manager.message;

import com.lib_im.core.entity.ChatMessage;

import java.util.List;

/**
 * Created by songgx on 2016/8/24.
 * 单人聊天记录查询回调接口
 */
public interface OnChatRecordListener {
    /**
     * @param t 群记录实体
     * @descript 单聊历史消息回调
     */
    void onChatRecorder(List<ChatMessage> t);
}

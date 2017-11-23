package com.lib_im.core.manager.message;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.lib_im.core.entity.ChatMessage;
import com.lib_im.pro.im.listener.MessageCallBack;

public interface IConversation {

    void send(@NonNull ChatMessage msg, @Nullable MessageCallBack call);
}

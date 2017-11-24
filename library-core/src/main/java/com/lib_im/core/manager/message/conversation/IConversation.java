package com.lib_im.core.manager.message.conversation;

import com.lib_im.core.entity.ChatMessage;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;

public interface IConversation {

    @NonNull
    Observable<ChatMessage> send(@NonNull ChatMessage msg);
}

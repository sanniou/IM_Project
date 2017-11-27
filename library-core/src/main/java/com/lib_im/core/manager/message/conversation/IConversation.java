package com.lib_im.core.manager.message.conversation;

import com.lib_im.profession.entity.ChatMessage;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;

public interface IConversation {

    @NonNull
    Observable<String> send(@NonNull String msg);
}

package com.lib_im.core.manager.message.conversation;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

/**
 * 基础的会话功能实现，包装 Chat
 */
public abstract class AbsUserConversation implements IConversation {

    private Chat mChat;

    public AbsUserConversation(@NonNull Chat chat) {
        mChat = chat;
    }

    @Override
    public final Observable<String> send(@NonNull String msg) {
        return Observable.create((ObservableOnSubscribe<String>) e -> {
            Message stanza = new Message();
            stanza.setBody(msg);
            //设置消息发送需要回执
            DeliveryReceiptRequest.addTo(stanza);
            mChat.send(stanza);
            //消息撤回，将消息发送出去
            e.setCancellable(() -> {
                e.onNext(msg);
            });
            e.onComplete();

        }).subscribeOn(Schedulers.io())
                         .observeOn(AndroidSchedulers.mainThread());
    }
}

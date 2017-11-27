package com.lib_im.core.manager.message.conversation;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jxmpp.jid.Jid;

import java.util.Collection;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

/**
 * 基础的会话功能实现，包装 MultiUserChat
 */
public abstract class AbsGroupConversation implements IConversation {

    private MultiUserChat mGroup;

    public AbsGroupConversation(@NonNull MultiUserChat group) {
        mGroup = group;
    }

    public final void banUser(Jid jid, String reason)
            throws XMPPException.XMPPErrorException, SmackException.NoResponseException, SmackException.NotConnectedException, InterruptedException {
        mGroup.banUser(jid, reason);
    }

    public final void banUsers(Collection<? extends Jid> jids)
            throws XMPPException.XMPPErrorException, SmackException.NoResponseException, SmackException.NotConnectedException, InterruptedException {
        mGroup.banUsers(jids);
    }

    @Override
    public final Observable<String> send(@NonNull String msg) {
        return Observable.create((ObservableOnSubscribe<String>) e -> {
            mGroup.sendMessage(msg);
            //消息撤回，将消息发送出去
            e.setCancellable(() -> {
                e.onNext(msg);
            });
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                         .observeOn(AndroidSchedulers.mainThread());
    }

}

package com.lib_im.core.manager.message.conversation;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatException;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

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
            e.setCancellable(() -> e.onNext(msg));
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                         .observeOn(AndroidSchedulers.mainThread());
    }

    public final void leave() throws SmackException.NotConnectedException, InterruptedException {
        mGroup.leave();
    }

    public final void destroy(String reason, String userId)
            throws SmackException.NotConnectedException, InterruptedException, XmppStringprepException, XMPPException.XMPPErrorException, SmackException.NoResponseException {
        mGroup.destroy(reason, JidCreate.entityBareFrom(userId));
    }

    public final void changeNickname(String nickname)
            throws SmackException.NoResponseException, XMPPException.XMPPErrorException, SmackException.NotConnectedException, InterruptedException, MultiUserChatException.MucNotJoinedException, XmppStringprepException {
        mGroup.changeNickname(Resourcepart.from(nickname));
    }

    /**
     * 禁言
     */
    public final void revokeVoice(String nickname)
            throws XmppStringprepException, XMPPException.XMPPErrorException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException {
        mGroup.revokeVoice(Resourcepart.from(nickname));
    }

    public void release(MessageListener messageListener) {
        mGroup.removeMessageListener(messageListener);
    }
}

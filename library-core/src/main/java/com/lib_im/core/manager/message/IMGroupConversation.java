package com.lib_im.core.manager.message;

import android.support.annotation.NonNull;

import com.lib_im.core.entity.ChatMessage;
import com.lib_im.pro.im.listener.MessageCallBack;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jxmpp.jid.Jid;

import java.util.Collection;

public class IMGroupConversation implements IConversation {

    private MultiUserChat mGroup;

    public IMGroupConversation(@NonNull MultiUserChat group) {
        mGroup = group;
    }

    public void banUser(Jid jid, String reason)
            throws XMPPException.XMPPErrorException, SmackException.NoResponseException, SmackException.NotConnectedException, InterruptedException {
        mGroup.banUser(jid, reason);
    }

    public void banUsers(Collection<? extends Jid> jids)
            throws XMPPException.XMPPErrorException, SmackException.NoResponseException, SmackException.NotConnectedException, InterruptedException {
        mGroup.banUsers(jids);
    }

    @Override
    public void send(@NonNull ChatMessage msg, MessageCallBack call) {
        try {
            mGroup.sendMessage(msg.getMsg());
            msg.setState(ChatMessage.SEND_STATUS_SUCCESS);
            if (call != null) {
                call.onSuccess(msg);
            }
        } catch (SmackException.NotConnectedException | InterruptedException e) {
            msg.setState(ChatMessage.SEND_STATUS_FAILD);
            if (call != null) {
                call.onError(msg);
            }
        }

    }
}

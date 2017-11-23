package com.lib_im.core.manager.message;

import android.support.annotation.NonNull;

import com.lib_im.core.entity.ChatMessage;
import com.lib_im.pro.im.listener.MessageCallBack;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;

public class IMUserConversation implements IConversation {

    private Chat mChat;

    public IMUserConversation(@NonNull Chat chat) {
        mChat = chat;
    }

    @Override
    public void send(@NonNull ChatMessage msg, MessageCallBack call) {
        try {
            Message stanza = new Message();
            stanza.setBody(msg.getMsg());
            //设置消息发送需要回执
            DeliveryReceiptRequest.addTo(stanza);
            mChat.send(stanza);
            msg.setState(ChatMessage.SEND_STATUS_SUCCESS);
            if (call != null) {
                call.onSuccess(msg);
            }
        } catch (SmackException.NotConnectedException | InterruptedException e) {
            e.printStackTrace();
            if (call != null) {
                call.onError(msg);
            }
        }
    }
}

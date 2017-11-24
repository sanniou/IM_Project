package com.lib_im.core.manager.message.conversation;


import com.lib_im.core.api.IMRequest;
import com.lib_im.core.entity.ChatMessage;
import com.lib_im.core.entity.ChatRecord;
import com.lib_im.core.entity.Contact;
import com.lib_im.core.retrofit.rx.SimpleCompleteObserver;
import com.lib_im.core.retrofit.rx.SimpleObserver;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

public class IMUserConversation implements IConversation {

    private Chat mChat;

    public IMUserConversation(@NonNull Chat chat) {
        mChat = chat;
    }

    @Override
    public Observable<ChatMessage> send(@NonNull ChatMessage msg) {
        return Observable.create((ObservableOnSubscribe<ChatMessage>) e -> {
            Message stanza = new Message();
            stanza.setBody(msg.getMsg());
            //设置消息发送需要回执
            DeliveryReceiptRequest.addTo(stanza);
            mChat.send(stanza);
            msg.setState(ChatMessage.SEND_STATUS_SUCCESS);
            e.onNext(msg);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                         .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @param messageId  xmpp消息id
     * @param fromUserId 发送人登录名
     * @param rows       每页多少条，可传空
     */
    public void getChatHistoryMessage(String messageId, String fromUserId, String toUserId,
                                      int page, int rows, OnChatRecordListener listener) {
        final List<ChatMessage> chatmsglist = new ArrayList<>();
        IMRequest.getInstance()
                 .queryChatRecord(messageId, fromUserId, toUserId, page, rows)
                 .subscribe(new SimpleObserver<List<ChatRecord>>() {
                     @Override
                     public void onNext(
                             @NonNull List<ChatRecord> chatRecords) {
                         if (chatRecords != null) {
                             for (ChatRecord chatRecord : chatRecords) {
                                 if (chatRecord != null) {
                                     ChatMessage chatMessage = packChatHistoryMessage(chatRecord);
                                     chatmsglist.add(chatMessage);
                                 }
                             }
                             listener.onChatRecorder(chatmsglist);
                         } else {
                             listener.onChatRecorder(null);
                         }
                     }

                     @Override
                     public void onError(@NonNull Throwable e) {
                         e.printStackTrace();
                         listener.onChatRecorder(null);
                     }
                 });

    }

    /**
     * @param chatRecord 群组历史记录实体
     * @descript 封装单聊历史记录消息实体
     */
    private ChatMessage packChatHistoryMessage(ChatRecord chatRecord) {

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoom(false);
        chatMessage.setSelfId(null);
        chatMessage.setSelfName(null);
        if (chatRecord.getSendTime() != null && !chatRecord.getSendTime().equals("")) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            try {
                Date date = format.parse(chatRecord.getSendTime());
                long sendTime = date.getTime();
                chatMessage.setDate(sendTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        try {
            JSONObject jsonObject = new JSONObject(chatRecord.getContent());
            String fromId = jsonObject.getString("sendUserId");
            String fromName = jsonObject.getString("sendUserName");
            String fromIcon = jsonObject.getString("headIcon");
            chatMessage.setFromName(fromName);
            chatMessage.setFromId(fromId);
            chatMessage.setHeadIcon(fromIcon);
            chatMessage.setMT(false);

            chatMessage.setState(ChatMessage.SEND_STATUS_SUCCESS);
            parseMessage(chatMessage, jsonObject);
            if (chatMessage.getMsg_type() == ChatMessage.MESSAGE_TYPE_SOUNDS) {
                if (chatMessage.isMT()) {
                    chatMessage.setMark(1);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return chatMessage;
    }

    /**
     * @descript 解析收到的新消息
     */
    private void parseMessage(ChatMessage chatMessage, JSONObject jsonObject) throws JSONException {
        String sendUserId = jsonObject.getString("sendUserId");
        chatMessage.setFromId(sendUserId);
        String sendUserName = jsonObject.getString("sendUserName");
        chatMessage.setFromName(sendUserName);
        String sendUserIcon = jsonObject.getString("headIcon");
        chatMessage.setHeadIcon(sendUserIcon);
        String msgId = jsonObject.getString("msgId");
        chatMessage.setMsgId(msgId);
        String content = jsonObject.getString("content");
        chatMessage.setMsg(content);
        int msgType = jsonObject.getInt("msgType");
        chatMessage.setMsg_type(msgType);
        String imageFileName = jsonObject.getString("fileName");
        chatMessage.setFileName(imageFileName);
        String imageLocalPath = jsonObject.getString("localPath");
        chatMessage.setFile_path(imageLocalPath);
        String imageRemoteUrl = jsonObject.getString("remoteUrl");
        chatMessage.setRemoteUrl(imageRemoteUrl);
        float voiceLength = (float) jsonObject.getDouble("fileLength");
        chatMessage.setFileLength(voiceLength);
    }

    /**
     * 加载所有通讯录
     */
    public void loadContact(final IMContactListener contactListener) {
        IMRequest.getInstance()
                 .queryFriendList("1")
                 .subscribe(new SimpleObserver<List<Contact>>() {
                     @Override
                     public void onNext(@NonNull List<Contact> contacts) {

                         contactListener.onContactUpdate(contacts);

                     }

                     @Override
                     public void onError(@NonNull Throwable e) {
                         contactListener.onContactError(e.getMessage());
                     }
                 });

    }

    /**
     * 删除好友 
     */
    public void removeContact(String otherUserID, final IMContactListener contactListener) {
        IMRequest.getInstance().removeContact(otherUserID)
                 .subscribe(new SimpleCompleteObserver<List<String>>() {
                     @Override
                     public void onError(@NonNull Throwable e) {
                         contactListener.onContactError(e.getMessage());
                     }

                     @Override
                     public void onComplete() {
                         contactListener.onContactDeleted("");
                     }
                 });
    }

    /**
     * 添加好友
     */
    public void addContact(String otherUserID, final IMContactListener contactListener) {
        IMRequest.getInstance().addContact(otherUserID)
                 .subscribe(new SimpleCompleteObserver<List<String>>() {
                     @Override
                     public void onError(@NonNull Throwable e) {
                         contactListener.onContactError(e.getMessage());
                     }

                     @Override
                     public void onComplete() {
                         contactListener.onContactAdded("");
                     }
                 });
    }

    /**
     * 搜索好友
     */
    public void searchContact(String key, final OnLoadListener onLoadListener) {
        IMRequest.getInstance().searchFriendList(key)
                 .subscribe(new SimpleObserver<List<Contact>>() {
                     @Override
                     public void onNext(@NonNull List<Contact> contacts) {
                         onLoadListener.onLoadSuccess(contacts);
                     }

                     @Override
                     public void onError(@NonNull Throwable e) {
                         onLoadListener.onLoadFailed(e.getMessage());
                     }
                 });
    }

    /**
     * 同意好友请求
     */
    public void acceptFriendInvitation(String otherUserID, final OnLoadListener onLoadListener) {
        IMRequest.getInstance().acceptRequest(otherUserID)
                 .subscribe(new SimpleCompleteObserver<List<String>>() {
                     @Override
                     public void onError(
                             @NonNull Throwable e) {
                         onLoadListener.onLoadFailed(e.getMessage());
                     }

                     @Override
                     public void onComplete() {
                         onLoadListener.onLoadSuccess("");
                     }
                 });

    }

    /**
     * 拒绝好友请求
     */
    public void refuseFriendInvitation(String otherUserID, final OnLoadListener onLoadListener) {
        IMRequest.getInstance().refuseRequest(otherUserID)
                 .subscribe(new SimpleCompleteObserver<List<String>>() {
                     @Override
                     public void onError(
                             @NonNull Throwable e) {
                         onLoadListener.onLoadFailed(e.getMessage());
                     }

                     @Override
                     public void onComplete() {
                         onLoadListener.onLoadSuccess("");
                     }
                 });
    }

}

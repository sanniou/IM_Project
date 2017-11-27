package com.lib_im.profession.message;

import com.lib_im.core.manager.message.conversation.AbsUserConversation;
import com.lib_im.profession.api.IMRequest;
import com.lib_im.profession.entity.ChatMessage;
import com.lib_im.core.entity.ChatRecord;
import com.lib_im.profession.entity.Contact;

import org.jivesoftware.smack.chat2.Chat;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;

/**
 * 在基础上封装一层业务操作
 */
public class IMUserConversation extends AbsUserConversation {

    public IMUserConversation(@NonNull Chat chat) {
        super(chat);
    }

    /**
     * @param messageId  xmpp消息id
     * @param fromUserId 发送人登录名
     * @param rows       每页多少条，可传空
     */
    public Observable<List<ChatMessage>> getChatHistoryMessage(String messageId, String fromUserId,
                                                               String toUserId,
                                                               int page, int rows) {
        return IMRequest.getInstance()
                        .queryChatRecord(messageId, fromUserId, toUserId, page, rows)
                        .map(chatRecords -> {
                            List<ChatMessage> chatMessageList = new ArrayList<>();
                            for (ChatRecord chatRecord : chatRecords) {
                                if (chatRecord != null) {
                                    ChatMessage chatMessage = packChatHistoryMessage(chatRecord);
                                    chatMessageList.add(chatMessage);
                                }
                            }
                            return chatMessageList;
                        });
    }

    /**
     * @param chatRecord 群组历史记录实体
     * @descript 封装单聊历史记录消息实体
     */
    private ChatMessage packChatHistoryMessage(ChatRecord chatRecord) throws JSONException {
        ChatMessage message = ChatMessage.paresMessage(chatRecord.getContent());
        message.setRoom(false);
        return message;
    }

    /**
     * 加载所有通讯录
     */
    public Observable<List<Contact>> loadContact() {
        return IMRequest.getInstance()
                        .queryFriendList(null);
    }

    /**
     * 删除好友 
     */
    public Observable<List<String>> removeContact(String otherUserID) {
        return IMRequest.getInstance()
                        .removeContact(otherUserID);
    }

    /**
     * 添加好友
     */
    public Observable<List<String>> addContact(String otherUserID) {
        return IMRequest.getInstance().addContact(otherUserID);
    }

    /**
     * 搜索好友
     */
    public Observable<List<Contact>> searchContact(String key) {
        return IMRequest.getInstance().searchFriendList(key);
    }

    /**
     * 同意好友请求
     */
    public Observable<List<String>> acceptFriendInvitation(String otherUserID) {
        return IMRequest.getInstance().acceptRequest(otherUserID);

    }

    /**
     * 拒绝好友请求
     */
    public Observable<List<String>> refuseFriendInvitation(String otherUserID) {
        return IMRequest.getInstance().refuseRequest(otherUserID);
    }

}

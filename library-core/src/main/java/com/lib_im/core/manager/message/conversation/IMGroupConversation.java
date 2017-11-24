package com.lib_im.core.manager.message.conversation;

import com.lib_im.core.api.IMRequest;
import com.lib_im.core.entity.ChatMessage;
import com.lib_im.core.entity.GroupChatRecord;
import com.lib_im.core.entity.GroupContact;
import com.lib_im.core.entity.GroupDetails;
import com.lib_im.core.entity.GroupMember;
import com.lib_im.core.retrofit.rx.SimpleCompleteObserver;
import com.lib_im.core.retrofit.rx.SimpleObserver;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.json.JSONException;
import org.json.JSONObject;
import org.jxmpp.jid.Jid;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

public class IMGroupConversation implements IConversation {

    private MultiUserChat mGroup;
    private String mGroupId;

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
    public Observable<ChatMessage> send(@NonNull ChatMessage msg) {
        return Observable.create((ObservableOnSubscribe<ChatMessage>) e -> {
            Message stanza = new Message();
            stanza.setBody(msg.getMsg());
            mGroup.sendMessage(msg.getMsg());
            msg.setState(ChatMessage.SEND_STATUS_SUCCESS);
            e.onNext(msg);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                         .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @param rows      一页多少条
     * @param logId     记录id
     * @param messageId 消息id
     * @descript 获取历史记录或者未读消息
     */
    public void getHistoryMessage(String logId, String messageId, int page,
                                  int rows, OnRoomChatRecordListener listener) {
        IMRequest.getInstance()
                 .queryGroupChatRecord(mGroupId, logId, messageId, page, rows)
                 .subscribe(new SimpleObserver<List<GroupChatRecord>>() {
                     @Override
                     public void onNext(@NonNull List<GroupChatRecord> groupChatRecords) {
                         List<ChatMessage> historylist = new ArrayList<>();
                         for (GroupChatRecord groupChatRecord : groupChatRecords) {
                             if (groupChatRecord != null) {
                                 ChatMessage chatMessage = packRoomHistoryMessage(
                                         groupChatRecord);
                                 historylist.add(chatMessage);
                             }
                         }
                         listener.onRoomChatRecorder(historylist);
                     }

                     @Override
                     public void onError(@NonNull Throwable e) {
                         e.printStackTrace();
                         listener.onRoomChatRecorder(null);
                     }
                 });

    }

    /**
     * @param groupChatRecord 群组历史记录实体
     * @descript 封装群组历史记录消息实体
     */
    private ChatMessage packRoomHistoryMessage(GroupChatRecord groupChatRecord) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoom(true);
        chatMessage.setRoomId(groupChatRecord.getGroupId());
        chatMessage.setSelfId(null);
        chatMessage.setSelfName(null);
        if (groupChatRecord.getSendTime() != null && !groupChatRecord.getSendTime().equals("")) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            try {
                Date date = format.parse(groupChatRecord.getSendTime());
                long sendTime = date.getTime();
                chatMessage.setDate(sendTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        try {
            JSONObject jsonObject = new JSONObject(groupChatRecord.getContent());
            String fromId = jsonObject.getString("sendUserId");
            String fromName = jsonObject.getString("sendUserName");
            String fromIcon = jsonObject.getString("headIcon");
            chatMessage.setFromName(fromName);
            chatMessage.setFromId(fromId);
            chatMessage.setHeadIcon(fromIcon);
            chatMessage.setMT(true);
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
     * 加载群组
     */

    public void loadGroupContact(final String groupType, final OnLoadListener onLoadListener) {
        IMRequest.getInstance()
                 .queryGroupContact()
                 .subscribe(new SimpleObserver<List<GroupContact>>() {

                     @Override
                     public void onNext(
                             @NonNull List<GroupContact> groupContacts) {
                         onLoadListener.onLoadSuccess(groupContacts);
                     }

                     @Override
                     public void onError(@NonNull Throwable e) {
                         e.printStackTrace();
                         onLoadListener.onLoadFailed(e.getMessage());
                     }
                 });
    }

    /**
     * 加人
     */

    public void addUsersToGroup(String otherUserID, String groupID,
                                final IMGroupListener imGroupListener) {
        IMRequest.getInstance()
                 .addUserToGroup(otherUserID, groupID)
                 .subscribe(new SimpleCompleteObserver<List<String>>() {

                     @Override
                     public void onError(@NonNull Throwable e) {
                         imGroupListener.OnHandleGroupMemberError(e.getMessage());
                     }

                     @Override
                     public void onComplete() {
                         imGroupListener.OnAddUserToGroup("");
                     }
                 });
    }

    /**
     * 踢人
     */

    public void removeUserFromGroup(String otherUserID, String groupID,
                                    final IMGroupListener imGroupListener) {
        IMRequest.getInstance()
                 .removeUserFromGroup(otherUserID, groupID)
                 .subscribe(new SimpleCompleteObserver<List<String>>() {

                     @Override
                     public void onError(@NonNull Throwable e) {
                         imGroupListener.OnHandleGroupMemberError(e.getMessage());
                     }

                     @Override
                     public void onComplete() {
                         imGroupListener.OnDeleteUserFromGroup("");
                     }
                 });
    }

    public void dismissGroup(String groupID, final HandleGroupListener handleGroupListener) {
        IMRequest.getInstance().dismissGroup(groupID)
                 .subscribe(new SimpleCompleteObserver<List<String>>() {

                     @Override
                     public void onError(@NonNull Throwable e) {
                         handleGroupListener.handleGroupError(e.getMessage());
                     }

                     @Override
                     public void onComplete() {
                         handleGroupListener.dismissGroup();
                     }
                 });
    }

    public void exitGroup(String groupID, final HandleGroupListener handleGroupListener) {
        IMRequest.getInstance().exitGroup(groupID)
                 .subscribe(new SimpleCompleteObserver<List<String>>() {

                     @Override
                     public void onError(@NonNull Throwable e) {
                         handleGroupListener.handleGroupError(e.getMessage());
                     }

                     @Override
                     public void onComplete() {
                         handleGroupListener.exitGroup();
                     }
                 });
    }

    /**
     * 获取群成员
     */

    public void getGroupMemberList(String groupID, String page, String rows,
                                   final OnLoadListener onLoadListener) {
        IMRequest.getInstance()
                 .queryMemberList(groupID, page, rows)
                 .subscribe(new SimpleObserver<List<GroupMember>>() {

                     @Override
                     public void onNext(@NonNull List<GroupMember> members) {
                         onLoadListener.onLoadSuccess(members);

                     }

                     @Override
                     public void onError(@NonNull Throwable e) {
                         onLoadListener.onLoadFailed(e.getMessage());
                     }
                 });
    }

    /**
     * 查询群详情
     */

    public void queryGroupDetails(String groupID, final OnLoadListener onLoadListener) {
        IMRequest.getInstance()
                 .queryGroupDetails(groupID)
                 .subscribe(new SimpleObserver<List<GroupDetails>>() {

                     @Override
                     public void onNext(@NonNull List<GroupDetails> groupDetails) {
                         onLoadListener.onLoadSuccess(groupDetails);
                     }

                     @Override
                     public void onError(@NonNull Throwable e) {
                         onLoadListener.onLoadFailed(e.getMessage());
                     }
                 });
    }

}

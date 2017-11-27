package com.lib_im.profession.message;

import com.lib_im.core.manager.message.conversation.AbsGroupConversation;
import com.lib_im.profession.api.IMRequest;
import com.lib_im.profession.entity.ChatMessage;
import com.lib_im.core.entity.GroupChatRecord;
import com.lib_im.profession.entity.GroupContact;
import com.lib_im.profession.entity.GroupDetails;
import com.lib_im.profession.entity.GroupMember;

import org.jivesoftware.smackx.muc.MultiUserChat;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * 在基础上封装一层业务操作
 */
public class IMGroupConversation extends AbsGroupConversation {

    private String mGroupId;

    public IMGroupConversation(@NonNull MultiUserChat group) {
        super(group);
    }

    /**
     * @param logId     记录id
     * @param messageId 消息id
     * @param rows      一页多少条
     * @descript 获取历史记录或者未读消息
     */
    public Observable<List<ChatMessage>> getHistoryMessage(String logId, String messageId, int page,
                                                           int rows) {
        return IMRequest.getInstance()
                        .queryGroupChatRecord(mGroupId, logId, messageId, page, rows)
                        .map(new Function<List<GroupChatRecord>, List<ChatMessage>>() {
                            @Override
                            public List<ChatMessage> apply(List<GroupChatRecord> groupChatRecords)
                                    throws Exception {
                                List<ChatMessage> historylist = new ArrayList<>();
                                for (GroupChatRecord groupChatRecord : groupChatRecords) {
                                    if (groupChatRecord != null) {
                                        ChatMessage chatMessage = packRoomHistoryMessage(
                                                groupChatRecord);
                                        historylist.add(chatMessage);
                                    }
                                }
                                return historylist;
                            }
                        });

    }

    /**
     * @param groupChatRecord 群组历史记录实体
     * @descript 封装群组历史记录消息实体
     */
    private ChatMessage packRoomHistoryMessage(GroupChatRecord groupChatRecord)
            throws JSONException {
        ChatMessage message = ChatMessage.paresMessage(groupChatRecord.getContent());
        message.setRoom(true);
        return message;
    }

    /**
     * 加载群组
     */

    public Observable<List<GroupContact>> loadGroupContact(final String groupType) {
        return IMRequest.getInstance().queryGroupContact();
    }

    /**
     * 加人
     */

    public Observable<List<String>> addUsersToGroup(String otherUserID, String groupID) {
        return IMRequest.getInstance()
                        .addUserToGroup(otherUserID, groupID);
    }

    /**
     * 踢人
     */

    public Observable<List<String>> removeUserFromGroup(String otherUserID, String groupID) {
        return IMRequest.getInstance()
                        .removeUserFromGroup(otherUserID, groupID);
    }

    /**
     * 解散群组
     */
    public Observable<List<String>> dissolveGroup(String groupID) {
        return IMRequest.getInstance().dissolveGroup(groupID);
    }

    /**
     * 退出群组
     */
    public Observable<List<String>> exitGroup(String groupID) {
        return IMRequest.getInstance().exitGroup(groupID);
    }

    /**
     * 获取群成员
     */

    public Observable<List<GroupMember>> getGroupMemberList(String groupID, String page,
                                                            String rows) {
        return IMRequest.getInstance()
                        .queryMemberList(groupID, page, rows);
    }

    /**
     * 查询群详情
     */

    public Observable<List<GroupDetails>> queryGroupDetails(String groupID) {
        return IMRequest.getInstance()
                        .queryGroupDetails(groupID);
    }


}

package com.lib_im.profession;

import com.lib_im.core.ChatClient;
import com.lib_im.core.entity.GroupChatRecord;
import com.lib_im.core.exception.AppErrorException;
import com.lib_im.profession.api.IMRequest;

import java.util.List;

import io.reactivex.Observable;

/**
 * 聊天客户端
 */

public class IMChatClient extends ChatClient {

    private static IMChatClient sChatClient;

    protected IMChatClient() {
        if (sChatClient != null) {
            throw new AppErrorException("不能被初始化");
        }
    }

    public static IMChatClient getInstance() {
        if (sChatClient == null) {
            synchronized (ChatClient.class) {
                if (sChatClient == null) {
                    sChatClient = new IMChatClient();
                }
            }
        }
        return sChatClient;
    }

    /**
     * 获取群组未读的记录的数量，用于获取最新的一条消息和未读数量更新界面的消息数量
     *
     * @param groupListStr 获取未读记录数参数，形式为json串
     */
    public Observable<List<GroupChatRecord>> getRoomUnReadMessageCount(String groupListStr) {
        return IMRequest.getInstance()
                        .queryGroupRecordCount(groupListStr);
    }

}

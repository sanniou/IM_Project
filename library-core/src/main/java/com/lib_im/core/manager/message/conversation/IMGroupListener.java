package com.lib_im.core.manager.message.conversation;

/**
 * Created by songgx on 2017/1/19.
 * 群组人员变化监听接口
 */

public interface IMGroupListener {

    void OnAddUserToGroup(String msg);

    void OnDeleteUserFromGroup(String msg);

    void OnHandleGroupMemberError(String msg);
}

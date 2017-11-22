package com.lib_im.core.manager.group;

import com.lib_im.pro.im.listener.HandleGroupListener;
import com.lib_im.pro.im.listener.IMGroupListener;
import com.lib_im.pro.im.listener.OnLoadListener;

import java.util.List;

/**
 * 群组
 *
 * @param <T>
 * @author songgx
 */
public interface GroupContactManager<T> {


    void init();

    void initIm();

    /**
     * 设置当前用户
     *
     * @param uid
     */
    void setCurrentUser(String uid);

    /**
     * 加载群组
     */
    void loadGroupContact(String groupType, OnLoadListener onLoadListener);

    /**
     * 查询指定群组
     *
     * @param groupId
     * @return
     */
    T getGroupContact(String groupId);

    /**
     * @descript  加载群组历史记录
     *
     * @param _list  群组列表
     *
     */
    void loadRoomHistoryMsg(List<T> _list);

    /**
     * 加人
     *
     */

    void addUsersToGroup(String otherUserID, String groupID, IMGroupListener imGroupListener);

    /**
     * 踢人
     *
     */

    void removeUserFromGroup(String otherUserID, String groupID, IMGroupListener imGroupListener);


    /**
     * 添加监听
     *
     * @param imGroupListener
     */
    void addGroupUserListener(IMGroupListener imGroupListener);

    /**
     * 删除监听
     *
     * @param imGroupListener
     */
    void removeGroupUserListener(IMGroupListener imGroupListener);

    /**
    * @descript 查询群组列表
    *
    */
    List<T> getGroupList();

    /**
     * 获取群成员
     * */
    void getGroupMemberList(String groupID, String page, String rows, OnLoadListener onLoadListener);
    /**
     *查询群详情
     * */
    void queryGroupDetails(String groupID, OnLoadListener onLoadListener);

    void removeLoadListener(OnLoadListener onLoadListener);

    void addLoadListener(OnLoadListener onLoadListener);

    /**
     * 解散群组
     */
    void dismissGroup(String groupID, HandleGroupListener handleGroupListener);

    /**
     * 退出群组
     */
    void exitGroup(String groupID, HandleGroupListener handleGroupListener);

    void addHandleGroupListener(HandleGroupListener handleGroupListener);

    void removeHandleGroupListener(HandleGroupListener handleGroupListener);
}

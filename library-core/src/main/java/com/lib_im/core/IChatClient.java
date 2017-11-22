package com.lib_im.core;

public interface IChatClient {

    /**
     * 注册
     */
    void register(String user, String password);

    /**
     * 注销
     */
    void deleteUser(String user, String password);

    /**
     * 登陆
     */
    void login(String user, String password);

    /**
     * 登出
     */
    void logout();

    /**
     * 获取一个会话
     */
    void getConversation(String userID);

    /**
     * 获取一个群组会话
     */
    void getGroupConversation(String groupID);

    /**
     * 新增一个连接监听
     */
    void addConnectionListener();

    /**
     * 移除一个连接监听
     */
    void removeConnectionListener();

    /**
     * 新增一个消息监听
     */

    void addMessageListener();

    /**
     * 移除一个消息监听
     */

    void removwMessageListener();

    /**
     * 新增一个推送监听
     */
    void addPushListener();

    /**
     * 移除一个推送监听
     */
    void removePushListener();

}

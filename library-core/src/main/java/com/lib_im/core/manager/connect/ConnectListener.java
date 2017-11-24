package com.lib_im.core.manager.connect;

/**
 * 连接管理器接口
 */
public interface ConnectListener {

    /**
     * 监听连接上服务器
     */
    void onConnect();

    /**
     * 通过验证
     */
    void onAuthenticated();

    /**
     * 监听断开服务器
     */
    void onDisConnect();

    void onConnectError();

    /**
     * 登陆冲突
     */
    void onConnectConflict();

    /**
     * 正在重新连接
     */
    void reconnectingIn(int seconds);

    /**
     * 重连成功
     */
    void reconnectionSuccessful();

    /**
     * 重连失败
     */
    void reconnectionFailed(Exception e);

}

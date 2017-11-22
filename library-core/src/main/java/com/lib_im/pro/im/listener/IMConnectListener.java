package com.lib_im.pro.im.listener;

/**
 * Created by songgx on 2016/6/15.
 * 连接管理器接口
 */
public interface IMConnectListener {
    /**
     * 监听连接上服务器
     */
    void onConnect();

    /**
     * 监听断开服务器
     */
    void onDisConnect(int errorCode);

    /**
     * 重连成功
     */
    void reconnectionSuccessful();

    /**
     * 重连失败
     */
    void reconnectionFailed(Exception e);

    /**
     * 正在重新连接
     */
    void reconnectingIn(int i);

}

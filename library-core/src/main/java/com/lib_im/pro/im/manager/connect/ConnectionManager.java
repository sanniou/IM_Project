package com.lib_im.pro.im.manager.connect;
import com.lib_im.pro.im.listener.IMConnectListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 连接管理
 *
 * @author songgx
 */
public interface ConnectionManager {

    /**
     * 连接管理器接口管理集合
     */
    List<IMConnectListener> mConnectListeners = new ArrayList<>();

    /**
     * 初始化管理器
     */
    void init();

    /**
    * @descript  初始化聊天API相关
    *
    */
    void initIm();

    /**
     * 添加连接监听接口
     *
     * @param IMConnectListener
     */
    void addConnectListener(IMConnectListener IMConnectListener);

    /**
     * 释放注销接口
     *
     * @param IMConnectListener
     */
    void removeConnectListener(IMConnectListener IMConnectListener);

    /**
     * 移除XMPP连接监听器
     * */
   void removeXmppConnectListener();

}

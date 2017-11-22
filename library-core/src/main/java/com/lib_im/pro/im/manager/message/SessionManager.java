package com.lib_im.pro.im.manager.message;
import com.lib_im.pro.im.listener.IMSessionListener;
import com.lib_im.pro.im.listener.RefreshDataListener;

import java.util.List;

/**
 * 聊天回话管理器
 * Created by songgx on 16/6/15.
 */
public interface SessionManager<T> {



    void init();

    void initIm();


    /**
     * 设置当前用户
     *
     * @param uid
     */
    void setCurrentUser(String uid);

    /**
     * 获得会话列表
     * @return
     */
    List<T> getSessionList();

    /**
     * 获得一个会话对象
     * @return
     */
    T getSession(String _chatUserId, boolean isRoom);

    /**
     * Post一个回话消息
     * @param t
     */
    void postSession(T t, int _noReadCount);


    /**
     * 重置回话中未读消息总数
     * @param t
     */
    void resetSessionMessageCount(T t);


    void deleteSession(T t);

    /**
     * 注册一个回话监听接口
     * @param _call
     */
    void addSessionListener(IMSessionListener _call);

    /**
     * 注销一个消息监听接口
     * @param _call
     */
    void removeSessionListener(IMSessionListener _call);

    /**
     * 注册一个刷新界面session接口
     * @param _call
     */
    void addRefreshListener(RefreshDataListener _call);

    /**
     * 注销一个刷新界面session接口
     * @param _call
     */
    void removeRefreshListener(RefreshDataListener _call);
}

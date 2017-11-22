package com.lib_im.pro.im.listener;

/**
 * Created by songgx on 2016/6/15.
 * 会话管理器接口回调
 */
public interface IMSessionListener<T> {
    /**
     * @descript 更新session回调方法
     */
    void onUpdateSession(T t);

    /**
     * @descript 删除session回调方法
     */
    void onDeleteSession(T t);
}

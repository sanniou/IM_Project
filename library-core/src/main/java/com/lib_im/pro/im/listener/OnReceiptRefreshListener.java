package com.lib_im.pro.im.listener;

/**
 * Created by songgx on 2016/8/23.
 * 根据回执消息刷新界面接口
 */
public interface OnReceiptRefreshListener<T> {

    /**
     * 接收到回执消息回调方法
     */
    void onReceiptRefresh(T msg);
}

package com.lib_im.pro.im.listener;

/**
 * Created by songgx on 2017/9/28.
 * 解散，退出群组操作
 */

public interface HandleGroupListener {
    void exitGroup();
    void dismissGroup();
    void handleGroupError(String msg);
}

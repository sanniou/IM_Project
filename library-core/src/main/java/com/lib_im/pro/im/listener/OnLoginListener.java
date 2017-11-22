package com.lib_im.pro.im.listener;

/**
 * date:2017/9/1
 * author:songgx
 * describe:登录回调接口
 */

public interface OnLoginListener {

    /**
     * 登录成功
     */
    void OnLoginSuccess();

    /**
     * 登录失败
     * @param msg 错误信息
     */
    void OnLoginFailed(String msg);

}

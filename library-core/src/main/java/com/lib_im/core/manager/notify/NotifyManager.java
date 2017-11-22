package com.lib_im.core.manager.notify;

/**
 * 提醒管理器
 * Created by hljdrl on 15/11/25.
 */
public interface NotifyManager {

    void init();

    void initIm();

    /**
     * 是否开启震动提醒
     */
    void setVibrate(boolean _vibrate);

    /**
     * 是否开启铃声提醒
     */
    void setBell(boolean _bell);

    /**
     * 设置提醒数据
     */
    void setNotifyLink(String appName, int iconId, String action, Class<?> peddingClass);

    /**
     * 播放个人聊天声音提醒,前提聊天页面未打开
     */
    void playChatMessage(boolean isRoom, String chatUserId, String chatUserName,
                         String chatRoomJid);

    /**
     * @descript 取消通知栏
     */
    void cancelNotation();

}

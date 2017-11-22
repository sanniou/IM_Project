package com.lib_im.core.manager.notify;

import java.util.Map;

/**
 * Created by songgx on 2017/8/29.
 * 创建业务通知管理器
 */

public interface PushManager {

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
     * 播放业务推送通知
     */
    void playChatMessage(Map<String, String> map, String textContent);

    /**
     * @descript 取消通知栏
     */
    void cancelNotation();
}

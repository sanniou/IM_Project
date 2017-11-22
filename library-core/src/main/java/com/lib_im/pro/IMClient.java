package com.lib_im.pro;

import android.app.Application;
import android.support.v4.util.ArrayMap;

import library.san.library_ui.api.IMListRequest;
import library.san.library_ui.db.TableCache;
import com.lib_im.pro.im.client.IMChatClient;
import com.lib_im.pro.im.manager.notify.NotifyManager;
import com.lib_im.pro.retrofit.config.IMRetrofit;
import com.lib_im.pro.ui.chat.ChatActivity;
import library.san.library_ui.utils.Utils;

/**
 * Created by songgx on 2017/9/28.
 * 单独调试
 */

public class IMClient {

    public static void init(Application application) {
        //聊天模块初始化
        LiteChat.chatClient = new IMChatClient();
        LiteChat.chatClient.init(application);
        LiteChat.chatClient.setOpenfireServer("192.168.253.7", 5222, "127.0.0.1");

        //聊天消息推送
        NotifyManager notifyManager = LiteChat.chatClient.getNotifyManager();
        if (notifyManager != null) {
            String appname = application.getString(R.string.app_name);
            notifyManager.setNotifyLink(appname, android.R.mipmap.sym_def_app_icon, "",
                    ChatActivity.class);
            notifyManager.setBell(Boolean.TRUE);
            notifyManager.setVibrate(Boolean.FALSE);
        }

    }
}

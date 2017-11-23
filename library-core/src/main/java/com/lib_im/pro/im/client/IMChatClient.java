package com.lib_im.pro.im.client;

import android.app.Application;
import android.util.Log;

import com.lib_im.pro.im.api.IMRequest;
import com.lib_im.core.config.ChatCode;
import com.lib_im.core.config.XmppTool;
import com.lib_im.pro.im.entity.GroupContact;
import com.lib_im.pro.im.listener.OnLoginListener;
import com.lib_im.pro.im.manager.connect.ConnectionManager;
import com.lib_im.pro.im.manager.connect.IMConnectionManager;
import com.lib_im.pro.im.manager.contact.ContactManager;
import com.lib_im.pro.im.manager.contact.IMContactManger;
import com.lib_im.pro.im.manager.group.GroupContactManager;
import com.lib_im.pro.im.manager.group.IMGroupContactManger;
import com.lib_im.pro.im.manager.message.ChatMsgManager;
import com.lib_im.pro.im.manager.message.IMChatMsgManager;
import com.lib_im.pro.im.manager.message.IMSessionManager;
import com.lib_im.pro.im.manager.message.SessionManager;
import com.lib_im.pro.im.manager.notify.IMNotifyManager;
import com.lib_im.pro.im.manager.notify.IMPushManager;
import com.lib_im.pro.im.manager.notify.NotifyManager;
import com.lib_im.pro.im.manager.notify.PushManager;
import com.lib_im.pro.retrofit.exception.ApiErrorException;
import com.lib_im.pro.rx.SimpleListObserver;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import io.reactivex.annotations.NonNull;

/**
 * Created by songgx on 2017/9/21.
 * 聊天客户端
 */

public class IMChatClient {

    private static final String TAG = "IMChatClient";
    private AbstractXMPPConnection connection;
    private HashMap<String, String> configMap = new HashMap<>();

    private static IMChatClient sChatClient;
    /**
     * 连接管理
     */
    private IMConnectionManager connectManager;

    /**
     * 联系人管理
     */
    private IMContactManger contactManger;
    /**
     * 聊天管理器、消息接收管理器
     */
    private IMChatMsgManager chatManager;

    /**
     * 会话管理器
     */
    private IMSessionManager sessionManager;

    /**
     * 群组通讯录管理器
     */
    private IMGroupContactManger groupContactManager;

    private IMNotifyManager notifyManager;

    /**
     * 业务通知管理器
     *
     * @param context
     */
    private IMPushManager pushManager;

    private IMChatClient() {
        if (sChatClient != null) {
            throw new ApiErrorException("不能被初始化");
        }
    }

    public static IMChatClient getInstance() {
        if (sChatClient == null) {
            synchronized (IMChatClient.class) {
                if (sChatClient == null) {
                    sChatClient = new IMChatClient();
                }
            }
        }
        return sChatClient;
    }

    public void init(Application context) {
        connectManager = new IMConnectionManager(context);
        chatManager = new IMChatMsgManager();
        contactManger = new IMContactManger(context);
        notifyManager = new IMNotifyManager(context);
        pushManager = new IMPushManager(context);
        sessionManager = new IMSessionManager(context);
        groupContactManager = new IMGroupContactManger(context);
    }

    /**
     * 初始化聊天模块相关
     */

    public void initChatAbout() {
        connectManager.initIm();
        chatManager.initIm();
        contactManger.initIm();
        notifyManager.initIm();
        sessionManager.initIm();
        groupContactManager.initIm();
    }

    public void login(String userName, String passWord, OnLoginListener onLoginListener) {
        setConfig(ChatCode.KEY_USER_ID, userName);
        setConfig(ChatCode.KEY_USER_NAME, userName);
        setConfig(ChatCode.KEY_USER_PASS, passWord);
        initManager();
        loginXmpp(userName, passWord, onLoginListener);
    }

    /**
     * 登录
     *
     * @param userName        用户名
     * @param passWord        密码
     * @param onLoginListener 登录回调接口
     */

    public void loginXmpp(String userName, String passWord, OnLoginListener onLoginListener) {
        connection = XmppTool.getInstance().getConnection();
        if (connection != null) {
            try {
                // 首先判断是否还连接着服务器，需要先断开
                if (connection.isConnected()) {
                    try {
                        connection.disconnect();
                    } catch (Exception e) {
                        Log.d(TAG, "conn.disconnect() failed: " + e);
                    }
                }
                connection.connect();
                if (!connection.isConnected()) {
                    Log.d(TAG, "SMACK connect failed without exception!");
                }
                if (!connection.isAuthenticated()) {
                    connection.login(userName, passWord);
                }
                if (onLoginListener != null) {
                    //添加connectionLisenter监听
                    initChatAbout();
                    setOnLine();
                    onLoginListener.OnLoginSuccess();
                }
            } catch (XMPPException e) {
                e.printStackTrace();
                if (onLoginListener != null) {
                    onLoginListener.OnLoginFailed(e.getMessage());
                }
            } catch (SmackException e) {
                e.printStackTrace();
                if (onLoginListener != null) {
                    onLoginListener.OnLoginFailed(e.getMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (onLoginListener != null) {
                    onLoginListener.OnLoginFailed(e.getMessage());
                }
            }
        } else {
            if (onLoginListener != null) {
                onLoginListener.OnLoginFailed(ChatCode.CHAT_UN_CONNECT);
            }
        }
    }

    /**
     * 服务器如果是连接状态，则可执行自动登录，不再执行login操作
     */

    public void autoLogin() {
        connectManager.initIm();
        initManager();
    }

    /**
     * 登出
     */

    public void logout() {
        try {
            configMap.clear();
            connectManager.removeXmppConnectListener();
            chatManager.removeChatAboutListener();
            connection.disconnect(new Presence(Presence.Type.unavailable));
            connection = null;
            notifyManager.cancelNotation();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        getChatManger().removeRoomChatMap();
    }

    public boolean isLogin() {
        boolean isLoginIn = false;
        if (connection != null) {
            boolean isConnect = connection.isConnected();
            isLoginIn = isConnect && connection.isAuthenticated();
        }
        return isLoginIn;
    }

    public void setConfig(String key, String value) {
        configMap.put(key, value);
    }

    public String getConfig(String key) {
        return configMap.get(key);
    }

    public ChatMsgManager getChatManger() {
        return chatManager;
    }

    public ConnectionManager getConnectManager() {
        return connectManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public GroupContactManager getGroupContactManager() {
        return groupContactManager;
    }

    public NotifyManager getNotifyManager() {
        return notifyManager;
    }

    public ContactManager getContactManager() {
        return contactManger;
    }

    public PushManager getPushManager() {
        return pushManager;
    }

    /**
     * 加入聊天室，加入群组
     */

    public void joinGroupRoom(final String userId, final String nickName) {
        //获取聊天室信息,将userName作为加入房间后自己的昵称
        IMRequest.getInstance().queryGroupContact()
                 .subscribe(new SimpleListObserver<GroupContact>() {

                     public void onNext(@NonNull List<GroupContact> groupContacts) {
                         if (groupContacts != null) {
                             for (GroupContact groupContact : groupContacts) {
                                 groupContact.setAccount(userId);
                                 chatManager.initMultiRoom(groupContact.getGroupJid(), nickName);
                             }
                         }
                     }

                     public void onError(@NonNull Throwable e) {
                         e.printStackTrace();
                     }
                 });

    }

    /**
     * 初始化管理器
     */

    public void initManager() {
        String user = getConfig(ChatCode.KEY_USER_NAME);
        notifyManager.init();
        chatManager.init();
        contactManger.init();
        groupContactManager.init();
        sessionManager.init();

        chatManager.setCurrentUser(user);
        sessionManager.setCurrentUser(user);
        groupContactManager.setCurrentUser(user);
        contactManger.setCurrentUser(user);
    }

    /**
     * 设置在线
     */

    public void setOnLine() throws SmackException.NotConnectedException {
        Presence presence = new Presence(Presence.Type.available);
        //设置为"可聊天"以区分状态
        presence.setMode(Presence.Mode.chat);
        XmppTool.getInstance().getConnection().sendStanza(presence);
    }

    /**
     * 设置openfire参数
     */

    public void setOpenfireServer(String host, int port, String serverName) {
        ChatCode.XMPP_SERVER = host;
        ChatCode.XMPP_PORT = port;
        ChatCode.XMPP_SERVER_NAME = serverName;
    }
}

package com.lib_im.core;

import android.app.Application;

import com.lib_im.core.api.IMRequest;
import com.lib_im.core.entity.GroupContact;
import com.lib_im.core.manager.connect.ConnectionManager;
import com.lib_im.core.manager.message.IMChatMsgManager;
import com.lib_im.core.manager.notify.IMNotifyManager;
import com.lib_im.core.manager.notify.IMPushManager;
import com.lib_im.core.retrofit.exception.ApiErrorException;
import com.lib_im.core.retrofit.exception.AppErrorException;
import com.lib_im.core.retrofit.rx.SimpleObserver;

import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

/**
 * 聊天客户端
 */

public class IMChatClient {

    public static final String KEY_USER_ID = "key_user_id";

    public static final String KEY_USER_NAME = "key_user_name";

    public static final String KEY_USER_PASS = "key_user_pass";

    private XMPPTCPConnection connection;

    private HashMap<String, String> configMap = new HashMap<>();

    private static IMChatClient sChatClient;

    private ConnectionManager connectManager;

    private IMChatMsgManager mChatManager;

    private IMNotifyManager notifyManager;

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

    public void init(@NonNull ChatClientConfig config, @NonNull Application context) {
        try {
            connection = XmppTool.setOpenFireConnectionConfig(config);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }

        connectManager = new ConnectionManager();
        mChatManager = new IMChatMsgManager();
        notifyManager = new IMNotifyManager(context);
        pushManager = new IMPushManager(context);

        connection.addConnectionListener(connectManager);

        mChatManager.initIm(connection);
    }

    public void release() {
        connection.removeConnectionListener(connectManager);
    }

    public Observable<Object> login(String userName, String passWord) {
        return Observable.create(e -> {
            if (isLogin()) {
                throw new AppErrorException("用户已经登陆");
            }
            setConfig(KEY_USER_ID, userName);
            setConfig(KEY_USER_NAME, userName);
            setConfig(KEY_USER_PASS, passWord);
            loginXmpp(userName, passWord, e);
            mChatManager.readOfflineMessage();
            //设置为"可聊天"以区分状态
            setOnLine();
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                         .observeOn(AndroidSchedulers.mainThread());

    }

    /**
     * 登录
     *
     * @param userName 用户名
     * @param passWord 密码
     * @param emitter  登录回调接口
     */

    private void loginXmpp(String userName, String passWord, ObservableEmitter<Object> emitter)
            throws InterruptedException, XMPPException, SmackException, IOException {
        // 首先判断是否还连接着服务器，需要先断开
        if (connection.isConnected()) {
            connection.disconnect();
        }
        connection.connect();
        if (!connection.isConnected()) {
            throw new ApiErrorException("SMACK connect failed without exception!");
        }
        if (connection.isAuthenticated()) {
            throw new ApiErrorException("Authenticated before login!");
        }
        connection.login(userName, passWord);
        if (emitter != null) {
            emitter.onComplete();
        }

    }

    /**
     * 登出
     */

    public void logout() {
        if (!isLogin()) {
            return;
        }
        configMap.clear();
        notifyManager.cancelNotation();
        mChatManager.destroy();
        connectManager.destroy();
        connection.disconnect();
    }

    /**
     * 加入聊天室，加入群组
     */

    public void joinGroupRoom(String userId, final String nickName) {
        //获取聊天室信息,将userName作为加入房间后自己的昵称
        IMRequest.getInstance()
                 .queryGroupContact()
                 .subscribe(new SimpleObserver<List<GroupContact>>() {

                     @Override
                     public void onNext(@NonNull List<GroupContact> groupContacts) {
                         for (GroupContact groupContact : groupContacts) {
                             mChatManager
                                     .getGroupConversation(groupContact.getGroupJid(), nickName);
                         }
                     }

                     @Override
                     public void onError(@NonNull Throwable e) {
                         e.printStackTrace();
                     }
                 });

    }

    /**
     * 设置在线
     */

    public void setOnLine() throws SmackException.NotConnectedException, InterruptedException {
        Presence presence = new Presence(Presence.Type.available);
        //设置为"可聊天"以区分状态
        presence.setMode(Presence.Mode.chat);
        presence.setStatus("状态可不就是签名");
        connection.sendStanza(presence);
    }

    public boolean isLogin() {
        return connection != null && connection.isConnected() && connection.isAuthenticated();
    }

    private void setConfig(String key, String value) {
        configMap.put(key, value);
    }

    public String getConfig(String key) {
        return configMap.get(key);
    }

    public ConnectionManager getConnectManager() {
        return connectManager;
    }

    public IMNotifyManager getNotifyManager() {
        return notifyManager;
    }

    public IMPushManager getPushManager() {
        return pushManager;
    }

    public IMChatMsgManager getChatManager() {
        return mChatManager;
    }

}

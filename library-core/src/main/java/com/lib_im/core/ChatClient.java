package com.lib_im.core;

import com.lib_im.core.exception.ApiErrorException;
import com.lib_im.core.exception.AppErrorException;
import com.lib_im.core.manager.connect.ConnectionManager;
import com.lib_im.core.manager.message.IMChatMsgManager;
import com.lib_im.profession.message.IMGroupConversation;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

/**
 * 聊天客户端
 */

public abstract class ChatClient {

    public static final String KEY_USER_ID = "key_user_id";

    public static final String KEY_USER_NAME = "key_user_name";

    public static final String KEY_USER_PASS = "key_user_pass";

    private XMPPTCPConnection connection;

    private HashMap<String, String> configMap = new HashMap<>();

    private ConnectionManager connectManager;

    private IMChatMsgManager mChatManager;

    public void init(@NonNull ChatClientConfig config) {
        try {
            connection = XmppTool.setOpenFireConnectionConfig(config);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }

        connectManager = new ConnectionManager(connection);
        mChatManager = new IMChatMsgManager();
        mChatManager.initIm(connection);
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
            throws InterruptedException, XMPPException, SmackException, IOException, ApiErrorException {
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
        mChatManager.release();
        connectManager.release();
        connection.disconnect();
    }

    /**
     * 销毁连接，需要重新init
     */
    public void destroy() {
        configMap.clear();
        mChatManager.destroy();
        mChatManager = null;
        connectManager.destroy();
        connectManager = null;
        connection.disconnect();
        connection = null;
    }

    /**
     * 加入聊天室，加入群组
     */

    public Observable<IMGroupConversation> joinGroupRoom(Collection<String> groupIds,
                                                         String nickName) {
        //获取聊天室信息,将userName作为加入房间后自己的昵称
        return Observable.create((ObservableOnSubscribe<IMGroupConversation>) e -> {
            for (String groupJid : groupIds) {
                e.onNext(mChatManager.findGroupConversation(groupJid, nickName));
            }
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                         .observeOn(AndroidSchedulers.mainThread());

    }

    /**
     * 设置在线
     */

    public void setOnLine() throws SmackException.NotConnectedException, InterruptedException {
        //设置为"可聊天"以区分状态
        Presence presence = new Presence(Presence.Type.available);
        presence.setMode(Presence.Mode.chat);
        presence.setStatus("状态");
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

    public IMChatMsgManager getChatManager() {
        return mChatManager;
    }

}

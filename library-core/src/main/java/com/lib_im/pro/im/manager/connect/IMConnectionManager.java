package com.lib_im.pro.im.manager.connect;

import android.content.Context;
import android.util.Log;

import com.lib_im.core.config.ChatCode;
import com.lib_im.core.config.XmppTool;
import com.lib_im.pro.im.listener.IMConnectListener;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import library.san.library_ui.utils.ExecutorTasks;

/**
 * IM连接监听管理器
 * Created by songgx on 16/6/15.
 */
public class IMConnectionManager implements ConnectionListener, ConnectionManager {

    private Context mContext;
    private boolean ISCONNECT = false;
    private AbstractXMPPConnection connection;

    public IMConnectionManager(Context context) {
        this.mContext = context;
    }

    @Override
    public void connected(XMPPConnection xmppConnection) {
        Log.e("IMConnectionManager", "connected");
    }

    @Override
    public void authenticated(XMPPConnection xmppConnection, boolean b) {
        Log.e("IMConnectionManager", "authenticated");
        Log.e("IMConnectionManager", "authenticated" + ":" + String.valueOf(b));
    }

    /**
     * 异常断开导致的问题
     */
    @Override
    public void connectionClosed() {
        // 断开连接
        ExecutorTasks.getInstance().postRunnable(new Runnable() {
            @Override
            public void run() {
                notifyErrorListener(ChatCode.ERROR_DISCONNECT_OTHER);
                logout();
                // 重连服务器
                ISCONNECT = true;
                reconnectToOpenFire();
            }
        });
    }

    /**
     * 网络原因导致的掉线问题
     */
    @Override
    public void connectionClosedOnError(Exception e) {
        //断账号是否已经登录
        String message = e.getMessage();
        boolean error = message.contains("conflict");
        if (error) {//账号在其他设备登录
            // 重连服务器
            ExecutorTasks.getInstance().postRunnable(new Runnable() {
                @Override
                public void run() {
                    notifyErrorListener(ChatCode.ERROR_DISCONNECT_CONFLICT);
                    logout();
                }
            });
        } else {
            // 断开连接
            ExecutorTasks.getInstance().postRunnable(new Runnable() {
                @Override
                public void run() {
                    notifyErrorListener(ChatCode.ERROR_DISCONNECT_OTHER);
                    logout();
                    // 重连服务器
                    ISCONNECT = true;
                    reconnectToOpenFire();
                }
            });
        }
    }

    /**
     * @descript 下发通知错误信息，界面提示
     */
    private void notifyErrorListener(int code) {
        List<IMConnectListener> _list = new ArrayList<>(mConnectListeners);
        for (IMConnectListener _call : _list) {
            _call.onDisConnect(code);
        }
    }

    @Override
    public void reconnectingIn(int i) {
        Log.d("reconnectingIn", "正在重连第" + i + "次");
        List<IMConnectListener> _copy = new ArrayList<>(mConnectListeners);
        for (IMConnectListener _call : _copy) {
            _call.reconnectingIn(i);
        }
    }

    @Override
    public void reconnectionSuccessful() {
        List<IMConnectListener> _copy = new ArrayList<>(mConnectListeners);
        for (IMConnectListener _call : _copy) {
            _call.reconnectionSuccessful();
        }
    }

    @Override
    public void reconnectionFailed(Exception e) {
        List<IMConnectListener> _copy = new ArrayList<>(mConnectListeners);
        for (IMConnectListener _call : _copy) {
            _call.reconnectionFailed(e);
        }
    }

    @Override
    public void init() {

    }

    @Override
    public void initIm() {
        connection = XmppTool.getInstance().getConnection();
        if (connection != null) {
            connection.removeConnectionListener(this);
            connection.addConnectionListener(this);
        }
    }

    @Override
    public void addConnectListener(IMConnectListener IMConnectListener) {
        if (mConnectListeners.indexOf(IMConnectListener) == -1) {
            mConnectListeners.add(IMConnectListener);
        }
    }

    @Override
    public void removeConnectListener(IMConnectListener IMConnectListener) {
        if (mConnectListeners.indexOf(IMConnectListener) != -1) {
            mConnectListeners.remove(IMConnectListener);
        }
    }

    /**
     * 移除连接监听器
     */
    @Override
    public void removeXmppConnectListener() {
        connection.removeConnectionListener(this);
    }

    /**
     * 执行重新连接任务
     */
    public void reconnectToOpenFire() {
        while (ISCONNECT) {
            connect();
        }
    }

    /**
     * 重新连接操作
     */
    public void connect() {
        String userName = LiteChat.chatClient.getConfig(ChatCode.KEY_USER_ID);
        String password = LiteChat.chatClient.getConfig(ChatCode.KEY_USER_PASS);
        connection = XmppTool.getInstance().getConnection();
        if (connection != null) {
            try {
                // 首先判断是否还连接着服务器，需要先断开
                if (connection.isConnected()) {
                    try {
                        connection.disconnect();
                    } catch (Exception e) {
                        Log.d("IMConnectionManager", "conn.disconnect() failed: " + e);
                    }
                }
                connection.connect();
                if (!connection.isConnected()) {
                    Log.d("IMConnectionManager", "SMACK connect failed without exception!");
                }
                if (!connection.isAuthenticated()) {
                    //添加connectionLisenter监听
                    connection.login(userName, password);
                }
                LiteChat.chatClient.initChatAbout();
                ISCONNECT = false;

                /**获取聊天室信息,将nickName作为加入房间后自己的昵称*/
                reconnectionSuccessful();

            } catch (XMPPException e) {
                e.printStackTrace();
                logout();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                ISCONNECT = true;
                //reconnectionFailed(e);
            } catch (SmackException e) {
                e.printStackTrace();
                logout();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                ISCONNECT = true;
                // reconnectionFailed(e);
            } catch (IOException e) {
                e.printStackTrace();
                logout();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                ISCONNECT = true;
                //reconnectionFailed(e);
            }
        } else {
            logout();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            ISCONNECT = true;
            //  reconnectionFailed(null);
        }

    }

    /**
     * 登出操作
     */
    private void logout() {
        removeXmppConnectListener();
        LiteChat.chatClient.getChatManger().removeChatAboutListener();
        try {
            if (connection.isConnected()) {
                connection.disconnect(new Presence(Presence.Type.unavailable));
            }
            connection = null;
            LiteChat.chatClient.getNotifyManager().cancelNotation();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        LiteChat.chatClient.getChatManger().removeRoomChatMap();
    }

}





package com.lib_im.core.manager.connect;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.util.LinkedList;
import java.util.List;

/**
 * openfire 的连接监听重定义为自身接口
 */
public class ConnectionManager {

    /**
     * 登陆冲突
     */
    public static final String VALUE_CONNECT_CONFLICT = "conflict";
    private final XMPPTCPConnection mConnection;
    private final IMConnectionListener mListener;
    /**
     * 连接管理器接口管理集合
     */
    private List<ConnectListener> mListeners = new LinkedList<>();

    public ConnectionManager(
            XMPPTCPConnection connection) {
        mConnection = connection;
        mListener = new IMConnectionListener();
        mConnection.addConnectionListener(mListener);
    }

    public void addConnectListener(ConnectListener connectionListener) {
        if (connectionListener == null) {
            return;
        }
        mListeners.add(connectionListener);
    }

    public void removeConnectListener(ConnectListener connectionListener) {
        mListeners.remove(connectionListener);
    }

    public void release() {
        mListeners.clear();

    }

    public void destroy() {
        release();
        mConnection.removeConnectionListener(mListener);
    }

    public class IMConnectionListener implements ConnectionListener {

        /**
         * 通知连接已成功连接到远程端点（例如XMPP服务器）。
         * 请注意，连接可能尚未通过身份验证，因此只能进行有限的操作，例如注册帐户。
         *
         * @param connection 成功连接到其端点的XMPPConnection。
         */
        @Override
        public void connected(XMPPConnection connection) {
            for (ConnectListener listener : mListeners) {
                listener.onConnect();
            }
        }

        /**
         * 通知连接已被认证。
         *
         * @param connection - 成功验证的XMPPConnection。
         * @param resumed    - 如果之前的XMPP会话流已恢复，则为true。
         */
        @Override
        public void authenticated(XMPPConnection connection, boolean resumed) {
            for (ConnectListener listener : mListeners) {
                listener.onAuthenticated();
            }
        }

        /**
         * 通知连接正常关闭。
         */
        @Override
        public void connectionClosed() {
            for (ConnectListener listener : mListeners) {
                listener.onDisConnect();
            }
        }

        /**
         * 由于例外情况通知连接已关闭。突然断开连接时，可能会尝试重新连接到服务器。
         *
         * @param e exception
         */
        @Override
        public void connectionClosedOnError(Exception e) {
            //账号在其他设备登录
            if (e.getMessage().contains(VALUE_CONNECT_CONFLICT)) {
                for (ConnectListener listener : mListeners) {
                    listener.onConnectConflict();
                }
            } else {
                for (ConnectListener listener : mListeners) {
                    listener.onConnectError();
                }
            }
        }

        /**
         * 连接将重试以指定的秒数重新连接。
         * 注意：只有当 ReconnectionManager.isAutomaticReconnectEnabled() 返回true时，也就是仅当为连接启用重新连接管理器时，才会调用此方法。
         *
         * @param seconds 在试图重新连接之前剩下的秒数。
         */
        @Override
        public void reconnectingIn(int seconds) {
            for (ConnectListener listener : mListeners) {
                listener.reconnectingIn(seconds);
            }
        }

        /**
         * 连接已成功重新连接到服务器。当以前的套接字连接突然关闭时，连接将重新连接到服务器。
         */
        @Override
        public void reconnectionSuccessful() {
            for (ConnectListener listener : mListeners) {
                listener.reconnectionSuccessful();
            }
        }

        /**
         * 尝试连接到服务器失败。连接将立即尝试重新连接到服务器。
         * 注意：只有当ReconnectionManager.isAutomaticReconnectEnabled（）返回true时，也就是仅当为连接启用重新连接管理器时，才会调用此方法。
         *
         * @param exception 导致重新连接失败的异常
         */
        @Override
        public void reconnectionFailed(Exception exception) {
            for (ConnectListener listener : mListeners) {
                listener.reconnectionFailed(exception);
            }
        }
    }
}





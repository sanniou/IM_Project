package com.lib_im.core;

public class ChatClientConfig {

    private int mPingInterval;
    private int mConnectTimeOut;
    private boolean mDebugg;
    private String mServerHost;
    private int mServerPort;
    private String mServerName;
    private CharSequence mClientName;

    public ChatClientConfig(int pingInterval, int connectTimeOut, boolean debugg,
                            String serverHost, int serverPort, String serverName,
                            CharSequence clientName) {
        mPingInterval = pingInterval;
        mConnectTimeOut = connectTimeOut;
        mDebugg = debugg;
        mServerHost = serverHost;
        mServerPort = serverPort;
        mServerName = serverName;
        mClientName = clientName;
    }

    public String getServerHost() {
        return mServerHost;
    }

    public int getServerPort() {
        return mServerPort;
    }

    /**
     * public static String XMPP_SERVER_NAME = "127.0.0.1";
     */
    public String getServerName() {
        return mServerName;
    }

    public int getPingInterval() {
        return mPingInterval;
    }

    public int getConnectTimeOut() {
        return mConnectTimeOut;
    }

    public boolean getDebugg() {
        return mDebugg;
    }

    public CharSequence getClientName() {
        return mClientName;
    }
}

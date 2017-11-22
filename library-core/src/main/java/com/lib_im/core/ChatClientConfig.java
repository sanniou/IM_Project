package com.lib_im.core;

public class ChatClientConfig {

    public ChatClientConfig(String serverHost, String serverPort, String serverName) {
        mServerHost = serverHost;
        mServerPort = serverPort;
        mServerName = serverName;
    }

    private String mServerHost;
    private String mServerPort;
    private String mServerName;

    public String getServerHost() {
        return mServerHost;
    }

    public String getServerPort() {
        return mServerPort;
    }

    public String getServerName() {
        return mServerName;
    }
}

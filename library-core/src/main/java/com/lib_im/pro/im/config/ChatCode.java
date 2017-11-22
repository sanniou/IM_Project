package com.lib_im.pro.im.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by songgx on 2017/9/22.
 * 聊天常量字段
 */

public class ChatCode {

    public static final String CHAT_UN_CONNECT = "聊天服务未连接";
    public static final String KEY_USER_ID = "key_user_id";
    /**
     * 用户登录id
     */
    public static final String KEY_USER_NAME = "key_user_name";
    /**
     * 用户昵称
     */
    /**
     * 用户昵称
     */
    public static final String KEY_USER_PASS = "key_user_pass";
    /**
     * 用户密码
     */
    public static final int ERROR_DISCONNECT_OTHER = 110;
    /**
     * 未知原因断开
     */
    public static final int ERROR_DISCONNECT_CONFLICT = 111;
    /**
     * 用户登录冲突
     */

    public static String XMPP_SERVER = "192.168.3.150";
    /**
     * openfire服务器ip
     */
    public static int XMPP_PORT = 5222;
    /**
     * openfire服务器端口
     */
    public static String XMPP_SERVER_NAME = "127.0.0.1";
    /**
     * openfire服务器名称
     */
    public static final String XMPP_IDENTITY_NAME = "PHONE";
    /**
     * 客户端名称
     */
    public static final int PACKET_TIMEOUT = 10000;
    /**
     *
     超时时间
     */
    /**
     * 退出应用全局广播
     */
    public static final String BROAD_EXIT_APP = "broadcast_exit_app";

    /**
     * xmpp连接对象存储集合
     */
    public static Map<String, Object> conMap = new HashMap<>();

    /**
     * 添加存储聊天室对象的全局map
     */
    public static Map<String, Object> roomMap = new HashMap<>();

    public static final int INIT_ROOM_DATA = 100;
    public static int GET_ROOM_DATA;
    public static int GET_SINGLE_DATA;
    public static final int REFRESH_ROOM_DATA = 101;
    public static final int INIT_SINGLE_DATA = 102;
    public static final int REFRESH_SINGLE_DATA = 103;
    public static final int REPEAT_SEND_MSG = 201;
    public static final int SEND_MSG = 202;
    public static int SEND_MSG_TYPE;
    public static Map<String, Integer> positionMap = new HashMap<>();
    public static ChatMessage roomCacheMsg;
    public static final String POSITION_0 = "position_0";
    public static final int REFRESH_DATA = 90;
    public static final int INIT_DATA = 91;
    public static final int PULL_DOWN_REFRESH = 105;

    /**
     * 定义map用来根据本地文件名来对应当前的ChatMessage
     */
    public static Map<String, ChatMessage> messageMap = new HashMap<>();
}

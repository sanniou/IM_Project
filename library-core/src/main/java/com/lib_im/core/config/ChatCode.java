package com.lib_im.core.config;

import com.lib_im.core.entity.ChatMessage;

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
     * 用户密码
     */
    public static final String KEY_USER_PASS = "key_user_pass";

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

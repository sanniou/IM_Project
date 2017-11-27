package com.lib_im.profession.entity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 聊天消息
 */
public class ChatMessage {

    /**
     * ChatMessage 的发送状态
     */
    public enum STATE {
        /**
         * 发送中
         */
        SENDING,
        /**
         * 发送成功
         */
        SUCCESS,
        /**
         * 发送失败
         */
        FAIL,
        /**
         * 发送取消
         */
        CANCELED,
    }

    /**
     * 消息回执状态：已读
     */
    public static final int MESSAGE_HAS_READ = 3;

    /**
     * 消息回执状态:未读
     */
    public static final int MESSAGE_UN_READ = 4;

    /**
     * 声音
     */
    public static final int MESSAGE_TYPE_SOUNDS = 3;
    /**
     * 通知
     */
    public static final int MESSAGE_TYPE_NOTICE = 4;

    /**
     * 接收者id
     */
    private String toId;

    /**
     * 接收者名称
     */
    private String toName;

    /**
     * 发送者id
     */
    private String fromId;

    /**
     * 发送者名称
     */
    private String fromName;

    /**
     * 发送时间，时间戳
     */
    private Long date = System.currentTimeMillis();

    /**
     * 消息类型
     */
    private int msgType;

    /**
     * 消息id
     */
    private String msgId;
    /**
     * 文本消息
     */
    private String msg;

    /**
     * 发送状态
     * 0发送中 1成功 2失败 3已读
     */
    private STATE state;

    /**
     * 是否为群聊
     */
    private boolean room;

    /**
     * 0未读，1已读
     */
    private int mark;

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public STATE getState() {
        return state;
    }

    public void setState(STATE state) {
        this.state = state;
    }

    public boolean isRoom() {
        return room;
    }

    public void setRoom(boolean room) {
        this.room = room;
    }

    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }

    public static ChatMessage paresMessage(String json) throws JSONException {

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoom(false);
        JSONObject jsonObject = new JSONObject(json);
        String fromId = jsonObject.getString("sendUserId");
        String fromName = jsonObject.getString("sendUserName");
        chatMessage.setFromName(fromName);
        chatMessage.setFromId(fromId);
        chatMessage.setState(ChatMessage.STATE.SUCCESS);
        String sendUserId = jsonObject.getString("sendUserId");
        chatMessage.setFromId(sendUserId);
        String sendUserName = jsonObject.getString("sendUserName");
        chatMessage.setFromName(sendUserName);
        String msgId = jsonObject.getString("msgId");
        chatMessage.setMsgId(msgId);
        String content = jsonObject.getString("content");
        chatMessage.setMsg(content);
        int msgType = jsonObject.getInt("msgType");
        chatMessage.setMsgType(msgType);
        return chatMessage;
    }
}

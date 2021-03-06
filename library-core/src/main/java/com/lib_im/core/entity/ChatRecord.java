package com.lib_im.core.entity;

/**
 * 单人聊天历史记录
 */
public class ChatRecord {

    /**
     * 记录id
     */
    private String logId;
    /**
     * 发送人id
     */
    private String fromUserId;
    /**
     * 接收人
     */
    private String toUserId;
    /**
     * 发送时间
     */
    private String sendTime;
    /**
     * 发送内容
     */
    private String content;
    /**
     * 消息长度
     */
    private String contentLength;
    /**
     * 消息id（xmpp的id）
     */
    private String messageId;

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentLength() {
        return contentLength;
    }

    public void setContentLength(String contentLength) {
        this.contentLength = contentLength;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}


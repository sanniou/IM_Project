package com.lib_im.core.entity;

/**
 * 群聊历史记录
 */
public class GroupChatRecord {

    /**
     * 记录id
     */
    private String logId;
    /**
     * 群id
     */
    private String groupId;
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
    /**
     * 未读记录数
     */
    private String count;

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}

package com.lib_im.core.entity;

/**
 * 聊天消息
 */
public class ChatMessage {

    /**
     * 消息发送状态:发送中
     */
    public static final int SEND_STATUS_SENDING = 0;

    /**
     * 消息发送状态:发送成功
     */
    public static final int SEND_STATUS_SUCCESS = 1;

    /**
     * 消息发送状态:发送失败
     */
    public static final int SEND_STATUS_FAILD = 2;

    /**
     * 消息回执状态：已读
     */
    public static final int MESSAGE_HAS_READ = 3;

    /**
     * 消息回执状态:未读
     */
    public static final int MESSAGE_UN_READ = 4;

    /**
     * 文本
     */
    public static final int MESSAGE_TYPE_TXT = 0;
    /**
     * 图片
     */
    public static final int MESSAGE_TYPE_IMG = 1;
    /**
     * 文件
     */
    public static final int MESSAGE_TYPE_FILE = 2;
    /**
     * 声音
     */
    public static final int MESSAGE_TYPE_SOUNDS = 3;
    /**
     * 通知
     */
    public static final int MESSAGE_TYPE_NOTICE = 4;
    /**  */
    public static final int MESSAGE_TYPE_CLASSROOM = 5;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 文件名
     */
    public String fileName;
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
     * 当前用户名字
     */
    private String selfName;

    /**
     * 当前用户id
     */
    private String selfId;

    /**
     * 发送时间，时间戳
     */
    private Long date = System.currentTimeMillis();

    /**
     * 消息类型
     */
    private int msg_type;
    /**
     * 消息id
     */
    private String msgId;
    /**
     * 文本消息
     */
    private String msg;

    /**
     * 文件地址
     */
    private String file_path;
    /**
     * 文件长度
     */
    private float fileLength;
    /**
     * 文件远程地址
     */
    private String remoteUrl;

    /**
     * 是否下行，false为自己发送的数据
     */
    private boolean MT;

    /**
     * 发送状态
     * 0发送中 1成功 2失败 3已读
     */
    private int state;

    /**
     * 是否为群聊
     */
    private boolean Room;

    /**
     * 群ID
     */
    private String roomId;

    /**
     * 发送者头像地址
     */
    private String headPath;

    /**
     *
     */
    private String headIcon;

    /**
     * 文件发送进度
     */
    private int progress;
    /**
     * 0未读，1已读
     */
    private int mark;

    private boolean SoundPlaying = false;

    public ChatMessage() {
    }

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

    public boolean isSoundPlaying() {
        return SoundPlaying;
    }

    public void setSoundPlaying(boolean soundPlaying) {
        SoundPlaying = soundPlaying;
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

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public int getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(int msg_type) {
        this.msg_type = msg_type;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public float getFileLength() {
        return fileLength;
    }

    public void setFileLength(float fileLength) {
        this.fileLength = fileLength;
    }

    public boolean isMT() {
        return MT;
    }

    public void setMT(boolean isMT) {
        this.MT = isMT;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public boolean isRoom() {
        return Room;
    }

    public void setRoom(boolean isRoom) {
        this.Room = isRoom;
    }

    public String getSelfName() {
        return selfName;
    }

    public void setSelfName(String selfName) {
        this.selfName = selfName;
    }

    public String getSelfId() {
        return selfId;
    }

    public void setSelfId(String selfId) {
        this.selfId = selfId;
    }

    public String getHeadPath() {
        return headPath;
    }

    public void setHeadPath(String headPath) {
        this.headPath = headPath;
    }

    public String getHeadIcon() {
        return headIcon;
    }

    public void setHeadIcon(String headIcon) {
        this.headIcon = headIcon;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }

    @Override
    public String toString() {
        return fromId + "-" + fromName + ":" + msg;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public boolean getMT() {
        return this.MT;
    }

    public boolean getRoom() {
        return this.Room;
    }

    public boolean getSoundPlaying() {
        return this.SoundPlaying;
    }

}

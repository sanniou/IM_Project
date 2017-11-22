package com.lib_im.pro.im.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by songgx on 2016/12/12.
 * 会话实体
 */
@DatabaseTable(tableName = "SessionItem")
public class SessionItem extends ChatMessage implements Serializable{

    /**
     * 未读消息
     */
    @DatabaseField(columnName = "noReadCount")
    private int noReadCount = 0;
    public SessionItem() {
    }

    /**
     * @param msg
     * @return
     */
    public static SessionItem toSessionItem(ChatMessage msg){
        SessionItem _item = new SessionItem();
        _item.setState(msg.getState());
        _item.setFromId(msg.getFromId());
        _item.setFromName(msg.getFromName());
        _item.setDate(msg.getDate());
        _item.setFile_path(msg.getFile_path());
        _item.setFileLength(msg.getFileLength());
        _item.setHeadIcon(msg.getHeadIcon());
        _item.setHeadPath(msg.getHeadPath());
        _item.setMT(msg.isMT());
        _item.setRoom(msg.isRoom());
        _item.setMark(msg.getMark());
        _item.setMsg(msg.getMsg());
        _item.setMsgId(msg.getMsgId());
        _item.setMsg_type(msg.getMsg_type());
        _item.setProgress(msg.getProgress());
        _item.setRemoteUrl(msg.getRemoteUrl());
        _item.setSelfId(msg.getSelfId());
        _item.setSelfName(msg.getSelfName());
        _item.setRoomId(msg.getRoomId());
        _item.setToName(msg.getToName());
        _item.setToId(msg.getToId());
        return _item;
    }

    /**
     * 未读消息
     * @return
     */
    public int getNoReadCount() {
        return noReadCount;
    }

    /**
     * 未读消息
     * @param noReadCount
     */
    public void setNoReadCount(int noReadCount) {
        this.noReadCount = noReadCount;
    }
}

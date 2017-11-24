package com.lib_im.core.entity;

/**
 * Created by songgx on 16/6/15.
 * 联系人实体
 */
public class Contact {

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    /**
     * openfire
     */
    private String jid;
    /**
     * 账号
     */
    private String chatUserid;
    /**
     * 用户姓名
     */

    private String name;
    /**
     * 昵称
     */

    private String nickname;
    /**
     * 头像文件名
     */

    private String headicon;
    /**
     * 性别
     */

    private String sex;

    /**
     * 个性签名
     */

    private String sig;

    /**
     * 用户类型 0：朋友  1：新的朋友(验证的和自己添加的)
     */

    private int userType = -1;

    private String groupname;

    private String groupid;

    private String friend;

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public String getGroupid() {
        return groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public String getChatUserid() {
        return chatUserid;
    }

    public void setChatUserid(String chatUserid) {
        this.chatUserid = chatUserid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getHeadicon() {
        return headicon;
    }

    public void setHeadicon(String headicon) {
        this.headicon = headicon;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
        this.sig = sig;
    }

    public String getFriend() {
        return friend;
    }

    public void setFriend(String friend) {
        this.friend = friend;
    }
}

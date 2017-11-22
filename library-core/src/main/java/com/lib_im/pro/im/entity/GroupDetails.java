package com.lib_im.pro.im.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by songgx on 2017/8/10.
 * 群详情实体
 */

public class GroupDetails implements Serializable{

    private String groupID;//群id
    private String groupName;//群名称
    private String groupNotice;//群公告
    private String groupOwnerName;//群主
    private String groupOwnerID;//群主id
    private List<GroupMember> groupMembers;//群成员
    private List<String> groupManagement;//群管

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupNotice() {
        return groupNotice;
    }

    public void setGroupNotice(String groupNotice) {
        this.groupNotice = groupNotice;
    }

    public List<GroupMember> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(List<GroupMember> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public String getGroupOwnerName() {
        return groupOwnerName;
    }

    public void setGroupOwnerName(String groupOwnerName) {
        this.groupOwnerName = groupOwnerName;
    }

    public String getGroupOwnerID() {
        return groupOwnerID;
    }

    public void setGroupOwnerID(String groupOwnerID) {
        this.groupOwnerID = groupOwnerID;
    }

    public List<String> getGroupManagement() {
        return groupManagement;
    }

    public void setGroupManagement(List<String> groupManagement) {
        this.groupManagement = groupManagement;
    }
}

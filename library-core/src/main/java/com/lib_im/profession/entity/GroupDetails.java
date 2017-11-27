package com.lib_im.profession.entity;

import java.util.List;

/**
 * Created by songgx on 2017/8/10.
 * 群详情实体
 */

public class GroupDetails {

    /**
     * 群id
     */
    private String groupID;
    /**
     * 群名称
     */
    private String groupName;
    /**
     * 群公告
     */
    private String groupNotice;
    /**
     * 群主
     */
    private String groupOwnerName;
    /**
     * 群主id
     */
    private String groupOwnerID;
    /**
     * 群成员
     */
    private List<GroupMember> groupMembers;
    /**
     * 群管
     */
    private List<String> groupManagement;

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

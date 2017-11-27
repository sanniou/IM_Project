package com.lib_im.profession.entity;

import java.util.List;

public class GroupContact {

    /**
     * groupName :
     * groupID : aj201604
     * groupManagerID : wls
     * groupUserCounts : 50
     * groupHead : icon/io487l0.png
     */
    private String groupName;
    private String groupID;
    private List<String> groupManagers;
    private int groupUserCounts;
    private String groupHead;
    private String groupJid;
    private String groupType;

    public String getGroupJid() {
        return groupJid;
    }

    public void setGroupJid(String groupJid) {
        this.groupJid = groupJid;
    }

    public GroupContact(String name) {
        groupName = name;
    }

    public GroupContact() {
    }

    public int getGroupUserCounts() {
        return groupUserCounts;
    }

    public void setGroupUserCounts(int groupUserCounts) {
        this.groupUserCounts = groupUserCounts;
    }

    public String getGroupHead() {
        return groupHead;
    }

    public void setGroupHead(String groupHead) {
        this.groupHead = groupHead;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupID() {
        return this.groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public List<String> getGroupManagers() {
        return groupManagers;
    }

    public void setGroupManagers(List<String> groupManagers) {
        this.groupManagers = groupManagers;
    }

}

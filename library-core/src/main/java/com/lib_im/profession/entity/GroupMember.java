package com.lib_im.profession.entity;

/**
 * Created by songgx on 2017/8/10.
 * 群成员实体
 */

public class GroupMember {

    /**
     * 群成员id
     */
    private String memberID;
    /**
     * 群成员头像
     */
    private String memberIcon;
    /**
     * 群成员名称
     */
    private String memberName;
    /**
     * 是否选中
     */
    private boolean isCheck;

    public String getMemberID() {
        return memberID;
    }

    public void setMemberID(String memberID) {
        this.memberID = memberID;
    }

    public String getMemberIcon() {
        return memberIcon;
    }

    public void setMemberIcon(String memberIcon) {
        this.memberIcon = memberIcon;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }
}

package com.lib_im.pro.im.entity;

import java.io.Serializable;

/**
 * Created by songgx on 2017/8/10.
 * 群成员实体
 */

public class GroupMember implements Serializable{
    private String memberID;//群成员id
    private String memberIcon;//群成员头像
    private String memberName;//群成员名称
    private boolean isCheck;//是否选中

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

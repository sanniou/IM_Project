package com.lib_im.pro.im.entity;

import java.io.Serializable;

public class UserInfo implements Serializable {

    private Integer ID;


    private String userName;


    private String password;


    private String email;


    private String nickName;


    private String userID;


    private static final long serialVersionUID = 1L;


    public Integer getID() {
        return ID;
    }


    public void setID(Integer ID) {
        this.ID = ID;
    }


    public String getUserName() {
        return userName;
    }


    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }


    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }


    public String getNickName() {
        return nickName;
    }


    public void setNickName(String nickName) {
        this.nickName = nickName == null ? null : nickName.trim();
    }


    public String getUserID() {
        return userID;
    }


    public void setUserID(String userID) {
        this.userID = userID == null ? null : userID.trim();
    }
}
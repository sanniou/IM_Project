package com.lib_im.pro.im.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * 数据库实体
 * Created by songgx on 16/6/15.
 */
@DatabaseTable(tableName = "DataEntity")
public class DataEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @DatabaseField(generatedId = true)
    private int id;
    /**
     * 当前用户
     */
    @DatabaseField(columnName = "account")
    private String account;

    public DataEntity() {
    }

    /**
     * 当前登录用户
     * @return
     */
    public String getAccount() {
        return account;
    }

    /**
     * 当前登录用户
     * @param account
     */
    public void setAccount(String account) {
        this.account = account;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

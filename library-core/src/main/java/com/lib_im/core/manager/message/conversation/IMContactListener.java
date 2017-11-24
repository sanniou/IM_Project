package com.lib_im.core.manager.message.conversation;

import com.lib_im.core.entity.Contact;

import java.util.List;

/**
 * Created by songgx on 2016/6/15.
 * 联系人接口
 */
public interface IMContactListener {

    /**
     * @descript 添加联系人回调方法
     */
    void onContactAdded(String actionID);

    /**
     * @descript 删除联系人回调方法
     */

    void onContactDeleted(String actionID);

    /**
     * @descript 更新联系人回调方法
     */

    void onContactUpdate(List<Contact> list);

    /**
     * 操作联系人失败
     */
    void onContactError(String msg);

}

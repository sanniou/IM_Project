package com.lib_im.core.manager.contact;
import com.lib_im.pro.im.listener.IMContactListener;
import com.lib_im.pro.im.listener.OnLoadListener;

/**
 * 通讯录
 *
 * @param <T>
 * @author songgx
 */
public interface ContactManager<T> {

    /**
     * 初始化管理器
     */
    void init();

    /**
     * @descript  初始化聊天API相关
     *
     */
    void initIm();

    /**
     * 设置当前用户
     *
     * @param uid
     */
    void setCurrentUser(String uid);

    /**
     * 加载所有通讯录
     */
    void loadContact(IMContactListener contactListener);


    /**
     * 删除好友 
     */
    void removeContact(String otherUserID, IMContactListener contactListener);

    /**
     * 添加好友
     *
     * @param
     */
    void addContact(String otherUserID, IMContactListener contactListener);

    /**
     * 搜索好友
     *
     */
    void searchContact(String key, OnLoadListener onLoadListener);

    /**
     * 查询指定通讯录内容，从数据库获取
     *
     * @param chatUserId
     * @return T
     */
    T getContact(String chatUserId);

    /**
     * 同意好友请求
     * @param otherUserID
     * @param onLoadListener
     */

    void acceptFriendInvitation(String otherUserID, OnLoadListener onLoadListener);

    /**
     * 拒绝好友请求
     *
     * @param otherUserID
     * @param onLoadListener
     */
    void refuseFriendInvitation(String otherUserID, OnLoadListener onLoadListener);

    /**
     * 添加监听
     *
     * @param IMContactListener
     */
    void addContactListener(IMContactListener IMContactListener);

    /**
     * 删除监听
     *
     * @param IMContactListener
     */
    void removeContactListener(IMContactListener IMContactListener);

}

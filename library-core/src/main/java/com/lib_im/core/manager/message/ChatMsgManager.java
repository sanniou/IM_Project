package com.lib_im.core.manager.message;

import com.lib_im.pro.im.listener.HistoryMessageListener;
import com.lib_im.pro.im.listener.IMMessageListener;
import com.lib_im.pro.im.listener.MessageCallBack;
import com.lib_im.pro.im.listener.MessageStateListener;
import com.lib_im.pro.im.listener.OnChatRecordListener;
import com.lib_im.pro.im.listener.OnReceiptRefreshListener;
import com.lib_im.pro.im.listener.OnRoomChatRecordListener;
import com.lib_im.pro.im.listener.RefreshViewListener;

import java.util.List;

/**
 * 聊天管理器
 * Created by songgx on 15/11/12.
 */
public interface ChatMsgManager<T> {

    /**
     * 初始化方法
     */
    void init();

    /**
     * @descript 初始化聊天API相关
     */
    void initIm();

    /**
     * 设置当前用户
     */
    void setCurrentUser(String uid);

    /**
     * 提取离线消息
     */
    void readOfflineMessage();

    /**
     * @param group 群组聊天
     */
    int getCacheMessaegCount(String chatUserID, boolean group);

    /**
     * @param group 群组聊天
     */
    List<T> getCacheMessages(String chatUserID, int startIndex, int endIndex, boolean group);

    /**
     * 绑定一个聊天用户,意味着和这个用户存在一个聊天窗体
     */

    void bindOpenChatId(String chatId, boolean isRoom);

    boolean isBindOpenChatId(String chatId);

    /**
     * 永久删除一条消息
     */
    void deleteMessage(T msg, MessageCallBack _call);

    /**
     * 个人聊天-发送消息
     */
    void sendSingleChatMessage(T msg, MessageCallBack _call);

    /**
     * 注册消息监听接口
     */
    void addMessageListener(IMMessageListener _call);

    /**
     * 注销消息监听接口
     */
    void removeMessageListener(IMMessageListener _call);

    /**
     * 注册回执消息监听接口
     */
    void addReceiptMessageListener(OnReceiptRefreshListener _call);

    /**
     * 注销回执消息监听接口
     */
    void removeReceiptMessageListener(OnReceiptRefreshListener _call);

    /**
     * 注册群组历史消息监听接口
     */
    void addRoomRecordListener(OnRoomChatRecordListener _call);

    /**
     * 注销群组历史消息监听接口
     */
    void removeRoomRecordListener(OnRoomChatRecordListener _call);

    /**
     * 注册单人历史消息监听接口
     */
    void addChatRecordListener(OnChatRecordListener _call);

    /**
     * 注销单人历史消息监听接口
     */
    void removeChatRecordListener(OnChatRecordListener _call);

    /**
     * @param jid          聊天室jid
     * @param chatRoomName 聊天室名称
     * @descript 初始化聊天室信息
     */
    void initMultiRoom(String jid, String chatRoomName);

    /**
     * @descript 发送群消息
     */
    void sendRoomMessage(String chatRoomName, T msg, MessageCallBack _call);

    /**
     * 移除监听器
     */
    void removeChatAboutListener();

    /**
     * @param groupid   群组id
     * @param rows      一页多少条
     * @param logid     记录id
     * @param messageid 消息id
     * @descript 获取历史记录或者未读消息
     */
    void getRoomHistoryMessage(String groupId, String logId, String messageId, int page, int rows);

    /**
     * @param messageId  xmpp消息id
     * @param fromUserId 发送人登录名
     * @param rows       每页多少条，可传空
     * @descript 获取单聊历史消息
     */

    void getChatHistoryMessage(String messageId, String fromUserId, String toUserId, int page,
                               int rows);

    /**
     * @descript 发送消息回执
     */
    void sendMessageReceipt();

    /**
     * @param string 获取未读记录数参数，形式为json串
     * @descript 登录成功后获取群组未读记录数
     */

    void getRoomUnReadMessageCount(String string);

    /**
     * @descript 当应用断开连接或者是，注销登录时移除掉群聊天对象map
     */
    void removeRoomChatMap();

    /**
     * 聊天页面初始化获取历史消息下发通知
     */
    void notifyHistoryMsgListener(int code);

    /**
     * 添加监听
     */
    void addHistoryMessageListener(HistoryMessageListener historyMessageListener);

    /**
     * 删除监听
     */
    void removeHistoryMessageListener(HistoryMessageListener historyMessageListener);

    /**
     * 聊天页面初始化获取历史消息下发通知
     */
    void notifyMsgStateSuccessListener(T msg);

    void notifyMsgStateFailedListener(T msg);

    /**
     * 添加监听
     */
    void addMessageStateListener(MessageStateListener messageStateListener);

    /**
     * 删除监听
     */
    void removeMessageStateListener(MessageStateListener messageStateListener);

    void notifyRefreshView(T msg);

    /**
     * 添加监听
     */
    void addRefreshViewListener(RefreshViewListener refreshViewListener);

    /**
     * 删除监听
     */
    void removeRefreshViewListener(RefreshViewListener refreshViewListener);
}

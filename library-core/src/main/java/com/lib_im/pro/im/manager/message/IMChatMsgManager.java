package com.lib_im.pro.im.manager.message;

import android.content.Context;
import android.util.Log;

import com.lib_im.pro.im.config.ChatCode;
import com.lib_im.pro.im.config.XmppTool;
import com.lib_im.pro.im.listener.HistoryMessageListener;
import com.lib_im.pro.im.listener.IMMessageListener;
import com.lib_im.pro.im.listener.MessageCallBack;
import com.lib_im.pro.im.listener.MessageStateListener;
import com.lib_im.pro.im.listener.OnChatRecordListener;
import com.lib_im.pro.im.listener.OnReceiptRefreshListener;
import com.lib_im.pro.im.listener.OnRoomChatRecordListener;
import com.lib_im.pro.im.listener.RefreshViewListener;
import com.lib_im.pro.im.manager.group.GroupContactManager;
import com.lib_im.pro.im.manager.notify.PushManager;
import com.lib_im.pro.rx.SimpleListObserver;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.annotations.NonNull;
import library.san.library_ui.entity.ChatMessage;
import library.san.library_ui.entity.ChatRecord;
import library.san.library_ui.entity.GroupChatRecord;
import library.san.library_ui.entity.GroupContact;
import library.san.library_ui.entity.SessionItem;
import library.san.library_ui.utils.LogUtils;

/**
 * 聊天管理器
 * Created by songgx on 16/6/15.
 */
public class IMChatMsgManager implements ChatMsgManager<ChatMessage>,
        ChatManagerListener, StanzaListener, ReceiptReceivedListener,
        org.jivesoftware.smack.MessageListener {

    private AbstractXMPPConnection connection;
    private List<IMMessageListener> mIMMessageListeners = new ArrayList<>();
    private List<OnReceiptRefreshListener> mReceiptMessageListeners = new ArrayList<>();
    private List<OnRoomChatRecordListener> mOnRoomChatRecordListeners = new ArrayList<>();
    private List<OnChatRecordListener> mChatRecordListeners = new ArrayList<>();
    private List<HistoryMessageListener> mHistoryMessageListeners = new ArrayList<>();
    private List<MessageStateListener> mMessageStateListeners = new ArrayList<>();
    private List<RefreshViewListener> mRefreshViewListeners = new ArrayList<>();
    private List<MultiUserChat> roomChatList = new ArrayList<>();

    private Context mContext;

    private String mChatUserId = "";

    private final String TAG = "IMChatMsgManager";
    private org.jivesoftware.smack.chat.ChatManager smackChatManager;

    private DeliveryReceiptManager deliveryReceiptManager;//消息回执管理器

    public IMChatMsgManager(Context context) {
        mContext = context;

    }

    /**
     * 消息回执map
     */
    private Map<String, Message> receiptMap = new HashMap<>();

    /**
     * packetId临时存储的list
     */
    private LinkedList<String> packetIdList = new LinkedList<>();

    /**
     * 根据packetId存储发送chatMessageId的map
     */
    private Map<String, String> sendMsgIdMap = new HashMap<>();

    private String account;

    /**
     * 初始化方法
     */
    @Override
    public void init() {
        ChatCode.roomMap.clear();
        roomChatList.clear();
        receiptMap.clear();
        packetIdList.clear();
        sendMsgIdMap.clear();
    }

    /**
     * @descript
     */
    @Override
    public void initIm() {
        connection = XmppTool.getInstance().getConnection();
        if (connection != null) {
            smackChatManager = org.jivesoftware.smack.chat.ChatManager.getInstanceFor(connection);
            smackChatManager.addChatListener(this);
            StanzaFilter stanzaFilter = new StanzaTypeFilter(Stanza.class);
            connection.addSyncStanzaListener(this, stanzaFilter);
            //消息回执添加监听器操作
            deliveryReceiptManager = DeliveryReceiptManager.getInstanceFor(connection);
            //deliveryReceiptManager.dontAutoAddDeliveryReceiptRequests();
            deliveryReceiptManager.addReceiptReceivedListener(this);
        }
    }

    /**
     * 设置当前用户
     */
    @Override
    public void setCurrentUser(String uid) {
        account = uid;
    }

    /**
     * 获取离线消息
     */
    @Override
    public void readOfflineMessage() {
        final String _userId = LiteChat.chatClient.getConfig(ChatCode.KEY_USER_ID);
        final String _userName = LiteChat.chatClient.getConfig(ChatCode.KEY_USER_NAME);
        if (connection != null) {
            OfflineMessageManager offlineManager = new OfflineMessageManager(connection);
            List<Message> messageList;
            try {
                messageList = offlineManager.getMessages();
                if (messageList != null && messageList.size() >= 0) {
                    for (Message message : messageList) {
                        if (message != null) {
                            Log.e(TAG,
                                    "收到离线消息, Received from 【" + message.getFrom() + "】 message: " +
                                            message.getBody());
                            Message.Type type = message.getType();
                            if (type.toString().equals("chat")) {
                                if (message.getBodies().size() > 0) {//如果是空消息则不接收
                                    String messageBody = message.getBody();
                                    if (messageBody != null) {
                                        ChatMessage chatMessage = new ChatMessage();
                                        //解析接收到的消息体
                                        try {
                                            JSONObject jsonObject = new JSONObject(
                                                    message.getBody());
                                            String sendUserId = jsonObject.getString("sendUserId");
                                            String sendUserName = jsonObject
                                                    .getString("sendUserName");
                                            String sendUserIcon = jsonObject.getString("headIcon");
                                            chatMessage.setFromId(sendUserId);
                                            chatMessage.setFromName(sendUserName);
                                            chatMessage.setHeadIcon(sendUserIcon);
                                            parseMessage(chatMessage, jsonObject);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        //对消息内容包装成界面需要的实体类型
                                        packSigleMessageEntity(chatMessage, _userName, _userId);
                                        packReceiptMessage(message, chatMessage);
                                        Log.d(TAG, "processMessage....保存完成消息");
                                        int count = 0;
                                        //是否绑定消息聊天页面
                                        if (!isBindOpenChatId(
                                                String.valueOf(chatMessage.getFromId()))) {
                                            count++;
                                        }
                                        //--通知SessionManager--创建更新Sessioin-----------------
                                        if (LiteChat.chatClient.getSessionManager() != null) {
                                            LiteChat.chatClient.getSessionManager().postSession(
                                                    SessionItem.toSessionItem(chatMessage), count);
                                        }
                                        if (count > 0) {
                                            if (LiteChat.chatClient.getNotifyManager() != null) {
                                                LiteChat.chatClient.getNotifyManager()
                                                                   .playChatMessage(false,
                                                                           chatMessage.getFromId(),
                                                                           chatMessage
                                                                                   .getFromName(),
                                                                           "");
                                            }
                                        }
                                        //------END------------------------------------------
                                        notifyMessageListener(chatMessage);
                                        /***接收消息完成，存储至消息数据库，同时判断，消息窗体的数量，更新session-----------------end*/

                                    }
                                }
                                break;
                            }

                        }
                    }

                }
                //删除离线消息
                //程序启动后20+S才得到反馈
                if (connection != null && connection.isConnected()) {
                    offlineManager.deleteMessages();
                    Presence presence = new Presence(Presence.Type.available);
                    presence.setMode(Presence.Mode.chat);//设置为"可聊天"以区分状态
                    connection.sendStanza(presence);
                    Log.e("presence", presence.toXML().toString());
                }
            } catch (XMPPException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param group 群组聊天
     */
    @Override
    public int getCacheMessaegCount(String chatUserID, boolean group) {
        return 0;

    }

    /**
     * @param group 群组聊天
     */
    @Override
    public List getCacheMessages(String chatUserID, int startIndex, int endIndex, boolean group) {
        Log.d(TAG, "getCacheMessages2....");
        List<ChatMessage> _list = new ArrayList<ChatMessage>();

        return _list;

    }

    @Override
    public boolean isBindOpenChatId(String chatId) {
        if (mChatUserId.equals(chatId)) {
            return Boolean.TRUE;
        }
        return false;
    }

    /**
     * 绑定一个聊天用户,意味着和这个用户存在一个聊天窗体
     */
    @Override
    public void bindOpenChatId(String chatId, boolean isRoom) {
        mChatUserId = chatId;
        SessionManager<SessionItem> _sessionManager = LiteChat.chatClient.getSessionManager();
        SessionItem _item = _sessionManager.getSession(String.valueOf(chatId), isRoom);
        _sessionManager.resetSessionMessageCount(_item);
    }

    /**
     * 永久删除一条消息
     */
    @Override
    public void deleteMessage(ChatMessage msg, MessageCallBack _call) {
        Log.d(TAG, "deleteMessage....");

    }

    /**
     * 个人聊天-发送消息
     */
    @Override
    public void sendSingleChatMessage(ChatMessage msg, MessageCallBack _call) {
        final Message newMessage = new Message();
        newMessage.setBody(msg.getMsg());
        newMessage.setType(Message.Type.normal);
        //设置消息发送需要回执-----------------------------------------------------------------------start
        DeliveryReceiptRequest.addTo(newMessage); //smack 4.x变动，添加回执请求
        try {
            connection.sendStanza(newMessage);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        //设置消息发送需要回执-----------------------------------------------------------------------end
        String jid = msg.getFromId() + "@" + connection.getServiceName();
        //单人聊天，后续要加多人聊天
        Chat chat = smackChatManager.createChat(jid, null);
        if (connection != null && connection.isConnected()) {//保持服务连接的情况下发送消息
            try {/**发送消息成功*/
                if (chat != null) {
                    chat.sendMessage(msg.getMsg());
                    if (msg.getMsg().contains("{")) {
                        msgConfig(msg);
                    }
                    /**回执消息确认，回调接口中已经实现，这里只保存数据库，不进行消息状态的更新操作*/
//                //**发送消息成功，创建session-------------------start*/
                    msg.setState(ChatMessage.SEND_STATUS_SUCC);
                    /**存储发送的消息的message的packetId，用于接收回执的时候根据id匹配本地数据库中的msgid来更新消息的已读状态*/
                    sendMsgIdMap.put(newMessage.getStanzaId(), msg.getMsgId());
                    //--通知SessionManager--创建更新Sessioin-----------------
                    if (LiteChat.chatClient.getSessionManager() != null) {
                        LiteChat.chatClient.getSessionManager()
                                           .postSession(SessionItem.toSessionItem(msg), 0);
                    }
                    if (_call != null) {
                        _call.onSuccess(msg);
                    }
                } else {//聊天对象没有初始化时发送消息提示发送失败
                    //**发送消息后，首先要保存消息----------------------------------------------------------------start*/
                    if (msg.getMsg().contains("{")) {
                        msgConfig(msg);
                    }
                    //**发送消息失败，创建session---------------------------start*/
                    msg.setState(ChatMessage.SEND_STATUS_FAIL);
                    //   mTable.update(msg, new WhereBean("msgId", "=", msg.getMsgId()));
                    //--通知SessionManager--创建更新Sessioin-----------------
                    if (LiteChat.chatClient.getSessionManager() != null) {
                        LiteChat.chatClient.getSessionManager()
                                           .postSession(SessionItem.toSessionItem(msg), 0);
                    }
                    if (_call != null) {
                        _call.onError(msg);
                    }
                    //**发送消息失败，创建session---------------------------end*/
                }
                //**发送消息成功，创建session-----------------------end*/
            } catch (SmackException.NotConnectedException e) {/**发送消息失败*/
                //**发送消息后，首先要保存消息----------------------------------------------------------------start*/
                if (msg.getMsg().contains("{")) {
                    msgConfig(msg);
                }
                //**发送消息失败，创建session---------------------------start*/
                msg.setState(ChatMessage.SEND_STATUS_FAIL);
                //    mTable.update(msg, new WhereBean("msgId", "=", msg.getMsgId()));
                //--通知SessionManager--创建更新Sessioin-----------------
                if (LiteChat.chatClient.getSessionManager() != null) {
                    LiteChat.chatClient.getSessionManager()
                                       .postSession(SessionItem.toSessionItem(msg), 0);
                }
                if (_call != null) {
                    _call.onError(msg);
                }
                //**发送消息失败，创建session---------------------------end*/
            }
        } else {/**发送消息失败*/
            if (msg.getMsg().contains("{")) {
                msgConfig(msg);
            }
            //**发送消息失败，创建session---------------------------start*/
            msg.setState(ChatMessage.SEND_STATUS_FAIL);
            //    mTable.update(msg, new WhereBean("msgId", "=", msg.getMsgId()));
            //--通知SessionManager--创建更新Sessioin-----------------
            if (LiteChat.chatClient.getSessionManager() != null) {
                LiteChat.chatClient.getSessionManager()
                                   .postSession(SessionItem.toSessionItem(msg), 0);
            }
            if (_call != null) {
                _call.onError(msg);
            }
            //**发送消息失败，创建session---------------------------end*/
        }
    }

    /**
     * 发送消息无论成功还是失败都要进行的消息相关配置
     */
    private void msgConfig(ChatMessage msg) {
        /**解析消息体用于发送消息界面显示*/
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(msg.getMsg());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        parseMessage(msg, jsonObject);
    }

    /**
     * 注册消息监听接口
     */
    @Override
    public void addMessageListener(IMMessageListener _call) {
        if (mIMMessageListeners.indexOf(_call) == -1) {
            mIMMessageListeners.add(_call);
        }
    }

    /**
     * 注销消息监听接口
     */
    @Override
    public void removeMessageListener(IMMessageListener _call) {
        if (mIMMessageListeners.indexOf(_call) != -1) {
            mIMMessageListeners.remove(_call);
        }
    }

    /**
     * 注册回执消息监听接口
     */
    @Override
    public void addReceiptMessageListener(OnReceiptRefreshListener _call) {
        if (mReceiptMessageListeners.indexOf(_call) == -1) {
            mReceiptMessageListeners.add(_call);
        }
    }

    /**
     * 注销回执消息监听接口
     */
    @Override
    public void removeReceiptMessageListener(OnReceiptRefreshListener _call) {
        if (mReceiptMessageListeners.indexOf(_call) != -1) {
            mReceiptMessageListeners.remove(_call);
        }
    }

    /**
     * 注册历史消息监听接口
     */
    @Override
    public void addRoomRecordListener(OnRoomChatRecordListener _call) {
        if (mOnRoomChatRecordListeners.indexOf(_call) == -1) {
            mOnRoomChatRecordListeners.add(_call);
        }
    }

    /**
     * 注销历史消息监听接口
     */
    @Override
    public void removeRoomRecordListener(OnRoomChatRecordListener _call) {
        if (mOnRoomChatRecordListeners.indexOf(_call) != -1) {
            mOnRoomChatRecordListeners.remove(_call);
        }
    }

    /**
     * 注册单人历史消息监听接口
     */
    @Override
    public void addChatRecordListener(OnChatRecordListener _call) {
        if (mChatRecordListeners.indexOf(_call) == -1) {
            mChatRecordListeners.add(_call);
        }
    }

    /**
     * 注销单人历史消息监听接口
     */
    @Override
    public void removeChatRecordListener(OnChatRecordListener _call) {
        if (mChatRecordListeners.indexOf(_call) != -1) {
            mChatRecordListeners.remove(_call);
        }
    }

    /**
     * @descript 初始化聊天室信息
     */
    @Override
    public void initMultiRoom(String jid, String nickName) {
        if (connection != null && connection.isConnected()) {
            MultiUserChatManager multiUserChatManager = MultiUserChatManager
                    .getInstanceFor(connection);
            MultiUserChat multiUserChat = multiUserChatManager.getMultiUserChat(jid);
            try {
                Log.e(TAG, "初始化聊天室>>>>>>>>>>>>>>>>>" + jid + Thread.currentThread().getName());
                multiUserChat.join(nickName);
                multiUserChat.addMessageListener(this);
                roomChatList.add(multiUserChat);
                ChatCode.roomMap.put(jid, multiUserChat);
                Log.e(TAG, "初始化聊天室成功<<<<<<<<<<<<" + jid + Thread.currentThread());
            } catch (XMPPException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param chatRoomJid 聊天室jid
     * @param chatMessage 聊天消息封装实体
     * @param _call       接口对象
     * @descript 发送群消息
     */
    @Override
    public void sendRoomMessage(String chatRoomJid, ChatMessage chatMessage,
                                MessageCallBack _call) {
        // 获取到与聊天人的会话对象。参数username为聊天人的userid或者groupid，后文中的username皆是如此
        MultiUserChat multiUserChat = (MultiUserChat) ChatCode.roomMap.get(chatRoomJid);
        Message message = new Message();
        message.setBody(chatMessage.getMsg());
        message.setType(Message.Type.normal);
        if (connection != null && connection.isConnected()) {//发送消息的条件判断
            try {
                if (multiUserChat != null) {
                    multiUserChat.sendMessage(chatMessage.getMsg());
                    if (chatMessage.getMsg().contains("{")) {
                        msgConfig(chatMessage);
                    }
                    /**回执消息确认，回调接口中已经实现，这里只保存数据库，不进行消息状态的更新操作*/
                    chatMessage.setState(ChatMessage.SEND_STATUS_SUCC);
                    String userId = LiteChat.chatClient.getConfig(ChatCode.KEY_USER_ID);
                    chatMessage.setAccount(userId);
                    LiteChat.chatCache.saveObject(chatMessage.getRoomId(), chatMessage);
                    //--通知SessionManager--创建更新Sessioin-----------------
                    if (LiteChat.chatClient.getSessionManager() != null) {
                        LiteChat.chatClient.getSessionManager()
                                           .postSession(SessionItem.toSessionItem(chatMessage), 0);
                    }
                    if (_call != null) {
                        _call.onSuccess(chatMessage);
                    }
                } else {//群聊对象没有初始化之前提示发送消息失败
                    //**发送消息失败，创建session---------------------------start*/
                    if (chatMessage.getMsg().contains("{")) {
                        msgConfig(chatMessage);
                    }
                    chatMessage.setState(ChatMessage.SEND_STATUS_FAIL);
                    // mTable.update(chatMessage, new WhereBean("msgId", "=", chatMessage.getMsgId()));
                    //--通知SessionManager--创建更新Sessioin-----------------
                    if (LiteChat.chatClient.getSessionManager() != null) {
                        LiteChat.chatClient.getSessionManager()
                                           .postSession(SessionItem.toSessionItem(chatMessage), 0);
                    }
                    if (_call != null) {
                        _call.onError(chatMessage);
                    }
                    //**发送消息失败，创建session---------------------------end*/
                }
            } catch (SmackException.NotConnectedException e) {
                //**发送消息失败，创建session---------------------------start*/
                if (chatMessage.getMsg().contains("{")) {
                    msgConfig(chatMessage);
                }
                chatMessage.setState(ChatMessage.SEND_STATUS_FAIL);
                // mTable.update(chatMessage, new WhereBean("msgId", "=", chatMessage.getMsgId()));
                //--通知SessionManager--创建更新Sessioin-----------------
                if (LiteChat.chatClient.getSessionManager() != null) {
                    LiteChat.chatClient.getSessionManager()
                                       .postSession(SessionItem.toSessionItem(chatMessage), 0);
                }
                if (_call != null) {
                    _call.onError(chatMessage);
                }
                //**发送消息失败，创建session---------------------------end*/
            }
        } else {
            //**发送消息失败，创建session---------------------------start*/
            if (chatMessage.getMsg().contains("{")) {
                msgConfig(chatMessage);
            }
            chatMessage.setState(ChatMessage.SEND_STATUS_FAIL);
            //mTable.update(chatMessage, new WhereBean("msgId", "=", chatMessage.getMsgId()));
            //--通知SessionManager--创建更新Sessioin-----------------
            if (LiteChat.chatClient.getSessionManager() != null) {
                LiteChat.chatClient.getSessionManager()
                                   .postSession(SessionItem.toSessionItem(chatMessage), 0);
            }
            if (_call != null) {
                _call.onError(chatMessage);
            }
            //**发送消息失败，创建session---------------------------end*/
        }

    }

    /**
     * 移除监听器
     */
    @Override
    public void removeChatAboutListener() {
        connection.removeSyncStanzaListener(this);
        smackChatManager.removeChatListener(this);
        deliveryReceiptManager.removeReceiptReceivedListener(this);
        if (roomChatList != null) {
            for (MultiUserChat userChat : roomChatList) {
                if (userChat != null) {
                    userChat.removeMessageListener(this);
                }
            }

        }
    }

    /**
     * @param groupId   群组id
     * @param rows      一页多少条
     * @param logId     记录id
     * @param messageId 消息id
     * @descript 获取历史记录或者未读消息
     */
    @Override
    public void getRoomHistoryMessage(String groupId, String logId, String messageId, int page,
                                      int rows) {
        final List<ChatMessage> _historyList = new ArrayList<>();
        //TODO 此处进行网络请求，请求返回数据操作如下注释所示，此处逻辑根据后台群聊插件而定
        LiteChat.imRequestManager.getListInstance()
                                 .queryGroupChatRecord(groupId, logId, messageId, page, rows)
                                 .subscribe(new SimpleListObserver<GroupChatRecord>() {
                                     @Override
                                     public void onNext(
                                             @NonNull List<GroupChatRecord> groupChatRecords) {
                                         if (groupChatRecords != null) {
                                             for (GroupChatRecord groupChatRecord : groupChatRecords) {
                                                 if (groupChatRecord != null) {
                                                     ChatMessage chatMessage = packRoomHistoryMessage(
                                                             groupChatRecord);
                                                     _historyList.add(chatMessage);
                                                 }
                                             }
                                             notifyRoomChatRecordListener(_historyList);
                                         } else {//获取数据出错
                                             notifyRoomChatRecordListener(null);
                                         }
                                     }

                                     @Override
                                     public void onError(@NonNull Throwable e) {
                                         LogUtils.e(e.getMessage());
                                         notifyRoomChatRecordListener(null);
                                     }
                                 });

    }

    /**
     * smack 4.x版本改动，packet过时，用stanza替代
     */
    @Override
    public void processPacket(Stanza stanza) throws SmackException.NotConnectedException {
        if (stanza instanceof Message) {
            Message message = (Message) stanza;
            final Message.Type type = message.getType();
            if (type.toString().equals("headline")) {
                Log.e("IM", "headLine");
                String content = message.getBody();
                // TODO 推送模块逻辑，根据具体业务逻辑而定
//                try {
//                    JSONObject jsonObject=new JSONObject(content);
//                    JSONObject jsonObject1=jsonObject.getJSONObject("rows");
//                    String contentID=jsonObject1.getString("contentID");
//                    String pushContent=jsonObject1.getString("pushContent");
//                    String ID=jsonObject1.getString("ID");
//                    String pushType=jsonObject1.getString("pushType");
//                    initNotify(NotifyActivity.class);
//                    LiteChat.chatClient.getPushManager().playChatMessage(contentID,pushContent,ID,pushType);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }

            }
        }
    }

         //初始化推送通知管理器
    private void initNotify(Class<?> cls) {
        String _appName = "水泊梁山";
        PushManager pushManager=LiteChat.chatClient.getPushManager();
        pushManager.setNotifyLink(_appName, R.drawable.icon, "", cls);
        pushManager.setBell(Boolean.TRUE);
        pushManager.setVibrate(Boolean.FALSE);
    }
    @Override
    public void onReceiptReceived(String fromJid, String toJid, String receiptId, Stanza stanza) {
        // TODO 消息回执逻辑实现思路，在对方收到消息后，标记已读后发送消息至初始发送消息方来更新界面上的消息状态
    }

    /**
     * 修改群聊消息接收方式，群聊对象实现MessageListener接口,替换之前的PackListener，现在的StanzaListener接收stanza的方式，单独拆出来进行个性化
     */
    @Override
    public void processMessage(Message message) {
        if (message.getBodies().size() > 0) {
            final String strFrom = message.getFrom();
            Log.d(TAG, "processMessage 接收消息....");
            String roomId = strFrom.split("@")[0];
            /**添加群组历史消息的判断*/
            final String _userId = LiteChat.chatClient.getConfig(ChatCode.KEY_USER_ID);
            String _fromUserName = strFrom.split("/")[1];
            /**解析接收到的消息json*/
            try {
                //如果发送消息的人与当前接收消息的人一致则过滤掉此消息
                JSONObject jsonObject = new JSONObject(message.getBody());
                String sendUserId = jsonObject.getString("sendUserId");
                String sendUserName = jsonObject.getString("sendUserName");
                String sendUserIcon = jsonObject.getString("headIcon");
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setHeadIcon(sendUserIcon);
                chatMessage.setFromId(sendUserId);
                chatMessage.setFromName(sendUserName);
                parseMessage(chatMessage, jsonObject);
                packRoomMessageEntity(chatMessage, roomId, _userId, _userName);
                /***接收文本消息完成，存储至消息数据库，同时判断，消息窗体的数量，更新session-----------------start*/
                chatMessage.setAccount(_userId);
                LiteChat.chatCache.saveObject(chatMessage.getRoomId(), chatMessage);
                Log.d(TAG, "processMessage....保存完成消息");
                int count = 0;
                //是否绑定消息聊天页面
                if (!isBindOpenChatId(String.valueOf(chatMessage.getRoomId()))) {
                    count++;
                }
                //--通知SessionManager--创建更新Sessioin-----------------
                if (LiteChat.chatClient.getSessionManager() != null) {
                    LiteChat.chatClient.getSessionManager()
                                       .postSession(SessionItem.toSessionItem(chatMessage),
                                               count);
                }
                GroupContactManager<GroupContact> groupContactManager = LiteChat.chatClient
                        .getGroupContactManager();
                GroupContact groupContact = groupContactManager
                        .getGroupContact(chatMessage.getRoomId());
                String roomName = groupContact.getGroupName();
                if (count > 0) {
                    if (LiteChat.chatClient.getNotifyManager() != null) {
                        if (roomName != null && !roomName.equals("")) {
                            LiteChat.chatClient.getNotifyManager()
                                               .playChatMessage(true, groupContact.getGroupID(),
                                                       roomName, groupContact.getGroupJid());
                        }
                    }
                }
                //------END------------------------------------------
                notifyMessageListener(chatMessage);
                /***接收文本消息完成，存储至消息数据库，同时判断，消息窗体的数量，更新session-----------------end*/
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 按时间排序
     */
    private class RecordComparator implements Comparator<Object> {

        @Override
        public int compare(Object lhs, Object rhs) {
            ChatMessage map1 = (ChatMessage) lhs;
            ChatMessage map2 = (ChatMessage) rhs;
            Date date1 = new Date(map1.getDate());
            Date date2 = new Date(map2.getDate());
            return date1.compareTo(date2);
        }
    }

    /**
     * @param groupChatRecord 群组历史记录实体
     * @descript 封装群组历史记录消息实体
     */
    private ChatMessage packRoomHistoryMessage(GroupChatRecord groupChatRecord) {
        String selfName = LiteChat.chatClient.getConfig(ChatCode.KEY_USER_NAME);
        String selfId = LiteChat.chatClient.getConfig(ChatCode.KEY_USER_ID);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoom(true);
        chatMessage.setRoomId(groupChatRecord.getGroupId());
        chatMessage.setSelfId(selfId);
        chatMessage.setSelfName(selfName);
        if (groupChatRecord.getSendTime() != null && !groupChatRecord.getSendTime().equals("")) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            try {
                Date date = format.parse(groupChatRecord.getSendTime());
                long sendTime = date.getTime();
                chatMessage.setDate(sendTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        try {
            JSONObject jsonObject = new JSONObject(groupChatRecord.getContent());
            String fromId = jsonObject.getString("sendUserId");
            String fromName = jsonObject.getString("sendUserName");
            String fromIcon = jsonObject.getString("headIcon");
            chatMessage.setFromName(fromName);
            chatMessage.setFromId(fromId);
            chatMessage.setHeadIcon(fromIcon);
            if (fromName.equals(selfName)) {
                chatMessage.setMT(false);
            } else {
                chatMessage.setMT(true);
            }
            chatMessage.setState(ChatMessage.SEND_STATUS_SUCC);
            parseMessage(chatMessage, jsonObject);
            if (chatMessage.getMsg_type() == ChatMessage.MESSAGE_TYPE_SONDS) {
                if (chatMessage.isMT()) {
                    chatMessage.setMark(1);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return chatMessage;
    }

    /**
     * @param chatRecord 群组历史记录实体
     * @descript 封装单聊历史记录消息实体
     */
    private ChatMessage packChatHistoryMessage(ChatRecord chatRecord) {
        String selfName = LiteChat.chatClient.getConfig(ChatCode.KEY_USER_NAME);
        String selfId = LiteChat.chatClient.getConfig(ChatCode.KEY_USER_ID);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoom(false);
        chatMessage.setSelfId(selfId);
        chatMessage.setSelfName(selfName);
        if (chatRecord.getSendTime() != null && !chatRecord.getSendTime().equals("")) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            try {
                Date date = format.parse(chatRecord.getSendTime());
                long sendTime = date.getTime();
                chatMessage.setDate(sendTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        try {
            JSONObject jsonObject = new JSONObject(chatRecord.getContent());
            String fromId = jsonObject.getString("sendUserId");
            String fromName = jsonObject.getString("sendUserName");
            String fromIcon = jsonObject.getString("headIcon");
            chatMessage.setFromName(fromName);
            chatMessage.setFromId(fromId);
            chatMessage.setHeadIcon(fromIcon);
            if (fromName.equals(selfName)) {
                chatMessage.setMT(false);
            } else {
                chatMessage.setMT(true);
            }
            chatMessage.setState(ChatMessage.SEND_STATUS_SUCC);
            parseMessage(chatMessage, jsonObject);
            if (chatMessage.getMsg_type() == ChatMessage.MESSAGE_TYPE_SONDS) {
                if (chatMessage.isMT()) {
                    chatMessage.setMark(1);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return chatMessage;
    }

    /**
     * @param messageId  xmpp消息id
     * @param fromUserId 发送人登录名
     * @param rows       每页多少条，可传空
     * @descript
     */
    @Override
    public void getChatHistoryMessage(String messageId, String fromUserId, String toUserId,
                                      int page, int rows) {
        final List<ChatMessage> _chatMsgList = new ArrayList<>();
        //TODO 获取单个人聊天历史记录，处理逻辑如下注释，这里取决于后台单聊历史纪录插件的逻辑
        LiteChat.imRequestManager.getListInstance()
                                 .queryChatRecord(messageId, fromUserId, toUserId, page, rows)
                                 .subscribe(new SimpleListObserver<ChatRecord>() {
                                     @Override
                                     public void onNext(@NonNull List<ChatRecord> chatRecords) {
                                         if (chatRecords != null) {
                                             for (ChatRecord chatRecord : chatRecords) {
                                                 if (chatRecord != null) {
                                                     ChatMessage chatMessage = packChatHistoryMessage(
                                                             chatRecord);
                                                     _chatMsgList.add(chatMessage);
                                                 }
                                             }
                                             notifyChatRecordListener(_chatMsgList);
                                         } else {
                                             notifyChatRecordListener(null);
                                         }
                                     }

                                     @Override
                                     public void onError(@NonNull Throwable e) {
                                         LogUtils.e(e.getMessage());
                                         notifyChatRecordListener(null);
                                     }
                                 });

    }

    /**
     * @descript 发送消息回执
     */
    @Override
    public void sendMessageReceipt() {
        //TODO 发送消息回执
    }

    /**
     * @param string 获取未读记录数参数，形式为json串
     * @descript 登录成功后获取群组未读记录数
     */
    @Override
    public void getRoomUnReadMessageCount(String string) {
        //TODO 获取群组未读的记录的数量，用于获取最新的一条消息和未读数量更新界面的消息数量
        LiteChat.imRequestManager.getListInstance().queryGroupRecordCount(string)
                                 .subscribe(new SimpleListObserver<GroupChatRecord>() {
                                     @Override
                                     public void onNext(
                                             @NonNull List<GroupChatRecord> groupChatRecords) {
                                         if (groupChatRecords != null) {
                                             for (GroupChatRecord groupChatRecord : groupChatRecords) {
                                                 if (groupChatRecord != null) {
                                                     int unReadCount = Integer
                                                             .parseInt(groupChatRecord.getCount());
                                                     if (unReadCount > 0) {//有未读消息时创建session
                                                         ChatMessage chatMessage = packRoomHistoryMessage(
                                                                 groupChatRecord);
                                                         LiteChat.chatCache.saveObject(
                                                                 chatMessage.getRoomId(),
                                                                 chatMessage);
                                                         //--通知SessionManager--创建更新Sessioin-----------------
                                                         if (LiteChat.chatClient
                                                                 .getSessionManager() != null) {
                                                             LiteChat.chatClient.getSessionManager()
                                                                                .postSession(
                                                                                        SessionItem
                                                                                                .toSessionItem(
                                                                                                        chatMessage),
                                                                                        unReadCount);
                                                         }
                                                         GroupContactManager<GroupContact> groupContactManager = LiteChat.chatClient
                                                                 .getGroupContactManager();
                                                         GroupContact groupContact = groupContactManager
                                                                 .getGroupContact(
                                                                         chatMessage.getRoomId());
                                                         String roomName = groupContact
                                                                 .getGroupName();
                                                         if (LiteChat.chatClient
                                                                 .getNotifyManager() != null) {
                                                             if (roomName != null &&
                                                                     !roomName.equals("")) {
                                                                 LiteChat.chatClient
                                                                         .getNotifyManager()
                                                                         .playChatMessage(true,
                                                                                 groupContact
                                                                                         .getGroupID(),
                                                                                 roomName,
                                                                                 groupContact
                                                                                         .getGroupJid());
                                                             }
                                                         }
                                                     } else {
                                                         //unReadCount==0
                                                     }
                                                 }
                                             }
                                         }
                                     }

                                     @Override
                                     public void onError(@NonNull Throwable e) {
                                         LogUtils.e(e.getMessage());
                                     }
                                 });

    }

    /**
     * @descript 当应用断开连接或者是，注销登录时移除掉群聊天对象map
     */
    @Override
    public void removeRoomChatMap() {
        ChatCode.roomMap.clear();
        roomChatList.clear();
    }

    /**
     * 聊天页面初始化获取历史消息下发通知
     */
    @Override
    public void notifyHistoryMsgListener(int code) {
        List<HistoryMessageListener> _call = new ArrayList<>(mHistoryMessageListeners);
        for (HistoryMessageListener historyMessageListener : _call) {
            historyMessageListener.onHistoryMsg(code);
        }
    }

    /**
     * 添加监听
     */
    @Override
    public void addHistoryMessageListener(HistoryMessageListener historyMessageListener) {
        if (mHistoryMessageListeners.indexOf(historyMessageListener) == -1) {
            mHistoryMessageListeners.add(historyMessageListener);
        }
    }

    /**
     * 删除监听
     */
    @Override
    public void removeHistoryMessageListener(HistoryMessageListener historyMessageListener) {
        if (mHistoryMessageListeners.indexOf(historyMessageListener) != -1) {
            mHistoryMessageListeners.remove(historyMessageListener);
        }
    }

    /**
     * 聊天页面初始化获取历史消息下发通知
     */
    @Override
    public void notifyMsgStateSuccessListener(ChatMessage msg) {
        List<MessageStateListener> _call = new ArrayList<>(mMessageStateListeners);
        for (MessageStateListener messageStateListener : _call) {
            messageStateListener.stateSuccess(msg);
        }
    }

    @Override
    public void notifyMsgStateFailedListener(ChatMessage msg) {
        List<MessageStateListener> _call = new ArrayList<>(mMessageStateListeners);
        for (MessageStateListener messageStateListener : _call) {
            messageStateListener.stateFailed(msg);
        }
    }

    /**
     * 添加监听
     */
    @Override
    public void addMessageStateListener(MessageStateListener messageStateListener) {
        if (mMessageStateListeners.indexOf(messageStateListener) == -1) {
            mMessageStateListeners.add(messageStateListener);
        }
    }

    /**
     * 删除监听
     */
    @Override
    public void removeMessageStateListener(MessageStateListener messageStateListener) {
        if (mMessageStateListeners.indexOf(messageStateListener) != -1) {
            mHistoryMessageListeners.remove(messageStateListener);
        }
    }

    @Override
    public void notifyRefreshView(ChatMessage msg) {
        List<RefreshViewListener> _call = new ArrayList<>(mRefreshViewListeners);
        for (RefreshViewListener refreshViewListener : _call) {
            refreshViewListener.onRefreshView(msg);
        }
    }

    /**
     * 添加监听
     */
    @Override
    public void addRefreshViewListener(RefreshViewListener refreshViewListener) {
        if (mRefreshViewListeners.indexOf(refreshViewListener) == -1) {
            mRefreshViewListeners.add(refreshViewListener);
        }
    }

    /**
     * 删除监听
     */
    @Override
    public void removeRefreshViewListener(RefreshViewListener refreshViewListener) {
        if (mRefreshViewListeners.indexOf(refreshViewListener) != -1) {
            mRefreshViewListeners.remove(refreshViewListener);
        }
    }

    /**
     * @descript 打包群消息实体
     */
    private void packRoomMessageEntity(final ChatMessage chatMessage, String roomId, String _selfId,
                                       String _selfName) {
        chatMessage.setMT(Boolean.TRUE);
        chatMessage.setRoom(Boolean.TRUE);
        chatMessage.setRoomId(roomId);
        chatMessage.setSelfName(_selfName);
        chatMessage.setSelfId(_selfId);
    }

    /**
     * 回调消息监听接口
     */
    private void notifyMessageListener(ChatMessage _msg) {
        Log.d(TAG, "notifyMessageListener size:" + mIMMessageListeners.size());
        List<IMMessageListener> _copy = new ArrayList<>(mIMMessageListeners);
        for (IMMessageListener _call : _copy) {
            _call.onReceiveMessage(_msg);
        }
    }

    /**
     * 回调回执消息监听接口
     */
    private void notifyReceiptMessageListener(ChatMessage _msg) {
        Log.d(TAG, "notifyMessageListener size:" + mReceiptMessageListeners.size());
        List<OnReceiptRefreshListener> _copy = new ArrayList<>(mReceiptMessageListeners);
        for (OnReceiptRefreshListener _call : _copy) {
            _call.onReceiptRefresh(_msg);
        }
    }

    /**
     * 回调群组历史消息监听接口
     */
    private void notifyRoomChatRecordListener(List<ChatMessage> _msgList) {
        Log.d(TAG, "notifyMessageListener size:" + mOnRoomChatRecordListeners.size());
        List<OnRoomChatRecordListener> _copy = new ArrayList<>(mOnRoomChatRecordListeners);
        for (OnRoomChatRecordListener _call : _copy) {
            _call.onRoomChatRecorder(_msgList);
        }
    }

    /**
     * 回调单人历史消息监听接口
     */
    private void notifyChatRecordListener(List<ChatMessage> _msgList) {
        Log.d(TAG, "notifyMessageListener size:" + mChatRecordListeners.size());
        List<OnChatRecordListener> _copy = new ArrayList<>(mChatRecordListeners);
        for (OnChatRecordListener _call : _copy) {
            _call.onChatRecorder(_msgList);
        }
    }

    /**
     * @descript 接收消息只包含文本消息(openfire接收消息接口回调方法, 单人聊天接收消息方法)
     */
    @Override
    public void chatCreated(Chat chat, boolean b) {
        chat.addMessageListener(new ChatMessageListener() {
            @Override
            public void processMessage(Chat chat, Message message) {
                Log.d(TAG, "processMessage 接收消息....");
                if (message.getBodies().size() > 0) {//如果是空消息则不接收
                    //获取当前用户的用户名和id
                    final String strFrom = message.getFrom();
                    Log.d(TAG, "processMessage 接收消息....");
                    String fromId = strFrom.split("@")[0];
                    final String _userId = LiteChat.chatClient.getConfig(ChatCode.KEY_USER_ID);
                    if (!fromId.equals(_userId)) {
                        String messageBody = message.getBody();
                        if (messageBody != null) {
                            ChatMessage chatMessage = new ChatMessage();
                            //解析接收到的消息体
                            try {
                                JSONObject jsonObject = new JSONObject(message.getBody());
                                String sendUserId = jsonObject.getString("sendUserId");
                                String sendUserName = jsonObject.getString("sendUserName");
                                String sendUserIcon = jsonObject.getString("headIcon");
                                chatMessage.setFromId(sendUserId);
                                chatMessage.setHeadIcon(sendUserIcon);
                                chatMessage.setFromName(sendUserName);
                                parseMessage(chatMessage, jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //对消息内容包装成界面需要的实体类型
                            packSigleMessageEntity(chatMessage, _userName, _userId);
                            /**封装回执消息体*/
                            packReceiptMessage(message, chatMessage);
//                            /***接收消息完成，存储至消息数据库，同时判断，消息窗体的数量，更新session-----------------start*/
//                            mTable.save(chatMessage);
                            Log.d(TAG, "processMessage....保存完成消息");
                            int count = 0;
                            //是否绑定消息聊天页面
                            if (!isBindOpenChatId(String.valueOf(chatMessage.getFromId()))) {
                                count++;
                            }
                            //--通知SessionManager--创建更新Sessioin-----------------
                            if (LiteChat.chatClient.getSessionManager() != null) {
                                LiteChat.chatClient.getSessionManager().postSession(
                                        SessionItem.toSessionItem(chatMessage), count);
                            }
                            if (count > 0) {
                                if (LiteChat.chatClient.getNotifyManager() != null) {
                                    LiteChat.chatClient.getNotifyManager().playChatMessage(false,
                                            chatMessage.getFromId(), chatMessage.getFromName(), "");
                                }
                            }
                            //------END------------------------------------------
                            notifyMessageListener(chatMessage);
                            /***接收消息完成，存储至消息数据库，同时判断，消息窗体的数量，更新session-----------------end*/
                        }
                    }
                }
            }
        });
    }

    /**
     * 封装回执消息
     */
    private void packReceiptMessage(Message message, ChatMessage chatMessage) {
        /**单人聊天消息做回执处理*/

    }

    /**
     * @descript 包装单人聊天相关实体
     */
    public void packSigleMessageEntity(final ChatMessage chatMessage, String _userName,
                                       String _userId) {
        /**构建消息实体-------------------------------------------------------------------------------start*/
        chatMessage.setMT(Boolean.TRUE);
        chatMessage.setSelfId(_userId);
        chatMessage.setSelfName(_userName);
        /**构建消息实体-------------------------------------------------------------------------------end*/
    }

    /**
     * @descript 解析收到的新消息
     */
    public void parseMessage(ChatMessage chatMessage, JSONObject jsonObject) {
        /**解析接收到的消息------------------------------------------------------------------------start*/
        try {
            int msgType = jsonObject.getInt("msgType");
            String msgId = jsonObject.getString("msgId");
            chatMessage.setMsgId(msgId);
            switch (msgType) {
                case ChatMessage.MESSAGE_TYPE_TXT:
                    String content = jsonObject.getString("content");
                    chatMessage.setMsg(content);
                    chatMessage.setMsg_type(ChatMessage.MESSAGE_TYPE_TXT);
                    break;
                case ChatMessage.MESSAGE_TYPE_IMG:
                    String imageFileName = jsonObject.getString("fileName");
                    String imageLocalPath = jsonObject.getString("localPath");
                    String imageRemoteUrl = jsonObject.getString("remoteUrl");
                    chatMessage.setRemoteUrl(imageRemoteUrl);
                    chatMessage.setFile_path(imageLocalPath);
                    chatMessage.setFileName(imageFileName);
                    chatMessage.setMsg_type(ChatMessage.MESSAGE_TYPE_IMG);
                    break;
                case ChatMessage.MESSAGE_TYPE_SONDS:
                    String voiceFileName = jsonObject.getString("fileName");
                    String voiceLocalPath = jsonObject.getString("localPath");
                    String voiceRemoteUrl = jsonObject.getString("remoteUrl");
                    float voiceLength = (float) jsonObject.getDouble("fileLength");
                    chatMessage.setFileName(voiceFileName);
                    chatMessage.setRemoteUrl(voiceRemoteUrl);
                    chatMessage.setFile_path(voiceLocalPath);
                    chatMessage.setMsg_type(ChatMessage.MESSAGE_TYPE_SONDS);
                    chatMessage.setFileLength(voiceLength);
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        /**解析接收到的消息------------------------------------------------------------------------end*/
    }

}



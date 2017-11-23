package com.lib_im.core.manager.message;

import android.util.Log;

import com.lib_im.core.api.IMRequest;
import com.lib_im.core.config.ChatCode;
import com.lib_im.core.entity.ChatMessage;
import com.lib_im.core.entity.GroupChatRecord;
import com.lib_im.core.manager.group.GroupContactManager;
import com.lib_im.pro.im.entity.ChatRecord;
import com.lib_im.pro.im.listener.HistoryMessageListener;
import com.lib_im.pro.im.listener.MessageCallBack;
import com.lib_im.pro.im.listener.MessageStateListener;
import com.lib_im.pro.im.listener.OnReceiptRefreshListener;
import com.lib_im.pro.im.listener.OnRoomChatRecordListener;
import com.lib_im.pro.im.listener.RefreshViewListener;
import com.lib_im.pro.rx.SimpleListObserver;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatException;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.muc.UserStatusListener;
import org.jivesoftware.smackx.offline.OfflineMessageHeader;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.annotations.NonNull;

/**
 * 聊天管理器
 * Created by songgx on 16/6/15.
 */
public class IMChatMsgManager {

    private AbstractXMPPConnection connection;
    private List<IMMessageListener> mIMMessageListeners = new ArrayList<>();
    private List<OnReceiptRefreshListener> mReceiptMessageListeners = new ArrayList<>();
    private List<OnRoomChatRecordListener> mOnRoomChatRecordListeners = new ArrayList<>();
    private List<HistoryMessageListener> mHistoryMessageListeners = new ArrayList<>();
    private List<MessageStateListener> mMessageStateListeners = new ArrayList<>();
    private List<RefreshViewListener> mRefreshViewListeners = new ArrayList<>();
    private List<MultiUserChat> roomChatList = new ArrayList<>();

    private final String TAG = "IMChatMsgManager";
    private ChatManager mChatManager;

    //消息回执管理器
    private DeliveryReceiptManager mDeliveryReceiptManager;
    /**
     * 添加存储聊天室对象的全局map
     */
    public static Map<String, Object> roomMap = new HashMap<>();

    public IMChatMsgManager() {

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
     * 初始化方法
     */
    public void init() {
        roomMap.clear();
        roomChatList.clear();
        receiptMap.clear();
        packetIdList.clear();
    }

    /**
     * @descript
     */
    public void initIm(AbstractXMPPConnection connection) {
        this.connection = connection;
        mChatManager = ChatManager.getInstanceFor(connection);
        //为传入的聊天消息添加一个新的监听器。
        mChatManager.addIncomingListener((from, message, chat) -> {

        });
        //为发出的聊天消息添加一个新的监听器。
        mChatManager.addOutgoingListener((to, message, chat) -> {

        });
        StanzaFilter stanzaFilter = new StanzaTypeFilter(Stanza.class);
        //提供一种机制来侦听通过指定过滤器的数据包。这允许事件风格的编程 - 每次找到新的节（/数据包）时，
        // 将调用{@link #processStanza（Stanza）}方法。这是一个由连接器提供的功能的另一种方法，它可以让你在等待结果时阻塞。
        connection.addSyncStanzaListener(packet -> {
            if (packet instanceof Message) {
                Message message = (Message) packet;
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
        }, stanzaFilter);
        //消息回执添加监听器操作
        mDeliveryReceiptManager = DeliveryReceiptManager.getInstanceFor(connection);
        //为外发邮件启用送达收据的自动请求
        mDeliveryReceiptManager.autoAddDeliveryReceiptRequests();
        //收到新收据时调用回调。
        mDeliveryReceiptManager.addReceiptReceivedListener((fromJid, toJid, receiptId, receipt) -> {

        });
    }

    /**
     * 获取离线消息
     */
    public void readOfflineMessage() {
        OfflineMessageManager offlineManager = new OfflineMessageManager(connection);
        try {
            if (offlineManager.getMessageCount() == 0) {
                return;
            }
            List<OfflineMessageHeader> headers = offlineManager.getHeaders();
            List<Message> messageList = offlineManager.getMessages();
            for (Message message : messageList) {
                Log.e(TAG, "收到离线消息, Received from 【" + message.getFrom() + "】 message: " +
                        message.getBody());
                Message.Type type = message.getType();
                if (Message.Type.chat == type || Message.Type.groupchat == type) {
                    //如果是空消息则不接收
                    String messageBody = message.getBody();
                    ChatMessage chatMessage = new ChatMessage();
                    //解析接收到的消息体
                    JSONObject jsonObject = new JSONObject(messageBody);
                    parseMessage(chatMessage, jsonObject);
                    notifyMessageListener(chatMessage);
                }
                break;

            }
            offlineManager.deleteMessages();
            //设置为"可聊天"以区分状态
            Presence presence = new Presence(Presence.Type.available);
            presence.setMode(Presence.Mode.chat);
            connection.sendStanza(presence);
        } catch (SmackException.NotConnectedException | XMPPException | InterruptedException | JSONException | SmackException.NoResponseException e) {
            e.printStackTrace();
        }
    }

    /**
     * 个人聊天-发送消息
     */
    public void sendSingleChatMessage(ChatMessage msg, MessageCallBack call) {
        try {
            Message stanza = new Message();
            stanza.setBody(msg.getMsg());
            //设置消息发送需要回执
            DeliveryReceiptRequest.addTo(stanza);

            String jid = msg.getFromId() + "@" + connection.getServiceName();
            Chat chat = mChatManager.chatWith(JidCreate.entityBareFrom(jid));
            chat.send(stanza);
            msg.setState(ChatMessage.SEND_STATUS_SUCCESS);
            if (call != null) {
                call.onSuccess(msg);
            }
        } catch (SmackException.NotConnectedException | InterruptedException | XmppStringprepException e) {
            e.printStackTrace();
            if (call != null) {
                call.onError(msg);
            }
        }
    }

    /**
     * 注册消息监听接口
     */
    public void addMessageListener(IMMessageListener call) {
        if (!mIMMessageListeners.contains(call)) {
            mIMMessageListeners.add(call);
        }
    }

    /**
     * 注销消息监听接口
     */
    public void removeMessageListener(IMMessageListener call) {
        if (mIMMessageListeners.indexOf(call) != -1) {
            mIMMessageListeners.remove(call);
        }
    }

    /**
     * 注册回执消息监听接口
     */
    public void addReceiptMessageListener(OnReceiptRefreshListener _call) {
        if (mReceiptMessageListeners.indexOf(_call) == -1) {
            mReceiptMessageListeners.add(_call);
        }
    }

    /**
     * 注销回执消息监听接口
     */
    public void removeReceiptMessageListener(OnReceiptRefreshListener _call) {
        if (mReceiptMessageListeners.indexOf(_call) != -1) {
            mReceiptMessageListeners.remove(_call);
        }
    }

    /**
     * 注册历史消息监听接口
     */
    public void addRoomRecordListener(OnRoomChatRecordListener _call) {
        if (mOnRoomChatRecordListeners.indexOf(_call) == -1) {
            mOnRoomChatRecordListeners.add(_call);
        }
    }

    /**
     * 注销历史消息监听接口
     */
    public void removeRoomRecordListener(OnRoomChatRecordListener _call) {
        if (mOnRoomChatRecordListeners.indexOf(_call) != -1) {
            mOnRoomChatRecordListeners.remove(_call);
        }
    }

    /**
     * @descript 初始化聊天室信息
     */
    public void initMultiRoom(String jid, String nickName) {
        try {
            MultiUserChatManager multiUserChatManager = MultiUserChatManager
                    .getInstanceFor(connection);
            EntityBareJid entityBareJid = JidCreate.entityBareFrom(jid + "@bbb");
            MultiUserChat multiUserChat = multiUserChatManager.getMultiUserChat(entityBareJid);
            Log.e(TAG, "初始化聊天室>>>>>>>>>>>>>>>>>" + jid + Thread.currentThread().getName());
            multiUserChat.join(Resourcepart.from(nickName));
            //向邀请拒绝通知添加一个监听器。任何时候邀请被拒绝，听众将被解雇。
            multiUserChat.addInvitationRejectionListener((invitee, reason, message, rejection) -> {

            });
            //添加一个节（/数据包）侦听器，该侦听器将被通知任何发送到群聊的新状态数据包。
            // 使用监听器是一种合适的方式来知道什么时候应该由于任何改变而重新加载占用者列表。
            multiUserChat.addParticipantListener(presence -> {

            });
            //添加一个监听器，将被通知房间内占有者状态的变化，如用户被踢，禁止或授予管理员权限。
            multiUserChat.addParticipantStatusListener(new IMParticipantStatusListener());
            /*//添加一个新的StanzaListener，每当这个MultiUserChat将要发送一个新的状态到服务器时，
            这个新的状态将被调用。 Stanza（/ Packet）拦截器可以为将要发送到MUC服务的存在添加新的扩展。
            multiUserChat.addPresenceInterceptor();*/
            //将侦听器添加到主题更改通知。只要房间的主题发生变化，听众就会被解雇。
            multiUserChat.addSubjectUpdatedListener((subject, from) -> {});
            //添加一个监听程序，通知您在房间中状态的变化，例如用户被踢，禁止或授予管理员权限。
            multiUserChat.addUserStatusListener(new IMUserStatusListener());
            //*添加一个节（/数据包）侦听器，将通知群聊中的任何新消息。只有发给这个群聊的“group chat”类型的消息才会传送给听众。
            // 如果您希望监听可能与此群聊相关的其他数据包，则应该使用适当的PacketListener直接向XMPPConnection注册PacketListener。
            multiUserChat.addMessageListener(IMChatMsgManager.this::processMessage);
            roomChatList.add(multiUserChat);
            roomMap.put(jid, multiUserChat);
            Log.e(TAG, "初始化聊天室成功<<<<<<<<<<<<" + jid + Thread.currentThread());
        } catch (XMPPException | SmackException.NotConnectedException | SmackException.NoResponseException
                | XmppStringprepException | InterruptedException
                | MultiUserChatException.NotAMucServiceException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param chatRoomJid 聊天室jid
     * @param chatMessage 聊天消息封装实体
     * @param call        接口对象
     * @descript 发送群消息
     */
    public void sendRoomMessage(String chatRoomJid, ChatMessage chatMessage,
                                MessageCallBack call) {
        MultiUserChat multiUserChat = (MultiUserChat) roomMap.get(chatRoomJid);
        Message message = new Message();
        message.setBody(chatMessage.getMsg());
        message.setType(Message.Type.groupchat);
        //发送消息的条件判断
        try {
            if (multiUserChat != null) {
                multiUserChat.sendMessage(chatMessage.getMsg());
                chatMessage.setState(ChatMessage.SEND_STATUS_SUCCESS);
                if (call != null) {
                    call.onSuccess(chatMessage);
                }
            }
        } catch (SmackException.NotConnectedException | InterruptedException e) {
            chatMessage.setState(ChatMessage.SEND_STATUS_FAILD);
            if (call != null) {
                call.onError(chatMessage);
            }
        }

    }

    /**
     * 移除监听器
     */
    public void removeChatAboutListener() {

    }

    /**
     * @param groupId   群组id
     * @param rows      一页多少条
     * @param logId     记录id
     * @param messageId 消息id
     * @descript 获取历史记录或者未读消息
     */
    public void getRoomHistoryMessage(String groupId, String logId, String messageId, int page,
                                      int rows) {
        IMRequest.getInstance()
                 .queryGroupChatRecord(groupId, logId, messageId, page, rows)
                 .subscribe(new SimpleListObserver<GroupChatRecord>() {
                     @Override
                     public void onNext(@NonNull List<GroupChatRecord> groupChatRecords) {
                         List<ChatMessage> historylist = new ArrayList<>();
                         for (GroupChatRecord groupChatRecord : groupChatRecords) {
                             if (groupChatRecord != null) {
                                 ChatMessage chatMessage = packRoomHistoryMessage(
                                         groupChatRecord);
                                 historylist.add(chatMessage);
                             }
                         }
                         notifyRoomChatRecordListener(historylist);
                     }

                     @Override
                     public void onError(@NonNull Throwable e) {
                         e.printStackTrace();
                         notifyRoomChatRecordListener(null);
                     }
                 });

    }

    /**
     * 修改群聊消息接收方式，群聊对象实现MessageListener接口,替换之前的PackListener，现在的StanzaListener接收stanza的方式，单独拆出来进行个性化
     */
    public void processMessage(Message message) {
        final String strFrom = message.getFrom().toString();
        Log.d(TAG, "processMessage 接收消息....");
        String roomId = strFrom.split("@")[0];
        /**解析接收到的消息json*/
        try {
            JSONObject jsonObject = new JSONObject(message.getBody());
            ChatMessage chatMessage = new ChatMessage();
            parseMessage(chatMessage, jsonObject);
            packRoomMessageEntity(chatMessage, roomId, null, null);
            notifyMessageListener(chatMessage);
        } catch (JSONException e) {
            e.printStackTrace();
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
            chatMessage.setState(ChatMessage.SEND_STATUS_SUCCESS);
            parseMessage(chatMessage, jsonObject);
            if (chatMessage.getMsg_type() == ChatMessage.MESSAGE_TYPE_SOUNDS) {
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
     */
    public void getChatHistoryMessage(String messageId, String fromUserId, String toUserId,
                                      int page, int rows, OnChatRecordListener listener) {
        final List<ChatMessage> chatmsglist = new ArrayList<>();
        //TODO 获取单个人聊天历史记录，处理逻辑如下注释，这里取决于后台单聊历史纪录插件的逻辑
        IMRequest.getInstance()
                 .queryChatRecord(messageId, fromUserId, toUserId, page, rows)
                 .subscribe(new SimpleListObserver<ChatRecord>() {
                     @Override
                     public void onNext(@NonNull List<ChatRecord> chatRecords) {
                         if (chatRecords != null) {
                             for (ChatRecord chatRecord : chatRecords) {
                                 if (chatRecord != null) {
                                     ChatMessage chatMessage = packChatHistoryMessage(
                                             chatRecord);
                                     chatmsglist.add(chatMessage);
                                 }
                             }
                             listener.onChatRecorder(chatmsglist);
                         } else {
                             listener.onChatRecorder(null);
                         }
                     }

                     @Override
                     public void onError(@NonNull Throwable e) {
                         e.printStackTrace();
                         listener.onChatRecorder(null);
                     }
                 });

    }

    /**
     * @param string 获取未读记录数参数，形式为json串
     * @descript 登录成功后获取群组未读记录数
     */
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
        roomMap.clear();
        roomChatList.clear();
    }

    /**
     * 聊天页面初始化获取历史消息下发通知
     */
    public void notifyHistoryMsgListener(int code) {
        List<HistoryMessageListener> _call = new ArrayList<>(mHistoryMessageListeners);
        for (HistoryMessageListener historyMessageListener : _call) {
            historyMessageListener.onHistoryMsg(code);
        }
    }

    /**
     * 聊天页面初始化获取历史消息下发通知
     */
    public void notifyMsgStateSuccessListener(ChatMessage msg) {
        List<MessageStateListener> _call = new ArrayList<>(mMessageStateListeners);
        for (MessageStateListener messageStateListener : _call) {
            messageStateListener.stateSuccess(msg);
        }
    }

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
    private void notifyMessageListener(ChatMessage msg) {
        for (IMMessageListener call : mIMMessageListeners) {
            call.onReceiveMessage(msg);
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
     * @descript 接收消息只包含文本消息(openfire接收消息接口回调方法, 单人聊天接收消息方法)
     */
    @Override
    public void chatCreated(Chat chat, boolean b) {
        chat.addMessageListener((chat1, message) -> {
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
        });
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
    private void parseMessage(ChatMessage chatMessage, JSONObject jsonObject) throws JSONException {
        String sendUserId = jsonObject.getString("sendUserId");
        chatMessage.setFromId(sendUserId);
        String sendUserName = jsonObject.getString("sendUserName");
        chatMessage.setFromName(sendUserName);
        String sendUserIcon = jsonObject.getString("headIcon");
        chatMessage.setHeadIcon(sendUserIcon);
        String msgId = jsonObject.getString("msgId");
        chatMessage.setMsgId(msgId);
        String content = jsonObject.getString("content");
        chatMessage.setMsg(content);
        int msgType = jsonObject.getInt("msgType");
        chatMessage.setMsg_type(msgType);
        String imageFileName = jsonObject.getString("fileName");
        chatMessage.setFileName(imageFileName);
        String imageLocalPath = jsonObject.getString("localPath");
        chatMessage.setFile_path(imageLocalPath);
        String imageRemoteUrl = jsonObject.getString("remoteUrl");
        chatMessage.setRemoteUrl(imageRemoteUrl);
        float voiceLength = (float) jsonObject.getDouble("fileLength");
        chatMessage.setFileLength(voiceLength);
    }

    private class IMParticipantStatusListener implements ParticipantStatusListener {

        @Override
        public void joined(EntityFullJid participant) {

        }

        @Override
        public void left(EntityFullJid participant) {

        }

        @Override
        public void kicked(EntityFullJid participant, Jid actor, String reason) {

        }

        @Override
        public void voiceGranted(EntityFullJid participant) {

        }

        @Override
        public void voiceRevoked(EntityFullJid participant) {

        }

        @Override
        public void banned(EntityFullJid participant, Jid actor, String reason) {

        }

        @Override
        public void membershipGranted(EntityFullJid participant) {

        }

        @Override
        public void membershipRevoked(EntityFullJid participant) {

        }

        @Override
        public void moderatorGranted(EntityFullJid participant) {

        }

        @Override
        public void moderatorRevoked(EntityFullJid participant) {

        }

        @Override
        public void ownershipGranted(EntityFullJid participant) {

        }

        @Override
        public void ownershipRevoked(EntityFullJid participant) {

        }

        @Override
        public void adminGranted(EntityFullJid participant) {

        }

        @Override
        public void adminRevoked(EntityFullJid participant) {

        }

        @Override
        public void nicknameChanged(EntityFullJid participant, Resourcepart newNickname) {

        }
    }

    private class IMUserStatusListener implements UserStatusListener {

        @Override
        public void kicked(Jid actor, String reason) {

        }

        @Override
        public void voiceGranted() {

        }

        @Override
        public void voiceRevoked() {

        }

        @Override
        public void banned(Jid actor, String reason) {

        }

        @Override
        public void membershipGranted() {

        }

        @Override
        public void membershipRevoked() {

        }

        @Override
        public void moderatorGranted() {

        }

        @Override
        public void moderatorRevoked() {

        }

        @Override
        public void ownershipGranted() {

        }

        @Override
        public void ownershipRevoked() {

        }

        @Override
        public void adminGranted() {

        }

        @Override
        public void adminRevoked() {

        }

        @Override
        public void roomDestroyed(MultiUserChat alternateMUC, String reason) {

        }
    }
}



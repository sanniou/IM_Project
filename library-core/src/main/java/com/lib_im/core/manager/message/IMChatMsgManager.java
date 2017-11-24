package com.lib_im.core.manager.message;

import android.util.Log;

import com.lib_im.core.api.IMRequest;
import com.lib_im.core.entity.ChatMessage;
import com.lib_im.core.entity.GroupChatRecord;
import com.lib_im.core.manager.message.conversation.IMGroupConversation;
import com.lib_im.core.manager.message.conversation.IMUserConversation;
import com.lib_im.core.retrofit.rx.SimpleObserver;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatException;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.muc.UserStatusListener;
import org.jivesoftware.smackx.offline.OfflineMessageHeader;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.annotations.NonNull;

/**
 * 聊天管理器
 * Created by songgx on 16/6/15.
 */
public class IMChatMsgManager {

    private AbstractXMPPConnection connection;
    private List<IMMessageListener> mIMMessageListeners = new ArrayList<>();

    private final String TAG = "IMChatMsgManager";
    private ChatManager mChatManager;

    //消息回执管理器
    private DeliveryReceiptManager mDeliveryReceiptManager;
    /**
     * 添加存储聊天室对象的全局map
     */
    private static Map<String, IMGroupConversation> roomMap = new HashMap<>();
    private static Map<String, IMUserConversation> chatMap = new HashMap<>();

    public void initIm(AbstractXMPPConnection connection) {
        this.connection = connection;
        mChatManager = ChatManager.getInstanceFor(connection);
        //为传入的聊天消息添加一个新的监听器。
        mChatManager.addIncomingListener(this::receiveMessage);
        //为发出的聊天消息添加一个新的监听器。
        mChatManager.addOutgoingListener((to, message, chat) -> {
            Log.e(TAG, "发出消息" + message.getBody());
        });
        StanzaFilter stanzaFilter = new StanzaTypeFilter(Stanza.class);
        //提供一种机制来侦听通过指定过滤器的数据包。这允许事件风格的编程 - 每次找到新的节（/数据包）时，
        // 将调用{@link #processStanza（Stanza）}方法。这是一个由连接器提供的功能的另一种方法，它可以让你在等待结果时阻塞。
        connection.addSyncStanzaListener(packet -> {
            Log.e(TAG, "headLine" + packet.toString());
            if (packet instanceof Message) {
                Message message = (Message) packet;
                final Message.Type type = message.getType();
                if (type.toString().equals("headline")) {
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
        mDeliveryReceiptManager.addReceiptReceivedListener(this::notifyReceiptMessageListener);
    }

    /**
     * 获取离线消息
     */
    public void readOfflineMessage() {
        Log.e(TAG, "获取离线消息 ");
        OfflineMessageManager offlineManager = new OfflineMessageManager(connection);
        try {
            int count = offlineManager.getMessageCount();
            Log.e(TAG, "收到离线消息 " + count);
            if (count == 0) {
                return;
            }
            List<OfflineMessageHeader> headers = offlineManager.getHeaders();
            Log.e(TAG, "收到离线消息头 " + headers.toString());
            List<Message> messageList = offlineManager.getMessages();
            for (Message message : messageList) {
                Log.e(TAG, "收到离线消息 , Received from 【" + message.getFrom() + "】 message: " +
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
            }
            offlineManager.deleteMessages();
        } catch (SmackException.NotConnectedException | XMPPException | InterruptedException | JSONException | SmackException.NoResponseException e) {
            e.printStackTrace();
        }
    }

    public IMUserConversation getUserConversation(String userId) {
        try {
            if (chatMap.containsKey(userId)) {
                return chatMap.get(userId);
            }
            IMUserConversation conversation = null;
            conversation = new IMUserConversation(getChat(userId));
            chatMap.put(userId, conversation);
            return conversation;
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        return null;
    }

    public IMGroupConversation getGroupConversation(String groupId, String nickName) {
        try {
            if (roomMap.containsKey(groupId)) {
                return roomMap.get(groupId);
            }
            IMGroupConversation conversation = new IMGroupConversation(
                    getMultiRoom(groupId, nickName));
            roomMap.put(groupId, conversation);
            return conversation;
        } catch (XMPPException.XMPPErrorException | XmppStringprepException | SmackException.NoResponseException | MultiUserChatException.NotAMucServiceException | SmackException.NotConnectedException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Chat getChat(String toId) throws XmppStringprepException {
        String jid = toId + "@" + connection.getServiceName();
        return mChatManager.chatWith(JidCreate.entityBareFrom(jid));
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
     * 初始化聊天室信息
     */
    private MultiUserChat getMultiRoom(String jid, String nickName)
            throws XmppStringprepException, XMPPException.XMPPErrorException, MultiUserChatException.NotAMucServiceException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException {
        MultiUserChatManager multiUserChatManager = MultiUserChatManager
                .getInstanceFor(connection);
        EntityBareJid entityBareJid = JidCreate.entityBareFrom(jid);
        MultiUserChat multiUserChat = multiUserChatManager.getMultiUserChat(entityBareJid);
        Log.e(TAG, "初始化聊天室>>>>>>>>>>>>>>>>>" + jid + Thread.currentThread().getName());

        //使用指定的昵称加入聊天室。如果已经使用另一个昵称加入，则此方法将首先离开房间，然后使用新的昵称重新加入。
        // 将使用来自组聊天服务器的联接成功的答复的默认连接超时。房间加入后，房间将决定发送的历史数量。
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

        Log.e(TAG, "初始化聊天室成功<<<<<<<<<<<<" + jid + Thread.currentThread());
        return multiUserChat;
    }

    /**
     * 收到群聊消息的处理
     */
    private void processMessage(Message message) {
        final String strFrom = message.getFrom().toString();
        Log.e(TAG, "processMessage 接收消息...." + message.getBody());
        String roomId = strFrom.split("@")[0];
        //**解析接收到的消息json
        try {
            JSONObject jsonObject = new JSONObject(message.getBody());
            ChatMessage chatMessage = new ChatMessage();
            parseMessage(chatMessage, jsonObject);
            packRoomMessageEntity(chatMessage, roomId, null, null);
            notifyGrouopMessageListener(chatMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取群组未读的记录的数量，用于获取最新的一条消息和未读数量更新界面的消息数量
     *
     * @param string 获取未读记录数参数，形式为json串
     */
    public void getRoomUnReadMessageCount(String string) {
        IMRequest.getInstance()
                 .queryGroupRecordCount(string)
                 .subscribe(new SimpleObserver<List<GroupChatRecord>>() {
                     @Override
                     public void onNext(@NonNull List<GroupChatRecord> groupChatRecords) {

                     }

                     @Override
                     public void onError(@NonNull Throwable e) {
                         e.printStackTrace();
                     }
                 });

    }

    /**
     * 当应用断开连接或者是，注销登录时移除掉群聊天对象map
     */
    public void destroy() {
        roomMap.clear();
        chatMap.clear();
        mIMMessageListeners.clear();
//        mDeliveryReceiptManager.removeReceiptReceivedListener();
    }

    /**
     * 打包群消息实体
     */
    private void packRoomMessageEntity(final ChatMessage chatMessage, String roomId, String selfId,
                                       String selfName) {
        chatMessage.setMT(Boolean.TRUE);
        chatMessage.setRoom(Boolean.TRUE);
        chatMessage.setRoomId(roomId);
        chatMessage.setSelfName(selfName);
        chatMessage.setSelfId(selfId);
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
     * 回调消息监听接口
     */
    private void notifyGrouopMessageListener(ChatMessage msg) {
        for (IMMessageListener call : mIMMessageListeners) {
            call.onReceiveGroupMessage(msg);
        }
    }

    /**
     * 回调回执消息监听接口
     */
    private void notifyReceiptMessageListener(Jid fromJid, Jid toJid, String receiptId,
                                              Stanza receipt) {
        Log.e(TAG, "接收回执" + receipt.toString());
        for (IMMessageListener call : mIMMessageListeners) {
            call.onReceiveReceipt(null);
        }
    }

    /**
     * 接收消息只包含文本消息(openfire接收消息接口回调方法, 单人聊天接收消息方法)
     */
    private void receiveMessage(EntityBareJid from, Message message, Chat chat) {
        Log.e(TAG, "接收消息...." + from.toString() + ":" + message.getBody());
        ChatMessage chatMessage = new ChatMessage();
        //解析接收到的消息体
        try {
            JSONObject jsonObject = new JSONObject(message.getBody());
            parseMessage(chatMessage, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyMessageListener(chatMessage);
    }

    /**
     * 解析收到的新消息
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

    /************************* 群组管理员状态更新 IMParticipantStatusListener *************************/
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

    /************************* 群组用户状态更新 IMUserStatusListener *************************/
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



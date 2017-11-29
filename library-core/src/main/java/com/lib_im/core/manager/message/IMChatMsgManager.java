package com.lib_im.core.manager.message;

import android.util.Log;

import com.lib_im.core.exception.ApiErrorException;
import com.lib_im.profession.message.IMGroupConversation;
import com.lib_im.profession.message.IMUserConversation;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.muc.AutoJoinFailedCallback;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatException;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.muc.UserStatusListener;
import org.jivesoftware.smackx.offline.OfflineMessageHeader;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.DomainBareJid;
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

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * 聊天管理器
 * Created by songgx on 16/6/15.
 */
public class IMChatMsgManager {

    private final String TAG = "IMChatMsgManager";

    private List<IMMessageListener> mIMMessageListeners = new ArrayList<>();
    private PublishSubject<Message> mMessageSubject = PublishSubject.create();

    private AbstractXMPPConnection connection;
    private ChatManager mChatManager;
    private DeliveryReceiptManager mDeliveryReceiptManager;
    private MultiUserChatManager mMultiUserChatManager;
    /**
     * 添加存储聊天室对象的全局map
     */
    private static Map<String, IMGroupConversation> roomMap = new HashMap<>();
    private static Map<String, IMUserConversation> chatMap = new HashMap<>();
    private ReceiptReceivedListener mReceivedListener;
    private StanzaListener mSyncStanzListener;
    private IncomingChatMessageListener mIncomingListener;
    private OutgoingChatMessageListener mOutgoingListener;
    private AutoJoinFailedCallback mGroupAutoJoinCallback;
    private MessageListener mMessageListener;

    {

        mMessageListener = this::processMessage;
        mOutgoingListener = (to, message, chat) -> Log.e(TAG, "发出消息" + message.getBody());
        mIncomingListener = this::receiveMessage;
        mReceivedListener = this::notifyReceiptMessageListener;
        mSyncStanzListener = packet -> {
            Log.e(TAG, "SyncStanzaListener " + packet.toString());
            if (packet instanceof Message) {
                Message message = (Message) packet;
                Log.e(TAG,
                        "SyncStanzaMessage from " + message.getFrom() + " to " + message.getTo() +
                                " type:" + message.getType() + " \nbody:" + message.getBody());
                final Message.Type type = message.getType();
                if (Message.Type.headline.equals(type)) {
                    String content = message.getBody();
                    // TODO 推送模块逻辑，根据具体业务逻辑而定
                }
            }
        };

        mGroupAutoJoinCallback = (muc, e) -> {
            Log.e("ChatManager", "AutoJoinFailed");
            e.printStackTrace();
        };
    }

    public void initIm(AbstractXMPPConnection connection) {
        this.connection = connection;
        mChatManager = ChatManager.getInstanceFor(connection);
        //为传入的聊天消息添加一个新的监听器。
        mChatManager.addIncomingListener(mIncomingListener);
        //为发出的聊天消息添加一个新的监听器。
        mChatManager.addOutgoingListener(mOutgoingListener);
        StanzaFilter stanzaFilter = new StanzaTypeFilter(Stanza.class);
        //提供一种机制来侦听通过指定过滤器的数据包。这允许事件风格的编程 - 每次找到新的节（/数据包）时，
        // 将调用{@link #processStanza（Stanza）}方法。这是一个由连接器提供的功能的另一种方法，它可以让你在等待结果时阻塞。
        connection.addSyncStanzaListener(mSyncStanzListener, stanzaFilter);
        mDeliveryReceiptManager = DeliveryReceiptManager.getInstanceFor(connection);
        //为外发邮件启用送达收据的自动请求
        mDeliveryReceiptManager.autoAddDeliveryReceiptRequests();
        //消息回执添加监听器操作
        mDeliveryReceiptManager.addReceiptReceivedListener(mReceivedListener);
        // 群聊管理器
        mMultiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
        // 重连时自动重连群聊
        mMultiUserChatManager.setAutoJoinOnReconnect(true);
        // 重连监听
        mMultiUserChatManager.setAutoJoinFailedCallback(mGroupAutoJoinCallback);
    }

    /**
     * 获取离线消息
     */
    public void readOfflineMessage()
            throws XMPPException.XMPPErrorException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException {
        Log.e(TAG, "获取离线消息 ");
        OfflineMessageManager offlineManager = new OfflineMessageManager(connection);
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
                //解析接收到的消息体
                String messageBody = message.getBody();
                notifyMessageListener(messageBody);
            }
        }
        offlineManager.deleteMessages();

    }

    /**
     * 获取一个用户单聊会话控制器
     */
    public IMUserConversation getUserConversation(String userId) {
        try {
            if (chatMap.containsKey(userId)) {
                return chatMap.get(userId);
            }
            IMUserConversation conversation = new IMUserConversation(getChat(userId));
            chatMap.put(userId, conversation);
            return conversation;
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取一个用户群聊会话控制器，加入群组是一个耗时操作，所以异步实现
     */
    public Observable<IMGroupConversation> getGroupConversation(String groupId, String nickName) {
        return Observable.create((ObservableOnSubscribe<IMGroupConversation>) e -> {
            e.onNext(findGroupConversation(groupId, nickName));
            e.onComplete();
        })
                         .subscribeOn(Schedulers.io())
                         .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 获取一个用户群聊会话控制器的同步操作
     */
    public IMGroupConversation findGroupConversation(String groupId, String nickName)
            throws ApiErrorException {
        try {
            if (!roomMap.containsKey(groupId)) {
                IMGroupConversation conversation = new IMGroupConversation(
                        getMultiRoom(groupId, nickName));
                roomMap.put(groupId, conversation);
            }
            return roomMap.get(groupId);
        } catch (XMPPException.XMPPErrorException | XmppStringprepException | SmackException.NoResponseException | MultiUserChatException.NotAMucServiceException | SmackException.NotConnectedException |
                InterruptedException e) {
            e.printStackTrace();
            // 抛出自定义的异常
            throw new ApiErrorException("连接群组失败");
        }
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
        if (mIMMessageListeners.contains(call)) {
            mIMMessageListeners.remove(call);
        }
    }

    /**
     * 初始化聊天室信息
     */
    private MultiUserChat getMultiRoom(String jid, String nickName)
            throws XmppStringprepException, XMPPException.XMPPErrorException, MultiUserChatException.NotAMucServiceException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException {
        EntityBareJid entityBareJid = JidCreate.entityBareFrom(jid);
        MultiUserChat multiUserChat = mMultiUserChatManager.getMultiUserChat(entityBareJid);
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
        multiUserChat.addMessageListener(mMessageListener);
        Log.e(TAG, "初始化聊天室成功<<<<<<<<<<<<" + jid + Thread.currentThread());
        return multiUserChat;
    }

    /**
     * 收到群聊消息的处理
     */
    private void processMessage(Message message) {
        String body = message.getBody();
        Log.e(TAG, "processMessage 收到群聊消息...." + body);
        mMessageSubject.onNext(message);
        notifyGroupMessageListener(body);
    }

    /**
     * 当应用断开连接或者时，注销登录时移除掉群聊天对象map
     */
    public void release() {
        for (IMGroupConversation conversation : roomMap.values()) {
            try {
                conversation.leave();
                conversation.release(mMessageListener);
            } catch (SmackException.NotConnectedException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        roomMap.clear();
        chatMap.clear();
        mIMMessageListeners.clear();
    }

    /**
     * 销毁 connect
     */
    public void destroy() {
        release();
        mMessageSubject.onComplete();
        mMessageSubject = null;
        mMultiUserChatManager.setAutoJoinFailedCallback(null);
        mMultiUserChatManager = null;
        mChatManager.removeListener(mIncomingListener);
        mChatManager.removeListener(mOutgoingListener);
        mChatManager = null;
        connection.removeSyncStanzaListener(mSyncStanzListener);
        connection = null;
        mDeliveryReceiptManager.removeReceiptReceivedListener(mReceivedListener);
        mDeliveryReceiptManager = null;
    }

    /**
     * 回调消息监听接口
     */
    private void notifyMessageListener(String msg) {
        for (IMMessageListener call : mIMMessageListeners) {
            call.onReceiveMessage(msg);
        }
    }

    /**
     * 回调消息监听接口
     */
    private void notifyGroupMessageListener(String msg) {
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
            call.onReceiveReceipt(receiptId);
        }
    }

    /**
     * openfire 接收消息回调方法,单聊消息监听
     */
    private void receiveMessage(EntityBareJid from, Message message, Chat chat) {
        String body = message.getBody();
        mMessageSubject.onNext(message);
        notifyMessageListener(body);
    }

    public Observable<String> observeMessage() {
        return mMessageSubject.subscribeOn(Schedulers.io())
                              .map(Message::getBody)
                              .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<String> observeMessage(@NonNull String id) {
        return mMessageSubject.subscribeOn(Schedulers.io())
                              .filter(message -> message.getFrom().asBareJid().toString().equals(id))
                              .map(Message::getBody)
                              .observeOn(AndroidSchedulers.mainThread());
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



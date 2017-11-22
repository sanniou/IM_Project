package com.lib_im.pro.im.config;

/**
 * Created by songgx on 2016/6/16.
 */

import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.address.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.chatstates.provider.ChatStateExtensionProvider;
import org.jivesoftware.smackx.commands.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.delay.provider.DelayInformationProvider;
import org.jivesoftware.smackx.disco.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.disco.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.iqlast.packet.LastActivity;
import org.jivesoftware.smackx.iqprivate.PrivateDataManager;
import org.jivesoftware.smackx.muc.packet.GroupChatInvitation;
import org.jivesoftware.smackx.muc.provider.MUCAdminProvider;
import org.jivesoftware.smackx.muc.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.muc.provider.MUCUserProvider;
import org.jivesoftware.smackx.offline.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.offline.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.privacy.provider.PrivacyProvider;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.search.UserSearch;
import org.jivesoftware.smackx.sharedgroups.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.si.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.vcardtemp.provider.VCardProvider;
import org.jivesoftware.smackx.xdata.provider.DataFormProvider;
import org.jivesoftware.smackx.xevent.provider.MessageEventProvider;
import org.jivesoftware.smackx.xhtmlim.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.xroster.provider.RosterExchangeProvider;

/**
 * xmpp 工具类
 *
 * @author sgx
 */
public class XmppTool {

    private static XmppTool xmppTool = new XmppTool();

    public static XmppTool getInstance() {
        return xmppTool;
    }

    /**
     * 创建连接
     */
    public AbstractXMPPConnection getConnection() {
        AbstractXMPPConnection con;
        if (ChatCode.conMap.size() == 0) {
            registerSmackProviders();
            con = setOpenFireConnectionConfig();
            if (con != null) {
                ChatCode.conMap.put("xmppcon", con);
            }
        } else {
            con = (AbstractXMPPConnection) ChatCode.conMap.get("xmppcon");
        }
        return con;
    }

    /**
     * @descript 客户端连接openfire服务配置信息
     */
    private AbstractXMPPConnection setOpenFireConnectionConfig() {
        if (ChatCode.XMPP_SERVER != null && ChatCode.XMPP_SERVER_NAME != null) {
            XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration
                    .builder();
            builder.setConnectTimeout(ChatCode.PACKET_TIMEOUT)
                   .setSendPresence(false)
                   .setHost(ChatCode.XMPP_SERVER)
                   .setPort(ChatCode.XMPP_PORT)
                   .setResource(ChatCode.XMPP_IDENTITY_NAME)
                   .setDebuggerEnabled(true)
                   .setServiceName(ChatCode.XMPP_SERVER_NAME)
                   .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
            XMPPTCPConnectionConfiguration configuration = builder.build();
            AbstractXMPPConnection con = new XMPPTCPConnection(configuration);
            con.setPacketReplyTimeout(ChatCode.PACKET_TIMEOUT);
            XMPPTCPConnection.setUseStreamManagementDefault(true);
            SmackConfiguration.setDefaultPacketReplyTimeout(ChatCode.PACKET_TIMEOUT);
            PingManager pingManager = PingManager.getInstanceFor(con);
            pingManager.setPingInterval(0);
            return con;
        } else {
            return null;
        }

    }

    /**
     * 做一些基本的配置
     */
    public static void registerSmackProviders() {
        // add IQ handling // Service Discovery # Info
        ProviderManager.addIQProvider("query", "http://jabber.org/protocol/disco#info",
                new DiscoverInfoProvider());
        // Private Data Storage
        ProviderManager.addIQProvider("query", "jabber:iq:private",
                new PrivateDataManager.PrivateDataIQProvider());
        // Time
        try {
            ProviderManager.addIQProvider("query", "jabber:iq:time",
                    Class.forName("org.jivesoftware.smackx.packet.Time"));
        } catch (ClassNotFoundException e) {
            Log.w("TestClient", "Can't load class for org.jivesoftware.smackx.packet.Time");
        }
        // Roster Exchange
        ProviderManager.addExtensionProvider("x", "jabber:x:roster", new RosterExchangeProvider());

        // Message Events
        ProviderManager.addExtensionProvider("x", "jabber:x:event", new MessageEventProvider());

        // Chat State
        ProviderManager.addExtensionProvider("active", "http://jabber.org/protocol/chatstates",
                new ChatStateExtensionProvider());

        ProviderManager.addExtensionProvider("composing", "http://jabber.org/protocol/chatstates",
                new ChatStateExtensionProvider());

        ProviderManager.addExtensionProvider("paused", "http://jabber.org/protocol/chatstates",
                new ChatStateExtensionProvider());

        ProviderManager.addExtensionProvider("inactive", "http://jabber.org/protocol/chatstates",
                new ChatStateExtensionProvider());

        ProviderManager.addExtensionProvider("gone", "http://jabber.org/protocol/chatstates",
                new ChatStateExtensionProvider());

        // XHTML
        ProviderManager.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
                new XHTMLExtensionProvider());

        // Group Chat Invitations
        ProviderManager.addExtensionProvider("x", "jabber:x:conference",
                new GroupChatInvitation.Provider());

        // Service Discovery # Items
        ProviderManager.addIQProvider("query", "http://jabber.org/protocol/disco#items",
                new DiscoverItemsProvider());

        // Data Forms
        ProviderManager.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());

        // MUC User
        ProviderManager.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
                new MUCUserProvider());

        // MUC Admin
        ProviderManager.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
                new MUCAdminProvider());

        // MUC Owner
        ProviderManager.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
                new MUCOwnerProvider());

        // Delayed Delivery
        ProviderManager.addExtensionProvider("x", "jabber:x:delay", new DelayInformationProvider());
        // Version
        try {
            ProviderManager.addIQProvider("query", "jabber:iq:version",
                    Class.forName("org.jivesoftware.smackx.packet.Version"));
        } catch (ClassNotFoundException e) {
            // Not sure what's happening here.
        }
        // VCard
        ProviderManager.addIQProvider("vCard", "vcard-temp", new VCardProvider());

        // Offline Message Requests
        ProviderManager.addIQProvider("offline", "http://jabber.org/protocol/offline",
                new OfflineMessageRequest.Provider());

        // Offline Message Indicator
        ProviderManager.addExtensionProvider("offline", "http://jabber.org/protocol/offline",
                new OfflineMessageInfo.Provider());

        // Last Activity
        ProviderManager.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());

        // User Search
        ProviderManager.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());

        // SharedGroupsInfo
        ProviderManager
                .addIQProvider("sharedgroup", "http://www.jivesoftware.org/protocol/sharedgroup",
                        new SharedGroupsInfo.Provider());

        // JEP-33: Extended Stanza Addressing
        ProviderManager.addExtensionProvider("addresses", "http://jabber.org/protocol/address",
                new MultipleAddressesProvider());

        // FileTransfer
        ProviderManager.addIQProvider("si", "http://jabber.org/protocol/si",
                new StreamInitiationProvider());

        ProviderManager.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
                new BytestreamsProvider());

        // Privacy
        ProviderManager.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());

        ProviderManager.addIQProvider("command", "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider());
        ProviderManager
                .addExtensionProvider("malformed-action", "http://jabber.org/protocol/commands",
                        new AdHocCommandDataProvider.MalformedActionError());
        ProviderManager.addExtensionProvider("bad-locale", "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.BadLocaleError());
        ProviderManager.addExtensionProvider("bad-payload", "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.BadPayloadError());
        ProviderManager.addExtensionProvider("bad-sessionid", "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.BadSessionIDError());
        ProviderManager
                .addExtensionProvider("session-expired", "http://jabber.org/protocol/commands",
                        new AdHocCommandDataProvider.SessionExpiredError());
        // add delivery receipts  消息回执配置
        ProviderManager.addExtensionProvider(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE,
                new DeliveryReceipt.Provider());
        ProviderManager
                .addExtensionProvider(DeliveryReceiptRequest.ELEMENT, DeliveryReceipt.NAMESPACE,
                        new DeliveryReceiptRequest.Provider());
    }

}

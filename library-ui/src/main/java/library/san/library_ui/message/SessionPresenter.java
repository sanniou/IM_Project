package library.san.library_ui.message;

import android.content.Context;
import android.content.Intent;

import library.san.library_ui.entity.ChatMessage;
import library.san.library_ui.entity.GroupContact;
import library.san.library_ui.entity.SessionItem;
import com.lib_im.pro.im.listener.RefreshDataListener;
import com.lib_im.pro.im.manager.group.GroupContactManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import library.san.library_ui.chat.ChatActivity;

/**
 * Created by songgx on 2016/12/12.
 * 消息模块---处理数据操作---设定调用方式
 */

public class SessionPresenter {
    private List<SessionItem> listSession=new ArrayList<>();
    private RefreshDataListener refreshDataListener;

    public SessionPresenter(RefreshDataListener refreshDataListener) {
        this.refreshDataListener = refreshDataListener;
    }


    /**
     * 获取本地的会话记录
     * @return
     */
    public List<SessionItem> getSession() {
        List<SessionItem> _copy = LiteChat.chatClient.getSessionManager().getSessionList();
        List<GroupContact> groupContacts = LiteChat.chatClient.getGroupContactManager().getGroupList();
        List<SessionItem> _sessionList = new ArrayList<>();
        if (_copy != null && _copy.size() > 0) {
            //查询当前用户所包含的群组
            for (SessionItem _session : _copy) {//过滤出群组会话
                if (_session.getRoomId() != null && !_session.getRoomId().equals("")) {
                    _sessionList.add(_session);
                }
            }
            _copy.removeAll(_sessionList);
            int groupSize = groupContacts.size();
            int sessionSize = _sessionList.size();
            List<SessionItem> $list = new ArrayList<>();
            if (groupSize >= sessionSize) {
                for (GroupContact groupContact : groupContacts) {
                    for (SessionItem sessionItem : _sessionList) {
                        if (sessionItem.getRoomId().equals(groupContact.getGroupID())) {
                            if (!$list.contains(sessionItem)) {
                                $list.add(sessionItem);
                            }
                        }
                    }
                }
            } else {
                for (SessionItem sessionItem : _sessionList) {
                    for (GroupContact groupContact : groupContacts) {
                        if (groupContact.getGroupID().equals(sessionItem.getRoomId())) {
                            if ($list.contains(sessionItem)) {
                                $list.add(sessionItem);
                            }

                        }
                    }
                }
            }
            _copy.addAll($list);
            listSession.addAll(_copy);
            Collections.sort(listSession, new SessionComparator());
        }
        return listSession;
    }


    void deleteSession(SessionItem sessionItem) {
        LiteChat.chatClient.getSessionManager().deleteSession(sessionItem);
    }


    void updateSession(SessionItem sessionItem) {
        List<SessionItem> $list = new ArrayList<>();
        boolean isRoom = sessionItem.isRoom();
        for (SessionItem _sessionItem : listSession) {
            if (_sessionItem.isRoom()) {//本地session为群
                if (isRoom) {//传递的session为群
                    if (_sessionItem.getRoomId().equals(sessionItem.getRoomId())) {
                        $list.add(_sessionItem);
                    }
                } else {//传递的session不为群

                }
            } else {//本地session不为群
                if (isRoom) {//传递的session为群

                } else {//传递的session不为群
                    if (_sessionItem.getFromId().equals(sessionItem.getFromId())) {
                        $list.add(_sessionItem);
                    }
                }
            }
        }
        listSession.removeAll($list);
        listSession.add(sessionItem);
        Collections.sort(listSession, new SessionComparator());
        refreshDataListener.onRefresh();
    }


    void itemOnClick(int position, Context context) {
        SessionItem _item = listSession.get(position);
        Intent intent = new Intent(context, ChatActivity.class);
        // 发送进入聊天窗口，消除主界面提醒
        switch (_item.getMsg_type()) {
            case ChatMessage.MESSAGE_TYPE_NOTICE:
                //收到各种各样的通知，根据code
                return;
            default:
                if (_item.isRoom()) {// 群聊
                    GroupContactManager<GroupContact> groupContactManager = LiteChat.chatClient.getGroupContactManager();
                    GroupContact _groupContact = groupContactManager.getGroupContact(_item.getRoomId());
                    String roomId = _item.getRoomId();
                    String roomJid = "";
                    String _roomName = "";
                    if (_groupContact != null) {
                        if (_groupContact.getGroupName() != null && !_groupContact.getGroupName().equals("")) {
                            _roomName = _groupContact.getGroupName();
                        }
                        roomJid = _groupContact.getGroupJid();
                    }
                    intent.putExtra("chatUserId", roomId);
                    intent.putExtra("chatUserName", _roomName);
                    intent.putExtra("groupChat", Boolean.TRUE);
                    intent.putExtra("chatRoomJid", roomJid);

                } else {
                    String fromId = _item.getFromId();
                    String name = _item.getFromName();
                    intent.putExtra("chatUserId", fromId);
                    intent.putExtra("chatUserName", name);
                    intent.putExtra("groupChat", Boolean.FALSE);
                }
                context.startActivity(intent);
                break;
        }
    }

}

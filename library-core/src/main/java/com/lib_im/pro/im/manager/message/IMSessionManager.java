package com.lib_im.pro.im.manager.message;

import android.content.Context;

import library.san.library_ui.db.BaseDao;
import library.san.library_ui.db.DataBaseHelper;
import library.san.library_ui.entity.SessionItem;
import com.lib_im.core.config.ChatCode;
import com.lib_im.pro.im.listener.IMSessionListener;
import com.lib_im.pro.im.listener.RefreshDataListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天、群组聊天会话管理器
 * Created by songgx on 15/11/19.
 */
public class IMSessionManager implements SessionManager<SessionItem> {

    private List<IMSessionListener> mIMSessionListeners = new ArrayList<>();
    private List<RefreshDataListener> refreshDataListeners = new ArrayList<>();

    private Context mContext;

    private String TAG = "IMSessionManager";

    private String account;
    private BaseDao<SessionItem> dao;

    public IMSessionManager(Context ctx) {
        mContext = ctx;
        dao = new BaseDao<>(DataBaseHelper.getInstance(mContext), SessionItem.class);
    }

    @Override
    public void init() {
    }

    @Override
    public void initIm() {

    }

    @Override
    public void setCurrentUser(String uid) {
        account = uid;
    }

    @Override
    public List<SessionItem> getSessionList() {
        List<SessionItem> list = dao.queryByColumn("account", account);
        if (list != null) {
            return list;
        }
        return new ArrayList<>();
    }

    @Override
    public SessionItem getSession(String _chatUserId, boolean isRoom) {
        List<SessionItem> _list;
        SessionItem _sessionItem = null;
        if (isRoom) {
            _list = dao.queryByColumn("account", account, "roomId", _chatUserId);
        } else {
            _list = dao.queryByColumn("account", account, "fromId", _chatUserId);
        }
        if (_list != null) {
            for (SessionItem sessionItem : _list) {
                if (sessionItem != null) {
                    _sessionItem = sessionItem;
                }
            }
        }
        return _sessionItem;
    }

    @Override
    public void postSession(SessionItem _newSession, int _noReadCount) {
        boolean isRoom = _newSession.isRoom();
        if (isRoom) {//群会话
            SessionItem tableSession = getSession(_newSession.getRoomId(), true);
            createSession(_newSession, tableSession);

        } else {//单聊会话
           SessionItem tableSession=getSession(_newSession.getFromId(),false);
           createSession(_newSession, tableSession);
        }
    }

    /**
     * 根据群组与个人判断数据库中数据与当前数据的差别进行替换
     * @param _newSession
     * @param tableSession
     */
    private void createSession(SessionItem _newSession, SessionItem tableSession) {
        if (tableSession == null) {
            _newSession.setAccount(account);
            dao.add(_newSession);
        } else {
            int count = _newSession.getNoReadCount() + tableSession.getNoReadCount();
            _newSession.setNoReadCount(count);
            _newSession.setAccount(account);
            dao.delete(tableSession);
            dao.add(_newSession);
        }
        notifyUpdateSessionListeners(_newSession);
    }

    @Override
    public void resetSessionMessageCount(SessionItem sessionItem) {
        if (sessionItem != null) {
            String userId = LiteChat.chatCache.readString(ChatCode.KEY_USER_ID);
            SessionItem tableSession = null;
            boolean room = sessionItem.isRoom();
            if (room) {
                tableSession = getSession(sessionItem.getRoomId(), true);
            } else {
                tableSession = getSession(sessionItem.getFromId(), false);
            }
            if (tableSession != null) {
                dao.delete(tableSession);
                sessionItem.setAccount(userId);
                sessionItem.setNoReadCount(0);
                dao.add(sessionItem);
            }
            notifyUpdateSessionListeners(sessionItem);
        } else {
            //封口操作
        }


    }

    @Override
    public void deleteSession(SessionItem sessionItem) {
        dao.delete(sessionItem);
        notifyDeleteSessionListeners(sessionItem);
    }

    /**
     * @descript 下发通知，界面更新session
     */
    private void notifyUpdateSessionListeners(SessionItem _session) {
        List<IMSessionListener> _copy = new ArrayList<>(mIMSessionListeners);
        for (IMSessionListener _call : _copy) {
            try {
                _call.onUpdateSession(_session);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * @descript 下发通知，界面删除session
     */
    private void notifyDeleteSessionListeners(SessionItem _session) {
        List<IMSessionListener> _copy = new ArrayList<>(mIMSessionListeners);
        for (IMSessionListener _call : _copy) {
            try {
                _call.onDeleteSession(_session);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * @descript 下发通知, 刷新界面
     */
    public void notifyRefreshDataListeners() {
        List<RefreshDataListener> _copy = new ArrayList<>(refreshDataListeners);
        for (RefreshDataListener _call : _copy) {
            try {
                _call.onRefresh();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void addSessionListener(IMSessionListener _call) {
        if (_call == null) {
            return;
        }
        if (mIMSessionListeners.indexOf(_call) == -1) {
            mIMSessionListeners.add(_call);
        }
    }

    @Override
    public void removeSessionListener(IMSessionListener _call) {
        if (_call == null) {
            return;
        }
        if (mIMSessionListeners.indexOf(_call) != -1) {
            mIMSessionListeners.remove(_call);
        }
    }

    /**
     * 注册一个刷新界面session接口
     */
    @Override
    public void addRefreshListener(RefreshDataListener _call) {
        if (_call == null) {
            return;
        }
        if (refreshDataListeners.indexOf(_call) == -1) {
            refreshDataListeners.add(_call);
        }
    }

    /**
     * 注销一个刷新界面session接口
     */
    @Override
    public void removeRefreshListener(RefreshDataListener _call) {
        if (_call == null) {
            return;
        }
        if (refreshDataListeners.indexOf(_call) != -1) {
            refreshDataListeners.remove(_call);
        }
    }
}
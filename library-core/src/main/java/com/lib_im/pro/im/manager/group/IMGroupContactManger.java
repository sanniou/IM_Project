package com.lib_im.pro.im.manager.group;


import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.lib_im.pro.R;
import library.san.library_ui.db.BaseDao;
import library.san.library_ui.db.DataBaseHelper;
import library.san.library_ui.entity.GroupDetails;
import library.san.library_ui.entity.GroupMember;
import com.lib_im.core.config.ChatCode;
import library.san.library_ui.entity.ChatMessage;
import library.san.library_ui.entity.GroupChatRecord;
import library.san.library_ui.entity.GroupContact;
import com.lib_im.pro.im.listener.HandleGroupListener;
import com.lib_im.pro.im.listener.IMGroupListener;
import com.lib_im.pro.im.listener.OnLoadListener;
import com.lib_im.pro.rx.SimpleListCompleteObserver;
import com.lib_im.pro.rx.SimpleListObserver;
import library.san.library_ui.utils.LogUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.annotations.NonNull;

/**
 * 群组管理器
 * Created by songgx on 16/6/15.
 */
public class IMGroupContactManger implements GroupContactManager<GroupContact> {

    private String TAG = "IMGroupContactManger";

    private String VERSION = "1.0.0";

    private Context mContext;
    private BaseDao<GroupContact> mBaseDao;
    private List<IMGroupListener> mIMGroupListeners = new ArrayList<>();
    private List<OnLoadListener> mOnLoadListeners = new ArrayList<>();
    private List<HandleGroupListener> mHandleListeners = new ArrayList<>();

    public IMGroupContactManger(Context context) {
        this.mContext = context;
        Log.i(TAG, "Version--> " + VERSION);
        mBaseDao = new BaseDao<>(DataBaseHelper.getInstance(mContext), GroupContact.class);
    }

    private List<GroupChatRecord> groupStrList = new ArrayList<>();
    private String account;

    @Override
    public void init() {
    }

    @Override
    public void initIm() {

    }

    /**
     * 设置当前用户
     *
     * @param uid
     */
    @Override
    public void setCurrentUser(String uid) {
        account = uid;
    }

    /**
     * 加载群组
     */
    @Override
    public void loadGroupContact(final String groupType, final OnLoadListener onLoadListener) {
        //TODO 从网络加载群组列表
        LiteChat.imRequestManager.getListInstance().queryGroupContact(groupType).subscribe(new SimpleListObserver<GroupContact>() {
            @Override
            public void onNext(@NonNull List<GroupContact> groupContacts) {
                if (groupContacts != null) {
                    BaseDao<GroupContact> dao = new BaseDao<>(DataBaseHelper.getInstance(mContext), GroupContact.class);
                    String nickName = LiteChat.chatCache.readString(ChatCode.KEY_USER_NAME);
                    for (GroupContact groupContact : groupContacts) {
                        groupContact.setAccount(account);
                        GroupContact table = getGroupContact(groupContact.getGroupID());
                        if (table != null) {
                            dao.delete(table);
                        }
                        dao.add(groupContact);
                        //判断是否有新建的群组
                        String jid = groupContact.getGroupJid();
                        MultiUserChat multiUserChat = (MultiUserChat) ChatCode.roomMap.get(jid);
                        if (multiUserChat == null) {
                            LiteChat.chatClient.getChatManger().initMultiRoom(jid, nickName);
                        }
                    }
                    onLoadListener.onLoadSuccess(groupContacts);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                LogUtils.e(e.getMessage());
                onLoadListener.onLoadFailed(e.getMessage());
            }
        });
    }





    /**
     * @param _list 群组列表
     * @descript 加载群组历史记录
     */
    @Override
    public void loadRoomHistoryMsg(List<GroupContact> _list) {
        //TODO 根据后端历史纪录插件实现逻辑
        groupStrList.clear();
        if (_list != null) {
            for (GroupContact groupContact : _list) {
                if (groupContact != null) {
                    List<GroupChatRecord> list = packRoomUnReadCountString(groupContact.getGroupID());
                    if (list != null) {
                        groupStrList.addAll(list);
                    }
                }
            }
            String string = new Gson().toJson(groupStrList);
            LiteChat.chatClient.getChatManger().getRoomUnReadMessageCount(string);
        }
    }

    /**
     * 加人
     */
    @Override
    public void addUsersToGroup(String otherUserID,String groupID,final IMGroupListener imGroupListener) {
        //TODO 通过业务来添加人员到群组
        LiteChat.imRequestManager.getListInstance().addUserToGroup(otherUserID,groupID).subscribe(new SimpleListCompleteObserver<String>() {
            @Override
            public void onError(@NonNull Throwable e) {
                imGroupListener.OnHandleGroupMemberError(e.getMessage());
            }

            @Override
            public void onComplete() {
                imGroupListener.OnAddUserToGroup(mContext.getString(R.string.handle_success));
            }
        });
    }

    /**
     * 踢人
     */
    @Override
    public void removeUserFromGroup(String otherUserID,String groupID, final IMGroupListener imGroupListener) {
        //TODO 通过业务来删除人员
        LiteChat.imRequestManager.getListInstance().removeUserFromGroup(otherUserID,groupID).subscribe(new SimpleListCompleteObserver<String>() {
            @Override
            public void onError(@NonNull Throwable e) {
                imGroupListener.OnHandleGroupMemberError(e.getMessage());
            }

            @Override
            public void onComplete() {
                imGroupListener.OnDeleteUserFromGroup(mContext.getString(R.string.handle_success));
            }
        });
    }




    /**
     * 封装获取未读消息数量的字符串
     */
    private List<GroupChatRecord> packRoomUnReadCountString(String groupId) {
        List<GroupChatRecord> list = new ArrayList<>();
        ChatMessage chatMessage = LiteChat.chatCache.readObject(groupId, ChatMessage.class);//每次应用退出存储的最新消息
        String selfName = LiteChat.chatClient.getConfig(ChatCode.KEY_USER_NAME);
        org.json.JSONObject jsonObject = new org.json.JSONObject();
        if (chatMessage != null && chatMessage.getSelfName().equals(selfName)) {
            String messageId = chatMessage.getMsgId();
            // 时间戳
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            String sendTime = format.format(chatMessage.getDate());
            GroupChatRecord groupChatRecord = new GroupChatRecord();
            groupChatRecord.setGroupId(groupId);
            groupChatRecord.setMessageId(messageId);
            groupChatRecord.setSendTime(sendTime);
            list.add(groupChatRecord);
//            try {
//                jsonObject.put("groupId", groupId);
//                jsonObject.put("messageId", messageId);
//                jsonObject.put("sendTime", sendTime);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
        } else {
            LiteChat.chatCache.deleteValue(groupId);
//            try {
//                jsonObject.put("groupId", groupId);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
            GroupChatRecord groupChatRecord = new GroupChatRecord();
            groupChatRecord.setGroupId(groupId);
            list.add(groupChatRecord);
        }
        return list;
    }


    /**
     * 查询指定通讯录内容
     *
     * @param groupId
     * @return
     */
    @Override
    public GroupContact getGroupContact(String groupId) {
        //TODO 从数据库获取群组的相关信息
        List<GroupContact> _list = mBaseDao.queryByColumn("account", account, "groupID", groupId);
        GroupContact _groupContact = null;
        if (_list != null) {
            for (GroupContact groupContact : _list) {
                if (groupContact != null) {
                    _groupContact = groupContact;
                }
            }
        }
        return _groupContact;
    }

    /**
     * 添加监听
     *
     * @param imGroupListener
     */
    @Override
    public void addGroupUserListener(IMGroupListener imGroupListener) {
        if (mIMGroupListeners.indexOf(imGroupListener) == -1) {
            mIMGroupListeners.add(imGroupListener);
        }
    }

    /**
     * 删除监听
     *
     * @param imGroupListener
     */
    @Override
    public void removeGroupUserListener(IMGroupListener imGroupListener) {
        if (mIMGroupListeners.indexOf(imGroupListener) != -1) {
            mIMGroupListeners.remove(imGroupListener);
        }
    }

    /**
     * 添加监听
     *
     * @param onLoadListener
     */
    @Override
    public void addLoadListener(OnLoadListener onLoadListener) {
        if (mOnLoadListeners.indexOf(onLoadListener) == -1) {
            mOnLoadListeners.add(onLoadListener);
        }
    }

    @Override
    public void dismissGroup(String groupID, final HandleGroupListener handleGroupListener) {
        LiteChat.imRequestManager.getListInstance().dismissGroup(groupID).subscribe(new SimpleListCompleteObserver<String>() {
            @Override
            public void onError(@NonNull Throwable e) {
                handleGroupListener.handleGroupError(e.getMessage());
            }

            @Override
            public void onComplete() {
                handleGroupListener.dismissGroup();
            }
        });
    }

    @Override
    public void exitGroup(String groupID, final HandleGroupListener handleGroupListener) {
     LiteChat.imRequestManager.getListInstance().exitGroup(groupID).subscribe(new SimpleListCompleteObserver<String>() {
         @Override
         public void onError(@NonNull Throwable e) {
             handleGroupListener.handleGroupError(e.getMessage());
         }

         @Override
         public void onComplete() {
             handleGroupListener.exitGroup();
         }
     });
    }





    @Override
    public void addHandleGroupListener(HandleGroupListener handleGroupListener) {
        if (mHandleListeners.indexOf(handleGroupListener) == -1) {
            mHandleListeners.add(handleGroupListener);
        }
    }

    @Override
    public void removeHandleGroupListener(HandleGroupListener handleGroupListener) {
        if (mHandleListeners.indexOf(handleGroupListener) != -1) {
            mHandleListeners.remove(handleGroupListener);
        }
    }

    /**
     * 添加监听
     *
     * @param onLoadListener
     */
    @Override
    public void removeLoadListener(OnLoadListener onLoadListener) {
        if (mOnLoadListeners.indexOf(onLoadListener) != -1) {
            mOnLoadListeners.remove(onLoadListener);
        }
    }

    /**
     * @descript 查询群组列表
     */
    @Override
    public List<GroupContact> getGroupList() {
        //TODO 从本地查询到群组列表
        return mBaseDao.queryByColumn("account", account);
    }

    /**
     * 获取群成员
     *
     * @param groupID
     */
    @Override
    public void getGroupMemberList(String groupID,String page,String rows, final OnLoadListener onLoadListener) {
        //TODO 获取群成员列表根据业务实现
        LiteChat.imRequestManager.getListInstance().queryMemberList(groupID,page,rows).subscribe(new SimpleListObserver<GroupMember>() {
            @Override
            public void onNext(@NonNull List<GroupMember> members) {
                if (members != null) {
                    onLoadListener.onLoadSuccess(members);
                } else {
                    onLoadListener.onLoadFailed(mContext.getString(R.string.has_no_data));
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                onLoadListener.onLoadFailed(e.getMessage());
            }
        });
    }

    /**
     * 查询群详情
     *
     * @param groupID
     */
    @Override
    public void queryGroupDetails(String groupID, final OnLoadListener onLoadListener) {
        //TODO 获取群详情根据业务实现
        LiteChat.imRequestManager.getListInstance().queryGroupDetails(groupID).subscribe(new SimpleListObserver<GroupDetails>() {
            @Override
            public void onNext(@NonNull List<GroupDetails> groupDetails) {
                if (groupDetails!=null) {
                    onLoadListener.onLoadSuccess(groupDetails);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                onLoadListener.onLoadFailed(e.getMessage());
            }
        });
    }


}

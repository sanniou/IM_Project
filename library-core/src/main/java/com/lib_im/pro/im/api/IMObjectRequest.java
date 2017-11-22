package com.lib_im.pro.im.api;

import com.lib_im.pro.im.entity.ChatRecord;
import com.lib_im.pro.im.entity.Contact;
import com.lib_im.pro.im.entity.GroupChatRecord;
import com.lib_im.pro.im.entity.GroupContact;
import com.lib_im.pro.im.entity.GroupDetails;
import com.lib_im.pro.im.entity.GroupMember;
import com.lib_im.pro.retrofit.base.BaseObjectResponseMapper;
import com.lib_im.pro.retrofit.config.IMRetrofit;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

/**
 * Created by songgx on 2017/9/26.
 * 封装整个 module 的网络操作
 */

public class IMObjectRequest {

    private static IMObjectRequest sInstance;
    private final IMObjectService mImService;

    private IMObjectRequest() {
        Retrofit retrofit = IMRetrofit.getRetrofit();
        mImService = retrofit.create(IMObjectService.class);
    }

    public IMObjectRequest getObjectInstance() {
        synchronized (IMObjectRequest.class) {
            if (sInstance == null) {
                sInstance = new IMObjectRequest();
            }
        }
        return sInstance;
    }

    /**
     * 获取群组接口
     */
    public Observable<GroupContact> queryGroupContact(String userID) {
        return mImService.queryGroupList(userID)
                         // .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseObjectResponseMapper<GroupContact>());
    }

    /**
     * 群组添加成员接口
     */
    public Observable<String> addUserToGroup(String otherUserID, String groupID) {
        return mImService.addMember(otherUserID, groupID)
                         //.observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseObjectResponseMapper<String>());
    }

    /**
     * 群组删除成员接口
     */
    public Observable<String> removeUserFromGroup(String otherUserID, String groupID) {
        return mImService.removeMember(otherUserID, groupID)
                         // .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseObjectResponseMapper<String>());
    }

    /**
     * 查询群组未读记录数
     */
    public Observable<GroupChatRecord> queryGroupRecordCount(String groupStr) {
        return mImService.queryGroupUnReadCount(groupStr)
                         //.observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseObjectResponseMapper<GroupChatRecord>());
    }

    /**
     * 查询群组历史纪录
     */
    public Observable<GroupChatRecord> queryGroupChatRecord(String groupId, String rows,
                                                            String logId, String messageId) {
        return mImService.queryGroupChatRecord(groupId, rows, logId, messageId)
                         //.observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseObjectResponseMapper<GroupChatRecord>());
    }

    /**
     * 查询单人聊天历史纪录
     */
    public Observable<ChatRecord> queryChatRecord(String messageId, String fromUserId,
                                                  String toUserId, String rows) {
        return mImService.queryChatRecord(messageId, fromUserId, toUserId, rows)
                         //.observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseObjectResponseMapper<ChatRecord>());
    }

    /**
     * 查询群成员列表
     */
    public Observable<GroupMember> queryMemberList(String ID, String page, String rows) {
        return mImService.queryGroupMember(ID, page, rows)
                         //.observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseObjectResponseMapper<GroupMember>());
    }

    /**
     * 查询群详情
     */
    public Observable<GroupDetails> queryGroupDetails(String ID) {
        return mImService.queryGroupDetails(ID)
                         //.observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseObjectResponseMapper<GroupDetails>());
    }

    /**
     * 查询好友列表
     */
    public Observable<Contact> queryFriendList() {
        return mImService.queryFriendList()
//                .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseObjectResponseMapper<Contact>());
    }

    /**
     * 解散群组
     */
    public Observable<String> dismissGroup(String id) {
        return mImService.dismissGroup(id)
                         //.observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseObjectResponseMapper<String>());
    }

    /**
     * 退出群组
     */
    public Observable<String> exitGroup(String id) {
        return mImService.dismissGroup(id)
                         //.observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseObjectResponseMapper<String>());
    }

    /**
     * 模糊查找联系人
     */
    public Observable<Contact> searchFriendList(String key) {
        return mImService.searchFriendList(key)
//                .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseObjectResponseMapper<Contact>());
    }

    /**
     * 删除联系人
     */
    public Observable<String> removeContact(String userID) {
        return mImService.removeContact(userID)
//                .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseObjectResponseMapper<String>());
    }

    /**
     * 添加联系人
     */
    public Observable<String> addContact(String userID) {
        return mImService.addContact(userID)
//                .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseObjectResponseMapper<String>());
    }

    /**
     * 同意好友申请
     */
    public Observable<String> acceptRequest(String otherUserID) {
        return mImService.acceptRequest(otherUserID)
//                .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseObjectResponseMapper<String>());
    }

    /**
     * 拒绝好友申请
     */
    public Observable<String> refuseRequest(String otherUserID) {
        return mImService.refuseRequest(otherUserID)
//                .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseObjectResponseMapper<String>());
    }
}

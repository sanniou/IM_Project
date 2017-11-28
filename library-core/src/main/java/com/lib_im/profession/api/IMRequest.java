package com.lib_im.profession.api;

import com.lib_im.core.entity.ChatRecord;
import com.lib_im.core.entity.GroupChatRecord;
import com.lib_im.profession.entity.Contact;
import com.lib_im.profession.entity.GroupContact;
import com.lib_im.profession.entity.GroupDetails;
import com.lib_im.profession.entity.GroupMember;
import com.lib_im.profession.retrofit.base.BaseResponseMapper;
import com.lib_im.profession.retrofit.config.IMRetrofit;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

/**
 * 封装整个 module 的网络操作
 */
public class IMRequest {

    private static IMRequest sInstance;
    private final IMService mImService;

    private IMRequest() {
        Retrofit retrofit = IMRetrofit.getRetrofit();
        mImService = retrofit.create(IMService.class);
    }

    public static IMRequest getInstance() {
        synchronized (IMRequest.class) {
            if (sInstance == null) {
                sInstance = new IMRequest();
            }
        }
        return sInstance;
    }

    /**
     * 获取群组接口
     */
    public Observable<List<GroupContact>> queryGroupContact() {
        return mImService.queryGroupList(null)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseResponseMapper<>());
    }

    /**
     * 群组添加成员接口
     */
    public Observable<List<String>> addUserToGroup(String otherUserID, String groupID) {
        return mImService.addMember(otherUserID, groupID)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseResponseMapper<>());
    }

    /**
     * 群组删除成员接口
     */
    public Observable<List<String>> removeUserFromGroup(String otherUserID, String groupID) {
        return mImService.removeMember(otherUserID, groupID)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseResponseMapper<>());
    }

    /**
     * 查询群组未读记录数
     */
    public Observable<List<GroupChatRecord>> queryGroupRecordCount(String groupStr) {
        return mImService.queryGroupUnReadCount(groupStr)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseResponseMapper<>());
    }

    /**
     * 查询群组历史纪录
     */
    public Observable<List<GroupChatRecord>> queryGroupChatRecord(String groupId, String logId,
                                                                  String messageId, int page,
                                                                  int rows) {
        return mImService.queryGroupChatRecord(groupId, logId, messageId, page, rows)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseResponseMapper<>());
    }

    /**
     * 查询单人聊天历史纪录
     */
    public Observable<List<ChatRecord>> queryChatRecord(String messageId, String fromUserId,
                                                        String toUserId, int page, int rows) {
        return mImService.queryChatRecord(messageId, fromUserId, toUserId, page, rows)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseResponseMapper<>());
    }

    /**
     * 查询群成员列表
     */
    public Observable<List<GroupMember>> queryMemberList(String ID, String page, String rows) {
        return mImService.queryGroupMember(ID, page, rows)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseResponseMapper<>());
    }

    /**
     * 查询群详情
     */
    public Observable<List<GroupDetails>> queryGroupDetails(String ID) {
        return mImService.queryGroupDetails(ID)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseResponseMapper<>());
    }

    /**
     * 查询好友列表
     */
    public Observable<List<Contact>> queryFriendList(String userType) {
        return mImService.queryFriendList(userType)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseResponseMapper<>());
    }

    /**
     * 解散群组
     */
    public Observable<List<String>> dissolveGroup(String id) {
        return mImService.dissolveGroup(id)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseResponseMapper<>());
    }

    /**
     * 退出群组
     */
    public Observable<List<String>> exitGroup(String id) {
        return mImService.dissolveGroup(id)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseResponseMapper<>());
    }

    /**
     * 模糊查找联系人
     */
    public Observable<List<Contact>> searchFriendList(String key) {
        return mImService.searchFriendList(key)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseResponseMapper<>());
    }

    /**
     * 删除联系人
     */
    public Observable<List<String>> removeContact(String userID) {
        return mImService.removeContact(userID)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseResponseMapper<>());
    }

    /**
     * 添加联系人
     */
    public Observable<List<String>> addContact(String userID) {
        return mImService.addContact(userID)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseResponseMapper<>());
    }

    /**
     * 同意好友申请
     */
    public Observable<List<String>> acceptRequest(String otherUserID) {
        return mImService.acceptRequest(otherUserID)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseResponseMapper<>());
    }

    /**
     * 拒绝好友申请
     */
    public Observable<List<String>> refuseRequest(String otherUserID) {
        return mImService.refuseRequest(otherUserID)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseResponseMapper<>());
    }

}
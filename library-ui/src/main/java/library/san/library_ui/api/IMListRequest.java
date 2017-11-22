package library.san.library_ui.api;

import com.lib_im.pro.retrofit.base.BaseListResponseMapper;
import com.lib_im.pro.retrofit.config.IMRetrofit;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import library.san.library_ui.entity.ChatRecord;
import library.san.library_ui.entity.Contact;
import library.san.library_ui.entity.GroupChatRecord;
import library.san.library_ui.entity.GroupContact;
import library.san.library_ui.entity.GroupDetails;
import library.san.library_ui.entity.GroupMember;
import library.san.library_ui.entity.UserInfo;
import retrofit2.Retrofit;

/**
 * 封装整个 module 的网络操作
 */
public class IMListRequest extends IMRequestManager {

    private static IMListRequest sInstance;
    private final IMListService mImService;

    public IMListRequest() {
        Retrofit retrofit = IMRetrofit.getRetrofit();
        mImService = retrofit.create(IMListService.class);
    }

    @Override
    public IMListRequest getListInstance() {
        synchronized (IMListRequest.class) {
            if (sInstance == null) {
                sInstance = new IMListRequest();
            }
        }
        return sInstance;
    }

    /**
     * 登录接口
     */
    public Observable<List<UserInfo>> login(String userName, String password) {
        return mImService.login(userName, password)
                         //.observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseListResponseMapper<UserInfo>());
    }

    /**
     * 获取群组接口
     */
    public Observable<List<GroupContact>> queryGroupContact(String groupType) {
        return mImService.queryGroupList(groupType)
                         // .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseListResponseMapper<GroupContact>());
    }

    /**
     * 群组添加成员接口
     */
    public Observable<List<String>> addUserToGroup(String otherUserID, String groupID) {
        return mImService.addMember(otherUserID, groupID)
                         //.observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseListResponseMapper<String>());
    }

    /**
     * 群组删除成员接口
     */
    public Observable<List<String>> removeUserFromGroup(String otherUserID, String groupID) {
        return mImService.removeMember(otherUserID, groupID)
                         // .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseListResponseMapper<String>());
    }

    /**
     * 查询群组未读记录数
     */
    public Observable<List<GroupChatRecord>> queryGroupRecordCount(String groupStr) {
        return mImService.queryGroupUnReadCount(groupStr)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseListResponseMapper<GroupChatRecord>());
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
                         .flatMap(new BaseListResponseMapper<GroupChatRecord>());
    }

    /**
     * 查询单人聊天历史纪录
     */
    public Observable<List<ChatRecord>> queryChatRecord(String messageId, String fromUserId,
                                                        String toUserId, int page, int rows) {
        return mImService.queryChatRecord(messageId, fromUserId, toUserId, page, rows)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseListResponseMapper<ChatRecord>());
    }

    /**
     * 查询群成员列表
     */
    public Observable<List<GroupMember>> queryMemberList(String ID, String page, String rows) {
        return mImService.queryGroupMember(ID, page, rows)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseListResponseMapper<GroupMember>());
    }

    /**
     * 查询群详情
     */
    public Observable<List<GroupDetails>> queryGroupDetails(String ID) {
        return mImService.queryGroupDetails(ID)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseListResponseMapper<GroupDetails>());
    }

    /**
     * 查询好友列表
     */
    public Observable<List<Contact>> queryFriendList(String userType) {
        return mImService.queryFriendList(userType)
//                .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseListResponseMapper<Contact>());
    }

    /**
     * 解散群组
     */
    public Observable<List<String>> dismissGroup(String id) {
        return mImService.dismissGroup(id)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseListResponseMapper<String>());
    }

    /**
     * 退出群组
     */
    public Observable<List<String>> exitGroup(String id) {
        return mImService.dismissGroup(id)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseListResponseMapper<String>());
    }

    /**
     * 模糊查找联系人
     */
    public Observable<List<Contact>> searchFriendList(String key) {
        return mImService.searchFriendList(key)
//                .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseListResponseMapper<Contact>());
    }

    /**
     * 删除联系人
     */
    public Observable<List<String>> removeContact(String userID) {
        return mImService.removeContact(userID)
//                .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseListResponseMapper<String>());
    }

    /**
     * 添加联系人
     */
    public Observable<List<String>> addContact(String userID) {
        return mImService.addContact(userID)
//                .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseListResponseMapper<String>());
    }

    /**
     * 同意好友申请
     */
    public Observable<List<String>> acceptRequest(String otherUserID) {
        return mImService.acceptRequest(otherUserID)
//                .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseListResponseMapper<String>());
    }

    /**
     * 拒绝好友申请
     */
    public Observable<List<String>> refuseRequest(String otherUserID) {
        return mImService.refuseRequest(otherUserID)
//                .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .flatMap(new BaseListResponseMapper<String>());
    }

}
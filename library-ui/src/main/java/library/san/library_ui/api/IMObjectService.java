package library.san.library_ui.api;

import library.san.library_ui.entity.ChatRecord;
import library.san.library_ui.entity.Contact;
import library.san.library_ui.entity.GroupChatRecord;
import library.san.library_ui.entity.GroupContact;
import library.san.library_ui.entity.GroupDetails;
import library.san.library_ui.entity.GroupMember;
import com.lib_im.pro.retrofit.base.BaseObjectResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by songgx on 2017/9/26.
 * 聊天模块请求接口服务
 */

public interface IMObjectService {

    //查询群组列表
    @GET("chat/getGroup")
    Observable<BaseObjectResponse<GroupContact>> queryGroupList(
            @Query("userID") String userID);

    //添加成员
    @GET("")
    Observable<BaseObjectResponse<String>> addMember(
            @Query("otherUserID") String otherUserID,
            @Query("groupID") String groupID);

    //删除成员
    @GET("")
    Observable<BaseObjectResponse<String>> removeMember(
            @Query("otherUserID") String otherUserID,
            @Query("groupID") String groupID);

    //添加联系人
    @GET("")
    Observable<BaseObjectResponse<String>> addContact(
            @Query("userID") String userID);
    //删除联系人
    @GET("")
    Observable<BaseObjectResponse<String>> removeContact(
            @Query("userID") String userID);
    //同意
    @GET("")
    Observable<BaseObjectResponse<String>> acceptRequest(
            @Query("otherUserID") String otherUserID);
    //拒绝
    @GET("")
    Observable<BaseObjectResponse<String>> refuseRequest(
            @Query("otherUserID") String otherUserID);

    //查询群组未读消息记录数量
    @GET("chat/queryCount")
    Observable<BaseObjectResponse<GroupChatRecord>> queryGroupUnReadCount(
            @Query("list") String groupStr);

    //查询群组聊天历史纪录列表
    @GET("chat/getGroupLogs")
    Observable<BaseObjectResponse<GroupChatRecord>> queryGroupChatRecord(
            @Query("groupId") String groupId,
            @Query("rows") String rows,
            @Query("logId") String logId,
            @Query("messageId") String messageId);

    //查询担任聊天历史纪录列表
    @GET("chat/getChatLogs")
    Observable<BaseObjectResponse<ChatRecord>> queryChatRecord(
            @Query("messageId") String messageId,
            @Query("fromUserId") String fromUserId,
            @Query("toUserId") String toUserId,
            @Query("rows") String rows);

    //查询群成员列表
    @GET("chat/getMember")
    Observable<BaseObjectResponse<GroupMember>> queryGroupMember(
            @Query("ID") String ID,
            @Query("page") String page,
            @Query("rows") String rows);

    //查询群详情
    @GET("chat/getGroupDetails")
    Observable<BaseObjectResponse<GroupDetails>> queryGroupDetails(
            @Query("ID") String ID);

    //获取好友列表
    @GET("")
    Observable<BaseObjectResponse<Contact>> queryFriendList();

    //解散群组
    @GET("")
    Observable<BaseObjectResponse<String>> dismissGroup(
            @Query("groupID") String groupID);
    //退出群组
    @GET("")
    Observable<BaseObjectResponse<String>> exitGroup(
            @Query("groupID") String groupID);
    //模糊查找联系人
    @GET("")
    Observable<BaseObjectResponse<Contact>> searchFriendList(
            @Query("key") String key);
}

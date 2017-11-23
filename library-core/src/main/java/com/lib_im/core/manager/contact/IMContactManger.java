package com.lib_im.core.manager.contact;

import android.content.Context;
import android.util.Log;

import com.lib_im.pro.R;
import com.lib_im.core.config.ChatCode;
import com.lib_im.pro.im.listener.IMContactListener;
import com.lib_im.pro.im.listener.OnLoadListener;
import com.lib_im.pro.rx.SimpleListCompleteObserver;
import com.lib_im.pro.rx.SimpleListObserver;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.NonNull;
import library.san.library_ui.db.BaseDao;
import library.san.library_ui.db.DataBaseHelper;
import library.san.library_ui.entity.Contact;

/**
 * 通讯录管理器
 * Created by songgx on 16/6/15.
 */
public class IMContactManger implements ContactManager<Contact> {


    private String TAG = "IMFriendManger";

    private String VERSION = "1.0.1";
    private Context context;

    private String account;

    public IMContactManger(Context context) {
        this.context = context;
        Log.i(TAG, "Version--> " + VERSION);
       mDao=new BaseDao<>(DataBaseHelper.getInstance(context),Contact.class);
    }

    private List<IMContactListener> mIMContactListeners = new ArrayList<>();


    private List<Contact> mContacts = new ArrayList<>();

    private BaseDao<Contact> mDao;

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
        account=uid;
    }

    /**
     * 加载所有通讯录
     *
     * @param contactListener
     */
    @Override
    public void loadContact(final IMContactListener contactListener) {
        final String userId = LiteChat.chatClient.getConfig(ChatCode.KEY_USER_ID);
        LiteChat.imRequestManager.getListInstance().queryFriendList("1").subscribe(new SimpleListObserver<Contact>() {
            @Override
            public void onNext(@NonNull List<Contact> contacts) {
                mContacts.clear();
                if (contacts != null) {
                    for (Contact contact : contacts) {
                        if (contact != null) {
                            contact.setAccount(userId);
                            Contact tableContact = getContact(contact.getChatUserid());
                            if (tableContact != null) {
                                mDao.delete(tableContact);
                            }
                            mDao.add(contact);
                        }
                    }
                    mContacts.addAll(contacts);
                    contactListener.onContactUpdate(mContacts);
                } else {
                    contactListener.onContactError(context.getString(R.string.has_no_data));
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                contactListener.onContactError(e.getMessage());
            }
        });

    }




    /**
     * 删除好友 
     *
     * @param otherUserID
     * @param contactListener
     */
    @Override
    public void removeContact(String otherUserID, final IMContactListener contactListener) {
      LiteChat.imRequestManager.getListInstance().removeContact(otherUserID).subscribe(new SimpleListCompleteObserver<String>() {
          @Override
          public void onError(@NonNull Throwable e) {
              contactListener.onContactError(e.getMessage());
          }

          @Override
          public void onComplete() {
              contactListener.onContactDeleted(context.getString(R.string.delete_failed));
          }
      });
    }





    /**
     * 添加好友
     *
     * @param otherUserID
     * @param contactListener
     */
    @Override
    public void addContact(String otherUserID, final IMContactListener contactListener) {
     LiteChat.imRequestManager.getListInstance().addContact(otherUserID).subscribe(new SimpleListCompleteObserver<String>() {
         @Override
         public void onError(@NonNull Throwable e) {
             contactListener.onContactError(e.getMessage());
         }

         @Override
         public void onComplete() {
             contactListener.onContactAdded(context.getString(R.string.add_request_success));
         }
     });
    }

    /**
     * 搜索好友
     *
     * @param key
     * @param onLoadListener
     */
    @Override
    public void searchContact(String key, final OnLoadListener onLoadListener) {
     LiteChat.imRequestManager.getListInstance().searchFriendList(key).subscribe(new SimpleListObserver<Contact>() {
         @Override
         public void onNext(@NonNull List<Contact> contacts) {
             onLoadListener.onLoadSuccess(contacts);
         }

         @Override
         public void onError(@NonNull Throwable e) {
             onLoadListener.onLoadFailed(e.getMessage());
         }
     });
    }





    /**
     * 查询指定通讯录内容
     *
     * @param chatUserId
     * @return
     */
    @Override
    public Contact getContact(String chatUserId) {
        //TODO 从数据库获取数据
        List<Contact> _list = mDao.queryByColumn("account", account, "chatUserid", chatUserId);
        Contact _contact = null;
        if (_list != null) {
            for (Contact contact : _list) {
                if (contact != null) {
                    _contact = contact;
                }
            }
        }
        return _contact;
    }

    /**
     * 同意好友请求
     *
     * @param otherUserID
     * @param onLoadListener
     */
    @Override
    public void acceptFriendInvitation(String otherUserID, final OnLoadListener onLoadListener) {
      //TODO 同意好友请求，具体逻辑根据业务而定
     LiteChat.imRequestManager.getListInstance().acceptRequest(otherUserID).subscribe(new SimpleListCompleteObserver<String>() {
         @Override
         public void onError(@NonNull Throwable e) {
             onLoadListener.onLoadFailed(e.getMessage());
         }

         @Override
         public void onComplete() {
             onLoadListener.onLoadSuccess(context.getString(R.string.handle_success));
         }
     });

    }



    /**
     * 拒绝好友请求
     *
     * @param otherUserID
     * @param onLoadListener
     */
    @Override
    public void refuseFriendInvitation(String otherUserID, final OnLoadListener onLoadListener) {
        //TODO 拒绝好友请求，具体逻辑根据业务而定
        LiteChat.imRequestManager.getListInstance().refuseRequest(otherUserID).subscribe(new SimpleListCompleteObserver<String>() {
            @Override
            public void onError(@NonNull Throwable e) {
                onLoadListener.onLoadFailed(e.getMessage());
            }

            @Override
            public void onComplete() {
                onLoadListener.onLoadSuccess(context.getString(R.string.handle_success));
            }
        });
    }



    /**
     * 监听联系人
     *
     * @param IMContactListener
     */
    @Override
    public void addContactListener(IMContactListener IMContactListener) {
        if (mIMContactListeners.indexOf(IMContactListener) == -1) {
            mIMContactListeners.add(IMContactListener);
        }
    }

    /**
     * 删除监听
     *
     * @param IMContactListener
     */
    @Override
    public void removeContactListener(IMContactListener IMContactListener) {
        if (mIMContactListeners.indexOf(IMContactListener) != -1) {
            mIMContactListeners.remove(IMContactListener);
        }
    }

}


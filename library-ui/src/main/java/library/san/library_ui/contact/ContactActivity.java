package library.san.library_ui.contact;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lib_im.pro.R;
import library.san.library_ui.entity.Contact;
import com.lib_im.core.manager.message.conversation.IMContactListener;
import com.lib_im.pro.im.manager.contact.ContactManager;
import library.san.library_ui.utils.LogUtils;
import library.san.library_ui.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import library.san.library_ui.base.BaseActivity;
import library.san.library_ui.chat.ChatActivity;
import library.san.library_ui.widget.recyler.BaseRecyclerAdapter;
import library.san.library_ui.widget.recyler.LViewHolder;
import library.san.library_ui.widget.swipe.LSwipeRefreshLayout;
import library.san.library_ui.widget.swipe.SwipeView;

public class ContactActivity extends BaseActivity implements
        BaseRecyclerAdapter.OnItemClickListener, IMContactListener<Contact>,View.OnClickListener{

    RecyclerView mRecycler;
    private BaseRecyclerAdapter<Contact> mAdapter;
    private LSwipeRefreshLayout mRefresh;
    ContactManager contactManager ;
    ContactViewHandle contactViewHandle;
    private List<Contact> contacts=new ArrayList<>();
    private ImageView backImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_contact);
        LogUtils.e("-----------------------------ContactActivity");
        initView();
        initData();
    }



    private void initView() {
        backImage= (ImageView) findViewById(R.id.im_contact_title_back_image);
        backImage.setOnClickListener(this);
        mRefresh = (LSwipeRefreshLayout) findViewById(R.id.contact_refresh_layout);
        mRecycler = (RecyclerView) findViewById(R.id.im_contact_listView);
        LinearLayoutManager mManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecycler.setLayoutManager(mManager);
        mRecycler.setHasFixedSize(true);
        mRefresh.setOnLoadMoreListener(new SwipeView.OnRequestListener() {
            @Override
            public void onRequest() {
                //上拉加载
            }
        });
        mRefresh.setOnRefreshListener(new SwipeView.OnRequestListener() {
            @Override
            public void onRequest() {
                //下拉刷新
                contactManager.loadContact(ContactActivity.this);
            }
        });

    }

    private void initData() {
        contactManager= LiteChat.chatClient.getContactManager();
        contactManager.addContactListener(this);
        contactViewHandle=new ContactViewHandle(this);
        contactManager.loadContact(ContactActivity.this);
    }

    private void showContact(final List<Contact> contacts) {
        if (mAdapter == null) {
            mAdapter = new BaseRecyclerAdapter<Contact>(this, contacts, R.layout.item_contact) {
                @Override
                public void onBindHolder(LViewHolder holder, int position) {
                    contactViewHandle.setView(position, contacts, holder);
                }
            };
            mRecycler.setAdapter(mAdapter);
        } else {
            mRefresh.stopRefresh(true);
            mAdapter.notifyDataSetChanged();
        }
        mAdapter.setOnItemClickListener(this);
    }


    @Override
    public void onItemClick(ViewGroup parent, View v, int position) {
        //TODO 根据业务进行处理
        Contact contact=contacts.get(position);
        String fromId = contact.getChatUserid();
        String name = contact.getNickname();
        Intent intent=new Intent(this, ChatActivity.class);
        intent.putExtra("chatUserId", fromId);
        intent.putExtra("chatUserName", name);
        intent.putExtra("groupChat", Boolean.FALSE);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LiteChat.chatClient.getContactManager().removeContactListener(this);
    }

    /**
     * 添加联系人回调方法
     */
    @Override
    public void onContactAdded(String actionID) {
        contactManager.loadContact(this);
    }

    /**
     * @descript 删除联系人回调方法
     */
    @Override
    public void onContactDeleted(String actionID) {
        contactManager.loadContact(this);
    }

    /**
     * @param list
     * @descript 更新联系人回调方法
     */
    @Override
    public void onContactUpdate(final List<Contact> list) {
        runOnUiThread(new Thread(){
            @Override
            public void run() {
                super.run();
                if (list != null) {
                    contacts.clear();
                    contacts.addAll(list);
                    showContact(contacts);
                }

            }
        });

    }

    @Override
    public void onContactError(String msg) {
        runOnUiThread(new Thread(){
            @Override
            public void run() {
                super.run();
                ToastUtils.showShortToast(getString(R.string.load_failed));
            }
        });
    }

    @Override
    public void onClick(View view) {
        finish();
    }
}

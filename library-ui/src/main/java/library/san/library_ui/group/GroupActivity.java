package library.san.library_ui.group;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.lib_im.pro.R;
import library.san.library_ui.entity.GroupContact;
import com.lib_im.pro.im.listener.OnLoadListener;
import com.lib_im.pro.im.manager.group.GroupContactManager;
import library.san.library_ui.utils.DividerDrawable;
import library.san.library_ui.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import library.san.library_ui.base.BaseActivity;
import library.san.library_ui.chat.ChatActivity;
import library.san.library_ui.widget.recyler.BaseRecyclerAdapter;
import library.san.library_ui.widget.recyler.ItemDecorations;
import library.san.library_ui.widget.recyler.LViewHolder;

/**
 * A simple {@link Fragment} subclass.
 */
@Route(path = GroupActivity.ROUTE_PATH)
public class GroupActivity extends BaseActivity implements
        BaseRecyclerAdapter.OnItemClickListener,View.OnClickListener,OnLoadListener {
    public static final String ROUTE_PATH = "/lib_im/group";
    private RecyclerView mRecycler;
    private BaseRecyclerAdapter<GroupContact> adapter;
    private GroupViewHandle mGroupViewHandle;
    GroupContactManager<GroupContact> groupContactManager;
    List<GroupContact> groupContactList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_group);
        initView();
        initData();
    }

    private void initView() {
        mRecycler = ((RecyclerView) findViewById(R.id.im_group_listView));
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        DividerDrawable dividerDrawable = new DividerDrawable(
                getResources().getDimension(R.dimen.divider_height));
        mRecycler.addItemDecoration(ItemDecorations.vertical(GroupActivity.this)
                                                   .type(0, dividerDrawable)
                                                   .create());
        ImageView backImage= (ImageView) findViewById(R.id.im_group_title_back_image);
        backImage.setOnClickListener(this);
    }

    /**
     * 展示数据
     * @param groups
     */
    public void showGroups(List<GroupContact> groups) {
        if (adapter == null) {
            adapter = new BaseRecyclerAdapter<GroupContact>(this, groups, R.layout.item_group) {
                @Override
                public void onBindHolder(LViewHolder holder, int position) {
                  mGroupViewHandle.setView(position,holder,adapter);
                }
            };
            mRecycler.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
        adapter.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(ViewGroup parent, View v, int position) {
        Intent intent = new Intent(this, ChatActivity.class);
        GroupContact groupContact = groupContactList.get(position);
        if (groupContact != null) {
            intent.putExtra("chatUserId", groupContact.getGroupID());
            intent.putExtra("chatUserName", groupContact.getGroupName());
            intent.putExtra("groupChat", Boolean.TRUE);
            intent.putExtra("chatRoomJid", groupContact.getGroupJid());
            startActivity(intent);
        }
    }


    public void initData() {
        groupContactManager = LiteChat.chatClient.getGroupContactManager();
        groupContactManager.addLoadListener(this);
        groupContactManager.loadGroupContact("1",this);
        mGroupViewHandle = new GroupViewHandle(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (groupContactManager != null) {
            groupContactManager.removeLoadListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.im_group_title_back_image) {
            finish();
        }
    }

    @Override
    public void onLoadSuccess(final Object o) {
        runOnUiThread(new Thread(){
            @Override
            public void run() {
                super.run();
                List<GroupContact> list= (List<GroupContact>) o;
                groupContactList.clear();
                groupContactList.addAll(list);
                showGroups(list);
            }
        });
    }

    @Override
    public void onLoadFailed(final String msg) {
       runOnUiThread(new Thread(){
           @Override
           public void run() {
               super.run();
               ToastUtils.showShortToast(msg);
           }
       });
    }
}

package library.san.library_ui.group;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.lib_im.pro.R;
import library.san.library_ui.entity.GroupMember;

import com.lib_im.pro.im.manager.group.GroupContactManager;
import library.san.library_ui.utils.DividerDrawable;
import library.san.library_ui.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import library.san.library_ui.base.BaseActivity;
import library.san.library_ui.widget.recyler.BaseRecyclerAdapter;
import library.san.library_ui.widget.recyler.ItemDecorations;
import library.san.library_ui.widget.recyler.LViewHolder;
import library.san.library_ui.widget.swipe.LSwipeRefreshLayout;
import library.san.library_ui.widget.swipe.NormalFooter;
import library.san.library_ui.widget.swipe.ParallaxFooter;
import library.san.library_ui.widget.swipe.ParallaxHeader;
import library.san.library_ui.widget.swipe.SwipeView;

/**
 * Created by songgx on 2017/8/14.
 * 群成员页面
 */
@Route(path = GroupMemberActivity.ROUTE_PATH)
public class GroupMemberActivity extends BaseActivity implements View.OnClickListener,
         BaseRecyclerAdapter.OnItemClickListener,OnLoadListener,IMGroupListener,IMContactListener{

    public static final String ROUTE_PATH = "/lib_im/groupMember";
    public static final String KEY_TYPE = "key_type";
    public static final String KEY_TYPE_DEL_MEMBER = "key_type_del_member";
    public static final String KEY_TYPE_ADD_MEMBER = "key_type_add_member";
    public static final String KEY_TYPE_VIEW_MEMBER = "key_type_view_member";
    public static final String GROUP_ID = "group_id";
    public static final int ADD_MEMBER_RESULT_CODE = 222222;
    public static final int DEL_MEMBER_RESULT_CODE = 222232;
    public String groupID;
    private RecyclerView recyclerView;
    private BaseRecyclerAdapter<GroupMember> baseRecyclerAdapter;
    public static String keyType;
    private List<GroupMember> seclectList = new ArrayList<>();
    private List<String> idList = new ArrayList<>();
    TextView submitText;
    TextView titleText;
    public int index = 0;
    public static GroupMemberActivity INSTANCE;
    public LSwipeRefreshLayout lSwipeRefreshLayout;
    GroupContactManager groupContactManager;
    private ArrayList<GroupMember> memberList=new ArrayList<>();
    private GroupViewHandle groupViewHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_member);
        INSTANCE = this;
        initView();
        init();
    }

    private void init() {
        groupContactManager= LiteChat.chatClient.getGroupContactManager();
        groupContactManager.addLoadListener(this);
        groupContactManager.addGroupUserListener(this);
        groupViewHandle=new GroupViewHandle(this);
        //TODO 添加好友的接口
        seclectList.clear();
        Intent intent = getIntent();
        if (intent != null) {
            keyType = intent.getExtras().getString(KEY_TYPE);
            groupID = intent.getExtras().getString(GROUP_ID);
        }
        switch (keyType) {
            case KEY_TYPE_VIEW_MEMBER:
                submitText.setVisibility(View.GONE);
                index = 1;
                groupContactManager.getGroupMemberList(groupID,String.valueOf(index), "10",this);
                break;
            case KEY_TYPE_ADD_MEMBER:
                titleText.setText("添加成员");
                submitText.setVisibility(View.VISIBLE);
                //TODO 获取好友
                LiteChat.chatClient.getContactManager().loadContact(this);
                break;
            case KEY_TYPE_DEL_MEMBER:
                titleText.setText("删除成员");
                submitText.setVisibility(View.VISIBLE);
                index = 1;
                groupContactManager.getGroupMemberList(groupID,String.valueOf(index), "10",this);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (groupContactManager != null) {
            groupContactManager.removeGroupUserListener(this);
            groupContactManager.removeLoadListener(this);
        }

    }

    private void initView() {
        ImageView imageView = (ImageView) findViewById(R.id.group_member_back_image);
        recyclerView = (RecyclerView) findViewById(R.id.group_member_recyclerView);
        imageView.setOnClickListener(this);
        submitText = (TextView) findViewById(R.id.group_member_title_submit);
        submitText.setOnClickListener(this);
        titleText = (TextView) findViewById(R.id.group_member_title_text);
        lSwipeRefreshLayout = (LSwipeRefreshLayout) findViewById(R.id.group_member_refresh_layout);

        if (KEY_TYPE_ADD_MEMBER.equals(getIntent().getExtras().getString(KEY_TYPE))) {
            lSwipeRefreshLayout.setHeader(new ParallaxHeader(this));
            lSwipeRefreshLayout.setFooter(new ParallaxFooter(this));
        } else {
            lSwipeRefreshLayout.setFooter(new NormalFooter(this));
        }
        lSwipeRefreshLayout.setOnLoadMoreListener(new SwipeView.OnRequestListener() {
            @Override
            public void onRequest() {
                index++;
                groupContactManager.getGroupMemberList(groupID,String.valueOf(index), "10",
                        GroupMemberActivity.this);
                lSwipeRefreshLayout.stopLoadMore(true);
            }
        });
        lSwipeRefreshLayout.setOnRefreshListener(new SwipeView.OnRequestListener() {
            @Override
            public void onRequest() {
                index = 1;
                groupContactManager.getGroupMemberList(groupID,String.valueOf(index), "10",
                        GroupMemberActivity.this);
                lSwipeRefreshLayout.stopRefresh(true);
            }
        });

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.group_member_back_image) {
            finish();
        } else if (id == R.id.group_member_title_submit) {
            submitAction();
        } else {
            //封口操作
        }
    }

    //确定按钮操作
    private void submitAction() {
        if (!seclectList.isEmpty()) {
            for (GroupMember groupMember : seclectList) {
                if (groupMember != null) {
                    String memberID = groupMember.getMemberID();
                    idList.add(memberID);
                }
            }
            switch (keyType) {
                case KEY_TYPE_ADD_MEMBER:
                    groupContactManager.addUsersToGroup("", groupID, this);
                    break;
                case KEY_TYPE_DEL_MEMBER:
                    groupContactManager.removeUserFromGroup("", groupID, this);
                    break;
                default:
            }

        }
    }

    /**
     * 展示成员
     * @param members
     */
    public void showMembers(List<GroupMember> members) {
        if (baseRecyclerAdapter == null) {
            baseRecyclerAdapter = new BaseRecyclerAdapter<GroupMember>(this, members,
                    R.layout.item_group_member) {
                @Override
                public void onBindHolder(LViewHolder holder, int position) {
                    groupViewHandle.setMemberView(holder, position,baseRecyclerAdapter);
                }
            };
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            DividerDrawable dividerDrawable = new DividerDrawable(
                    getResources().getDimension(R.dimen.divider_height));
            recyclerView.addItemDecoration(ItemDecorations.vertical(this)
                                                          .type(0, dividerDrawable)
                                                          .create());
            recyclerView.setAdapter(baseRecyclerAdapter);
            baseRecyclerAdapter.setOnItemClickListener(this);
        } else {
            baseRecyclerAdapter.setData(members);
            baseRecyclerAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 选择群组成员
     */
    private void setSelect(GroupMember member) {
        boolean check = member.isCheck();
        if (check) {
            member.setCheck(false);
            seclectList.remove(member);
        } else {
            member.setCheck(true);
            seclectList.add(member);
        }
        //刷新适配器操作
        baseRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(ViewGroup parent, View v, int position) {
        GroupMember groupMember = baseRecyclerAdapter.getItem(position);
        if (groupMember != null) {
            setSelect(groupMember);
        }
    }

    @Override
    public void onLoadSuccess(final Object o) {
        runOnUiThread(new Thread(){
            @Override
            public void run() {
                super.run();
                List<GroupMember> list= (List<GroupMember>) o;
                if (index == 1) {
                    memberList.clear();
                }
                if (memberList.size() == 10) {
                    lSwipeRefreshLayout.setCanLoadMore(true);
                }
                if (memberList.size() < 10) {
                    lSwipeRefreshLayout.setCanLoadMore(false);
                }
                memberList.addAll(list);
                showMembers(memberList);
            }
        });

    }

    @Override
    public void onLoadFailed(String msg) {
         runOnUiThread(new Thread(){
             @Override
             public void run() {
                 super.run();
                 ToastUtils.showShortToast(getString(R.string.load_failed));
             }
         });
    }

    @Override
    public void OnAddUserToGroup(String msg) {
        runOnUiThread(new Thread(){
            @Override
            public void run() {
                super.run();
                final Intent intent = new Intent();
                ToastUtils.showShortToast("添加成功");
                setResult(ADD_MEMBER_RESULT_CODE, intent);
                finish();
            }
        });

    }

    @Override
    public void OnDeleteUserFromGroup(String msg) {
        runOnUiThread(new Thread(){
            @Override
            public void run() {
                super.run();
                final Intent intent = new Intent();
                ToastUtils.showShortToast("删除成功");
                setResult(DEL_MEMBER_RESULT_CODE, intent);
                finish();
            }
        });

    }

    @Override
    public void OnHandleGroupMemberError(String msg) {
     runOnUiThread(new Thread(){
         @Override
         public void run() {
             super.run();
          ToastUtils.showShortToast(getString(R.string.handle_failed));
         }
     });
    }

    @Override
    public void onContactAdded(String actionID) {
        //TODO 不做处理
    }

    @Override
    public void onContactDeleted(String actionID) {
       //TODO 不做处理
    }

    @Override
    public void onContactUpdate(List list) {
      //TODO 不做处理
    }

    @Override
    public void onContactError(String msg) {
     //TODO 不做处理
    }
}

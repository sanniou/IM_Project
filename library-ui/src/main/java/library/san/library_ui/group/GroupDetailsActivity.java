package library.san.library_ui.group;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.lib_im.pro.R;
import library.san.library_ui.entity.GroupDetails;
import library.san.library_ui.entity.GroupMember;
import com.lib_im.pro.im.config.ChatCode;
import com.lib_im.pro.im.listener.HandleGroupListener;
import com.lib_im.pro.im.listener.IMGroupListener;
import com.lib_im.pro.im.listener.OnLoadListener;
import com.lib_im.pro.im.manager.group.GroupContactManager;
import library.san.library_ui.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import library.san.library_ui.base.PermissionActivity;
import library.san.library_ui.widget.recyler.BaseRecyclerAdapter;
import library.san.library_ui.widget.recyler.LViewHolder;

import static com.lib_im.pro.ui.group.GroupViewHandle.ADD_MEMBER;
import static com.lib_im.pro.ui.group.GroupViewHandle.DEL_MEMBER;

/**
 * Created by songgx on 2017/8/14.
 * 群组详情页面
 */
@Route(path = GroupDetailsActivity.ROTH_PATH)
public class GroupDetailsActivity extends PermissionActivity implements View.OnClickListener,
        BaseRecyclerAdapter.OnItemClickListener,OnLoadListener,IMGroupListener,HandleGroupListener{
    public static final String ROTH_PATH="/group/groupDetails";
    private static final int DEL_MEMBER_REQUEST_CODE = 203;
    private static final int ADD_MEMBER_REQUEST_CODE = 208;
    private TextView nameText;
    private TextView noticeText;
    private TextView countText;
    private RecyclerView recyclerView;
    private String groupID;
    private List<GroupMember> list=new ArrayList<>();
    private BaseRecyclerAdapter<GroupMember> adapter;
    private List<String> ownIDList=new ArrayList<>();
    Button button;
    public static final String QUIT_GROUP="quit_group";
    public static final String DISMISS_GROUP="dismiss_group";
    public static final int FINISH_CHAT_RESULT_CODE=999;
    public static String BTN_ACTION;
    private ProgressDialog progressDialog;
    GroupContactManager groupContactManager;
    GroupViewHandle groupViewHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        initView();
        init();

    }

    private void init() {
        list.clear();
        Intent intent=getIntent();
        if (intent != null) {
            groupID=intent.getExtras().getString(GroupMemberActivity.GROUP_ID);
        }
        groupContactManager = LiteChat.chatClient.getGroupContactManager();
        groupContactManager.addLoadListener(this);
        groupContactManager.addGroupUserListener(this);
        groupContactManager.addHandleGroupListener(this);
        groupViewHandle=new GroupViewHandle(this);
        groupContactManager.queryGroupDetails(groupID,this);
    }

    private void initView() {
        ImageView backImage = (ImageView) findViewById(R.id.im_group_details_back_image);
        nameText= (TextView) findViewById(R.id.im_group_details_name_text);
        noticeText= (TextView) findViewById(R.id.im_group_details_notice_content);
        countText= (TextView) findViewById(R.id.im_group_details_group_count_text);
        recyclerView= (RecyclerView) findViewById(R.id.im_group_details_recyclerView);
        TextView moreText = (TextView) findViewById(R.id.im_group_details_view_more_text);
        button = (Button) findViewById(R.id.im_group_details_btn);
        backImage.setOnClickListener(this);
        button.setOnClickListener(this);
        moreText.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (groupContactManager != null) {
            groupContactManager.removeLoadListener(this);
            groupContactManager.removeGroupUserListener(this);
            groupContactManager.removeHandleGroupListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.im_group_details_back_image) {
            finish();
        } else if (id == R.id.im_group_details_view_more_text) {
            ARouter.getInstance().build("/lib_im/groupMember")
                    .withString(GroupMemberActivity.GROUP_ID, groupID)
                    .withString(GroupMemberActivity.KEY_TYPE, GroupMemberActivity.KEY_TYPE_VIEW_MEMBER)
                    .navigation();
        } else if (id == R.id.im_group_details_btn) {
            String userID=LiteChat.chatCache.readString(ChatCode.KEY_USER_ID);
            progressDialog=ProgressDialog.show(this,"","正在操作",false,false);
            switch (BTN_ACTION) {
                case QUIT_GROUP://退出群组
                    groupContactManager.exitGroup(groupID,this);
                    break;
                case DISMISS_GROUP://解散群组
                    groupContactManager.dismissGroup(groupID,this);
                    break;
            }
        } else {
            //封口操作
        }

    }

    /**
     * 展示详情内容
     * @param groupDetails
     */
    public void showGroupDetails(List<GroupDetails> groupDetails) {
        final GroupDetails group = groupDetails.get(0);
        if (group != null) {
            //设置群名称
            String groupName = group.getGroupName();
            nameText.setText(groupName);
            //设置群公告
            String groupNotice = group.getGroupNotice();
            noticeText.setText(groupNotice);
            //管理员id列表
            List<String> managerIdList = group.getGroupManagement();
            if (managerIdList != null) {
                ownIDList.addAll(managerIdList);
            }
            //群成员列表
            List<GroupMember> groupMembers = group.getGroupMembers();
            //群成员数量
            int size = groupMembers.size();
            countText.setText("群成员(" + size + "人)");
            //匹配当前用户是否存在于管理员id列表当中，如果存在则设置添加，删除成员，解散群组功能
            String userID = LiteChat.chatCache.readString(ChatCode.KEY_USER_ID);
            for (String str : ownIDList) {
                if (str.equals(userID)) {
                    button.setText("解散组织");
                    BTN_ACTION = DISMISS_GROUP;
                } else {
                    button.setText("退出组织");
                    BTN_ACTION = QUIT_GROUP;
                }
            }
            //添加加减号按钮
            GroupMember groupMember = new GroupMember();
            groupMember.setMemberIcon(ADD_MEMBER);
            groupMembers.add(groupMember);
            GroupMember groupMember1 = new GroupMember();
            groupMember1.setMemberIcon(DEL_MEMBER);
            groupMembers.add(groupMember1);
            //绑定适配器
            list.clear();
            list.addAll(groupMembers);
            setListAdapter(list);
        }
    }

    /**
     * 绑定适配器操作
     * */
    private void setListAdapter(List<GroupMember> list) {
        if (adapter == null) {
            adapter = new BaseRecyclerAdapter<GroupMember>(this, list, R.layout.item_group_details_member) {
                @Override
                public void onBindHolder(LViewHolder holder, int position) {
                    groupViewHandle.setDetailsView(holder, position,adapter);
                }
            };
            recyclerView.setLayoutManager(new GridLayoutManager(
                    GroupDetailsActivity.this, 5));
            recyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener(this);
        } else {
            adapter.setData(list);
            adapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onItemClick(ViewGroup parent, View v, int position) {
        GroupMember groupMember=adapter.getItem(position);
        if (groupMember != null) {
            String memberIcon = groupMember.getMemberIcon();
            if (memberIcon != null) {
                if (memberIcon.equals(ADD_MEMBER)) {
                    ARouter.getInstance().build("/lib_im/groupMember")
                            .withString(GroupMemberActivity.GROUP_ID, groupID)
                            .withString(GroupMemberActivity.KEY_TYPE, GroupMemberActivity.KEY_TYPE_ADD_MEMBER)
                            .navigation(this,ADD_MEMBER_REQUEST_CODE);
                } else if (memberIcon.equals(DEL_MEMBER)) {
                    ARouter.getInstance().build("/lib_im/groupMember")
                            .withString(GroupMemberActivity.GROUP_ID, groupID)
                            .withString(GroupMemberActivity.KEY_TYPE, GroupMemberActivity.KEY_TYPE_DEL_MEMBER)
                            .navigation(this,DEL_MEMBER_REQUEST_CODE);
                } else {
                    //不做任何操作
                }

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_MEMBER_REQUEST_CODE && resultCode == GroupMemberActivity.ADD_MEMBER_RESULT_CODE) {
            groupContactManager.queryGroupDetails(groupID,this);
        } else if (requestCode == DEL_MEMBER_REQUEST_CODE && resultCode == GroupMemberActivity.DEL_MEMBER_RESULT_CODE) {
            groupContactManager.queryGroupDetails(groupID,this);
        } else {
            //封口操作
        }
    }

    @Override
    public void onLoadSuccess(final Object o) {
        runOnUiThread(new Thread(){
            @Override
            public void run() {
                super.run();
                List<GroupDetails> list= (List<GroupDetails>) o;
                showGroupDetails(list);
            }
        });

    }

    @Override
    public void onLoadFailed(String msg) {
      runOnUiThread(new Thread(){
          @Override
          public void run() {
              super.run();
              ToastUtils.showShortToast(R.string.load_failed);
          }
      });
    }

    @Override
    public void OnAddUserToGroup(String msg) {

    }

    @Override
    public void OnDeleteUserFromGroup(String msg) {

    }

    @Override
    public void OnHandleGroupMemberError(String msg) {

    }

    @Override
    public void exitGroup() {
        runOnUiThread(new Thread(){
            @Override
            public void run() {
                super.run();
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                ToastUtils.showShortToast(getString(R.string.handle_success));
                Intent intent=new Intent();
                setResult(FINISH_CHAT_RESULT_CODE,intent);
                finish();
            }
        });
    }

    @Override
    public void dismissGroup() {
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                super.run();
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                ToastUtils.showShortToast("解散成功");
                Intent intent = new Intent();
                setResult(FINISH_CHAT_RESULT_CODE, intent);
                finish();
            }
        });

    }

    @Override
    public void handleGroupError(String msg) {
        runOnUiThread(new Thread(){
            @Override
            public void run() {
                super.run();
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                ToastUtils.showShortToast(getString(R.string.handle_failed));
            }
        });
    }
}

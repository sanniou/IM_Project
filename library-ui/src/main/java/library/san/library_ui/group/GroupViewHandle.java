package library.san.library_ui.group;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lib_im.pro.R;
import library.san.library_ui.entity.GroupContact;
import library.san.library_ui.entity.GroupMember;
import library.san.library_ui.utils.ImageLoader;

import library.san.library_ui.widget.recyler.BaseRecyclerAdapter;
import library.san.library_ui.widget.recyler.LViewHolder;
import library.san.library_ui.widget.view.CircleImageView;

import static com.lib_im.pro.ui.group.GroupMemberActivity.KEY_TYPE_VIEW_MEMBER;
import static com.lib_im.pro.ui.group.GroupMemberActivity.keyType;

/**
 * Created by songgx on 2017/1/22.
 * 群组模块view设置
 */

public class GroupViewHandle {

    public static final String ADD_MEMBER = "add_member";
    public static final String DEL_MEMBER = "del_member";
    private Context mContext;
    public GroupViewHandle(Context context) {
        this.mContext=context;
    }

    /**
    * @descript adapter 相关view设置
    *
    * @param position
    *
    * @param holder
    *
    * @param adapter
    *
    */
    void setView(int position, LViewHolder holder, BaseRecyclerAdapter<GroupContact> adapter) {
        GroupContact bean = adapter.getItem(position);
        CircleImageView head = holder.getView(R.id.group_head);
        TextView name = holder.getView(R.id.group_name);
        String groupHead = bean.getGroupHead();
        ImageLoader.load(head.getContext(),head,groupHead,ImageLoader.getOrgOption());
        name.setText(bean.getGroupName());
    }

    /**
     * 详情页面设置
     * @param holder
     * @param position
     * @param adapter
     */
    public void setDetailsView(LViewHolder holder, int position, BaseRecyclerAdapter<GroupMember> adapter) {
        CircleImageView circleImageView = holder.getView(R.id.group_details_member_head);
        GroupMember groupMember = adapter.getItem(position);
        if (groupMember != null) {
            String memberIcon = groupMember.getMemberIcon();
            if (memberIcon != null) {
                if (memberIcon.equals(ADD_MEMBER)) {
                    circleImageView.setImageResource(R.mipmap.add_group_people);
                } else if (memberIcon.equals(DEL_MEMBER)) {
                    circleImageView.setImageResource(R.mipmap.del_group_people);
                } else {
                    ImageLoader.load(mContext, circleImageView, memberIcon, ImageLoader.getHeadOption());
                }
            } else {
                ImageLoader.load(mContext, circleImageView, memberIcon, ImageLoader.getHeadOption());
            }
        }
    }

    /**
     * 群成员适配器设置布局
     */
    public void setMemberView(LViewHolder holder, int position, BaseRecyclerAdapter<GroupMember> baseRecyclerAdapter) {
        CircleImageView circleImageView = holder.getView(R.id.group_member_head);
        TextView nameText = holder.getView(R.id.group_member_name);
        ImageView imageView = holder.getView(R.id.group_member_check);
        GroupMember groupMember = baseRecyclerAdapter.getItem(position);
        String memberIcon = groupMember.getMemberIcon();
        ImageLoader.load(mContext, circleImageView, memberIcon,
                ImageLoader.getHeadOption());
        String memberName = groupMember.getMemberName();
        nameText.setText(memberName);
        if (keyType.equals(KEY_TYPE_VIEW_MEMBER)) {
            imageView.setVisibility(View.GONE);
        } else {
            imageView.setVisibility(View.VISIBLE);
            boolean check = groupMember.isCheck();
            if (check) {
                imageView.setImageResource(R.mipmap.member_check);
            } else {
                imageView.setImageResource(R.mipmap.member_uncheck);
            }
        }
    }

}

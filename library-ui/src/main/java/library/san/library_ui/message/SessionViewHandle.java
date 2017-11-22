package library.san.library_ui.message;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

import com.lib_im.pro.R;
import library.san.library_ui.entity.GroupContact;
import library.san.library_ui.entity.SessionItem;
import com.lib_im.pro.im.manager.group.GroupContactManager;
import library.san.library_ui.utils.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import library.san.library_ui.widget.recyler.BaseRecyclerAdapter;
import library.san.library_ui.widget.recyler.LViewHolder;
import library.san.library_ui.widget.view.CircleImageView;

/**
 * Created by songgx on 2017/1/22.
 * session模块界面view相关操作
 */

public class SessionViewHandle {

    private Activity context;
    private ImageLoader.Option headOption = new ImageLoader.Option().placeholder(0).error(R.mipmap.default_avatar);//图片加载的默认设置

    SessionViewHandle(Activity context) {
        this.context = context;
    }

    /**
     * 弹出处理提示框
     */
    void showFontSizeDialog(final View arg1, final int position, final BaseRecyclerAdapter<SessionItem> adapter, Context context, final SessionPresenter mSessionPresenter) {
        final List<SessionItem> itemList = adapter.getData();
        final SessionItem _sitem = itemList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(_sitem.getFromName());
        builder.setItems(new String[]{"删除该聊天"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        itemList.remove(position);
                        mSessionPresenter.deleteSession(_sitem);
                        break;
                }
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * @param list
     * @param position
     * @param holder
     * @descript adapter设置view模块
     */
    public void setView(List<SessionItem> list, int position, LViewHolder holder) {
        SessionItem sessionItem = list.get(position);
        CircleImageView imageView = holder.getView(R.id.session_head);
        TextView nameText = holder.getView(R.id.session_name);
        TextView typeText = holder.getView(R.id.session_last_type);
        TextView timeText = holder.getView(R.id.session_time);
        TextView msgCountText = holder.getView(R.id.message_count);
        switch (sessionItem.getMsg_type()) {
            case SessionItem.MESSAGE_TYPE_IMG:
                typeText.setText("[图片]");
                break;
            case SessionItem.MESSAGE_TYPE_SONDS:
                typeText.setText("[语音]");
                break;
            case SessionItem.MESSAGE_TYPE_TXT:
                String msg = sessionItem.getMsg();
                if (msg != null) {
                   typeText.setText(msg);
                }
                break;
        }

        //设置名字
        if (sessionItem.isRoom()) {//收到的是群消息
            GroupContactManager<GroupContact> groupContactManager = LiteChat.chatClient.getGroupContactManager();
            GroupContact _groupContact = groupContactManager.getGroupContact(sessionItem.getRoomId());
            if (_groupContact != null) {
                nameText.setText(_groupContact.getGroupName());
            }
        } else {
            nameText.setText(sessionItem.getFromName());

        }

        //设置未读消息数量
        if (sessionItem.getNoReadCount() > 0) {
            msgCountText.setVisibility(View.VISIBLE);
//            if (sessionItem.getNoReadCount() >= 100) {
//                msgCountText.setText("99+");
//            } else {
//                msgCountText.setText(String.valueOf(sessionItem.getNoReadCount()));
//            }
        } else {
            msgCountText.setVisibility(View.INVISIBLE);
        }
        //聊天类型判断去加载头像
        if (sessionItem.isRoom()) {// 群聊
            //如果有群头像则设置群头像，没有则设置默认头像
            GroupContact groupContact= (GroupContact) LiteChat.chatClient.getGroupContactManager().getGroupContact(sessionItem.getRoomId());
            String roomHead = groupContact.getGroupHead();
            ImageLoader.load(context,imageView,roomHead,ImageLoader.getOrgOption());
        } else {
            String userHead=sessionItem.getHeadIcon();
            ImageLoader.load(context,imageView,userHead,ImageLoader.getHeadOption());
        }

        //时间显示
        Long time = sessionItem.getDate();
        if (time != null) {
            long intervalTime = System.currentTimeMillis() - time;
            if (intervalTime > 0 && intervalTime > 86400000) {
                if (intervalTime > 172800000) {
                    SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
                    String d = format.format(time);
                    timeText.setText(d);
                } else {
                    timeText.setText("昨天");
                }
            } else {
                SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String d = format.format(time);
                timeText.setText(d);
            }
        }


    }


}

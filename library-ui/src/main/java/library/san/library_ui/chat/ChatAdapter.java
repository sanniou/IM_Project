package library.san.library_ui.chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lib_im.pro.R;
import library.san.library_ui.entity.ChatMessage;
import library.san.library_ui.utils.ImageLoader;
import com.zzti.fengyongge.imagepicker.PhotoPreviewActivity;
import com.zzti.fengyongge.imagepicker.model.PhotoModel;
import com.zzti.fengyongge.imagepicker.util.CommonUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import library.san.library_ui.widget.view.CircleImageView;

public class ChatAdapter extends BaseAdapter {
    private List<ChatMessage> listMsg;
    private Context context;
    private Activity mActivity;
    private LayoutInflater mInflater;
    private AnimationDrawable animationDrawable = new AnimationDrawable();
    /**
     * 存储正在或者最后一次点击播放的声音所属list的索引
     */
    private int tempPosition = -1;

    /**
     * 存储正在或者最后一次点击播放的声音View对象
     */
    private ImageView tempSound;
    /**
     * 播放器
     */
    private MediaPlayer mMediaPlayer;

    private String mChatUserName;
    private String mChatUserId;
    private boolean mGroupChat;
    private String chatRoomJid;
    private ArrayList<String> selectPath=new ArrayList<>();
    private List<PhotoModel> single_photos=new ArrayList<>();

    public ChatAdapter(Activity a, List<ChatMessage> listMsg,String chatUserName, String chatUserID, boolean groupChat, String chatRoomJid) {
        this.listMsg = listMsg;
        mActivity = a;
        context = a;
        mInflater = LayoutInflater.from(context);
        this.mChatUserName = chatUserName;
        this.mChatUserId = chatUserID;
        this.mGroupChat = groupChat;
        this.chatRoomJid = chatRoomJid;
    }

    @Override
    public int getCount() {
        return listMsg == null ? 0 : listMsg.size();
    }


    @Override
    public Object getItem(int position) {
        return listMsg.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        MyViewHolder myViewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_chat, parent, false);
            myViewHolder = new MyViewHolder();
            convertView.setTag(myViewHolder);
        } else {
            myViewHolder = (MyViewHolder) convertView.getTag();
        }

        final ChatMessage chatMessage = listMsg.get(position);


        if (chatMessage.isMT()) {//收到的消息，显示左侧布局
            getLeftHolder(convertView, myViewHolder);
            convertView.findViewById(R.id.left_chatLayout).setVisibility(View.VISIBLE);
            convertView.findViewById(R.id.right_chatLayout).setVisibility(View.GONE);
        } else {//自己发送的消息
            getRightHolder(convertView, myViewHolder);
            convertView.findViewById(R.id.right_chatLayout).setVisibility(View.VISIBLE);
            convertView.findViewById(R.id.left_chatLayout).setVisibility(View.GONE);
        }
        showContentView(chatMessage, myViewHolder);
        showTime(chatMessage, position, myViewHolder);
        setContent(chatMessage, myViewHolder, position);
        //实现监听
        if (chatMessage.getRemoteUrl() != null || chatMessage.getFile_path() != null) {
            if (chatMessage.getRemoteUrl().equals(".amr") || chatMessage.getFile_path().equals(".amr")) {
                myViewHolder.voiceContent.setOnClickListener(new SoundOnClickListener(chatMessage, position));
            } else {
                myViewHolder.imageContent.setOnClickListener(new ImageOnClickListener(chatMessage));
            }
        }

        return convertView;
    }

    /**
     * 加载右边的布局控件
     */
    private void getRightHolder(View convertView, MyViewHolder mHolder) {
        mHolder.right_layout = (RelativeLayout) convertView
                .findViewById(R.id.right_chatLayout);
        mHolder.head = (CircleImageView) convertView
                .findViewById(R.id.right_messageHeadIcon);
        mHolder.name = (TextView) convertView
                .findViewById(R.id.right_chatName);
        mHolder.textContent = (TextView) convertView
                .findViewById(R.id.right_messageText);
        mHolder.voiceLayout = (RelativeLayout) convertView.findViewById(R.id.right_messageVoiceLayout);
        mHolder.voiceLength = (TextView) convertView.findViewById(R.id.right_messageVoiceLength);
        mHolder.voiceContent = (ImageView) convertView
                .findViewById(R.id.right_messageVoice);
        mHolder.imageContent = (ImageView) convertView
                .findViewById(R.id.right_messageImage);
        mHolder.time = (TextView) convertView.findViewById(R.id.chat_time);
        mHolder.sendMsgProgress = (ProgressBar) convertView.findViewById(R.id.right_sendMsg_progress);
        mHolder.sendSuccessText = (TextView) convertView.findViewById(R.id.right_send_success);
        mHolder.sendFailedImage = (ImageView) convertView.findViewById(R.id.right_send_failed);
    }

    /**
     * 对方发来的所有信息控件
     *
     * @param convertView
     * @param mHolder
     */
    private void getLeftHolder(View convertView, MyViewHolder mHolder) {
        mHolder.left_layout = (RelativeLayout) convertView
                .findViewById(R.id.left_chatLayout);
        mHolder.head = (CircleImageView) convertView
                .findViewById(R.id.left_messageHeadIcon);
        mHolder.name = (TextView) convertView
                .findViewById(R.id.left_chatName);
        mHolder.textContent = (TextView) convertView
                .findViewById(R.id.left_messageText);
        mHolder.voiceLayout = (RelativeLayout) convertView.findViewById(R.id.left_messageVoiceLayout);
        mHolder.voiceLength = (TextView) convertView.findViewById(R.id.left_messageVoiceCount);
        mHolder.voiceContent = (ImageView) convertView
                .findViewById(R.id.left_messageVoice);
        mHolder.imageContent = (ImageView) convertView
                .findViewById(R.id.left_messageImage);
        mHolder.time = (TextView) convertView.findViewById(R.id.chat_time);
        mHolder.voiceMarkImage = (ImageView) convertView.findViewById(R.id.left_voice_mark);
    }

    /**
     * 根据消息类型判断显示与隐藏
     */
    public void showContentView(ChatMessage message, MyViewHolder myViewHolder) {
        switch (message.getMsg_type()) {
            case ChatMessage.MESSAGE_TYPE_IMG:
                myViewHolder.imageContent.setVisibility(View.VISIBLE);
                myViewHolder.voiceLayout.setVisibility(View.GONE);
                myViewHolder.textContent.setVisibility(View.GONE);
                break;
            case ChatMessage.MESSAGE_TYPE_SONDS:
                myViewHolder.imageContent.setVisibility(View.GONE);
                myViewHolder.voiceLayout.setVisibility(View.VISIBLE);
                myViewHolder.textContent.setVisibility(View.GONE);
                break;
            case ChatMessage.MESSAGE_TYPE_TXT:
                myViewHolder.imageContent.setVisibility(View.GONE);
                myViewHolder.voiceLayout.setVisibility(View.GONE);
                myViewHolder.textContent.setVisibility(View.VISIBLE);
                break;

        }
    }

    /**
     * 设置相关界面控件信息
     */
    public void setContent(final ChatMessage chatMessage, final MyViewHolder myViewHolder, int position) {
            String imagePath = "";
            //设置头像
            myViewHolder.head.setVisibility(View.VISIBLE);
            boolean isFrom = chatMessage.isMT();
            if (isFrom) {//收到的消息
                String headPath=chatMessage.getHeadIcon();
                ImageLoader.load(context, myViewHolder.head, headPath,ImageLoader.getHeadOption());
            } else {//自己发送的消息
                String headIcon="";
                ImageLoader.load(context, myViewHolder.head, headIcon,ImageLoader.getHeadOption());
            }

            //设置用户名
        myViewHolder.name.setVisibility(View.VISIBLE);
        myViewHolder.name.setText(null);
            boolean isMT = chatMessage.isMT();
            if (isMT) {
                String fromId = chatMessage.getFromId();
                String fromName = chatMessage.getFromName();
                if (fromName != null) {
                    myViewHolder.name.setText(fromName);
                } else {
                    if (fromId != null) {
                        myViewHolder.name.setText(fromId);
                    }
                }
            } else {//单人聊天不设置名称
//            String selfId = chatMessage.getSelfId();
//            String selfName = chatMessage.getSelfName();
//            if (selfName != null) {
//                myViewHolder.name.setText(selfName);
//            } else {
//                if (selfId != null) {
//                    myViewHolder.name.setText(selfId);
//                }
//            }
            }
            //设置内容
            switch (chatMessage.getMsg_type()) {
                case ChatMessage.MESSAGE_TYPE_IMG:
                    myViewHolder.imageContent.setImageBitmap(null);
                    if (isMT) {
                        imagePath = chatMessage.getRemoteUrl();
                    } else {
                        imagePath = chatMessage.getFile_path();
                        String remoteUrl = chatMessage.getRemoteUrl();
                        if (remoteUrl != null && !remoteUrl.equals("")) {
                            imagePath = remoteUrl;
                        }
                    }
                    if (imagePath != null && !imagePath.equals("")) {
                    if (!selectPath.contains(imagePath)) {
                        selectPath.add(imagePath);
                    }
                        ImageLoader.load(context, myViewHolder.imageContent, imagePath);
                    } else {
                        myViewHolder.imageContent.setImageResource(R.mipmap.picture);
                    }

                    break;
                case ChatMessage.MESSAGE_TYPE_SONDS:
                    int soundLength = (int) chatMessage.getFileLength();
                    myViewHolder.voiceLength.setText(null);
                    myViewHolder.voiceLength.setText(String.valueOf(soundLength) + "\"");
                    boolean isComing = chatMessage.isMT();
                    if (isComing) {
                        int mark = chatMessage.getMark();
                        if (mark == 0) {//语音未读
                            myViewHolder.voiceMarkImage.setVisibility(View.VISIBLE);
                        } else {//语音已读
                            myViewHolder.voiceMarkImage.setVisibility(View.GONE);
                        }

                    }

                    break;
                case ChatMessage.MESSAGE_TYPE_TXT:
                    myViewHolder.textContent.setText(null);
                    myViewHolder.textContent.setText(chatMessage.getMsg());
                    break;
            }
            /**更新消息的状态*/
            boolean isComing = chatMessage.isMT();
            if (!isComing) {
                int state = chatMessage.getState();
                switch (state) {
                    case ChatMessage.SEND_STATUS_SENDING:
                        myViewHolder.sendMsgProgress.setVisibility(View.VISIBLE);
                        myViewHolder.sendSuccessText.setVisibility(View.GONE);
                        myViewHolder.sendFailedImage.setVisibility(View.GONE);
                        break;
                    case ChatMessage.SEND_STATUS_SUCC:
                        myViewHolder.sendMsgProgress.setVisibility(View.GONE);
                        myViewHolder.sendFailedImage.setVisibility(View.GONE);
                        break;
                    case ChatMessage.SEND_STATUS_FAIL:
                        myViewHolder.sendMsgProgress.setVisibility(View.GONE);
                        myViewHolder.sendSuccessText.setVisibility(View.GONE);
                        myViewHolder.sendFailedImage.setVisibility(View.VISIBLE);
                        myViewHolder.sendFailedImage.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //重新发送操作
                                myViewHolder.sendMsgProgress.setVisibility(View.VISIBLE);
                                myViewHolder.sendSuccessText.setVisibility(View.GONE);
                                myViewHolder.sendFailedImage.setVisibility(View.GONE);
                                ChatPresenter chatPresenter = new ChatPresenter(mChatUserName,mChatUserId,mGroupChat,chatRoomJid);
                                chatPresenter.repeatSendMessage(chatMessage);


                            }
                        });
                        break;
                    case ChatMessage.MESSAGE_HAS_READ:
                        myViewHolder.sendSuccessText.setVisibility(View.VISIBLE);
                        break;
                }
            }
        }


    /**
     * 设置显示时间
     */
    public void showTime(ChatMessage chatMessage, int position, MyViewHolder myViewHolder) {
        if (position > 0) {// 显示时间
            ChatMessage upBean = listMsg.get(position - 1);
            if (chatMessage.getDate() - upBean.getDate() > 5 * 60000) {// 大于5分钟或第一次的显示时间
                // 时间戳
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                String d = format.format(chatMessage.getDate());
                myViewHolder.time.setVisibility(View.VISIBLE);
                myViewHolder.time.setText(d);
                d = null;
                format = null;
            } else {
                myViewHolder.time.setVisibility(View.GONE);
            }

        } else {
            // 时间戳
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            String d = format.format(chatMessage.getDate());
            myViewHolder.time.setVisibility(View.VISIBLE);
            myViewHolder.time.setText(d);
            d = null;
            format = null;
        }
    }

    //静态类viewHolder
    public static class MyViewHolder {
        CircleImageView head;
        TextView name;
        TextView time;
        RelativeLayout left_layout, right_layout;
        TextView textContent;
        ImageView voiceContent;
        ImageView imageContent;
        TextView voiceLength;
        RelativeLayout voiceLayout;
        ProgressBar sendMsgProgress;
        TextView sendSuccessText;
        ImageView sendFailedImage;
        ImageView voiceMarkImage;

    }


    //  图片点击事件
    private class ImageOnClickListener implements OnClickListener {
        int index;
        ImageOnClickListener(ChatMessage chatMessage) {
            String remoteUrl1 = chatMessage.getRemoteUrl();
            String file_path = chatMessage.getFile_path();
            if (!remoteUrl1.equals("")) {
                index = selectPath.indexOf(remoteUrl1);
            } else {
                if (file_path!=null&&!file_path.equals(""))
                index = selectPath.indexOf(file_path);
            }
            for (String path : selectPath) {
                for (PhotoModel photoModel : single_photos) {
                    if (!photoModel.getOriginalPath().equals(path)) {
                        PhotoModel photoModel1 = new PhotoModel();
                        photoModel1.setOriginalPath(path);
                        photoModel1.setChecked(false);
                        single_photos.add(photoModel1);
                    }
                }
            }
        }

        @Override
        public void onClick(View v) {
            //PhotoModel 开发者将自己本地bean的list封装成PhotoModel的list，PhotoModel属性源码可查看
            Bundle bundle = new Bundle();
            bundle.putSerializable("photos",(Serializable)single_photos);
            bundle.putInt("position", index);//position预览图片地址
            bundle.putBoolean("isSave",false);//isSave表示是否可以保存预览图片，建议只有预览网络图片时设置true
            CommonUtils.launchActivity(context, PhotoPreviewActivity.class, bundle);
        }
    }

    // TODO 声音的点击事件
    private class SoundOnClickListener implements OnClickListener {
        private ChatMessage chatMessage;
        private int position;
        private String soundPath;

        public SoundOnClickListener(final ChatMessage chatMessage, int position) {
            this.chatMessage = chatMessage;
            this.position = position;
            if (chatMessage.isMT()) {
                soundPath = chatMessage.getRemoteUrl();
            } else {
                soundPath = chatMessage.getFile_path();
            }
        }

        @Override
        public void onClick(final View v) {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mMediaPlayer = null;
                setRecover(animationDrawable, tempSound, chatMessage.isMT());
                if (tempPosition != -1) {
                    chatMessage.setSoundPlaying(false);
                }
                if (tempSound == v) {
                    return;
                }
            }
            tempPosition = position;
            tempSound = (ImageView) v;
            ((ImageView) v).setImageBitmap(null);
            if (chatMessage.isMT()) {//左边的语音动画
                v.setBackgroundResource(R.drawable.play_anim_left);
            } else {
                v.setBackgroundResource(R.drawable.play_anim_right);
            }
            animationDrawable = (AnimationDrawable) v.getBackground();
            mMediaPlayer = new MediaPlayer();
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(soundPath); // 设置数据源
                mMediaPlayer.prepare(); // prepare自动播放
                mMediaPlayer.start();
                animationDrawable.start();
                chatMessage.setSoundPlaying(true);
                chatMessage.setMark(1);
                ChatActivity.INSTANCE.updateVoiceMark(chatMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    chatMessage.setSoundPlaying(false);
                    setRecover(animationDrawable, tempSound, chatMessage.isMT());
                    if (mp != null && mp.isPlaying()) {
                        mp.stop();
                        mp = null;
                    }
                }
            });

            if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                setRecover(animationDrawable, tempSound, chatMessage.isMT());
            }
        }

    }

    /**
     * 恢复
     */
    private void setRecover(AnimationDrawable ad, View view, boolean isMT) {
        ad.stop();
        if (isMT) {
            view.setBackgroundResource(R.mipmap.v_anim333);
        } else {
            view.setBackgroundResource(R.mipmap.v_anim3);
        }

    }


}

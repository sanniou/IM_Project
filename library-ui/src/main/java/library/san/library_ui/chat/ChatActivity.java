package library.san.library_ui.chat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.lib_im.pro.R;
import library.san.library_ui.entity.ChatMessage;
import library.san.library_ui.entity.FileUpEntity;
import com.lib_im.pro.im.listener.HistoryMessageListener;
import com.lib_im.core.manager.message.IMMessageListener;
import com.lib_im.pro.im.listener.MessageStateListener;
import com.lib_im.core.manager.message.OnChatRecordListener;
import com.lib_im.pro.im.listener.OnRoomChatRecordListener;
import com.lib_im.pro.im.listener.RefreshViewListener;
import com.lib_im.pro.im.listener.StopRefreshListener;
import library.san.library_ui.utils.IDUtil;
import library.san.library_ui.utils.ImageFactory;
import library.san.library_ui.utils.recorder.AudioRecorderButton;
import com.zzti.fengyongge.imagepicker.PhotoSelectorActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import library.san.library_ui.base.PermissionActivity;
import library.san.library_ui.group.GroupDetailsActivity;
import library.san.library_ui.widget.swipe.LSwipeRefreshLayout;
import library.san.library_ui.widget.swipe.SwipeView;

import static com.lib_im.core.config.ChatCode.CHOOSE_PIC;
import static com.lib_im.core.config.ChatCode.INIT_DATA;
import static com.lib_im.core.config.ChatCode.POSITION_0;
import static com.lib_im.core.config.ChatCode.PULL_DOWN_REFRESH;
import static com.lib_im.core.config.ChatCode.REFRESH_DATA;
import static com.lib_im.core.config.ChatCode.SEND_MSG;
import static com.lib_im.core.config.ChatCode.SEND_MSG_TYPE;
import static com.lib_im.core.config.ChatCode.TAKE_PHOTO;
import static com.lib_im.core.config.ChatCode.messageMap;
import static com.lib_im.core.config.ChatCode.positionMap;

/**
 * A simple {@link Fragment} subclass.
 */
@Route(path = ChatActivity.ROUTE_PATH)
public class ChatActivity extends PermissionActivity implements
        HistoryMessageListener, IMMessageListener<ChatMessage>,
        OnRoomChatRecordListener<List<ChatMessage>>, OnChatRecordListener,
        View.OnClickListener, MessageStateListener<ChatMessage>,
        RefreshViewListener<ChatMessage>, StopRefreshListener {
    /**
     * 拍照
     */
    public static final int TAKE_PHOTO = 1;
    /**
     * 选择照片
     */
    public static final int CHOOSE_PIC = 2;

    public static final String ROUTE_PATH = "/lib_im/chat";
    private static final int JUMP_GROUP = 108;
    private ListView chatListView;
    private String mChatUserID;
    private String mChatUserName;
    private boolean mGroupChat;
    private ChatPresenter mChatPresenter;
    private ChatAdapter chatAdapter;
    private RelativeLayout imageLayout;
    //	 1.voice 2. face 3. img
    private int whichDisplay = 0;
    /**
     * 是否已发送
     */
    boolean isSend = false;
    private String chatRoomJid;
    private ImageButton chatVoiceBtn;//文本，语音切换按钮
    EditText chatEditText;//聊天文本输入框
    private ImageButton chatAddImage;//输入框附近加号图标
    private Button sendTextBtn;//发送文本按钮
    private AudioRecorderButton chatRecordVoiceBtn;//录制语音的按钮
    private InputMethodManager inputMethodManager;//软键盘  类
    private TextView titleText;
    private String takePicPath;
    private List<ChatMessage> chatMessageList;
    private LSwipeRefreshLayout swipeRefreshLayout;
    public static ChatActivity INSTANCE;

    /**
     * 初始化界面控件
     */
    private void initView() {
        chatListView = (ListView) findViewById(R.id.chat_listView);
        ImageView backImage = (ImageView) findViewById(R.id.im_chat_title_back_image);
        backImage.setOnClickListener(this);
        titleText = (TextView) findViewById(R.id.im_chat_title_text);
        ImageView iconImage = (ImageView) findViewById(R.id.im_chat_title_self_icon);
        iconImage.setOnClickListener(this);

        chatEditText = (EditText) findViewById(R.id.chat_edit_message_text);
        //底部操作栏
        imageLayout = (RelativeLayout) findViewById(R.id.chat_bottom_image_layout);
        chatVoiceBtn = (ImageButton) findViewById(R.id.chatVoiceImage);
        chatAddImage = (ImageButton) findViewById(R.id.chat_addBtn);
        sendTextBtn = (Button) findViewById(R.id.chat_sendBtn);
        chatRecordVoiceBtn = (AudioRecorderButton) findViewById(R.id.chat_recoder_voice);
        ImageButton takePictureBtn = (ImageButton) findViewById(R.id.takePhoto);
        ImageButton choosePictureBtn = (ImageButton) findViewById(R.id.choosePicture);
        //添加监听
        chatAddImage.setOnClickListener(this);
        chatVoiceBtn.setOnClickListener(this);
        sendTextBtn.setOnClickListener(this);
        takePictureBtn.setOnClickListener(this);
        choosePictureBtn.setOnClickListener(this);
        init(iconImage);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        swipeRefreshLayout = (LSwipeRefreshLayout) findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeView.OnRequestListener() {
            @Override
            public void onRequest() {
                //下拉刷新
                mChatPresenter.pullDownRefresh(chatMessageList);
            }
        });
    }

    public void stopLSRefresh(LSwipeRefreshLayout swipeRefreshLayout) {
        swipeRefreshLayout.stopRefresh(true);
    }

    @Override
    protected String[] getPermission4Check() {
        return new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
    }

    private void init(ImageView iconImage) {
        Intent intent = getIntent();
        if (intent != null) {
            mGroupChat = intent.getExtras().getBoolean("groupChat");
            mChatUserID = intent.getExtras().getString("chatUserId");
            mChatUserName = intent.getExtras().getString("chatUserName");
            if (mGroupChat) {
                chatRoomJid = intent.getExtras().getString("chatRoomJid");
                iconImage.setImageResource(R.mipmap.group_members);
            } else {
                iconImage.setImageResource(R.mipmap.person);
            }
            titleText.setText(mChatUserName);
        }
    }

    /**
     * 初始化界面的数据源
     */
    public void initData() {
        chatMessageList = new ArrayList<>();
        mChatPresenter = new ChatPresenter(mChatUserName, mChatUserID, mGroupChat, chatRoomJid);
        mChatPresenter.setStopRefreshListener(this);
        LiteChat.chatClient.getChatManger().addHistoryMessageListener(this);
        LiteChat.chatClient.getChatManger().addMessageListener(this);
        LiteChat.chatClient.getChatManger().addRoomRecordListener(this);
        LiteChat.chatClient.getChatManger().addChatRecordListener(this);
//        LiteChat.chatClient.getChatManger().addReceiptMessageListener(this);
        LiteChat.chatClient.getChatManger().addMessageStateListener(this);
        LiteChat.chatClient.getChatManger().addRefreshViewListener(this);
        mChatPresenter.getHistoryMessage(mChatUserID,mGroupChat);
        LiteChat.chatClient.getChatManger().bindOpenChatId(mChatUserID, mGroupChat);
        if (!mGroupChat) {
            LiteChat.chatClient.getChatManger().sendMessageReceipt();
        }
        LiteChat.chatClient.getNotifyManager().cancelNotation();

    }

    @Override
    public void stateSuccess(final ChatMessage msg) {
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                super.run();
                int _msgIndex = chatMessageList.indexOf(msg);
                if (_msgIndex != -1) {
                    chatMessageList.set(_msgIndex, msg);
                    chatAdapter.notifyDataSetChanged();
                    if (SEND_MSG_TYPE == SEND_MSG) {
                        chatListView.setSelection(chatMessageList.size());
                    }
                }
            }
        });

    }

    @Override
    public void stateFailed(final ChatMessage msg) {
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                super.run();
                int _msgIndex = chatMessageList.indexOf(msg);
                if (_msgIndex != -1) {
                    chatMessageList.set(_msgIndex, msg);
                    chatAdapter.notifyDataSetChanged();
                    if (SEND_MSG_TYPE == SEND_MSG) {
                        chatListView.setSelection(chatMessageList.size());
                    }
                }
            }
        });

    }

    @Override
    public void onRefreshView(ChatMessage msg) {
        refreshView(msg);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (LiteChat.chatClient.getChatManger() != null) {
            LiteChat.chatClient.getChatManger().removeMessageListener(this);
            LiteChat.chatClient.getChatManger().bindOpenChatId("", mGroupChat);
//            LiteChat.chatClient.getChatManger().removeReceiptMessageListener(this);
            LiteChat.chatClient.getChatManger().removeRoomRecordListener(this);
            LiteChat.chatClient.getChatManger().removeChatRecordListener(this);
            LiteChat.chatClient.getChatManger().removeHistoryMessageListener(this);
            LiteChat.chatClient.getChatManger().removeMessageStateListener(this);
            LiteChat.chatClient.getChatManger().removeRefreshViewListener(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_chat);
        INSTANCE = this;
        initView();
        initData();
        chatEditChange();
        startRecord();
    }

    public void showMessage(List<ChatMessage> list) {
        if (chatAdapter == null) {
            chatAdapter = new ChatAdapter(this, list, mChatUserName, mChatUserID, mGroupChat, chatRoomJid);
            chatListView.setAdapter(chatAdapter);
        } else {
            chatAdapter.notifyDataSetChanged();
        }
    }

    /**
     * @param chatMessage 更新语音的状态
     * @descript 语音已读更新数据库
     */
    public void updateVoiceMark(final ChatMessage chatMessage) {
        if (mGroupChat) {//群组本地唯一的一条数据语音播放标志状态
            ChatMessage groupNewestMsg = LiteChat.chatCache.readObject(mChatUserID, ChatMessage.class);
            if (groupNewestMsg != null) {
                if (chatMessage.getMsgId().equals(groupNewestMsg.getMsgId())) {
                    groupNewestMsg = chatMessage;
                    LiteChat.chatCache.saveObject(mChatUserID, groupNewestMsg);
                }
            }
        } else {//单聊存储本地数据库语音状态更新

        }
        ChatActivity.this.runOnUiThread(new Thread() {
            public void run() {
                int _msgIndex = chatMessageList.indexOf(chatMessage);
                if (_msgIndex != -1) {
                    chatMessageList.set(_msgIndex, chatMessage);
                    chatAdapter.notifyDataSetChanged();
                }
            }
        });
    }


//    @Override
//    public void netFailed() {
//        ToastUtils.showShortToast("获取聊天记录失败");
//    }

    @Override
    public void onHistoryMsg(int code) {
        showMessage(chatMessageList);
        switch (code) {
            case INIT_DATA:

                break;
            case REFRESH_DATA:
                chatListView.setSelection(chatMessageList.size());
                break;
            case PULL_DOWN_REFRESH:
                if (positionMap.size() > 0) {
                    int position = positionMap.get(POSITION_0);
                    chatListView.setSelection(position);
                } else {
                    chatListView.setSelection(0);
                }
                break;
        }
    }

    /**
     * 监听接收到消息
     */
    @Override
    public void onReceiveMessage(final ChatMessage chatMessage) {
        String fromID = String.valueOf(chatMessage.getFromId());
        String chatUserID = mChatUserID;
        if (chatMessage.isRoom()) {
            fromID = chatMessage.getRoomId();
        }
        if (fromID.equals(chatUserID)) {
            //有新的消息需要显示
            runOnUiThread(new Thread() {
                @Override
                public void run() {
                    super.run();
                    chatMessageList.add(chatMessage);
                    showMessage(chatMessageList);
                    chatListView.setSelection(chatMessageList.size());
                }
            });

        }
    }

    /**
     * @param chatMessages 类型
     * @descript 群组历史消息回调
     */
    @Override
    public void onRoomChatRecorder(List<ChatMessage> chatMessages) {
        mChatPresenter.getRoomRecord(chatMessages, chatMessageList);
    }

    /**
     * @param chatMessages 群记录实体
     * @descript 单聊历史消息回调
     */
    @Override
    public void onChatRecorder(List<ChatMessage> chatMessages) {
        mChatPresenter.getChatRecorder(chatMessages, chatMessageList);
    }

//    /**
//     * 接收到回执消息回调方法
//     */
//    @Override
//    public void onReceiptRefresh(ChatMessage msg) {
//
//    }

    /**
     * chatEdit变化的时候保证下面的图片表情布局不显示
     */
    public void chatEditChange() {
        chatEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (chatEditText.getText().toString().length() > 0) {
                    isSend = true;
                    if (chatAddImage.getVisibility() != View.GONE) {
                        chatAddImage.setVisibility(View.GONE);
                    }
                    if (sendTextBtn.getVisibility() != View.VISIBLE) {
                        sendTextBtn.setVisibility(View.VISIBLE);
                    }
                } else {
                    isSend = false;
                    if (sendTextBtn.getVisibility() != View.GONE) {
                        sendTextBtn.setVisibility(View.GONE);
                    }
                    if (chatAddImage.getVisibility() != View.VISIBLE) {
                        chatAddImage.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.chat_addBtn) {//聊天底部加号按钮
            whichDisplay = 3;
            showWindows();
        } else if (i == R.id.chatVoiceImage) {//语音切换按钮
            whichDisplay = 1;
            showWindows();
        } else if (i == R.id.chat_sendBtn) {//文本发送按钮
            if (imageLayout.getVisibility() == View.VISIBLE) {
                imageLayout.setVisibility(View.GONE);
            }
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setMsg_type(ChatMessage.MESSAGE_TYPE_TXT);
            String msgStr = chatEditText.getText().toString();
            if (!msgStr.equals("")) {
                chatMessage.setMsg(msgStr);
                /**发送消息*/
                mChatPresenter.initSendMessageConfig(chatMessage);
            }
        }
        //拍照
        else if (i == R.id.takePhoto) {
            takePicPath = ImageFactory.getDiskCacheDir(this) + "/" + IDUtil.nextID() + ".jpg";
            toCamera(takePicPath);
        }
        //图片
        else if (i == R.id.choosePicture) {
            initPickSelector();
        } else if (i == R.id.im_chat_title_self_icon) {
            if (mGroupChat) {
                //跳转至群详情页面
                ARouter.getInstance().build("/group/groupDetails")
                        .withString("group_id", mChatUserID)
                        .navigation(this, JUMP_GROUP);
            } else {
                //跳转至个人信息页面
                ARouter.getInstance().build("/user/userDetails")
                        .withString("user_id", "")
                        .withString("loginName", mChatUserID)
                        .withBoolean("hideSendBtn", true)
                        .navigation();
            }
        } else if (i == R.id.im_chat_title_back_image) {
            finish();
        } else {
            //封口操作
        }
    }

    //显示相关窗口
    private void showWindows() {
        chatEditText.clearFocus();// 让chatEdit失去焦点
        switch (whichDisplay) {
            case 1:// voice
                if (chatRecordVoiceBtn.getVisibility() == View.VISIBLE) {
                    chatRecordVoiceBtn.setVisibility(View.GONE);
                    chatVoiceBtn.setImageResource(R.mipmap.voice);
                    if (chatEditText.getVisibility() != View.VISIBLE) {
                        chatEditText.setVisibility(View.VISIBLE);
                    }
                    showKeyboard();
                } else {
                    chatRecordVoiceBtn.setVisibility(View.VISIBLE);
                    chatVoiceBtn.setImageResource(R.mipmap.keyboard);
                    if (chatEditText.getVisibility() != View.GONE) {
                        chatEditText.setVisibility(View.GONE);
                    }
                    imageLayout.setVisibility(View.GONE);
                    hideKeyboard();
                }

                break;
            case 3:// img
                if (imageLayout.getVisibility() == View.VISIBLE) {
                    imageLayout.setVisibility(View.GONE);
//                    if (chatEditText.getVisibility() != View.VISIBLE)
//                        chatEditText.setVisibility(View.VISIBLE);
//                    showKeyboard();
                    if (chatEditText.getVisibility() == View.VISIBLE) {
                        hideKeyboard();
                    }

                } else {
                    imageLayout.setVisibility(View.VISIBLE);
                    //chatRecordVoiceBtn.setVisibility(View.GONE);
//                    if (chatEditText.getVisibility() != View.VISIBLE)
//                        chatEditText.setVisibility(View.VISIBLE);
//                    hideKeyboard();

                }
                break;
        }
    }

    /**
     * 隐藏键盘
     */
    private void hideKeyboard() {
        if (inputMethodManager != null && chatEditText != null) {
            inputMethodManager.hideSoftInputFromWindow(chatEditText.getWindowToken(), 0);
        }
    }

    /**
     * 展示键盘
     */
    private void showKeyboard() {
        if (inputMethodManager != null && chatEditText != null) {
            inputMethodManager.showSoftInput(chatEditText, 0);
        }
    }

    /**
     * 加入对相机7.0的适配
     */
    public void toCamera(String fileName) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File mPhotoFile = new File(fileName);
                intent.putExtra("outputFormat", "JPEG");// 返回格式
                //intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
                startActivityForResult(intent, TAKE_PHOTO);
            } else {
                Uri imageUri = FileProvider.getUriForFile(this, "com.lib_im.pro.fileprovider",
                        new File(fileName));//通过FileProvider创建一个content类型的Uri
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);//设置Action为拍照
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//将拍取的照片保存到指定URI
                startActivityForResult(intent, TAKE_PHOTO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 录音
     */
    private void startRecord() {
        chatRecordVoiceBtn
                .setFinishRecorderCallBack(new AudioRecorderButton.AudioFinishRecorderCallBack() {
                    @Override
                    public void onFinish(float seconds, String filePath) {
                        mChatPresenter.startRecord(filePath, seconds);
                    }
                });
    }

    /**
     * startActivityForResult返回的结果
     *
     * @param requestCode 请求码
     * @param resultCode  返回的结果
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final LinkedList<FileUpEntity> linkedList = new LinkedList<>();
        ImageFactory imageFactory = new ImageFactory();
        switch (requestCode) {
            case TAKE_PHOTO:
                takePhotoPack(linkedList, imageFactory);
                //上传图片并发送消息
//                mChatPresenter.UpLoadFile("", true, new HashMap<String, String>(), linkedList);
                break;
            case CHOOSE_PIC:
                if (data != null) {
                    List<String> paths = (List<String>) data.getExtras().getSerializable("photos");//path是选择拍照或者图片的地址数组
                    choosePicPack(linkedList, imageFactory, paths);
//                    mChatPresenter.UpLoadFile("", true, new HashMap<String, String>(), linkedList);
                }
                break;

        }
        if (imageLayout.getVisibility() == View.VISIBLE) {
            imageLayout.setVisibility(View.GONE);
        }

        if (resultCode == GroupDetailsActivity.FINISH_CHAT_RESULT_CODE &&
                requestCode == JUMP_GROUP) {
            finish();
        }

    }

    /**
     * @descript 拍摄照片封装
     */
    private void takePhotoPack(LinkedList<FileUpEntity> linkedList, ImageFactory imageFactory) {
        File takePhotoFile = new File(takePicPath);
        String takePhotoFileName = takePhotoFile.getName();
        String takeLocalFileName = takePhotoFile.getName();//本地文件名
        /**封装消息实体-----------------------------------------start*/
        ChatMessage takePhotoMsg = new ChatMessage();
        takePhotoMsg.setMsg_type(ChatMessage.MESSAGE_TYPE_IMG);
        takePhotoMsg.setFile_path(takePicPath);
        takePhotoMsg.setRemoteUrl("");
        takePhotoMsg.setFileName(takeLocalFileName);
        /**封装消息实体-----------------------------------------end*/
        messageMap.put(takePhotoFileName, takePhotoMsg);//使用远程服务器文件名去对应消息实体
        /**封装文件上传实体-------------------------------------------start*/
        FileUpEntity takePhotoUpEntity = new FileUpEntity();
        takePhotoUpEntity.setStoreFileName(takePhotoFileName);
        takePhotoUpEntity.setLocalFileName(takeLocalFileName);
        takePhotoUpEntity.setFile(takePhotoFile);
        linkedList.add(takePhotoUpEntity);
        /**封装文件上传实体-------------------------------------------end*/
        mChatPresenter.initSendMessageConfig(takePhotoMsg);
        /**进行图片压缩,并更改文件名*/
        imageFactory.listCompress(linkedList);
    }

    /**
     * 设置图片选择器属性
     */
    private void initPickSelector() {
        Intent intent = new Intent(this, PhotoSelectorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("limit", 9);//number是选择图片的数量
        startActivityForResult(intent, CHOOSE_PIC);
    }

    /**
     * @descript 选择图片选择器封装
     */
    private void choosePicPack(LinkedList<FileUpEntity> linkedList, ImageFactory imageFactory,
                               List<String> stringList) {
        for (String str : stringList) {
            if (str != null) {
                File file = new File(str);
                String fileName = file.getName();
                String localFileName = file.getName();
                /**封装消息实体------------------------------------start*/
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setMsg_type(ChatMessage.MESSAGE_TYPE_IMG);
                chatMessage.setFile_path(str);
                chatMessage.setRemoteUrl("");
                chatMessage.setFileName(localFileName);
                /**封装消息实体------------------------------------end*/
                messageMap.put(fileName, chatMessage);//使用远程服务器文件名去对应消息实体
                /**封装文件上传实体-----------------------------------------------start*/
                FileUpEntity ftpUpLoadEntity = new FileUpEntity();
                ftpUpLoadEntity.setStoreFileName(fileName);
                ftpUpLoadEntity.setLocalFileName(localFileName);
                ftpUpLoadEntity.setFile(file);
                linkedList.add(ftpUpLoadEntity);
                /**封装文件上传实体-----------------------------------------------end*/
                mChatPresenter.initSendMessageConfig(chatMessage);

                /**进行图片压缩,并更改文件名*/
                imageFactory.listCompress(linkedList);
            }
        }
    }

    /**
     * 刷新界面
     */
    public void refreshView(final ChatMessage _msg) {
        chatMessageList.add(_msg);
        showMessage(chatMessageList);
        chatListView.setSelection(chatMessageList.size());
        if (_msg.getMsg_type() == ChatMessage.MESSAGE_TYPE_TXT) {
            String _str = chatEditText.getText().toString();
            if (!_str.equals("")) {
                chatEditText.setText("");
            }
        }

    }
//
//    @Override
//    public void setPresenter(Object presenter) {
//
//    }

    @Override
    public void stopRefresh() {
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                super.run();
                stopLSRefresh(swipeRefreshLayout);
            }
        });
    }
}

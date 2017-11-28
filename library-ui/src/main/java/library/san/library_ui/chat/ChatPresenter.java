package library.san.library_ui.chat;

import library.san.library_ui.entity.ChatMessage;
import library.san.library_ui.entity.FileUpEntity;

import com.lib_im.pro.im.listener.StopRefreshListener;
import com.lib_im.profession.retrofit.upload.Uploader;
import library.san.library_ui.utils.ExecutorTasks;
import library.san.library_ui.utils.IDUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.lib_im.core.config.ChatCode.GET_ROOM_DATA;
import static com.lib_im.core.config.ChatCode.GET_SINGLE_DATA;
import static com.lib_im.core.config.ChatCode.INIT_DATA;
import static com.lib_im.core.config.ChatCode.INIT_ROOM_DATA;
import static com.lib_im.core.config.ChatCode.INIT_SINGLE_DATA;
import static com.lib_im.core.config.ChatCode.POSITION_0;
import static com.lib_im.core.config.ChatCode.PULL_DOWN_REFRESH;
import static com.lib_im.core.config.ChatCode.REFRESH_DATA;
import static com.lib_im.core.config.ChatCode.REFRESH_ROOM_DATA;
import static com.lib_im.core.config.ChatCode.REFRESH_SINGLE_DATA;
import static com.lib_im.core.config.ChatCode.REPEAT_SEND_MSG;
import static com.lib_im.core.config.ChatCode.SEND_MSG;
import static com.lib_im.core.config.ChatCode.SEND_MSG_TYPE;
import static com.lib_im.core.config.ChatCode.messageMap;
import static com.lib_im.core.config.ChatCode.positionMap;
import static com.lib_im.core.config.ChatCode.roomCacheMsg;

/**
 * 聊天模块页面处理逻辑类似于MVP框架P模块，但是由于管理器模块已经相当于是M模块，所以这里只进行处理
 */
class ChatPresenter implements ChatHandle {

    private String mChatUserName;
    private String mChatUserId;
    private boolean mGroupChat;
    private String chatRoomJid;
    private StopRefreshListener stopRefreshListener;
    private String url="";//上传文件的地址
    private Map<String,String> map;//上传图片需要传递的参数
    private boolean isImage=true;

    public void setStopRefreshListener(StopRefreshListener stopRefreshListener) {
        this.stopRefreshListener=stopRefreshListener;
    }

    public ChatPresenter(String chatUserName, String chatUserId, boolean groupChat, String chatRoomJid) {
        this.mChatUserName = chatUserName;
        this.mChatUserId = chatUserId;
        this.mGroupChat = groupChat;
        this.chatRoomJid = chatRoomJid;

    }

    @Override
    public void getHistoryMessage(String mChatUserID,boolean isRoom) {
        if (isRoom) {
            GET_ROOM_DATA = INIT_ROOM_DATA;
            LiteChat.chatClient.getChatManger().notifyHistoryMsgListener(INIT_DATA);
            roomCacheMsg = LiteChat.chatCache.readObject(mChatUserID, ChatMessage.class);
            if (roomCacheMsg != null) {
                LiteChat.chatClient.getChatManger().getRoomHistoryMessage(mChatUserID, "", roomCacheMsg.getMsgId(),1,20);
            } else {
                LiteChat.chatClient.getChatManger().getRoomHistoryMessage(mChatUserID, "", "",1,20);
            }
        } else {
            GET_SINGLE_DATA = INIT_SINGLE_DATA;
            LiteChat.chatClient.getChatManger().notifyHistoryMsgListener(INIT_DATA);
            String toUserId=LiteChat.chatCache.readString(ChatCode.KEY_USER_ID);
            LiteChat.chatClient.getChatManger().getChatHistoryMessage("", mChatUserID,toUserId,1,20);
        }
    }

    /**
     * 录音操作
     */
    @Override
    public void startRecord(String filePath, float seconds) {
        File file = new File(filePath);
        String localFileName = file.getName();//本地文件名
        String fileName = file.getName();//远程服务器文件名
        /**封装消息实体------------------------------------------------------start*/
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMsg_type(ChatMessage.MESSAGE_TYPE_SONDS);
        chatMessage.setFileLength(seconds);
        chatMessage.setMark(0);
        chatMessage.setFile_path(filePath);
        chatMessage.setSoundPlaying(false);
        chatMessage.setRemoteUrl("");
        chatMessage.setFileName(localFileName);
        /**封装消息实体------------------------------------------------------end*/
        messageMap.put(fileName, chatMessage);//使用远程服务器文件名去对应消息实体
        /**封装上传的文件实体------------------------------------------------start*/
        final LinkedList<FileUpEntity> linkedList = new LinkedList<>();
        FileUpEntity ftpUpLoadEntity = new FileUpEntity();
        ftpUpLoadEntity.setFile(file);
        ftpUpLoadEntity.setStoreFileName(fileName);
        ftpUpLoadEntity.setLocalFileName(localFileName);
        linkedList.add(ftpUpLoadEntity);
        /**封装上传的文件实体------------------------------------------------end*/
         initSendMessageConfig(chatMessage);//相关实体配置信息封装完毕，先进行界面的适配显示
        // UpLoadFile(url,isImage,map,linkedList);
    }

    /**
     * 初始化发送消息相关配置信息
     *
     * @param _msg 发送的消息实体
     */
    @Override
    public void initSendMessageConfig(final ChatMessage _msg) {
        String selfId = LiteChat.chatClient.getConfig(ChatCode.KEY_USER_ID);
        String selfName = LiteChat.chatCache.readString(ChatCode.KEY_USER_NAME);
        String sendUserIcon="";//TODO 当前登录用户的头像
        SEND_MSG_TYPE = SEND_MSG;//发送消息
        int msgType = _msg.getMsg_type();
        if (sendUserIcon == null) {
            sendUserIcon="";
        }
        _msg.setHeadIcon(sendUserIcon);
        _msg.setMT(Boolean.FALSE);
        _msg.setFromName(mChatUserName);
        _msg.setFromId(mChatUserId);
        _msg.setState(ChatMessage.SEND_STATUS_SENDING);
        String msgId = IDUtil.nextID();
        _msg.setMsgId(msgId);
        _msg.setSelfId(selfId);
        _msg.setSelfName(selfName);
        if (mGroupChat) {
            _msg.setRoom(true);
            _msg.setRoomId(mChatUserId);
        } else {
            _msg.setRoom(false);
        }
        //封装完消息体进行适配
        LiteChat.chatClient.getChatManger().notifyRefreshView(_msg);
        if (msgType == ChatMessage.MESSAGE_TYPE_TXT) {
            sendMessage(_msg);
        }
    }

    /**
     * @param _msg 消息实体
     * @descript 发送消息
     */
    @Override
    public void sendMessage(ChatMessage _msg) {
        String selfId = LiteChat.chatClient.getConfig(ChatCode.KEY_USER_ID);
        String selfName = LiteChat.chatCache.readString(ChatCode.KEY_USER_NAME);
        int msgType = _msg.getMsg_type();
        switch (msgType) {
            case ChatMessage.MESSAGE_TYPE_IMG:
                _msg.setMsg(packingMessage(msgType, _msg.getRemoteUrl(), _msg.getFile_path(), 0, _msg.getFileName(), "", selfId, selfName, _msg.getMsgId(),_msg.getHeadIcon()));
                break;
            case ChatMessage.MESSAGE_TYPE_SONDS:
                _msg.setMsg(packingMessage(msgType, _msg.getRemoteUrl(), _msg.getFile_path(), _msg.getFileLength(), _msg.getFileName(), "", selfId, selfName, _msg.getMsgId(),_msg.getHeadIcon()));
                break;
            case ChatMessage.MESSAGE_TYPE_TXT:
                _msg.setMsg(packingMessage(msgType, "", "", 0, "", _msg.getMsg(), selfId, selfName, _msg.getMsgId(),_msg.getHeadIcon()));
                if (mGroupChat) {
                    sendRoomOpenFireMessageAndRefresh(_msg);
                } else {
                    sendSingleOpenFireMessageAndRefresh(_msg);
                }
                break;
        }
    }

    /**
     * 封装消息体，用来判断消息类型
     */
    private String packingMessage(int msgType, String remoteUrl, String localPath, double fileLength, String fileName, String content, String sendUserId, String sendUserName, String msgId,String headIcon) {
        String jsonString = "";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("msgType", msgType);
            jsonObject.put("sendUserId", sendUserId);
            jsonObject.put("sendUserName", sendUserName);
            jsonObject.put("msgId", msgId);
            jsonObject.put("headIcon",headIcon);
            switch (msgType) {
                case ChatMessage.MESSAGE_TYPE_TXT:
                    jsonObject.put("content", content);
                    break;
                case ChatMessage.MESSAGE_TYPE_IMG:
                    jsonObject.put("remoteUrl", remoteUrl);
                    jsonObject.put("localPath", localPath);
                    jsonObject.put("fileName", fileName);
                    break;
                case ChatMessage.MESSAGE_TYPE_SONDS:
                    jsonObject.put("remoteUrl", remoteUrl);
                    jsonObject.put("localPath", localPath);
                    jsonObject.put("fileName", fileName);
                    jsonObject.put("fileLength", fileLength);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        jsonString = jsonObject.toString();
        return jsonString;
    }

    /**
     * 根据文件是否上传成功来执行openfire发送消息，以及刷新界面
     */
    @Override
    public void sendSingleOpenFireMessageAndRefresh(final ChatMessage _msg) {
        LiteChat.chatClient.getChatManger().sendSingleChatMessage(_msg, new MessageCallBack<ChatMessage>() {
            @Override
            public void onSuccess(final ChatMessage msg) {
                LiteChat.chatClient.getChatManger().notifyMsgStateSuccessListener(_msg);
            }

            @Override
            public void onError(final ChatMessage msg) {
                LiteChat.chatClient.getChatManger().notifyMsgStateFailedListener(_msg);
            }
        });
    }

    /**
     * @param chatMessage 重新发送的消息实体
     * @descript 消息发送失败后重新发送, 重新发送
     */
    @Override
    public void repeatSendMessage(ChatMessage chatMessage) {
        SEND_MSG_TYPE = REPEAT_SEND_MSG;
        String storeName = chatMessage.getFileName();
        File file = null;
        LinkedList<FileUpEntity> linkedList = new LinkedList<>();
        int msgType = chatMessage.getMsg_type();
        if (msgType != ChatMessage.MESSAGE_TYPE_TXT) {//文件需要重新上传,封装文件上传实体
            file = new File(chatMessage.getFile_path());
            FileUpEntity fileUpEntity = new FileUpEntity();
            fileUpEntity.setFile(file);
            fileUpEntity.setLocalFileName(storeName);
            fileUpEntity.setStoreFileName(storeName);
            linkedList.add(fileUpEntity);
            //上传文件操作
            UpLoadFile(url,isImage,map,linkedList);
        } else {//文本发送消息
            sendMessage(chatMessage);
        }
    }

    /**
     * 根据文件是否上传成功来执行openFire发送消息，以及刷新界面
     */
    @Override
    public void sendRoomOpenFireMessageAndRefresh(final ChatMessage _msg) {
        LiteChat.chatClient.getChatManger().sendRoomMessage(chatRoomJid, _msg, new MessageCallBack<ChatMessage>() {
            @Override
            public void onSuccess(final ChatMessage msg) {
                LiteChat.chatClient.getChatManger().notifyMsgStateSuccessListener(msg);
            }

            @Override
            public void onError(final ChatMessage msg) {
                LiteChat.chatClient.getChatManger().notifyMsgStateFailedListener(msg);
            }
        });
    }

    @Override
    public void getRoomRecord(List<ChatMessage> chatMessages, List<ChatMessage> mMessagesList) {
        if (GET_ROOM_DATA == INIT_ROOM_DATA) {
            if (chatMessages.size() > 0) {
                mMessagesList.addAll(chatMessages);
            }
            Collections.sort(mMessagesList, new ChatMsgCompare());
            if (roomCacheMsg != null) {
                mMessagesList.add(roomCacheMsg);
            }
            LiteChat.chatClient.getChatManger().notifyHistoryMsgListener(REFRESH_DATA);
        } else {
            stopRefreshListener.stopRefresh();
            ChatMessage msg_0 = null;
            if (mMessagesList.size() > 0) {
                msg_0 = mMessagesList.get(0);
            }
            if (chatMessages.size() > 0) {
                mMessagesList.addAll(chatMessages);
            }
            Collections.sort(mMessagesList, new ChatMsgCompare());
            if (msg_0 != null) {
                int msgIndex = mMessagesList.indexOf(msg_0);
                if (msgIndex != -1) {
                    positionMap.put(POSITION_0, msgIndex);
                }
            } else {
                positionMap.put(POSITION_0, 0);
            }
            LiteChat.chatClient.getChatManger().notifyHistoryMsgListener(PULL_DOWN_REFRESH);
        }
    }

    @Override
    public void getChatRecorder(List<ChatMessage> chatMessages, List<ChatMessage> mMessagesList) {
        if (GET_SINGLE_DATA == INIT_SINGLE_DATA) {
            if (chatMessages.size() > 0) {
                mMessagesList.addAll(chatMessages);
            }
            Collections.sort(mMessagesList, new ChatMsgCompare());
            LiteChat.chatClient.getChatManger().notifyHistoryMsgListener(REFRESH_DATA);
        } else {
            stopRefreshListener.stopRefresh();
            ChatMessage msg_0 = null;
            if (mMessagesList.size() > 0) {
                msg_0 = mMessagesList.get(0);
            }
            if (chatMessages.size() > 0) {
                mMessagesList.addAll(chatMessages);
            }
            Collections.sort(mMessagesList, new ChatMsgCompare());
            if (msg_0 != null) {
                int msgIndex = mMessagesList.indexOf(msg_0);
                if (msgIndex != -1) {
                    positionMap.put(POSITION_0, msgIndex);
                }
            } else {
                positionMap.put(POSITION_0, 0);
            }
            LiteChat.chatClient.getChatManger().notifyHistoryMsgListener(PULL_DOWN_REFRESH);
        }
    }

    @Override
    public void UpLoadFile(String url, boolean isImage,Map<String, String> params, final LinkedList<FileUpEntity> files) {
        for (final FileUpEntity fileUpEntity : files) {
            LiteChat.upLoadManager.uploadFile(url, isImage, fileUpEntity.getFile(), params, new Uploader.UploadProgressListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onProgress(long currentCount, long totalCount) {

                }

                @Override
                public void onSuccess(Call call, Response requestBody) {
                    JSONObject jsonObject = null;
                    try {
                        ResponseBody body = requestBody.body();
                        if (body != null) {
                            jsonObject = new JSONObject(body.string());
                            JSONArray jsonArray = jsonObject.getJSONArray("rows");
                            String url = jsonArray.getJSONObject(0).getString("url");
                            if (url != null) {//文件上传成功
                                fileUpEntity.setFileUrl(url);
                                String storeFileName = fileUpEntity.getStoreFileName();
                                ChatMessage chatMessage = messageMap.get(storeFileName);
                                chatMessage.setRemoteUrl(fileUpEntity.getFileUrl());
                                sendMessage(chatMessage);
                                if (mGroupChat) {
                                    sendRoomOpenFireMessageAndRefresh(chatMessage);
                                } else {
                                    sendSingleOpenFireMessageAndRefresh(chatMessage);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }


                @Override
                public void onFailed(Call call, Throwable t, String fileName) {
                    final ChatMessage chatMessage = messageMap.get(fileName);
                    //**发送消息成功，创建session-------------------start*/
                    chatMessage.setState(ChatMessage.SEND_STATUS_FAIL);
                    LiteChat.chatClient.getChatManger().notifyMsgStateFailedListener(chatMessage);
                }
            });

        }
    }


    @Override
    public void pullDownRefresh(List<ChatMessage> mMessagesList) {
        final int rows = 20;
        final int page=1;
        if (!mGroupChat) {//单聊刷新
            GET_SINGLE_DATA = REFRESH_SINGLE_DATA;
            final String fromId = mChatUserId;
            String msgId = "";
            if (mMessagesList.size() > 0) {
                ChatMessage singleMessage = mMessagesList.get(0);
                msgId = singleMessage.getMsgId();
            } else {
                msgId = "";
            }
            final String finalMsgId = msgId;
            final String toUserId= LiteChat.chatCache.readString(ChatCode.KEY_USER_ID);
            Runnable _run = new Runnable() {
                @Override
                public void run() {
                    LiteChat.chatClient.getChatManger().getChatHistoryMessage(finalMsgId, fromId,toUserId, page,rows);
                }
            };
            ExecutorTasks.getInstance().postRunnable(_run);
        } else {//群聊刷新
            GET_ROOM_DATA = REFRESH_ROOM_DATA;
            final String groupId = mChatUserId;
            final String logId = "";
            String msgId = "";
            if (mMessagesList.size() > 0) {
                ChatMessage roomMessage = mMessagesList.get(0);
                msgId = roomMessage.getMsgId();
            } else {
                msgId = "";
            }
            final String finalMsgId = msgId;
            Runnable _run = new Runnable() {
                @Override
                public void run() {
                    LiteChat.chatClient.getChatManger().getRoomHistoryMessage(groupId, logId, finalMsgId,page,rows);
                }
            };
            ExecutorTasks.getInstance().postRunnable(_run);
        }
    }

}

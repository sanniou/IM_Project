package library.zrhx.imsample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.lib_im.core.IMChatClient;
import com.lib_im.core.entity.ChatMessage;
import com.lib_im.core.manager.message.IMChatMsgManager;
import com.lib_im.core.manager.message.IMMessageListener;
import com.lib_im.core.manager.message.conversation.IMGroupConversation;
import com.lib_im.core.manager.message.conversation.IMUserConversation;
import com.lib_im.core.retrofit.rx.SimpleObserver;

public class MainActivity extends Activity {

    private IMUserConversation mConversation;
    private IMGroupConversation mGroupConversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView mainview = findViewById(R.id.main_view);
        mainview.setText("");
        IMChatMsgManager chatManager = IMChatClient.getInstance().getChatManager();
        chatManager.addMessageListener(new IMMessageListener() {
            @Override
            public void onReceiveMessage(ChatMessage chatMessage) {
                Log.e("Main", "聊天监听" + chatMessage.toString());
            }

            @Override
            public void onReceiveGroupMessage(ChatMessage chatMessage) {
                Log.e("Main", "群组监听" + chatMessage.toString());
            }

            @Override
            public void onReceiveReceipt(ChatMessage chatMessage) {
                Log.e("Main", "回执监听" + chatMessage.toString());
            }
        });

        //获取一个单聊会话
        mConversation = chatManager.getUserConversation("linyi001");

        //获取一个群聊会话
        mGroupConversation = chatManager
                .getGroupConversation("just@conference.127.0.0.1", "zhouss");
    }

    public void sendClick(View view) {
        ChatMessage msg = new ChatMessage();
        msg.setMsg("test");
        mConversation.send(msg)
                     .subscribe(new SimpleObserver<ChatMessage>() {
                         @Override
                         public void onNext(ChatMessage chatMessage) {
                             Log.e("send", "发送成功");
                         }

                         @Override
                         public void onError(Throwable e) {
                             e.printStackTrace();
                         }
                     });

        ChatMessage msg2 = new ChatMessage();
        msg2.setMsg("groupmsg");
        mGroupConversation.send(msg2)
                          .subscribe(new SimpleObserver<ChatMessage>() {
                              @Override
                              public void onNext(ChatMessage chatMessage) {
                                  Log.e("Main", "群聊成功");
                              }

                              @Override
                              public void onError(Throwable e) {
                                  e.printStackTrace();
                              }
                          });
    }

    public void logout(View view) {
        IMChatClient.getInstance().logout();
        finish();
    }
}

package library.zrhx.imsample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.lib_im.core.manager.message.IMChatMsgManager;
import com.lib_im.core.manager.message.IMMessageListener;
import com.lib_im.core.manager.message.conversation.MessageCancelException;
import com.lib_im.core.rx.SimpleObserver;
import com.lib_im.profession.IMChatClient;
import com.lib_im.profession.entity.ChatMessage;
import com.lib_im.profession.message.IMGroupConversation;
import com.lib_im.profession.message.IMUserConversation;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends Activity {

    private IMUserConversation mConversation;
    private IMGroupConversation mGroupConversation;
    private EditText mEdit;
    private TextView mMainview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMainview = findViewById(R.id.main_view);
        mEdit = findViewById(R.id.main_edit);
        mMainview.setText("");
        IMChatMsgManager chatManager = IMChatClient.getInstance().getChatManager();
        chatManager.addMessageListener(new IMMessageListener() {
            @Override
            public void onReceiveMessage(String chatMessage) {
                Log.e("Main", "聊天监听" + chatMessage);
                mMainview.setText(mMainview.getText().toString() + "\n消息" + chatMessage);
            }

            @Override
            public void onReceiveGroupMessage(String chatMessage) {
                Log.e("Main", "群组监听" + chatMessage);
                mMainview.setText(mMainview.getText().toString() + "\n群聊" + chatMessage);
            }

            @Override
            public void onReceiveReceipt(String chatMessage) {
                Log.e("Main", "回执监听" + chatMessage);
                mMainview.setText(mMainview.getText().toString() + "\n回执" + chatMessage);
            }
        });

        //获取一个单聊会话
        mConversation = chatManager.getUserConversation("pz");

        //获取一个群聊会话
        chatManager.getGroupConversation("just@conference.127.0.0.1", "zhouss").subscribe(
                new SimpleObserver<IMGroupConversation>() {
                    @Override
                    public void onNext(IMGroupConversation imGroupConversation) {
                        mGroupConversation = imGroupConversation;
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    public void sendClick(View view) {
        if (mConversation == null) {
            return;
        }

        String trim = mEdit.getText().toString().trim();
        ChatMessage msg = new ChatMessage();
        msg.setMsg(trim.isEmpty() ? "test" : trim);
        mConversation.send(msg.getMsg())
                     .subscribe(new Observer<String>() {
                         @Override
                         public void onSubscribe(Disposable d) {

                         }

                         @Override
                         public void onNext(String s) {
                             Log.e("send", "发送取消1");
                         }

                         @Override
                         public void onError(Throwable e) {
                             if (e instanceof MessageCancelException) {
                                 Log.e("send", "发送取消2");
                             } else {
                                 Log.e("send", "发送失败");
                             }
                         }

                         @Override
                         public void onComplete() {
                             Log.e("send", "发送成功");
                         }
                     });

        if (mGroupConversation == null) {
            return;
        }
        ChatMessage msg2 = new ChatMessage();
        msg2.setMsg("groupmsg");
        mGroupConversation.send(msg2.getMsg())
                          .subscribe(new Observer<String>() {
                              @Override
                              public void onSubscribe(Disposable d) {

                              }

                              @Override
                              public void onNext(String s) {
                                  Log.e("send", "群聊取消1");
                              }

                              @Override
                              public void onError(Throwable e) {
                                  if (e instanceof MessageCancelException) {
                                      Log.e("send", "群聊取消2");
                                  } else {
                                      Log.e("send", "群聊失败");
                                  }
                              }

                              @Override
                              public void onComplete() {
                                  Log.e("send", "群聊成功");
                              }
                          });
    }

    public void logout(View view) {
        IMChatClient.getInstance().logout();
        finish();
    }
}

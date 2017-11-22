package library.san.library_ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import library.san.library_ui.entity.UserInfo;
import com.lib_im.pro.im.config.ChatCode;
import com.lib_im.pro.im.listener.OnLoginListener;
import com.lib_im.pro.rx.SimpleListObserver;
import com.lib_im.pro.ui.base.BaseActivity;
import library.san.library_ui.utils.ToastUtils;

import java.util.List;

import io.reactivex.annotations.NonNull;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    EditText nameEdit;
    EditText passEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        nameEdit = findViewById(R.id.editText3);
        passEdit = findViewById(R.id.editText4);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(this);

        String nameString = LiteChat.chatCache.readString(ChatCode.KEY_USER_NAME);
        String passwordString = LiteChat.chatCache.readString(ChatCode.KEY_USER_PASS);
        if (nameString != null && passwordString != null) {
            nameEdit.setText(nameString);
            passEdit.setText(passwordString);
        }
    }

    @Override
    public void onClick(View view) {
        String name = nameEdit.getText().toString();
        String password = passEdit.getText().toString();
        LiteChat.chatCache.saveString(ChatCode.KEY_USER_NAME, name);
        LiteChat.chatCache.saveString(ChatCode.KEY_USER_PASS, password);
        if (!name.equals("")) {
            if (!password.equals("")) {
                autoLogin(name, password);
            } else {
                ToastUtils.showShortToast("请输入用户名");
            }
        } else {
            ToastUtils.showShortToast("请输入用户名");
        }

    }

    /**
     * 登录操作
     */
    private void autoLogin(final String name, final String password) {
        LiteChat.imRequestManager.getListInstance().login(name, password)
                                 .subscribe(new SimpleListObserver<UserInfo>() {
                                     @Override
                                     public void onNext(@NonNull List<UserInfo> list) {
                                         LiteChat.chatCache
                                                 .saveString(ChatCode.KEY_USER_NAME, name);
                                         LiteChat.chatCache
                                                 .saveString(ChatCode.KEY_USER_PASS, password);
                                         LiteChat.chatCache.saveString(ChatCode.KEY_USER_ID,
                                                 list.get(0).getUserID());
                                         LiteChat.chatClient
                                                 .login(name, password, new OnLoginListener() {
                                                     @Override
                                                     public void OnLoginSuccess() {
                                                         handler.sendEmptyMessage(LOGIN_SUCCESS);
                                                     }

                                                     @Override
                                                     public void OnLoginFailed(final String msg) {
                                                         Message message = new Message();
                                                         message.what = LOGIN_FAILED;
                                                         message.obj = msg;
                                                         handler.sendMessage(message);

                                                     }
                                                 });
                                     }

                                     @Override
                                     public void onError(@NonNull final Throwable e) {
                                         Message message = new Message();
                                         message.what = LOGIN_FAILED;
                                         message.obj = e.getMessage();
                                         handler.sendMessage(message);
                                     }
                                 });
    }

    private static final int LOGIN_SUCCESS = 111;
    private static final int LOGIN_FAILED = 112;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOGIN_SUCCESS:
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    break;
                case LOGIN_FAILED:
                    ToastUtils.showShortToast(msg.obj.toString());
                    break;
            }
        }
    };
}

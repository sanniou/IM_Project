package library.zrhx.imsample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lib_im.core.IMChatClient;
import com.lib_im.core.retrofit.rx.SimpleCompleteObserver;

public class LoginActivity extends Activity implements View.OnClickListener {

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
        nameEdit.setText("lsscj");
        passEdit.setText("lsscj");
    }

    @Override
    public void onClick(View view) {
        String name = nameEdit.getText().toString().trim();
        String password = passEdit.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }
        loginChat(name, password);
    }

    private void loginChat(String name, String password) {
        IMChatClient.getInstance()
                    .login(name, password)
                    .subscribe(new SimpleCompleteObserver<Object>() {
                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onComplete() {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    });
    }

}

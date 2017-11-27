package library.zrhx.imsample;

import android.app.Application;
import android.util.Log;

import com.lib_im.core.ChatClientConfig;
import com.lib_im.core.manager.connect.ConnectListener;
import com.lib_im.profession.IMChatClient;

public class DemoApplication extends Application {

    private String openfire = "222.132.114.42";
    private String openfire2 = "120.133.9.54";

    @Override
    public void onCreate() {
        ChatClientConfig clientConfig = new ChatClientConfig(60, 15_000, true, openfire2,
                5222, "127.0.0.1", "test");
        IMChatClient chatClient = IMChatClient.getInstance();
        chatClient.init(clientConfig, this);
        chatClient.getConnectManager().addConnectListener(new ConnectListener() {
            @Override
            public void onConnect() {
                Log.e("app", "onConnect");
            }

            @Override
            public void onAuthenticated() {
                Log.e("app", "onAuthenticated");
            }

            @Override
            public void onDisConnect() {
                Log.e("app", "onDisConnect");
            }

            @Override
            public void onConnectError() {
                Log.e("app", "onConnectError");
            }

            @Override
            public void onConnectConflict() {
                Log.e("app", "onConnectConflict");
            }

            @Override
            public void reconnectingIn(int seconds) {
                Log.e("app", "reconnectingIn" + seconds);
            }

            @Override
            public void reconnectionSuccessful() {
                Log.e("app", "reconnectionSuccessful");
            }

            @Override
            public void reconnectionFailed(Exception e) {
                e.printStackTrace();
                Log.e("app", "reconnectionFailed");
            }
        });
        super.onCreate();
    }
}

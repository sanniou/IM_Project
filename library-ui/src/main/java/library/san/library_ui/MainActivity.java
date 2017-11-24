package library.san.library_ui;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.alibaba.android.arouter.launcher.ARouter;
import com.lib_im.pro.im.listener.IMConnectListener;
import com.lib_im.pro.im.manager.connect.ConnectionManager;

import library.san.library_ui.base.BaseActivity;
import library.san.library_ui.contact.ContactActivity;
import library.san.library_ui.group.GroupActivity;
import library.san.library_ui.message.SessionActivity;
import library.san.library_ui.utils.ExecutorTasks;
import library.san.library_ui.utils.ToastUtils;

/**
 * Created by Think on 2017/10/19.
 */

public class MainActivity extends BaseActivity implements View.OnClickListener, IMConnectListener {

    private ConnectionManager mConnectManager;

    /**
     * 连接相关code
     */
    private static final int ONCONNECT = 0;
    private static final int ONCONFLICT = 1;
    private static final int ONOTHER = 4;
    private static final int RECONNECT_SUCCESS = 2;
    private static final int RECONNECT_FAILED = 3;
    private static final int RECONNECTIN = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button message = findViewById(R.id.message);
        Button contact = findViewById(R.id.contact);
        Button group = findViewById(R.id.group);
        message.setOnClickListener(this);
        contact.setOnClickListener(this);
        group.setOnClickListener(this);

        mConnectManager = LiteChat.chatClient.getConnectManager();
        mConnectManager.addConnectListener(this);

        //加入群聊
        if (ChatCode.roomMap.size() == 0) {
            LiteChat.chatClient.joinGroupRoom("", "");
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        Intent intent = new Intent();
        if (id == R.id.message) {
            intent.setClass(this, SessionActivity.class);
        } else if (id == R.id.contact) {
            intent.setClass(this, ContactActivity.class);
        } else if (id == R.id.group) {
            intent.setClass(this, GroupActivity.class);
        }
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConnectManager != null) {
            mConnectManager.removeConnectListener(this);
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    /************************* openfire *************************/
    /**
     * 监听连接上服务器
     */
    @Override
    public void onConnect() {
        handler.sendEmptyMessage(ONCONNECT);
    }

    /**
     * 监听断开服务器
     */
    @Override
    public void onDisConnect(int errorCode) {
        switch (errorCode) {
            case ChatCode.ERROR_DISCONNECT_CONFLICT:
                handler.sendEmptyMessage(ONCONFLICT);
                break;
            case ChatCode.ERROR_DISCONNECT_OTHER:
                handler.sendEmptyMessage(ONOTHER);
                break;
        }

    }

    /**
     * 重连成功
     */
    @Override
    public void reconnectionSuccessful() {
        /**获取未读消息数量和离线消息*/

        //加入群聊
        if (ChatCode.roomMap.size() == 0) {
            LiteChat.chatClient.joinGroupRoom("", "");
        }
        handler.sendEmptyMessage(RECONNECT_SUCCESS);
    }

    /**
     * 重连失败
     */
    @Override
    public void reconnectionFailed(Exception e) {
        handler.sendEmptyMessage(RECONNECT_FAILED);
    }

    /**
     * 正在重新连接
     */
    @Override
    public void reconnectingIn(int i) {
        handler.sendEmptyMessage(RECONNECTIN);
    }

    /**
     * 界面相关操作
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ONCONNECT:
                    break;
                case ONOTHER:
                    Log.d("MainTabActivity", "ONOTHER");
                    break;
                case ONCONFLICT:
                    new AlertDialog
                            .Builder(MainActivity.this)
                            .setTitle("提示")
                            .setMessage("你的账号在别处登录")
                            .setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            //设置你的操作事项
                                            Intent toLoginIntent = new Intent(
                                                    MainActivity.this,
                                                    LoginActivity.class);
                                            startActivity(
                                                    toLoginIntent);
                                            MainActivity.this
                                                    .finish();
                                        }
                                    }).setCancelable(false)
                            .show();
                    break;
                case RECONNECT_SUCCESS:

                    break;
                case RECONNECT_FAILED:
                    new AlertDialog.Builder(MainActivity.this).setTitle("提示")
                                                              .setMessage("聊天服务断开连接,请重新登录")
                                                              .setPositiveButton("确定",
                                                                      (dialog, which) -> {
                                                                          //设置你的操作事项
                                                                          Intent toLoginIntent = new Intent(
                                                                                  MainActivity.this,
                                                                                  LoginActivity.class);
                                                                          startActivity(
                                                                                  toLoginIntent);
                                                                          MainActivity.this
                                                                                  .finish();
                                                                      }).setNegativeButton("取消",
                            (dialogInterface, i) -> ToastUtils
                                    .showShortToast("现在将无法使用聊天相关功能，如需要请重新登录")).setCancelable(false)
                                                              .show();
                    break;
                case RECONNECTIN:
                    break;
                //先不区分
                case EXIT_APP:
                case EXIT_USER:
                    ARouter.getInstance()
                           .build("/client/login")
                           .navigation();
                    finish();
                    break;
                default:
            }
        }
    };

    /**
     * 退出登录操作
     */
    private static final int EXIT_APP = 1000;

    private static final int EXIT_USER = 1001;

    public void exitLogin() {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("请确认退出系统？")
                .setPositiveButton("退出",
                        (dialog, which) -> exitUser(EXIT_APP)).setNegativeButton("取消", null).show();
    }

    private void exitUser(final int code) {
        ExecutorTasks.getInstance()
                     .postRunnable(() -> {
                         LiteChat.chatClient.logout();
                         handler.sendEmptyMessage(code);
                     });
    }
}

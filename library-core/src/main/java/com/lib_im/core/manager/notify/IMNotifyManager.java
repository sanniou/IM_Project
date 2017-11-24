package com.lib_im.core.manager.notify;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.lib_im.pro.R;

/**
 * Created by songgx on 16/6/15.
 * 消息通知提醒实现管理器
 */

public class IMNotifyManager  {

    private String TAG = "IMNotifyManager";
    private Context mContext;
    private boolean mVibrate = Boolean.FALSE;
    private boolean mBell = true;
    private Class<?> pendingClass;
    private NotificationManager nm;


    public IMNotifyManager(Context context) {
        this.mContext = context;
    }

    /**
     * 是否开启震动提醒
     */
    public void setVibrate(boolean vibrate) {
        mVibrate = vibrate;
    }

    /**
     * 是否开启铃声提醒
     */
    public void setBell(boolean bell) {
        mBell = bell;
    }

    /**
     * 设置提醒数据
     */
    public void setNotifyLink(String appName, int iconId, String action, Class<?> pendingClass) {
        mAppName = appName;
        mAction = action;
        mIconId = iconId;
        this.pendingClass = pendingClass;
    }

    private String mAppName;
    private int mIconId;
    private String mAction;

    /**
     * 播放个人聊天声音提醒,前提聊天页面未打开
     */
    public void playChatMessage(boolean isRoom, String chatUserId, String chatUserName,
                                String chatRoomJid) {
        Log.d(TAG, "playChatMessage--->");
        nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        long when = System.currentTimeMillis();
        Intent in = new Intent(mContext, pendingClass);
        in.putExtra("chatUserId", chatUserId);
        in.putExtra("chatUserName", chatUserName);
        if (isRoom) {
            in.putExtra("groupChat", Boolean.TRUE);
            in.putExtra("chatRoomJid", chatRoomJid);
        } else {
            in.putExtra("groupChat", Boolean.FALSE);
        }
        PendingIntent sender = PendingIntent
                .getActivity(mContext, 0, in, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Builder builder = new Notification.Builder(
                mContext)
                .setAutoCancel(true)
                .setTicker("您有新的消息")
                .setAutoCancel(true)
                .setContentTitle(mAppName)
                .setContentText(
                        chatUserName + ":" + mContext.getString(R.string.chat_core_send_new_msg))
                .setContentIntent(sender)
                .setSmallIcon(mIconId)
                .setWhen(when);
        if (mBell) {
            builder.setSound(Uri.parse("android.resource://"
                    + mContext.getPackageName() + "/" + R.raw.notifi));
            mBell = Boolean.FALSE;
        }
        if (mVibrate) {
            builder.setVibrate(new long[]{0, 200});
        }
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        nm.notify(1, notification);
    }

    /**
     * @descript 取消通知栏
     */
    public void cancelNotation() {
        if (nm != null) {
            nm.cancel(1);
        }
    }

}

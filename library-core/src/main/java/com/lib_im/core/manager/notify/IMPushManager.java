package com.lib_im.core.manager.notify;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.lib_im.pro.R;

import java.util.Map;

/**
 * Created by songgx on 2017/8/29.
 * 创建业务通知管理器
 */

public class IMPushManager {

    private String TAG = "IMPushManager";
    private Context mContext;
    private boolean mVibrate = Boolean.FALSE;
    private boolean mBell = true;
    private Class<?> pendingClass;
    private NotificationManager nm;
    private String mAppName;
    private int mIconId;

    public IMPushManager(Context mContext) {
        this.mContext = mContext;
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
        mIconId = iconId;
        this.pendingClass = pendingClass;
    }

    /**
     * 播放个人聊天声音提醒,前提聊天页面未打开
     */
    public void playChatMessage(Map<String, String> map, String textContent) {
        Log.d(TAG, "playChatMessage--->");
        nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        long when = System.currentTimeMillis();
        Intent in = new Intent(mContext, pendingClass);
        for (String key : map.keySet()) {
            String value = map.get(key);
            in.putExtra(key, value);
        }
        PendingIntent sender = PendingIntent
                .getActivity(mContext, 0, in, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Builder builder = new Notification.Builder(
                mContext)
                .setAutoCancel(true)
                .setTicker("您有新的消息")
                .setAutoCancel(true)
                .setContentTitle(mAppName)
                .setContentText(textContent)
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

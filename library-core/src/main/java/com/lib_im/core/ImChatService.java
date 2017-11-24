package com.lib_im.core;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ImChatService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

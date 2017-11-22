package library.san.library_ui;

import android.app.Application;

import com.lib_im.pro.IMClient;

import library.san.library_ui.utils.Utils;

public class IMUIClient {

    public static void init(Application application) {
        IMClient.init(application);
        //工具类初始化
        Utils.init(application);
    }
}

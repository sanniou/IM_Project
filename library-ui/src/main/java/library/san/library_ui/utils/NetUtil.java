package library.san.library_ui.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * 判断网络类型
 *
 * @author songgx
 */
public class NetUtil {

    /**
     * 判断网络 ,根据showToast来判断是否显示吐司，false 不显示
     *
     * @param context
     * @param showToast
     * @return
     * @方法名称:isNetwork
     * @创建人：songgx
     * @返回类型：boolean
     */
    public static boolean isNetwork(Context context, boolean showToast) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (NetworkInfo anInfo : info) {
                    if (anInfo.isConnected()) {
                        return true;
                    }
                }
            }
        }
        if (showToast) {
            Log.d("NetUtil", "network error..........");
        }
        return false;
    }

    /**
     * 判断网络
     *
     * @return 网络正常 true ,没有网络 false 并显示吐司
     */
    public static boolean isNetwork(Context context) {
        return isNetwork(context, false);
    }

}


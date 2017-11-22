package library.san.library_ui.retrofit.download;

import io.reactivex.Flowable;

/**
 * Created by songgx on 2017/9/26.
 * 下载管理器
 */

public interface DownLoadManager {
    /**
     * 下载文件
     * @param url 文件地址
     * @param saveName 本地存储文件名
     * @return
     */
    Flowable<DownloadInfo> download(String url, String saveName);

}

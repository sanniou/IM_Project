package library.san.library_ui.retrofit.upload;

import java.io.File;
import java.util.Map;

/**
 * Created by songgx on 2017/9/26.
 * 上传文件管理器
 */

public interface UpLoadManager {
    /**
     * 上传文件
     * @param url 地址
     * @param isImage 是否是图片
     * @param file 文件
     * @param params 追加的参数
     * @param nothing 进度回调接口
     */
    void uploadFile(String url, boolean isImage, final File file, Map<String, String> params,
                    final Uploader.UploadProgressListener nothing);

}

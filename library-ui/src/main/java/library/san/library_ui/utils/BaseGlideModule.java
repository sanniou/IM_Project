package library.san.library_ui.utils;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

/**
 * 用于 Glide 生成 GlideApp
 */
@GlideModule
public final class BaseGlideModule extends AppGlideModule {

    //全局配置Glide
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setLogLevel(Log.ERROR);

        /*// Default empty impl.
        //设置Bitmap的缓存池
        builder.setBitmapPool(new LruBitmapPool(30));

        //设置内存缓存
        builder.setMemoryCache(new LruResourceCache(30));

        //设置磁盘缓存
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context));

        //设置读取不在缓存中资源的线程
        builder.setResizeExecutor(GlideExecutor.newSourceExecutor());

        //设置读取磁盘缓存中资源的线程
        builder.setDiskCacheExecutor(GlideExecutor.newDiskCacheExecutor());

        //设置日志级别
        builder.setLogLevel(Log.VERBOSE);

        //设置全局选项
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.format(DecodeFormat.PREFER_RGB_565);
        builder.setDefaultRequestOptions(requestOptions);
*/
    }

    // 避免二次加载
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

}
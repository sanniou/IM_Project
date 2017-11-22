package library.san.library_ui.utils;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lib_im.pro.R;

/**
 * Glide 的简单封装
 */
public class ImageLoader {

    /**
     * 默认的加载设置
     */
    private static Option sDefaultOption;
    /**
     * 缓存的加载设置，用于每次加载时单独的设置
     */
    private static Option sCacheOption;

    private ImageLoader() {

    }

    static {
        sDefaultOption = getAOption();
    }

    public static Option getAOption() {
        return new Option()
                .error(R.mipmap.loading_error)
                .placeholder(R.color.app_gray)
                .diskCache(true)
                .memoryCache(true)
                .centerCrop(false)
                .circle(false);
    }

    public static Option getHeadOption() {
        return new Option()
                .error(R.mipmap.default_avatar)
                .placeholder(R.mipmap.default_avatar)
                .diskCache(true)
                .memoryCache(true)
                .centerCrop(false)
                .circle(true);
    }

    public static Option getOrgOption() {
        return new Option()
                .error(R.mipmap.organize)
                .placeholder(R.mipmap.organize)
                .diskCache(true)
                .memoryCache(true)
                .centerCrop(false)
                .circle(false);
    }

    /**
     * 根据路径加载图片。
     */
    public static void load(Activity activity, ImageView view, Object obj, Option option) {
        load(GlideApp.with(activity).load(obj), view, option);
    }

    public static void load(Fragment fragment, ImageView view, Object obj, Option option) {
        load(GlideApp.with(fragment).load(obj), view, option);
    }

    public static void load(Context context, ImageView view, Object obj, Option option) {
        load(GlideApp.with(context).load(obj), view, option);
    }

    public static void load(Activity activity, ImageView view, Object obj) {
        load(GlideApp.with(activity).load(obj), view, sDefaultOption);
    }

    public static void load(Fragment fragment, ImageView view, Object obj) {
        load(GlideApp.with(fragment).load(obj), view, sDefaultOption);
    }

    public static void load(Context context, ImageView view, Object obj) {
        load(GlideApp.with(context).load(obj), view, sDefaultOption);
    }

    public static void load(Activity activity, ImageView view, Object obj, int placeholderId,
                            int errorId) {
        load(GlideApp.with(activity).load(obj), view,
                sCacheOption.error(errorId).placeholder(placeholderId));
    }

    public static void load(Fragment fragment, ImageView view, Object obj, int placeholderId,
                            int errorId) {
        load(GlideApp.with(fragment).load(obj), view,
                sCacheOption.error(errorId).placeholder(placeholderId));
    }

    public static void load(Context context, ImageView view, Object obj, int placeholderId,
                            int errorId) {
        load(GlideApp.with(context).load(obj), view,
                sCacheOption.error(errorId).placeholder(placeholderId));
    }

    /**
     * 真正开始加载图片
     */
    private static void load(GlideRequest builder, final ImageView view, Option option) {
        if (option.circle()) {
            builder.circleCrop();
        }
        if (option.centerCrop()) {
            builder.centerCrop();
        }
        builder.placeholder(option.placeholder())// 占位图片
               .error(option.error())
               .skipMemoryCache(!option.memoryCache())
               .diskCacheStrategy(
                       option.diskCache() ? DiskCacheStrategy.RESOURCE : DiskCacheStrategy.NONE)
               .into(view);
    }

    public static class Option {

        private int mError; //错误图
        private int mPlaceholder; //占位图
        private boolean mMemoryCache = true; //内存缓存
        private boolean mDiskCache = true; //磁盘缓存
        private boolean mCircle = false; //圆形
        private boolean mCenterCrop = false; //裁剪

        public Option error(int error) {
            mError = error;
            return this;
        }

        public int error() {
            return mError;
        }

        public Option memoryCache(boolean memoryCache) {
            mMemoryCache = memoryCache;
            return this;
        }

        public boolean memoryCache() {
            return mMemoryCache;
        }

        public Option diskCache(boolean diskCache) {
            mDiskCache = diskCache;
            return this;
        }

        public boolean diskCache() {
            return mDiskCache;
        }

        public Option placeholder(int placeholder) {
            mPlaceholder = placeholder;
            return this;
        }

        public int placeholder() {
            return mPlaceholder;
        }

        public Option circle(boolean circle) {
            this.mCircle = circle;
            return this;
        }

        public boolean circle() {
            return mCircle;
        }

        public Option centerCrop(boolean b) {
            mCenterCrop = b;
            return this;
        }

        public boolean centerCrop() {
            return mCenterCrop;
        }
    }
}

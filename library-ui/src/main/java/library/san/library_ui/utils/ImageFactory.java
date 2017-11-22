package library.san.library_ui.utils;

/**
 * Created by songgx on 2016/7/25.
 * 图片工具类
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import library.san.library_ui.entity.FileUpEntity;
import com.lib_im.pro.ui.widget.view.TextureVideoView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Image compress factory class
 *
 * @author
 */
public class ImageFactory {

    public static final int TAKE_PHOTO = 100;

    /**
     * Get bitmap from specified image path
     */
    public Bitmap getBitmap(String imgPath) {
        // Get bitmap through image path
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = false;
        newOpts.inPurgeable = true;
        newOpts.inInputShareable = true;
        // Do not compress
        newOpts.inSampleSize = 1;
        newOpts.inPreferredConfig = Config.RGB_565;
        return BitmapFactory.decodeFile(imgPath, newOpts);
    }

    /**
     * Store bitmap into specified image path
     */
    public void storeImage(Bitmap bitmap, String outPath) throws FileNotFoundException {
        FileOutputStream os = new FileOutputStream(outPath);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
    }

    /**
     * Compress image by pixel, this will modify image width/height.
     * Used to get thumbnail
     *
     * @param imgPath image path
     * @param pixelW  target pixel of width
     * @param pixelH  target pixel of height
     */
    public Bitmap ratio(String imgPath, float pixelW, float pixelH) throws IOException {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true，即只读边不读内容
        newOpts.inJustDecodeBounds = true;
        newOpts.inPreferredConfig = Config.RGB_565;
        // 得到 bitmap 信息, 此时bitmap返回null
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 想要缩放的目标尺寸
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w >= h && w >= pixelW) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / pixelW);
        } else if (w < h && h > pixelH) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / pixelH);
        }
        if (be <= 0) { be = 1; }
        newOpts.inSampleSize = be;//设置缩放比例
        // 开始压缩图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(imgPath, newOpts);
        if (bitmap == null) {
            throw new IOException("this file may not a image");
        }
        return bitmap;
    }

    /**
     * Compress image by size, this will modify image width/height.
     * Used to get thumbnail
     *
     * @param pixelW target pixel of width
     * @param pixelH target pixel of height
     */
    public Bitmap ratio(Bitmap image, float pixelW, float pixelH) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, os);
        if (os.toByteArray().length / 1024 >
                1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            os.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, os);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        newOpts.inPreferredConfig = Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > pixelW) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / pixelW);
        } else if (w < h && h > pixelH) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / pixelH);
        }
        if (be <= 0) { be = 1; }
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        is = new ByteArrayInputStream(os.toByteArray());
        bitmap = BitmapFactory.decodeStream(is, null, newOpts);
        //压缩好比例大小后再进行质量压缩
        //      return compress(bitmap, maxSize); // 这里再进行质量压缩的意义不大，反而耗资源，删除
        return bitmap;
    }

    /**
     * Compress by quality,  and generate image to the path specified
     *
     * @param maxSize target will be compressed to be smaller than this size.(kb)
     */
    public void compressAndGenImage(Bitmap image, String outPath, int maxSize) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // scale
        int options = 100;
        // Store the bitmap into output stream(no compress)
        image.compress(Bitmap.CompressFormat.JPEG, options, os);
        // Compress by loop
        while (os.toByteArray().length / 1024 > maxSize && options > 0) {
            // Clean up os
            os.reset();
            // interval 10
            options -= 10;
            // 质量压缩方法，这里options=100表示不压缩
            image.compress(Bitmap.CompressFormat.JPEG, options, os);
        }

        // Generate compressed image file
        FileOutputStream fos = new FileOutputStream(outPath);
        fos.write(os.toByteArray());
        fos.flush();
        fos.close();
    }

    /**
     * Compress by quality,  and generate image to the path specified
     *
     * @param maxSize     target will be compressed to be smaller than this size.(kb)
     * @param needsDelete Whether delete original file after compress
     */
    public void compressAndGenImage(String imgPath, String outPath, int maxSize,
                                    boolean needsDelete) throws IOException {
        compressAndGenImage(getBitmap(imgPath), outPath, maxSize);

        // Delete original file
        if (needsDelete) {
            File file = new File(imgPath);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * Ratio and generate thumb to the path specified
     *
     * @param pixelW target pixel of width
     * @param pixelH target pixel of height
     */
    public void ratioAndGenThumb(Bitmap image, String outPath, float pixelW, float pixelH)
            throws FileNotFoundException {
        Bitmap bitmap = ratio(image, pixelW, pixelH);
        storeImage(bitmap, outPath);
    }

    /**
     * Ratio and generate thumb to the path specified
     *
     * @param pixelW      target pixel of width
     * @param pixelH      target pixel of height
     * @param needsDelete Whether delete original file after compress
     */
    public void ratioAndGenThumb(String imgPath, String outPath, float pixelW, float pixelH,
                                 boolean needsDelete) throws IOException {
        Bitmap bitmap = ratio(imgPath, pixelW, pixelH);
        storeImage(bitmap, outPath);

        // Delete original file
        if (needsDelete) {
            File file = new File(imgPath);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    //将原图压缩并缓存至cache目录(安检圈用,多了文件名添加尺寸标识)
    public LinkedList<FileUpEntity> circleListCompress(LinkedList<FileUpEntity> files) {
        Context context = Utils.getApp();
        String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String cachePath = context == null ? absolutePath + "/cache" : getDiskCacheDir(context);
        if (files != null) {
            for (FileUpEntity file : files) {
                Bitmap bitmap = null;
                String storeFileName = file.getStoreFileName();
                try {
                    if (file.getFileType() == FileUpEntity.FILE_TYPE_VIDEO) {
                        throw new IOException("this file is not a image");
                    }
                    bitmap = ratio(file.getFile().getAbsolutePath(), 480f, 800f);
                    int[] imageSize = getImageInfo(file.getFile().getAbsolutePath());
                    String type = storeFileName
                            .substring(storeFileName.lastIndexOf("."), storeFileName.length());
                    storeFileName = storeFileName
                            .replace(type, "-w-" + imageSize[0] + "-h-" + imageSize[1] + type);
                    file.setStoreFileName(storeFileName);
                    String outPath = cachePath + File.separator + storeFileName;
                    compressAndGenImage(bitmap, outPath, 100);
                    file.setLocalFileName(storeFileName);
                    file.setFile(new File(outPath));
                } catch (IOException e) {
                    //压缩失败时，视为视频文件，直接将源文件改名
                    File videoFile = file.getFile();
                    int[] videoSize = getLocalVideoSize(videoFile.getAbsolutePath());
                    String type = storeFileName
                            .substring(storeFileName.lastIndexOf("."), storeFileName.length());
                    storeFileName = storeFileName
                            .replace(type, "-w-" + videoSize[0] + "-h-" + videoSize[1] + type);
                    file.setStoreFileName(storeFileName);
                    String replaceName = videoFile.getAbsolutePath()
                                                  .replace(file.getLocalFileName(), storeFileName);
                    File newPath = new File(replaceName);
                    videoFile.renameTo(newPath);
                    file.setLocalFileName(storeFileName);
                    file.setFile(newPath);
                    e.printStackTrace();
                } finally {
                    if (bitmap != null) {
                        bitmap.recycle();
                    }
                }
            }
        }
        return files;
    }

    public LinkedList<FileUpEntity> listCompress(LinkedList<FileUpEntity> files) {
        Context context = Utils.getApp();
        String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String cachePath = context == null ? absolutePath + "/cache" : getDiskCacheDir(context);
        if (files != null) {
            for (FileUpEntity file : files) {
                Bitmap bitmap = null;
                String storeFileName = file.getStoreFileName();
                try {
                    if (file.getFileType() == FileUpEntity.FILE_TYPE_VIDEO) {
                        throw new IOException("this file is not a image");
                    }
                    bitmap = ratio(file.getFile().getAbsolutePath(), 480f, 800f);
                    String outPath = cachePath + File.separator + storeFileName;
                    compressAndGenImage(bitmap, outPath, 100);
                    file.setLocalFileName(storeFileName);
                    file.setFile(new File(outPath));
                } catch (FileNotFoundException e) {

                } catch (IOException e) {
                    //压缩失败时，直接将源文件改名
                    File videoFile = file.getFile();
                    String replaceName = videoFile.getAbsolutePath()
                                                  .replace(file.getLocalFileName(), storeFileName);
                    File newPath = new File(replaceName);
                    videoFile.renameTo(newPath);
                    file.setLocalFileName(storeFileName);
                    file.setFile(newPath);
                    e.printStackTrace();
                } finally {
                    if (bitmap != null) {
                        bitmap.recycle();
                    }
                }
            }
        }
        return files;
    }

    public int[] getImageInfo(String imgPath) {
        int[] wh = new int[2];
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true，即只读边不读内容
        newOpts.inJustDecodeBounds = true;
        newOpts.inPreferredConfig = Config.RGB_565;
        // 得到 bitmap 信息, 此时bitmap返回null
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        wh[0] = w;
        wh[1] = h;
        return wh;
    }

    public LinkedList<FileUpEntity> headIconCompress(LinkedList<FileUpEntity> files) {
        try {
            Context context = Utils.getApp();
            String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String cachePath = context == null ? absolutePath + "/cache" : getDiskCacheDir(context);
            FileUpEntity file = files.getFirst();
            Bitmap bitmap = ratio(file.getFile().getAbsolutePath(), 100f, 100f);
            String fileName = file.getStoreFileName();
            String outPath = cachePath + File.separator + fileName;
            compressAndGenImage(bitmap, outPath, 20);
            file.setStoreFileName(fileName);
            file.setLocalFileName(fileName);
            file.setFile(new File(outPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    //    缩略图
    public static Observable<String> getVideoThumbnail(final String videoPath,
                                                       final Context context) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                e.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        Thread.currentThread().interrupt();
                    }
                });

                Bitmap bitmap;
                String finalSavePath = getFinalSavePath(context, videoPath) + "thum.jpg";
                File file = new File(finalSavePath);
                if (!file.exists()) {
                    MediaMetadataRetriever media = new MediaMetadataRetriever();
                    if (videoPath.startsWith("http") || videoPath.startsWith("HTTP")) {
                        media.setDataSource(videoPath, new ArrayMap<String, String>());
                    } else {
                        media.setDataSource(videoPath);
                    }
                    bitmap = media.getFrameAtTime();
                    if (bitmap == null) {
                        if (!e.isDisposed()) {
                            e.onError(new IllegalArgumentException("错误的视频地址"));
                        }
                        return;
                    }
                    FileOutputStream os = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                    bitmap.recycle();
                }
                if (!e.isDisposed()) {
                    e.onNext(finalSavePath);
                    e.onComplete();
                }
            }
        }).observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io());
    }

    public static void setVideoThumbnail(final ImageView image, final String videoPath) {
        getVideoThumbnail(videoPath, image.getContext())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull String s) {
                        ImageLoader.load(image.getContext(), image, "file://" + s);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    public static void playVideo(String videoPath, Context context) {
        if (videoPath != null) {
            Uri uri = Uri.parse(videoPath);
            //调用系统自带的播放器
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Log.v("URI:::::::::", uri.toString());
            intent.setDataAndType(uri, "video/mp4");
            context.startActivity(intent);
        }
    }

    public static void setVideoSize(final boolean forStart, final boolean sizeSetted,
                                    final String videoPath, final TextureVideoView video,
                                    final int reference) {

        Observable.create(new ObservableOnSubscribe<ViewGroup.LayoutParams>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<ViewGroup.LayoutParams> e)
                    throws Exception {
                final ViewGroup.LayoutParams params = video.getLayoutParams();
                if (!sizeSetted) {
                    MediaMetadataRetriever media = new MediaMetadataRetriever();
                    if (videoPath.startsWith("http") || videoPath.startsWith("HTTP")) {
                        media.setDataSource(videoPath, new ArrayMap<String, String>());
                    } else {
                        media.setDataSource(videoPath);
                    }
                    String widthS = media
                            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                    String heightS = media
                            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                    Integer width = Integer.valueOf(widthS);
                    Integer height = Integer.valueOf(heightS);
                    if (width > height) {
                        params.height = reference;
                        params.width = (int) ((((double) height / (double) width)) * reference);
                    } else {
                        params.width = reference;
                        params.height = (int) ((double) width / (double) height * reference);
                    }
                }
                e.onNext(params);
                e.onComplete();
            }
        }).observeOn(Schedulers.io()).subscribeOn(AndroidSchedulers.mainThread())
                  .subscribe(new Consumer<ViewGroup.LayoutParams>() {
                      @Override
                      public void accept(@NonNull ViewGroup.LayoutParams layoutParams)
                              throws Exception {
                          if (!sizeSetted) {
                              video.setLayoutParams(layoutParams);
                          }
                          if (forStart) {
                              video.setVideoPath("file://" + videoPath);
                              video.start();
                          }
                      }
                  });
    }

    /**
     * @return int[]{宽 高}
     */
    public static int[] getLocalVideoSize(String videoPath) {
        int[] size = new int[]{0, 0};
        try {
            MediaMetadataRetriever media = new MediaMetadataRetriever();
            media.setDataSource(videoPath);
            String widthS = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String rotationS = media
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            String heightS = media
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            Integer width = Integer.valueOf(widthS);
            Integer rotation = Integer.valueOf(rotationS);
            Integer height = Integer.valueOf(heightS);
            if (rotation == 0) {
                size[0] = width;
                size[1] = height;
            } else {
                size[1] = width;
                size[0] = height;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 得到视频本地下载的地址
     */
    public static String getSavePath(Context context, String videoPath) {
        return getDiskCacheDir(context) +
                File.separator +
                MD5Util.MD5Encode(videoPath);

    }

    public static String getFinalSavePath(Context context, String videoPath) {
        return getDiskCacheDir(context) +
                File.separator +
                MD5Util.MD5Encode(videoPath) + "i";

    }

    public static boolean downloaded(Context context, String remotePath) {
        String savePath = getFinalSavePath(context, remotePath);
        File file = new File(savePath);
        return file.exists() && file.length() > 100 * 1024;
    }

    public static String getDiskCacheDir(Context context) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getAbsolutePath();
        } else {
            cachePath = context.getCacheDir().getAbsolutePath();
        }
        return cachePath;
    }
}

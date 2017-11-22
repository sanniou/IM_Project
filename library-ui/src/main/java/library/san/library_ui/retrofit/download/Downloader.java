package library.san.library_ui.retrofit.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Predicate;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import library.san.library_ui.retrofit.exception.NetRequestException;
import library.san.library_ui.utils.CloseUtils;
import library.san.library_ui.utils.MD5Util;
import library.san.library_ui.utils.Utils;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Downloader implements DownLoadManager {

    private HashMap<String, Call> downCalls;//用来存放各个下载的请求
    private OkHttpClient mClient;
    private PublishProcessor<DownloadInfo> mProgressSubject;
    private CompositeDisposable mDisposables;
    private String mSavePath;

    private Downloader() {
        downCalls = new HashMap<>();
        mSavePath = Utils.getApp().getExternalCacheDir().getAbsolutePath();
        mClient = new OkHttpClient.Builder().build();
        mProgressSubject = PublishProcessor.create();
        mDisposables = new CompositeDisposable();
        mDisposables.clear();
    }

    /**
     * 开始下载
     *
     * @param url 下载请求的网址
     * @return 返回进度监听的 Flowable
     */
    @Override
    public Flowable<DownloadInfo> download(final String url, final String saveName) {
        if (!downCalls.containsKey(url)) {
            DownloadInfo downInfo = createDownInfo(url, saveName);
            DownloadInfo realDownInfo = getRealFileName(downInfo);
            DownloadSubscribe subscribe = new DownloadSubscribe(realDownInfo);
            Observable.create(subscribe)
                      .observeOn(AndroidSchedulers.mainThread())//在主线程回调
                      .subscribeOn(Schedulers.io())//在子线程执行
                      .subscribe(new DownloadObserver() {
                          @Override
                          public void onNext(DownloadInfo downloadInfo) {
                              super.onNext(downloadInfo);
                              mDownloadInfo.setStatus(DownloadInfo.STATUS_LOADING);
                              mProgressSubject.onNext(mDownloadInfo);
                          }

                          @Override
                          public void onComplete() {
                              downCalls.remove(url);
                              if (mDownloadInfo != null) {
                                  mDownloadInfo.setStatus(DownloadInfo.STATUS_SUCCESS);
                                  mProgressSubject.onNext(mDownloadInfo);
                                  deleteCacheFile(mDownloadInfo.getUrl());
                              }
                          }

                          @Override
                          public void onError(Throwable e) {
                              super.onError(e);
                              downCalls.remove(url);
                              if (mDownloadInfo == null) {
                                  mDownloadInfo = new DownloadInfo(url);
                              }
                              mDownloadInfo.setStatus(DownloadInfo.STATUS_FAILED);
                              mDownloadInfo.setMessage(e);
                              mProgressSubject.onNext(mDownloadInfo);
                          }
                      });
        }
        return getProgressObservable(url);

        /* 链式调用会造成判断没通过时直接发送 onError 或 complete 结果导致最终发送的进度错误
        Observable.just(url)
                  .filter(new Predicate<String>() {//call的map已经有了,就证明正在下载,则这次不下载
                      @Override
                      public boolean test(@NonNull String s) throws Exception {
                          return !downCalls.containsKey(s);
                      }
                  })
                  .flatMap(new Function<String, ObservableSource<DownloadInfo>>() {
                      @Override
                      public ObservableSource<DownloadInfo> apply(@NonNull String s)
                              throws Exception {
                          return Observable.just(createDownInfo(s, saveName));
                      }
                  })
                  .map(new Function<DownloadInfo, DownloadInfo>() {//检测本地文件夹,生成新的文件名

                      @Override
                      public DownloadInfo apply(@NonNull DownloadInfo s) throws Exception {
                          return getRealFileName(s);
                      }
                  })
                  .flatMap(new Function<DownloadInfo, ObservableSource<DownloadInfo>>() {
                      @Override
                      public ObservableSource<DownloadInfo> apply(
                              @NonNull DownloadInfo downloadInfo)
                              throws Exception {
                          return Observable.create(new DownloadSubscribe(downloadInfo));  //下载
                      }
                  })
                  .observeOn(AndroidSchedulers.mainThread())//在主线程回调
                  .subscribeOn(Schedulers.io())//在子线程执行
                  .subscribe(new DownloadObserver() {
                      @Override
                      public void onNext(DownloadInfo downloadInfo) {
                          super.onNext(downloadInfo);
                          mDownloadInfo.setStatus(DownloadInfo.STATUS_LOADING);
                          mProgressSubject.onNext(mDownloadInfo);
                      }

                      @Override
                      public void onComplete() {
                          downCalls.remove(url);
                          if (mDownloadInfo != null) {
                              mDownloadInfo.setStatus(DownloadInfo.STATUS_SUCCESS);
                              mProgressSubject.onNext(mDownloadInfo);
                              deleteCacheFile(mDownloadInfo.getUrl());
                          }
                      }

                      @Override
                      public void onError(Throwable e) {
                          super.onError(e);
                          downCalls.remove(url);
                          if (mDownloadInfo == null) {
                              mDownloadInfo = new DownloadInfo(url);
                          }
                          mDownloadInfo.setStatus(DownloadInfo.STATUS_FAILED);
                          mDownloadInfo.setMessage(e);
                          mProgressSubject.onNext(mDownloadInfo);
                      }
                  });
        //.subscribe(downLoadObserver);//添加观察者
        return getProgressObservable(url);*/

    }
    public Flowable<DownloadInfo> getProgressObservable(final String url) {
        return mProgressSubject.filter(new Predicate<DownloadInfo>() {
            @Override
            public boolean test(@NonNull DownloadInfo downloadInfo)
                    throws Exception {
                return downloadInfo.getUrl().equals(url);
            }
        }).onBackpressureLatest();
    }

    public void cancel(String url) {
        Call call = downCalls.get(url);
        if (call != null) {
            call.cancel();//取消
        }
        downCalls.remove(url);
    }

    public boolean contains(String url) {
        return downCalls.containsKey(url);
    }

    public void cancelAll() {
        for (Map.Entry<String, Call> entry : downCalls.entrySet()) {
            entry.getValue().cancel();
        }
        downCalls.clear();
    }

    /**
     * 创建DownInfo
     *
     * @param url 请求网址
     * @return DownInfo
     */
    private DownloadInfo createDownInfo(String url, String saveName) {
        DownloadInfo downloadInfo = new DownloadInfo(url);
        long contentLength = getContentLength(url);//获得文件大小
        downloadInfo.setTotal(contentLength);
        downloadInfo.setFileName(saveName);
        return downloadInfo;
    }

    private DownloadInfo getRealFileName(DownloadInfo downloadInfo) {
        String fileName = downloadInfo.getFileName();
        long downloadLength = 0, contentLength = downloadInfo.getTotal();
        File file = new File(mSavePath, fileName);
        if (file.exists()) {
            //找到了文件,代表已经下载过,则获取其长度
            downloadLength = file.length();
        }
        //之前下载过,需要重新来一个文件
        int i = 1;
        while ((contentLength != DownloadInfo.TOTAL_ERROR && downloadLength >= contentLength)
                || (contentLength == DownloadInfo.TOTAL_ERROR && downloadLength > 0)) {
            int dotIndex = fileName.lastIndexOf('.');
            String fileNameOther;
            if (dotIndex == -1) {
                fileNameOther = fileName + "(" + i + ")";
            } else {
                fileNameOther = fileName.substring(0, dotIndex)
                        + "(" + i + ")" + fileName.substring(dotIndex);
            }
            File newFile = new File(mSavePath, fileNameOther);
            file = newFile;
            downloadLength = newFile.length();
            i++;
        }
        //设置改变过的文件名/大小
        downloadInfo.setProgress(downloadLength);
        downloadInfo.setFileName(file.getName());
        return downloadInfo;
    }

    public String getSavePath() {
        return mSavePath;
    }

    private class DownloadSubscribe implements ObservableOnSubscribe<DownloadInfo> {

        private DownloadInfo downloadInfo;

        public DownloadSubscribe(DownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
        }

        @Override
        public void subscribe(ObservableEmitter<DownloadInfo> e) throws Exception {
            String url = downloadInfo.getUrl();
            long downloadLength = downloadInfo.getProgress();//已经下载好的长度
            long contentLength = downloadInfo.getTotal();//文件的总长度
            //初始进度信息
            e.onNext(downloadInfo);

            HttpUrl parsed = HttpUrl.parse(url);
            if (parsed == null) {
                throw new NetRequestException("文件不存在");
            }

            Request request = new Request.Builder()
                    //确定下载的范围,添加此头,则服务器就可以跳过已经下载好的部分
                    .addHeader("RANGE", "bytes=" + downloadLength + "-" +
                            (contentLength == DownloadInfo.TOTAL_ERROR ? "" : contentLength))
                    .url(url)
                    .build();
            Call call = mClient.newCall(request);
            downCalls.put(url, call);//把这个添加到call里,方便取消
            Response response = call.execute();

            if (response != null && response.isSuccessful()) {
                long length = response.body().contentLength();
                downloadInfo.setTotal(length);
                createFile(url, length);
            } else {
                if (response == null) {
                    throw new NetRequestException("下载错误");
                }
                switch (response.code()) {
                    case 404:
                        throw new NetRequestException("文件不存在");
                    default:
                        throw new NetRequestException("下载错误");
                }
            }

            File file = new File(mSavePath,
                    downloadInfo.getFileName());
            InputStream is = null;
            FileOutputStream fileOutputStream = null;
            try {
                is = response.body().byteStream();
                fileOutputStream = new FileOutputStream(file, true);
                byte[] buffer = new byte[2048];//缓冲数组2kB
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, len);
                    downloadLength += len;
                    downloadInfo.setProgress(downloadLength);
                    e.onNext(downloadInfo);
                }
                fileOutputStream.flush();
                downCalls.remove(url);
            } finally {
                //关闭IO流
                CloseUtils.closeIO(is, fileOutputStream);

            }
            e.onComplete();//完成
        }

    }

    /**
     * 获取下载长度
     */
    public long getContentLength(String downloadUrl) {
        String encode = MD5Util.MD5Encode(downloadUrl);
        File file = new File(mSavePath, encode);
        if (file.exists()) {
            return file.length();
        } else {
            return DownloadInfo.TOTAL_ERROR;
        }
    }

    private void createFile(String downloadUrl, long length) throws IOException {
        String encode = MD5Util.MD5Encode(downloadUrl);
        File file = new File(mSavePath, encode);
        if (file.exists()) {
            if (file.length() == length) {
                return;
            } else {
                file.delete();
            }
        }
        RandomAccessFile r = null;
        try {
            r = new RandomAccessFile(file, "rw");
            r.setLength(length);
        } finally {
            CloseUtils.closeIO(r);
        }
    }

    private void deleteCacheFile(String downloadUrl) {
        String encode = MD5Util.MD5Encode(downloadUrl);
        File file = new File(mSavePath, encode);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

}
package library.san.library_ui.retrofit.download;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public abstract class DownloadObserver implements Observer<DownloadInfo> {

    protected Disposable mDisposable;//可以用于取消注册的监听者
    protected DownloadInfo mDownloadInfo;

    @Override
    public void onSubscribe(Disposable d) {
        this.mDisposable = d;
    }

    @Override
    public void onNext(DownloadInfo downloadInfo) {
        mDownloadInfo = downloadInfo;
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }

}
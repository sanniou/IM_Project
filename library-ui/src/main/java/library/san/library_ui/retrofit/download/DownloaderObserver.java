package library.san.library_ui.retrofit.download;

import io.reactivex.annotations.NonNull;
import io.reactivex.subscribers.DisposableSubscriber;

public abstract class DownloaderObserver extends DisposableSubscriber<DownloadInfo> {

    protected DownloadInfo mDownloadInfo;

    @Override
    protected void onStart() {
        request(1);
    }

    @Override
    public void onNext(DownloadInfo downloadInfo) {
        mDownloadInfo = downloadInfo;
        switch (downloadInfo.getStatus()) {
            case DownloadInfo.STATUS_LOADING:
                onLoading(downloadInfo);
                break;
            case DownloadInfo.STATUS_SUCCESS:
                onSuccess(downloadInfo);
                break;
            case DownloadInfo.STATUS_FAILED:
                onFailed(downloadInfo);
                break;
            default:
        }
        request(1);
    }

    protected abstract void onFailed(DownloadInfo info);

    protected abstract void onSuccess(DownloadInfo info);

    protected abstract void onLoading(DownloadInfo info);

    @Override
    public void onError(@NonNull Throwable e) {
        if (mDownloadInfo == null) {
            mDownloadInfo = new DownloadInfo("");
            mDownloadInfo.setMessage(e);
        }
        onFailed(mDownloadInfo);
    }

    @Override
    public void onComplete() {

    }

}
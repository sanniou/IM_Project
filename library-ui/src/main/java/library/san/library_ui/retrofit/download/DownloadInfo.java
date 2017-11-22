package library.san.library_ui.retrofit.download;

public class DownloadInfo {

    public static final int STATUS_LOADING = 0;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_FAILED = 2;

    public static final long TOTAL_ERROR = -1;//获取进度失败
    private int status;
    private String url;
    private long total;
    private long progress;
    private String fileName;
    private Throwable mMessage;

    public DownloadInfo(String url) {
        this.url = url;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setMessage(Throwable message) {
        mMessage = message;
    }

    public Throwable getMessage() {
        return mMessage;
    }
}

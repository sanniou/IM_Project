package library.san.library_ui.retrofit.upload;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import library.san.library_ui.retrofit.config.IMRetrofit;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Uploader implements UpLoadManager {

    @Override
    public void uploadFile(String url, boolean isImage, final File file, Map<String, String> params,
                           final UploadProgressListener nothing) {
        //1.构建MultipartBody
        //2.构建RequestBody
        MultipartBody.Builder builder = getProgressRequestBody(isImage, file, nothing);
        //3.追加url定义的参数
        for (String key : params.keySet()) {
            String value = params.get(key);
            builder.addFormDataPart(key, value);
        }
        RequestBody requestBody = builder.build();
        //开始请求前回调监听
        nothing.onStart();
        IMRetrofit.getHttpClient()
                  .newCall(new Request.Builder()
                        .post(requestBody)
                        .url(url)
                        .build())
                  .enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        nothing.onFailed(call, e, file.getName());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response)
                            throws IOException {
                        nothing.onSuccess(call, response);
                    }
                });
    }

    public MultipartBody.Builder getProgressRequestBody(Boolean isImage, File file,
                                                        final UploadProgressListener nothing) {
        RequestBody requestFile = RequestBody
                .create(isImage ? MediaType.parse("image/jpeg") : MultipartBody.FORM, file);
        ProgressRequestBody requestBody = new ProgressRequestBody(requestFile,
                new ProgressRequestBody.RequestProgressListener() {
                    @Override
                    public void onProgress(long currentBytesCount, long totalBytesCount) {
                        nothing.onProgress(currentBytesCount, totalBytesCount);
                    }
                });

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.addFormDataPart("file", file.getName(), requestBody);
        builder.setType(MultipartBody.FORM);
        return builder;
    }

    public interface UploadProgressListener {

        void onStart();

        void onProgress(long currentCount, long totalCount);

        void onSuccess(Call call, Response requestBody);

        void onFailed(Call call, Throwable t, String name);
    }

}




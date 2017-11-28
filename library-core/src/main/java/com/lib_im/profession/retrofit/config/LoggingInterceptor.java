package com.lib_im.profession.retrofit.config;

import android.util.Log;

import java.io.IOException;
import java.util.Locale;

import io.reactivex.annotations.NonNull;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 打印网络日志类，打印网络请求具体信息
 */
public class LoggingInterceptor implements Interceptor {

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        //这个chain里面包含了request和response，所以你要什么都可以从这里拿
        Request request = chain.request();

        //请求发起的时间
        long t1 = System.nanoTime();
        Log.i("im",
                String.format("发送请求 %s on %s%n%s %n body: %s", request.url(), chain.connection(),
                        "" + request.headers(), ""));

        Response response = chain.proceed(request);

        //收到响应的时间
        long t2 = System.nanoTime();

        //这里不能直接使用response.body().string()的方式输出日志
        //因为response.body().string()之后，response中的流会被关闭，程序会报错，我们需要创建出一
        //个新的response给应用层处理
        ResponseBody responseBody = response.peekBody(1024L * 1024L);

        Log.i("im", String.format(Locale.getDefault(), "接收响应: [%s] %n返回json:%s %.1fms%n%s",
                response.request().url(), responseBody.string(), (t2 - t1) / 1e6d,
                response.headers()));

        return response;
    }
}
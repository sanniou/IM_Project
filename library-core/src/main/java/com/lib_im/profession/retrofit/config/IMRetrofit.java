package com.lib_im.profession.retrofit.config;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * 网络请求工具类
 */

public class IMRetrofit {

    private static final int TIMEOUT = 10_000;
    private static final String BASE_URL = "http://222.132.114.42:8888/mls-anShun/api/";
    private Retrofit retrofit;
    private OkHttpClient okHttpClient;
    private static IMRetrofit sCommonRetrofit;

    private IMRetrofit() {
        okHttpClient = new OkHttpClient
                .Builder()
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(new LoggingInterceptor())
                .build();

        retrofit = new Retrofit
                .Builder()
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
    }

    public static Retrofit getRetrofit() {
        if (sCommonRetrofit == null) {
            synchronized (IMRetrofit.class) {
                if (sCommonRetrofit == null) {
                    sCommonRetrofit = new IMRetrofit();
                }
            }
        }
        return sCommonRetrofit.retrofit;
    }
}

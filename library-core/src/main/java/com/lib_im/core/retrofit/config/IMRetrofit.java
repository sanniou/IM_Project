package com.lib_im.core.retrofit.config;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 网络请求工具类
 */

public class IMRetrofit {

    public static final int TIMEOUT = 10_000;
    public static final String BASE_URL = "http://192.168.253.7:8089/api/";
    public Retrofit retrofit;
    public OkHttpClient okHttpClient;
    public static IMRetrofit sCommonRetrofit;

    private IMRetrofit() {
        okHttpClient = new OkHttpClient
                .Builder()
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .addNetworkInterceptor(new StethoInterceptor())
                .addInterceptor(new LoggingInterceptor())
                .build();

        retrofit = new Retrofit
                .Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
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

package com.lib_im.pro.retrofit.exception;

import com.google.gson.JsonParseException;
import library.san.library_ui.utils.LogUtils;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import retrofit2.HttpException;

public class ExceptionEngine {

    private ExceptionEngine() {
        throw new IllegalArgumentException("what are you doing?");
    }

    //对应HTTP的状态码
    private static final int UNAUTHORIZED = 401;
    private static final int FORBIDDEN = 403;
    private static final int NOT_FOUND = 404;
    private static final int REQUEST_TIMEOUT = 408;
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final int BAD_GATEWAY = 502;
    private static final int SERVICE_UNAVAILABLE = 503;
    private static final int GATEWAY_TIMEOUT = 504;

    /**
     * 适用于 retrofit + rxJava onError 中对 Exception 进行适配 提示
     */
    public static NetRequestException handleException(Throwable e) {
        NetRequestException ex;
        if (e instanceof HttpException) {             //HTTP错误
            HttpException httpException = (HttpException) e;
            switch (httpException.code()) {
                case UNAUTHORIZED:
                case FORBIDDEN:
                case NOT_FOUND:
                case REQUEST_TIMEOUT:
                case GATEWAY_TIMEOUT:
                case INTERNAL_SERVER_ERROR:
                case BAD_GATEWAY:
                case SERVICE_UNAVAILABLE:
                default:
                    ex = new NetRequestException("网络错误");//均视为网络错误
                    break;
            }
            return ex;
        } else if (e instanceof ApiErrorException //服务器返回的错误
                || e instanceof EmptyDateException   //返回空数据，暂时没用上
                || e instanceof AppErrorException   //用户操作的错误
                || e instanceof NetRequestException) {
            return (NetRequestException) e;
        } else if (e instanceof JsonParseException) {
            ex = new NetRequestException("解析错误");//均视为解析错误
            return ex;
        } else if (e instanceof ConnectException) {
            ex = new NetRequestException("连接失败");//均视为网络错误
            return ex;
        } else if (e instanceof SocketTimeoutException) {
            ex = new NetRequestException("连接超时");//均视为网络错误
            return ex;
        } else {
            LogUtils.e(e);
            ex = new NetRequestException("未知错误");//未知错误
            return ex;
        }
    }

    public static String handleMessage(Throwable e) {
        return handleException(e).getMessage();
    }
}

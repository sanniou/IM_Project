package com.lib_im.core.retrofit.base;

import com.lib_im.core.retrofit.exception.ApiErrorException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * Created by songgx on 2017/9/26.
 * 网络请求处理的第一步，在请求结束后，验证返回的 BaseResponse code 是否成功，失败则抛出异常
 */

public class BaseResponseMapper<T> implements Function<BaseResponse<T>, ObservableSource<T>> {

    private static final int REQUEST_SUCCESS_CODE = 0;
    private static final int REQUEST_FAILED_CODE = 1;

    private String message;

    public BaseResponseMapper() {
    }

    public BaseResponseMapper(String message) {
        this.message = message;
    }

    @Override
    public ObservableSource<T> apply(@NonNull BaseResponse<T> result)
            throws Exception {
        if (result.getCode() != REQUEST_SUCCESS_CODE) {
            return Observable
                    .error(new ApiErrorException(message == null ? result.getMessage() : message));
        }
        T t = result.getResult();
        if (t == null) {
            return Observable.empty();
        }
        return Observable.just(t);
    }
}

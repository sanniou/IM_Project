package com.lib_im.pro.retrofit.base;

import com.lib_im.pro.retrofit.exception.ApiErrorException;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * 网络请求处理的第一步，在请求结束后，验证返回的 BaseResponse code 是否成功，失败则抛出异常
 * <p>
 * client.request()
 * .flatMap(new BaseListResponseMapper<T>())
 * </p>
 */
public class BaseListResponseMapper<T>
        implements Function<BaseListResponse<T>, ObservableSource<List<T>>> {

    private static final int REQUEST_SUCCESS_CODE = 0;
    private static final int REQUEST_FAILED_CODE = 1;

    private String message;

    public BaseListResponseMapper() {
    }

    public BaseListResponseMapper(String message) {
        this.message = message;
    }

    @Override
    public ObservableSource<List<T>> apply(@NonNull BaseListResponse<T> result)
            throws Exception {
        if (result.getCode() != REQUEST_SUCCESS_CODE) {
            return Observable
                    .error(new ApiErrorException(message == null ? result.getMessage() : message));
        }
        List<T> rows = result.getResult();
        if (rows == null) {
            return Observable.empty();
        }
        // rows 不为空时，去掉可能有的 null，再返回数据
        rows.removeAll(Collections.<T>singleton(null));
        return Observable.just(rows);
    }
}

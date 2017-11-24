package com.lib_im.core.retrofit.rx;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Created by songgx on 2017/9/26.
 * 用于请求数据接口对应 的 rx 实现，此时返回结果 T 的 Object 类型
 */

public abstract class SimpleObserver<T> implements Observer<T> {
    @Override
    public void onSubscribe(@NonNull Disposable d) {

    }

    @Override
    public void onComplete() {

    }
}

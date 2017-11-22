package com.lib_im.pro.rx;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Created by songgx on 2017/9/26.
 * 用于提交表单的接口对应 的 rx 实现，此时返回结果只有 code，所以不需要 onNext ，直接在 onComplete 中操作
 */

public abstract class SimpleObjectCompleteObserver<T> implements Observer<T> {
    @Override
    public void onSubscribe(@NonNull Disposable d) {

    }

    @Override
    public void onNext(@NonNull T t) {

    }
}

package com.lib_im.pro.rx;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * 用于提交表单的接口对应 的 rx 实现，此时返回结果只有 code，所以不需要 onNext ，直接在 onComplete 中操作
 *
 * @param <T> 无实际意义
 */
public abstract class SimpleListCompleteObserver<T> implements Observer<List<T>> {

  @Override
  public void onNext(@NonNull List<T> t) {

  }

  @Override
  public void onSubscribe(@NonNull Disposable d) {

  }
}

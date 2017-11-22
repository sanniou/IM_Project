package com.lib_im.pro.rx;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * 用于请求数据接口对应 的 rx 实现，此时返回结果 T 的 List 类型
 */
public abstract class SimpleListObserver<T> implements Observer<List<T>> {

  @Override
  public void onSubscribe(@NonNull Disposable d) {

  }

  @Override
  public void onComplete() {

  }
}

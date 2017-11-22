package library.san.library_ui.base;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.lib_im.pro.R;
import library.san.library_ui.utils.BarUtils;
import library.san.library_ui.utils.ScreenUtils;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.BehaviorSubject;

/**
 * author : songgx
 * time   : 2017/06/16
 * desc   : Activity 的基类，通用方法可写在这里,所有 Activity 继承 BaseActivity
 * version: 1.0
 */
public abstract class BaseActivity extends AppCompatActivity {

    private int mStatusColor = R.color.app_color;
    private int mNavigationColor = R.color.app_color;
    private boolean mIsPortrait = true;

    // 使用 rx 对 Activity 生命周期绑定，发送事件
    protected BehaviorSubject<Lifecycle> mLifeSubject = BehaviorSubject.create();

    /**
     * 基类中设置状态栏颜色
     * 可以在super.onCreate(savedInstanceState)调用前使用
     * setStatusColor(Color.TRANSPARENT)改变色值
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 默认竖屏不可切换
        if (mIsPortrait) {
            ScreenUtils.setPortrait(this);
        } else {
            ScreenUtils.setLandscape(this);
        }
        mLifeSubject.onNext(Lifecycle.ON_CREATE);
        //setNavigationBar();
        setImmersiveStatusBar();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mLifeSubject.onNext(Lifecycle.ON_START);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLifeSubject.onNext(Lifecycle.ON_DESTROY);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mLifeSubject.onNext(Lifecycle.ON_RESTART);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLifeSubject.onNext(Lifecycle.ON_RESUME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLifeSubject.onNext(Lifecycle.ON_PAUSE);
    }

    /**
     * 在 api21 系统设置导航栏颜色
     */
    private void setNavigationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(mNavigationColor));
        }
    }

    /**
     * 设置沉浸式状态栏
     */
    protected void setImmersiveStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setLightStatusBar(true);
        } else {
            // android 6.0 以下且颜色为白色，修改为灰色
            if (getResources().getColor(mStatusColor) == Color.WHITE) {
                mStatusColor = R.color.app_gray;
            }
            setTranslucentStatus();
        }
    }

    /**
     * 设置状态栏透明
     */
    private void setTranslucentStatus() {
        BarUtils.setStatusBarColor(this, getResources().getColor(mStatusColor));
    }

    /**
     * 设置Android状态栏的字体颜色，状态栏为亮色的时候字体和图标是黑色，状态栏为暗色的时候字体和图标为白色
     *
     * @param dark 状态栏字体是否为深色
     */
    protected void setLightStatusBar(boolean dark) {
        View view = getWindow().getDecorView();
        if (dark) {
            BarUtils.setLightStatusBar(this);
        } else {
            BarUtils.clearLightStatusBar(this);
        }
    }

    /**
     * 改变即将设置给状态栏的色值
     * 在super.onCreate(savedInstanceState)调用前使用
     * <p>
     * protected void onCreate(Bundle savedInstanceState) {
     * setNavigationColor(R.color.red);
     * super.onCreate(savedInstanceState);
     * }
     * </p>
     */

    public void setStatusColor(int statusColor) {
        mStatusColor = statusColor;
    }

    /**
     * 改变即将设置给导航栏的色值
     * 在super.onCreate(savedInstanceState)调用前使用
     */

    public void setNavigationColor(int navigationColor) {
        mStatusColor = navigationColor;
    }

    protected <T> ObservableTransformer<T, T> bindLife() {
        return bindLife(Lifecycle.ON_DESTROY);
    }

    /**
     * 使用 RxJava 对 生命周期进行响应
     * 使用
     * <p>
     * Observable.create()
     * //转换 Observable ，相当于追加操作,其实直接使用 takeUntil 操作符也可以，不过为了更好的封装
     * .compose(bindLife())
     * .……………………;
     * </p>
     *
     * @param <T> 原 Observable 的类型
     * @return ObservableTransformer
     */
    protected <T> ObservableTransformer<T, T> bindLife(final Lifecycle stop) {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                // takeUntil ：原 Observable 持续发送数据，直到 传入的 ObservableSource 开始发送数据
                // skipWhile : 跳过事件直到有第一个满足条件的事件为止，可以用来扩展响应更多的事件
                // 以下作用就是 当 s=4 (不严谨的条件)时，mLifeSubject 开始发送事件, upstream 的订阅者（下游）都执行 onComplete，
                // upstream 的 Observer 自动解除订阅，可以免除因为持有外部 Observer 导致的内存泄漏
                return upstream.takeUntil(mLifeSubject.skipWhile(new Predicate<Lifecycle>() {
                    @Override
                    public boolean test(Lifecycle s) throws Exception {
                        return s != stop;
                    }
                }));
            }
        };
    }

    /**
     * 提供给 titleView 的默认实现
     */
    public void onBackClick(View v) {
        onBackPressed();
    }

    public void setPortrait(boolean portrait) {
        mIsPortrait = portrait;
    }
}

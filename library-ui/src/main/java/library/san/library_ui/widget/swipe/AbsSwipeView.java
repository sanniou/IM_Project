package library.san.library_ui.widget.swipe;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * @author saniou
 *         下拉控件的基类,用于{@link LSwipeRefreshLayout}
 *         layout将滑动状态通过{@link #scrollOffset(float, int)}和{@link #stopScroll(float)}交由SwiperView来处理
 */
public abstract class AbsSwipeView extends ViewGroup implements SwipeView {

    protected int mState;//存储state状态
    protected boolean mRefreshing;//刷新状态
    protected OnRequestListener mRefreshListener;

    private boolean mCanRequest = true;

    public boolean isCanRequest() {
        return mCanRequest;
    }

    public void setCanRequest(boolean canRequest) {
        mCanRequest = canRequest;
    }

    public AbsSwipeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setRefreshing() {
        if (mState == STATE_REFRESH) {
            return;
        }
        mRefreshing = true;
        refreshAction();
        mState = STATE_REFRESH;
        if (mRefreshListener != null) {
            mRefreshListener.onRequest();
        }
    }

    public void setNormal() {
        if (mState == STATE_NORMAL) {
            return;
        }
        mRefreshing = false;
        mState = STATE_NORMAL;
        normalAction();
    }

    public void setPulling() {
        if (mState == STATE_PULLING) {
            return;
        }
        mRefreshing = false;
        mState = STATE_PULLING;
        pullAction();
    }

    @Override
    public void setFailed() {
        if (mState == STATE_FAILED) {
            return;
        }
        mRefreshing = false;
        mState = STATE_FAILED;
        failedAction();
    }

    public void setComplete() {
        if (mState == STATE_COMPLETE) {
            return;
        }
        mRefreshing = false;
        mState = STATE_COMPLETE;
        completeAction();
    }

    public void setOnRefreshListener(OnRequestListener refreshListener) {
        mRefreshListener = refreshListener;
    }

    //抽象方法，状态改变时的操作
    protected abstract void refreshAction();

    protected abstract void normalAction();

    protected abstract void pullAction();

    protected abstract void completeAction();

    protected abstract void failedAction();

    public int getState() {
        return mState;
    }

    public boolean isRefreshing() {
        return mRefreshing;
    }
}

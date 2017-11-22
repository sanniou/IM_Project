package library.san.library_ui.widget.swipe;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;

/**
 * 下拉刷新控件
 * 可在layout之前使用{@link #setHeader}添加自定义头部，若未设置，添加默认头部{@link #createHeader}
 */

public class LSwipeRefreshLayout extends ViewGroup
        implements NestedScrollingParent, NestedScrollingChild {

    private static final String TAG = LSwipeRefreshLayout.class.getSimpleName();
    private static final int STATE_LOAD_MORE = 861;
    private static final int STATE_REFRESH = 219;
    private static final int STATE_NORMAL = 718;
    private static final int INVALID_POINTER = -1;

    private int mState = STATE_NORMAL;
    private View mTarget; // 主控件
    SwipeView mHeader;//顶部下拉控件
    SwipeView mFooter;//底部上拉控件
    private float mTotalUnconsumed;//计算空间高度

    //using for nested scrolling
    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private final NestedScrollingChildHelper mNestedScrollingChildHelper;
    private final int[] mParentScrollConsumed = new int[2];
    private final int[] mParentOffsetInWindow = new int[2];
    private boolean mNestedScrollInProgress;

    //using for normal scrolling
    private int mTouchSlop;//在被判定为滚动之前用户手指可以移动的最大值。
    private float mInitialDownY;//初始Y点
    private boolean mIsBeingDragged;//滚动状态
    private int mActivePointerId = INVALID_POINTER;//位移焦点
    private SparseArrayCompat<Float> lastY = new SparseArrayCompat<>();//用于计算多点触控Y轴的偏移量

    private OnChildScrollUpCallback mChildScrollUpCallback;//用于自定义下拉判断条件的回调方法
    private boolean mCanLoadMore = false;
    private SwipeView.OnRequestListener mRefreshListener;
    private SwipeView.OnRequestListener mLoadMoreListener;

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public LSwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public LSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setWillNotDraw(false);
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
        //
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        if (mHeader == null) {
            createHeader();
        }
        if (mFooter == null) {
            createFooter();
        }
    }

    private void createHeader() {
        setHeader(new NormalHeader(getContext()));
    }

    @Override
    public void addView(View child) {
        super.addView(child);
    }

    private void createFooter() {
        setFooter(new ParallaxFooter(getContext()));
    }

    public void setHeader(SwipeView header) {
        if (header == mHeader) {
            return;
        }

        View view = (View) header;
        if (mHeader != null) {
            removeView((View) mHeader);
        }
        if (mRefreshListener != null) {
            header.setOnRefreshListener(mRefreshListener);
        }
        mHeader = header;
        LayoutParams headerParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
        addView(view, headerParams);
    }

    public void setFooter(SwipeView footer) {
        if (footer == mFooter) {
            return;
        }
        View view = (View) footer;
        if (mFooter != null) {
            removeView((View) mFooter);
        }
        if (mLoadMoreListener != null) {
            footer.setOnRefreshListener(mLoadMoreListener);
        }
        footer.setCanRequest(mCanLoadMore);
        mFooter = footer;
        LayoutParams headerParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
        addView(view, headerParams);
    }

    public void setOnRefreshListener(SwipeView.OnRequestListener listener) {
        mRefreshListener = listener;
        if (mHeader != null) {
            mHeader.setOnRefreshListener(listener);
        }
    }

    public void setOnLoadMoreListener(SwipeView.OnRequestListener listener) {
        mLoadMoreListener = listener;
        if (mFooter != null) {
            mFooter.setOnRefreshListener(listener);
        }
    }

    /**
     * 开始刷新
     */
    public void startRefresh() {
        if (!isRefreshing()) {
            mHeader.setRefreshing();
        }
    }

    /**
     * 停止刷新
     */
    public void stopRefresh(boolean success) {
        if (isRefreshing()) {
            if (success) {
                mHeader.setComplete();
            } else {
                mHeader.setFailed();
            }
        }
    }

    /**
     * 开始加载
     */
    public void startLoadMore() {
        if (!isLoadMore()) {
            mFooter.setRefreshing();
        }
    }

    /**
     * 停止加载
     */
    public void stopLoadMore(boolean success) {
        if (isLoadMore()) {
            if (success) {
                mFooter.setComplete();
            } else {
                mFooter.setFailed();
            }
        }
    }

    public boolean isRefreshing() {
        return mHeader != null && mHeader.isRefreshing();
    }

    public boolean isLoadMore() {
        return mFooter != null && mFooter.isRefreshing();
    }

    private void ensureTarget() {
        if (mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(mHeader) && !child.equals(mFooter)) {
                    mTarget = child;
                    break;
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mTarget == null) {
            ensureTarget();
        }
        View header = (View) mHeader;
        header.bringToFront();
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int headHeight = header.getMeasuredHeight();
        int targetHeight = mTarget.getMeasuredHeight();
        int targetWidth = mTarget.getMeasuredWidth();
        int childWidth = measuredWidth - paddingRight - paddingLeft;
        header.layout(paddingLeft, 0, childWidth, headHeight);
        if (mFooter != null) {
            View footer = (View) mFooter;
            footer.bringToFront();
            int footHeight = footer.getMeasuredHeight();
            //在ListView测试中，底部控件上拉而改变ListView高度，ListView内部会向上滚动相应的距离
            footer.layout(paddingLeft, measuredHeight - footHeight, childWidth, measuredHeight);
        }
        //mTarget.layout(paddingLeft, headHeight + paddingTop, targetWidth, headHeight + paddingTop+targetHeight);
        //在RecyclerView测试中，由于NestedScrolling机制，layout高度加上headHeight会因为recyclerView在布局中的变化向下产生一个向上“滑动”的误差，
        // 所以布局中不动，在HeadView中使用target.setTranslationY(mHeight);
        //注意：此时在刷新时会使target超出Layout而显示不全
        mTarget.layout(paddingLeft, paddingTop, targetWidth, paddingTop + targetHeight);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mTarget == null) {
            ensureTarget();
        }
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    private boolean canChildScrollUp() {
        if (mChildScrollUpCallback != null) {
            return mChildScrollUpCallback.canChildScrollUp(this, mTarget);
        }
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0 && (absListView.getFirstVisiblePosition() > 0
                        || absListView.getChildAt(0).getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mTarget, -1) || mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    private boolean canChildScrollDown() {
        if (mTarget == null) {
            return false;
        }
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0 && (absListView.getLastVisiblePosition()
                        == absListView.getChildCount() - 1
                        || absListView.getChildAt(absListView.getChildCount() - 1).getBottom()
                        <= absListView.getPaddingBottom());
            } else {
                return ViewCompat.canScrollVertically(mTarget, 1) || mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, 1);
        }
    }

    /**
     * 设置后在 {@link #canChildScrollUp() }中优先使用该方法判断能否下拉
     */
    public void setOnChildScrollUpCallback(@Nullable OnChildScrollUpCallback callback) {
        mChildScrollUpCallback = callback;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex;
        if (!isEnabled() || isRefreshing() || isLoadMore() || mNestedScrollInProgress) {
            return super.dispatchTouchEvent(ev);
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                mInitialDownY = ev.getY(pointerIndex);
                //必须加上滚动判定的偏移量
                lastY.put(mActivePointerId, ev.getY(pointerIndex) + mTouchSlop);
                mTotalUnconsumed = 0;
                break;

            case MotionEvent.ACTION_MOVE: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex > -1) {
                    final float y = ev.getY(pointerIndex);
                    startDragging(y);
                    float dy = y - lastY.get(mActivePointerId);
                    lastY.put(mActivePointerId, y);
                    if (mIsBeingDragged) {
                        if ((mTotalUnconsumed > 0 || dy > 0) && !canChildScrollUp()) {
                            moveHead(mTotalUnconsumed, (int) dy);
                            return true;
                        } else if ((mTotalUnconsumed > 0 || dy < 0) && !canChildScrollDown()) {
                            moveFoot(mTotalUnconsumed, (int) dy);
                            return true;
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                pointerIndex = MotionEventCompat.getActionIndex(ev);
                mActivePointerId = ev.getPointerId(pointerIndex);
                lastY.put(mActivePointerId, ev.getY(pointerIndex));
                break;
            }

            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                lastY.put(mActivePointerId, 0F);
                if (mIsBeingDragged) {
                    stopScrollToNormal();
                    mIsBeingDragged = false;
                }
                mActivePointerId = INVALID_POINTER;
        }

        return super.
                            dispatchTouchEvent(ev);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {

        if ((android.os.Build.VERSION.SDK_INT < 21 && mTarget instanceof AbsListView) || (mTarget
                != null && !ViewCompat.isNestedScrollingEnabled(mTarget))) {
            // Nope.
        } else {
            super.requestDisallowInterceptTouchEvent(b);
        }
    }

    // NestedScrollingParent

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return isEnabled()
                && !isRefreshing()
                && !isLoadMore()
                && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;//如果是纵向返回true
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        // Reset the counter of how much leftover scroll needs to be consumed.
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        // Dispatch up to the nested parent
        startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL);
        mTotalUnconsumed = 0;
        mNestedScrollInProgress = true;
    }

    @Override
    public void onStopNestedScroll(View target) {
        mNestedScrollingParentHelper.onStopNestedScroll(target);
        mNestedScrollInProgress = false;
        if (mTotalUnconsumed > 0) {
            mTotalUnconsumed = 0;
        }
        stopScrollToNormal();

        stopNestedScroll();
    }

    @Override
    public void onNestedScroll(final View target, final int dxConsumed, final int dyConsumed,
                               final int dxUnconsumed, final int dyUnconsumed) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                mParentOffsetInWindow);

        final int dy = dyUnconsumed + mParentOffsetInWindow[1];
        // 下滑且能被下拉
        if (dy < 0 && !canChildScrollUp()) {
            moveHead(mTotalUnconsumed, -dy);
        } else
            // 上滑且能被上拉
            if (dy > 0 && !canChildScrollDown()) {
                moveFoot(mTotalUnconsumed, -dy);
            }
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        //如果mHeadView或者mFooterView未隐藏
        if (mTotalUnconsumed > 0) {
            //如果是上滑且是下拉状态，消耗掉dy
            if (dy > 0 && mState == STATE_REFRESH) {
                if (dy > mTotalUnconsumed) {
                    consumed[1] = dy - (int) mTotalUnconsumed;
                } else {
                    consumed[1] = dy;
                }
                moveHead(mTotalUnconsumed, -dy);
                //如果是上下滑且是上拉状态
            } else if (dy < 0 && mState == STATE_LOAD_MORE) {
                if (-dy > mTotalUnconsumed) {
                    consumed[1] = dy - (int) mTotalUnconsumed;
                } else {
                    consumed[1] = dy;
                }
                moveFoot(mTotalUnconsumed, -dy);
            }
        }
        // Now let our nested parent consume the leftovers
        final int[] parentConsumed = mParentScrollConsumed;
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
        }
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        //Log.e(TAG, "onNestedPreFling11: " + velocityX + "      " + velocityY);
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        //Log.e(TAG, "onNestedPreFling22: " + velocityX + "      " + velocityY + consumed);
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    // NestedScrollingChild

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper
                .dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed,
                        dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper
                .dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    /**
     * @param overScrollTop
     * @param dy
     */

    private void moveHead(float overScrollTop, int dy) {
        mState = STATE_REFRESH;
        setTargetOffsetTopAndBottom(overScrollTop, dy, true /* requires update */);
    }

    private void moveFoot(float overScrollTop, int dy) {
        mState = STATE_LOAD_MORE;
        setTargetOffsetTopAndBottom2(overScrollTop, dy, true /* requires update */);
    }

    /**
     * 判断是否开始下拉(或者上拉)
     *
     * @param y 移动量
     */
    private void startDragging(float y) {
        final float yDiff = Math.abs(y - mInitialDownY);
        if (yDiff > mTouchSlop && !mIsBeingDragged) {
            mIsBeingDragged = true;
        }
    }

    public View getTarget() {
        return mTarget;
    }

    /**
     * 头部的计算交由Header
     *
     * @param totalHeight    SwipeRefreshLayout统计的head高度
     * @param offset         需要处理的滑动距离
     * @param requiresUpdate 需要重绘
     */
    void setTargetOffsetTopAndBottom(float totalHeight, int offset, boolean requiresUpdate) {

        mTotalUnconsumed = mHeader.scrollOffset(totalHeight, offset);//将累计高度限制在返回值
        if (requiresUpdate && android.os.Build.VERSION.SDK_INT < 11) {
            invalidate();
        }
    }

    void setTargetOffsetTopAndBottom2(float totalHeight, int offset, boolean requiresUpdate) {

        mTotalUnconsumed = mFooter.scrollOffset(totalHeight, offset);//将累计高度限制在返回值
        if (requiresUpdate && android.os.Build.VERSION.SDK_INT < 11) {
            invalidate();
        }
    }

    /**
     * 多点触控，切换激活的point
     */
    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    /**
     * 手势结束时被调用
     */
    private void stopScrollToNormal() {
        switch (mState) {
            case STATE_REFRESH:
                mHeader.stopScroll(mTotalUnconsumed);
                break;
            case STATE_LOAD_MORE:
                mFooter.stopScroll(mTotalUnconsumed);
                break;
        }
        mState = STATE_NORMAL;
    }

    /**
     * 开放给Swipe控件，用于改变主控件高度，不改变时效果类似google原生SwipeRefreshLayout
     *
     * @param y 滚动的距离
     */
    public void changeTargetY(float y) {
        if (mTarget == null) {
            ensureTarget();
        }
        mTarget.setTranslationY(y);
    }

    public void setCanLoadMore(boolean canLoadMore) {
        mCanLoadMore = canLoadMore;
        if (mFooter != null) {
            mFooter.setCanRequest(canLoadMore);
        }
    }

    public interface OnChildScrollUpCallback {

        boolean canChildScrollUp(LSwipeRefreshLayout parent,
                                 @Nullable View child);
    }

    /**
     使用onInterceptTouchEvent 和onTouchEvent 处理事件分发，在使用ListView时 ，一旦事件被parent占用，无法再分发事件
     如下拉时不能滑动到顶部再继续滑动List
     现使用dispatchTouchEvent代替

     @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
     final int action = MotionEventCompat.getActionMasked(ev);
     int pointerIndex;


     if (!isEnabled() || canChildScrollUp()
     || isRefreshing() || mNestedScrollInProgress) {
     return false;
     }

     switch (action) {
     case MotionEvent.ACTION_DOWN:
     mActivePointerId = ev.getPointerId(0);
     mIsBeingDragged = false;
     pointerIndex = ev.findPointerIndex(mActivePointerId);
     if (pointerIndex < 0) {
     return false;
     }
     mInitialDownY = ev.getY(pointerIndex);
     //必须加上滚动判定的偏移量
     lastY.put(mActivePointerId, ev.getY() + mTouchSlop);
     mTotalUnconsumed = 0;
     break;

     case MotionEvent.ACTION_MOVE:
     if (mActivePointerId == INVALID_POINTER) {
     Log.e(TAG, "Got ACTION_MOVE event but don't have an active pointer id.");
     return false;
     }

     pointerIndex = ev.findPointerIndex(mActivePointerId);
     if (pointerIndex < 0) {
     return false;
     }
     final float y = ev.getY(pointerIndex);
     startDragging(y);
     break;

     case MotionEventCompat.ACTION_POINTER_UP:
     onSecondaryPointerUp(ev);
     break;

     case MotionEvent.ACTION_UP:
     case MotionEvent.ACTION_CANCEL:
     mIsBeingDragged = false;
     mActivePointerId = INVALID_POINTER;
     break;
     }

     return mIsBeingDragged;
     }






     @Override public boolean onTouchEvent(MotionEvent ev) {
     final int action = MotionEventCompat.getActionMasked(ev);
     int pointerIndex;

     if (!isEnabled() || canChildScrollUp()
     || isRefreshing() || mNestedScrollInProgress) {
     // Fail fast if we're not in a state where a swipe is possible
     return false;
     }

     switch (action) {
     case MotionEvent.ACTION_DOWN:
     mActivePointerId = ev.getPointerId(0);
     mIsBeingDragged = false;
     break;

     case MotionEvent.ACTION_MOVE: {
     pointerIndex = ev.findPointerIndex(mActivePointerId);
     if (pointerIndex < 0) {
     Log.e(TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
     return false;
     }

     final float y = ev.getY(pointerIndex);
     startDragging(y);
     if (mIsBeingDragged) {
     float dy = y - lastY.get(mActivePointerId);
     mTotalUnconsumed += dy;
     lastY.put(mActivePointerId, y);
     if (mTotalUnconsumed > 0) {
     moveHead(mTotalUnconsumed, (int) dy);
     } else {
     return false;
     }
     }
     break;
     }
     case MotionEventCompat.ACTION_POINTER_DOWN: {
     pointerIndex = MotionEventCompat.getActionIndex(ev);
     if (pointerIndex < 0) {
     Log.e(TAG,
     "Got ACTION_POINTER_DOWN event but have an invalid action index.");
     return false;
     }
     mActivePointerId = ev.getPointerId(pointerIndex);
     //                lastY = ev.getY(pointerIndex);
     lastY.put(mActivePointerId, ev.getY(pointerIndex));
     break;
     }

     case MotionEventCompat.ACTION_POINTER_UP:
     onSecondaryPointerUp(ev);
     break;

     case MotionEvent.ACTION_UP: {
     //                lastY = 0;
     lastY.put(mActivePointerId, 0F);
     pointerIndex = ev.findPointerIndex(mActivePointerId);
     if (pointerIndex < 0) {
     Log.e(TAG, "Got ACTION_UP event but don't have an active pointer id.");
     return false;
     }

     if (mIsBeingDragged) {
     mHeader.stopScroll(mTotalUnconsumed);
     mIsBeingDragged = false;
     }
     mActivePointerId = INVALID_POINTER;
     return false;
     }
     case MotionEvent.ACTION_CANCEL:
     return false;
     }

     return true;
     }
     */

}

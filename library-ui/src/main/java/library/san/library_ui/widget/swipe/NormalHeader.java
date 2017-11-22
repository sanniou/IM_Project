package library.san.library_ui.widget.swipe;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lib_im.pro.R;

/**
 * SwipeView默认实现
 */
public class NormalHeader extends AbsSwipeView {

    protected float mFinalOffset = 300;//判断刷新的偏移量，默认为headView显示的高度*3
    protected float mFinalHeight = 150;
    private ImageView mIvIcon;
    private TextView mTvTips;
    private ProgressBar mProgressBar;
    private float mHeight;
    private float mHeadHeight;

    private ViewGroup mContent;
    private LayoutParams mLayoutParams;
    private ValueAnimator mAnimator;
    private LSwipeRefreshLayout mParent;
    private boolean mRefreshToUp = false;

    {
        mAnimator = new ValueAnimator();
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setHeadHeight((float) animation.getAnimatedValue());
            }
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //由于模拟下拉的刷新动画与隐藏动画都用mAnimator，这里判断当收起至顶部时切换状态
                if (mHeadHeight == 0) {
                    setNormal();
                }
                if (mRefreshToUp) {
                    mRefreshToUp = false;
                }
            }
        });
    }

    public NormalHeader(Context context) {
        this(context, null);
    }

    public NormalHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NormalHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public float scrollOffset(float totalHeight, int offset) {
        //刷新或者执行完成动画时不进行移动
        if (mState == STATE_REFRESH || (mState == STATE_COMPLETE && mHeight == mFinalHeight)) {
            return 0;
        }
        double exp = Math.exp(-(mHeadHeight / mFinalOffset));
//        double exp = 0.5;
        double v = offset * exp;
        setHeadHeight((int) (Math.max(mHeadHeight + v, 0)));
        if (mHeadHeight > mFinalOffset && mState == STATE_NORMAL) {
            setPulling();
        }
        if (mHeadHeight < mFinalOffset && mState == STATE_PULLING) {
            setNormal();
        }
        return mHeadHeight;
    }

    @Override
    public void stopScroll(float totalY) {
        //处于"释放刷新"状态
        if (mState == STATE_PULLING) {
            setRefreshing();
        } else {
            hideAction();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        measureChild(mContent, MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST)
                , MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
        int measuredHeight = mContent.getMeasuredHeight();
        mContent.layout(0, height - measuredHeight * 2, width, height - measuredHeight);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mParent == null) {
            ViewParent parent = getParent();
            if (parent instanceof LSwipeRefreshLayout) {
                mParent = (LSwipeRefreshLayout) parent;
            }
        }
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        if (mParent == null) {
            ViewParent parent = getParent();
            if (parent != null && parent instanceof LSwipeRefreshLayout) {
                mParent = (LSwipeRefreshLayout) parent;
            }
        }
    }

    @Override
    public void normalAction() {
        mIvIcon.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        ObjectAnimator.ofFloat(mIvIcon, "rotation", 180, 0).start();
        mTvTips.setText(getContext().getString(R.string.normal_tips));
        mState = STATE_NORMAL;
    }

    @Override
    public void pullAction() {
        mIvIcon.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        ObjectAnimator.ofFloat(mIvIcon, "rotation", 0, 180).start();
        mTvTips.setText(getContext().getString(R.string.pulling_tips));
    }

    @Override
    public void refreshAction() {
        mIvIcon.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mTvTips.setText(getResources().getString(R.string.loading_tips));
        mState = STATE_REFRESH;
        mRefreshToUp = mHeadHeight > mFinalHeight;
        /*if (mRefreshToUp) {
            mAnimator.setFloatValues(mHeadHeight, 0);
            mAnimator.start();
        } else {
            setHeadHeight(mFinalHeight, true);
        }*/
        mAnimator.setFloatValues(mHeadHeight, mRefreshToUp ? 0 : mFinalHeight);
        mAnimator.start();
    }

    @Override
    public void completeAction() {
        mIvIcon.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mTvTips.setText(getContext().getString(R.string.complete_tips));
        ObjectAnimator.ofFloat(mIvIcon, "rotation", 180, 0).start();
        //显示1000ms后隐藏
        postDelayed(new Runnable() {
            @Override
            public void run() {
                hideAction();
            }
        }, 500L);
    }

    @Override
    protected void failedAction() {
        mIvIcon.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mTvTips.setText(getContext().getString(R.string.failed_tips));
        ObjectAnimator.ofFloat(mIvIcon, "rotation", 180, 0).start();
        //显示1000ms后隐藏
        postDelayed(new Runnable() {
            @Override
            public void run() {
                hideAction();
            }
        }, 500L);
    }

    /**
     * 放手后隐藏HeadView的动画
     */

    private void hideAction() {
        mAnimator.setFloatValues(mHeadHeight, 0F);
        mAnimator.start();
    }

    private void init() {
        setBackgroundColor(Color.WHITE);
        LinearLayout content = new LinearLayout(getContext());
        content.setGravity(Gravity.CENTER);
        content.setOrientation(LinearLayout.HORIZONTAL);
        mContent = content;
        addView(mContent);
        initContentView();
    }

    private void initContentView() {
        mContent.removeAllViews();
        ImageView imageView = new ImageView(getContext());
        LinearLayout.LayoutParams imageViewParams =
                new LinearLayout.LayoutParams(dp2px(getContext(), 20), dp2px(getContext(), 20));
        imageViewParams.setMargins(0, 0, dp2px(getContext(), 10), 0);
        imageView.setLayoutParams(imageViewParams);
        imageView.setImageResource(R.mipmap.icon_pull);
        mContent.addView(imageView);

        ProgressBar progress = new ProgressBar(getContext());
        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(dp2px(getContext(), 20), dp2px(getContext(), 20));
        progressParams.setMargins(0, 0, dp2px(getContext(), 10), 0);
        progress.setLayoutParams(progressParams);
        progress.setVisibility(View.GONE);
        mContent.addView(progress);

        TextView tvTips = new TextView(getContext());
        tvTips.setText(R.string.normal_tips);
        mContent.addView(tvTips);

        mIvIcon = imageView;
        mTvTips = tvTips;
        mProgressBar = progress;

        mState = STATE_NORMAL;
    }

    private void setHeadHeight(float height) {
        setHeadHeight(height, false);
    }

    private void setHeadHeight(float height, boolean offset) {
        if (mLayoutParams == null) {
            mLayoutParams = getLayoutParams();
        }
        if (!mRefreshToUp//过滤 refreshAction 往上至指定高度之后的高度值,使 head 悬浮，而 target 恢复
                || height >= mFinalHeight) {
            setLayoutParams(mLayoutParams);
            mHeadHeight = height;
            mLayoutParams.height = (int) height;
        }
        //过滤请求成功或者失败时的动画值，除非target此时高度不为0
        if (offset || (mState != STATE_COMPLETE && mState != STATE_FAILED)
                || mHeight > 0) {
            mParent.changeTargetY(height);
            mHeight = height;
        }
    }

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}

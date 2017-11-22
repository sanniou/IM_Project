package library.san.library_ui.widget.recyler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hl on 2016/9/22.
 * RecyclerView的通用适配器，继承该类可省去大量代码，或者直接实现{@link #onBindHolder(LViewHolder, int)}即可，多种布局只需重写{@link #getItemViewType(int)}
 * 使用:构造器传入上下文对象，数据对象（可为空）和布局（id形式）
 * 通过设置RecyclerView的onTouchListener实现了onItemClickListener和onItemLongClickListener
 * 内部维护一个List，更新数据使用{@link #addData(List)}等方法
 */

public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter<LViewHolder> implements
        RecyclerView.OnItemTouchListener, GestureDetector.OnGestureListener {

    private final GestureDetector mGestureDetectorCompat;
    private List<T> mList;
    private Context mContext;
    protected LayoutInflater mInflater;
    private int[] mLayouts;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private RecyclerView mParent;

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    public BaseRecyclerAdapter(Context context, int... layouts) {
        this(context, null, layouts);
    }

    public BaseRecyclerAdapter(Context context, List<T> list, int... layouts) {
        mContext = context;
        mLayouts = layouts;
        mList = new ArrayList<>();
        if (list != null) {
            mList.addAll(list);
        }
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mGestureDetectorCompat = new GestureDetector(mContext, this);
    }

    @Override
    public LViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //mParent为空时执行一次
        if (this.mParent == null) {
            this.mParent = (RecyclerView) parent;
            mParent.addOnItemTouchListener(this);
        }

        View view = mInflater.inflate(mLayouts[viewType], parent, false);
        return new LViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final LViewHolder holder, int position) {
        onBindHolder(holder, position);
    }

    public abstract void onBindHolder(LViewHolder holder, int position);

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    /**
     * 返回对应Item项
     */
    public T getItem(int position) {
        return mList.get(position);
    }

    public void setData(List<T> list) {
        mList.clear();
        mList.addAll(list);
    }

    public void addData(List<T> list) {
        mList.addAll(list);
    }

    public void removeData(T item) {
        if (mList.contains(item)) {
            mList.remove(item);
        }
    }

    public void removeData(int index) {
        mList.remove(index);
    }

    public void addData(T item) {
        if (item != null) {
            mList.add(item);
        }
    }

    public void addData(int index, T item) {
        if (item != null) {
            mList.add(index, item);
        }
    }

    public int indexOf(T item) {
        return mList.indexOf(item);
    }

    public void replace(T item, int index) {
        mList.set(index, item);
    }

    public void clearData() {
        mList.clear();
    }

    public List<T> getData() {
        return mList;
    }

    public Context getContext() {
        return mContext;
    }

    public int[] getLayouts() {
        return mLayouts;
    }

    //OnItemTouchListener
    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        //把事件交给GestureDetector处理
        mGestureDetectorCompat.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    //GestureDetectorCompat.Listener
    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        View childView = mParent.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && mOnItemClickListener != null) {
            mOnItemClickListener
                    .onItemClick(mParent, childView, mParent.getChildAdapterPosition(childView));
            return true;
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        View childView = mParent.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && mOnItemLongClickListener != null) {
            mOnItemLongClickListener.onItemLongClick(mParent, childView,
                    mParent.getChildAdapterPosition(childView));
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    /**
     * 内部定义的接口
     */
    public interface OnItemClickListener {

        void onItemClick(ViewGroup parent, View v, int position);
    }

    public interface OnItemLongClickListener {

        void onItemLongClick(ViewGroup parent, View v, int position);
    }
}

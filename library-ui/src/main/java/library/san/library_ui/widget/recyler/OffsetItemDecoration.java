package library.san.library_ui.widget.recyler;

import android.graphics.Rect;
import android.support.v4.util.Pair;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * author : jichang
 * time   : 2017/06/27
 * desc   :
 * version: 1.0
 */
public class OffsetItemDecoration extends RecyclerView.ItemDecoration {

    // Integer 类型 的 View Type 和 包括间距和边距的 pair 对应的Array
    private SparseArrayCompat<Pair<Float, Float>> mOffsetSparseArray = new SparseArrayCompat<>();
    private float mSpace;// 间距
    private float mEdgeSpace;//边距

    private OffsetItemDecoration(SparseArrayCompat<Pair<Float, Float>> sparseArray) {
        mOffsetSparseArray = sparseArray;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {

        RecyclerView.LayoutManager manager = parent.getLayoutManager();

        if (manager instanceof GridLayoutManager) {
            GridLayoutManager gridManager = (GridLayoutManager) manager;

            int viewType = gridManager.getItemViewType(view);

            Pair<Float, Float> pair = mOffsetSparseArray.get(viewType);
            if (pair == null) {
                pair = new Pair<>(0F, 0F);
            }
            mSpace = pair.first;
            mEdgeSpace = pair.second;

            GridLayoutManager.SpanSizeLookup sizeLookup = gridManager.getSpanSizeLookup();
            int childPosition = parent.getChildAdapterPosition(view); // position
            int spanCount = gridManager.getSpanCount();// 空间等分
            // 一行中 span 开始计数的 index
            int spanIndex = sizeLookup.getSpanIndex(childPosition, spanCount);
            int spanSize = sizeLookup.getSpanSize(childPosition); // 所占空间
            int spanGroupIndex = sizeLookup.getSpanGroupIndex(childPosition, spanCount);// 行数

            RecyclerView.Adapter adapter = parent.getAdapter();
            // 上一行的 viewType
            int lastType = adapter
                    .getItemViewType(Math.max(childPosition - spanIndex / spanSize - 1, 0));
            int itemCount = adapter.getItemCount();
            // 下一行的 viewType
            int nextType = adapter.getItemViewType(Math.min(childPosition + 1, itemCount - 1));
            //总行数
            int rowCount = gridManager.getRowCountForAccessibility(null, state);
            setGridOffset(gridManager.getOrientation(), spanCount, outRect, spanSize, spanIndex,
                    spanGroupIndex == 0 || lastType != viewType,
                    // GroupIndex 0 开始的，所以 count -1
                    spanGroupIndex == rowCount - 1 || nextType != viewType);
        } else if (manager instanceof LinearLayoutManager) {
            // setLinearOffset(((LinearLayoutManager) manager).getOrientation(),
            // outRect, childPosition, itemCount);
        }
    }

    /**
     * 设置GridLayoutManager 类型的 offset
     *
     * @param orientation 方向
     * @param spanCount   个数
     * @param outRect     padding
     * @param column      在一行中的 position
     */
    private void setGridOffset(int orientation, int spanCount, Rect outRect, int spanSize,
                               int column,
                               Boolean isFirst, Boolean isLast) {
        float totalSpace = mSpace * (spanCount / spanSize - 1) + mEdgeSpace * 2;
        float eachSpace = totalSpace / spanCount * spanSize;
        float left;
        float right;
        float top;
        float bottom;
        if (orientation == GridLayoutManager.VERTICAL) {
            top = 0;
            bottom = mSpace;
            if (isFirst) {
                top = mEdgeSpace;
            }
            if (isLast) {
                bottom = mEdgeSpace;
            }
            if (spanCount == 1) {
                left = mEdgeSpace;
                right = mEdgeSpace;
            } else {
                left = column * (eachSpace - mEdgeSpace - mEdgeSpace) / (spanCount - 1) +
                        mEdgeSpace;
                right = eachSpace - left;
            }
        } else {
            left = 0;
            right = mSpace;
            if (isFirst) {
                left = mEdgeSpace;
            }
            if (isLast) {
                right = mEdgeSpace;
            }
            if (spanCount == 1) {
                top = mEdgeSpace;
                bottom = mEdgeSpace;
            } else {
                top = column * (eachSpace - mEdgeSpace - mEdgeSpace) / (spanCount - 1) + mEdgeSpace;
                bottom = eachSpace - left;
            }
        }
        outRect.set((int) left, (int) top, (int) right, (int) bottom);
    }

    public static class Builder {

        private SparseArrayCompat<Pair<Float, Float>> mOffsetSparseArray = new SparseArrayCompat<>();

        public OffsetItemDecoration.Builder type(int viewType) {
            type(viewType, 0);
            return this;
        }

        public OffsetItemDecoration.Builder type(int viewType, float space) {
            type(viewType, space, 0);
            return this;
        }

        /**
         * @param space     间距
         * @param edgeSpace 边距
         */
        public OffsetItemDecoration.Builder type(int viewType, float space, float edgeSpace) {
            mOffsetSparseArray.put(viewType, new Pair<>(space, edgeSpace));
            return this;
        }

        public OffsetItemDecoration create() {
            return new OffsetItemDecoration(mOffsetSparseArray);
        }
    }

}

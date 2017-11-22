package library.san.library_ui.widget.recyler;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.lib_im.pro.R;

/**
 * 为BaseRecyclerAdapter提供的ViewHolder类
 * 实现通用方法和setText()等快捷方法
 */
@SuppressWarnings({"unchecked", "unused"})
public class LViewHolder extends RecyclerView.ViewHolder {

    private View mItemView;
    //管理view的SparseArrayCompat，getView方法可以获取View，省去fb
    private SparseArrayCompat<View> viewMap;
    //holder的对象管理Map，存储和提取任意值
    private ArrayMap<Object, Object> objMap;

    public LViewHolder(View itemView) {
        super(itemView);
        this.mItemView = itemView;
        itemView.setTag(R.id.tag_key_holder, this);
        viewMap = new SparseArrayCompat<>();
        objMap = new ArrayMap<>();
    }

    public View getItemView() {
        return mItemView;
    }

    public <T extends View> T getView(int id) {
        View view = viewMap.get(id);
        if (view == null) {
            view = mItemView.findViewById(id);
            viewMap.put(id, view);
        }
        return (T) view;
    }

    public void putObj(Object key, Object value) {
        objMap.put(key, value);
    }

    public <T> T getObj(Object key) {
        return (T) objMap.get(key);
    }

    public <T> T removeObj(Object key) {
        return (T) objMap.remove(key);
    }

    /**
     * 设置View的值
     */
    public LViewHolder setText(int viewId, CharSequence text) {
        TextView tv = getView(viewId);
        tv.setText(text);
        return this;
    }

    public LViewHolder setSelected(int viewId, boolean selected) {
        View v = getView(viewId);
        v.setSelected(selected);
        return this;
    }

    public LViewHolder setImageResource(int viewId, int resId) {
        ImageView view = getView(viewId);
        view.setImageResource(resId);
        return this;
    }

    public LViewHolder setImageBitmap(int viewId, Bitmap bitmap) {
        ImageView view = getView(viewId);
        view.setImageBitmap(bitmap);
        return this;
    }

    public LViewHolder setImageDrawable(int viewId, Drawable drawable) {
        ImageView view = getView(viewId);
        view.setImageDrawable(drawable);
        return this;
    }

    public LViewHolder setBackgroundColor(int viewId, int color) {
        View view = getView(viewId);
        view.setBackgroundColor(color);
        return this;
    }

    public LViewHolder setBackgroundRes(int viewId, int backgroundRes) {
        View view = getView(viewId);
        view.setBackgroundResource(backgroundRes);
        return this;
    }

    public LViewHolder setTextColor(int viewId, int textColor) {
        TextView view = getView(viewId);
        view.setTextColor(textColor);
        return this;
    }

    public LViewHolder setTextColorRes(int viewId, int textColorRes) {
        TextView view = getView(viewId);
        view.setTextColor(ResourcesCompat.getColor(view.getResources(), textColorRes, null));
        return this;
    }

    public LViewHolder setAlpha(int viewId, float value) {
        getView(viewId).setAlpha(value);
        return this;
    }

    public LViewHolder setVisible(int viewId, int visible) {
        View view = getView(viewId);
        view.setVisibility(visible);
        return this;
    }

    public LViewHolder linkify(int viewId) {
        TextView view = getView(viewId);
        Linkify.addLinks(view, Linkify.ALL);
        return this;
    }

    public LViewHolder setTypeface(Typeface typeface, int... viewIds) {
        for (int viewId : viewIds) {
            TextView view = getView(viewId);
            view.setTypeface(typeface);
            view.setPaintFlags(view.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        }
        return this;
    }

    public LViewHolder setProgress(int viewId, int progress) {
        ProgressBar view = getView(viewId);
        view.setProgress(progress);
        return this;
    }

    public LViewHolder setProgress(int viewId, int progress, int max) {
        ProgressBar view = getView(viewId);
        view.setMax(max);
        view.setProgress(progress);
        return this;
    }

    public LViewHolder setMax(int viewId, int max) {
        ProgressBar view = getView(viewId);
        view.setMax(max);
        return this;
    }

    public LViewHolder setRating(int viewId, float rating) {
        RatingBar view = getView(viewId);
        view.setRating(rating);
        return this;
    }

    public LViewHolder setRating(int viewId, float rating, int max) {
        RatingBar view = getView(viewId);
        view.setMax(max);
        view.setRating(rating);
        return this;
    }

    public LViewHolder setTag(int viewId, Object tag) {
        View view = getView(viewId);
        view.setTag(tag);
        return this;
    }

    public LViewHolder setTag(int viewId, int key, Object tag) {
        View view = getView(viewId);
        view.setTag(key, tag);
        return this;
    }

    public LViewHolder setTagHolder(int viewId) {
        View view = getView(viewId);
        view.setTag(R.id.tag_key_holder, this);
        return this;
    }

    public LViewHolder setTagHolder(View imageView) {
        imageView.setTag(R.id.tag_key_holder, this);
        return this;
    }

    public static LViewHolder getTagHolder(View v) {
        return (LViewHolder) v.getTag(R.id.tag_key_holder);
    }

    public LViewHolder setEnable(int viewId, boolean enabled) {
        View view = getView(viewId);
        view.setEnabled(enabled);
        return this;
    }

    public LViewHolder setChecked(int viewId, boolean checked) {
        Checkable view = getView(viewId);
        view.setChecked(checked);
        return this;
    }

    /**
     * 关于事件的
     */
    public LViewHolder setOnClickListener(int viewId, View.OnClickListener listener) {
        View view = getView(viewId);
        view.setOnClickListener(listener);
        return this;
    }

    public LViewHolder setOnTouchListener(int viewId, View.OnTouchListener listener) {
        View view = getView(viewId);
        view.setOnTouchListener(listener);
        return this;
    }

    public LViewHolder setOnLongClickListener(int viewId, View.OnLongClickListener listener) {
        View view = getView(viewId);
        view.setOnLongClickListener(listener);
        return this;
    }

}

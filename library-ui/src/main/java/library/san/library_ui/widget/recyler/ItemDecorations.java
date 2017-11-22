package library.san.library_ui.widget.recyler;

import android.content.Context;

/**
 * author : jichang
 * time   : 2017/06/27
 * desc   :
 * version: 1.0
 */
public class ItemDecorations {

    public static VerticalItemDecoration.Builder vertical(Context context) {
        return new VerticalItemDecoration.Builder(context);
    }

    public static HorizontalItemDecoration.Builder horizontal(Context context) {
        return new HorizontalItemDecoration.Builder(context);
    }

    public static OffsetItemDecoration.Builder offset(Context context) {
        return new OffsetItemDecoration.Builder();
    }
}
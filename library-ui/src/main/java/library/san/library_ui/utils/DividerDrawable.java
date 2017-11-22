package library.san.library_ui.utils;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;

/************************* Divider *************************/
public class DividerDrawable extends Drawable {

    private float heightPx;

    public DividerDrawable(float heightPx) {
        this.heightPx = heightPx;
    }

    @Override
    public void draw(@android.support.annotation.NonNull Canvas canvas) {
        // Do nothing
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        // Do nothing
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        // Do nothing
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    @Override
    public int getIntrinsicHeight() {
        return (int) heightPx;
    }

    @Override
    public int getIntrinsicWidth() {
        return (int) heightPx;
    }

}
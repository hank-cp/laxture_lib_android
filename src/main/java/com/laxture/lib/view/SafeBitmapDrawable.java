package com.laxture.lib.view;

import java.lang.reflect.Method;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.laxture.lib.Configuration;
import com.laxture.lib.util.LLog;

public class SafeBitmapDrawable extends BitmapDrawable {

    protected SafeBitmapDrawableFactory mDrawableFactory;

    public SafeBitmapDrawable(Bitmap bitmap) {
        super(Configuration.getResources(), bitmap);
    }

    protected void guaranteeBitmapIsSafe() {
        if (getBitmap() == null || getBitmap().isRecycled()) {
            if (mDrawableFactory == null) return; // skip drawing if mDrawableFactory is not set

            LLog.d("Bitmap %s is recycled by LruCache. Reload it.", mDrawableFactory.mCacheId);

            Bitmap bitmap = mDrawableFactory.createBitmap();
            if (bitmap == null) return; // skip drawing if failed to reload bitmap

            Method method;
            try {
                method = BitmapDrawable.class.getDeclaredMethod("setBitmap", Bitmap.class);
                method.setAccessible(true);
                method.invoke(this, bitmap);
            } catch (Exception e) {
                LLog.w("Cannot set bitmap View. %s", e.getMessage());
                return;
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        guaranteeBitmapIsSafe();
        if (getBitmap() == null || getBitmap().isRecycled()) return;
        super.draw(canvas);
    }

    public static class SafeBitmapDrawableFactory extends BitmapDrawableFactory {
        @Override
        public Drawable create() {
            SafeBitmapDrawable drawable = new SafeBitmapDrawable(createBitmap());
            drawable.mDrawableFactory = cloneMe();
            return drawable;
        }
    }

}

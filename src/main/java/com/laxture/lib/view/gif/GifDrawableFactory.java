package com.laxture.lib.view.gif;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;

import com.laxture.lib.util.Checker;
import com.laxture.lib.util.LLog;
import com.laxture.lib.util.UnHandledException;
import com.laxture.lib.view.BitmapDrawableFactory;
import com.laxture.lib.view.SafeBitmapDrawable;

public class GifDrawableFactory extends BitmapDrawableFactory {

    public static final boolean DEBUG = false;

    /**
     * define 1/16 heap size, up to 4M, bottom to 2M
     */
    private static final GifLruCache sCache = new GifLruCache((int)
            Math.max(Math.min(Runtime.getRuntime().maxMemory()/16,
                    6 * 1024 * 1024),   // up to 6M
                    2 * 1024 * 1024));  // at least 2M

    public static void clearCache() {
        sCache.evictAll();
    }

    protected GifDecoder createGif() {
        if (Checker.isEmpty(mFile))
            throw new IllegalArgumentException("Bitmap source is not set.");

        if (Checker.isEmpty(mCacheId)) mCacheId = mFile.getAbsolutePath();
        GifDecoder gif = sCache.get(mCacheId);
        if (gif != null && !gif.isRecycled()) {
            if (DEBUG) LLog.v("Hit gif cache %s", mCacheId);
            return gif;
        }

        gif = new GifDecoder();
        gif.setDensity(mDensity > 0 ? mDensity : -1);
        if (gif.read(mFile) == GifDecoder.STATUS_OK) {
            sCache.put(mCacheId, gif);
        }
        return gif;
    }

    @SuppressWarnings("unchecked")
    protected <T extends BitmapDrawableFactory> T cloneMe() {
        try {
            return (T) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new UnHandledException("Failed to clone BitmapDrawableFactory");
        }
    }

    @Override
    public Drawable create() {
        AnimationDrawable drawable = new AnimationDrawable();
        GifDecoder gif = createGif();
        if (gif.getFrameCount() > 0) {
            for (int i=0; i<gif.getFrameCount(); i++) {
                SafeBitmapDrawable bitmapDrawable = new SafeBitmapDrawable(gif.getFrame(i));
                drawable.addFrame(bitmapDrawable, gif.getDelay(i));
            }
            drawable.setBounds(0, 0,
                    gif.getBitmap().getWidth(), gif.getBitmap().getHeight());
            drawable.setOneShot(false);
        }
        return drawable;
    }

}
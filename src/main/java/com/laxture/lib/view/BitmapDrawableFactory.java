package com.laxture.lib.view;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.laxture.lib.cache.men.BitmapCache;
import com.laxture.lib.util.BitmapUtil.ResizeMode;
import com.laxture.lib.util.Checker;
import com.laxture.lib.util.DigestUtils;
import com.laxture.lib.util.LLog;
import com.laxture.lib.util.UnHandledException;

public abstract class BitmapDrawableFactory implements Cloneable {

    protected String mCacheId;
    protected int mDensity;

    protected File mFile;
    protected byte[] mBytes;
    protected int mResId;
    protected String mAssetPath;

    protected int mWidth;
    protected int mHeight;
    protected ResizeMode mResizeMode;

    protected BitmapDecorator mBitmapDecorator;

    private boolean mFlushCache; // this field will only affect once.

    public void setCacheId(String cacheId) {
        mCacheId = cacheId;
    }

    public void setDensity(int density) {
        mDensity = density;
    }

    public void setFile(File file) {
        mFile = file;
    }

    public void setBytes(byte[] bytes) {
        mBytes = bytes;
    }

    public void setResId(int resId) {
        mResId = resId;
    }

    public void setAssetPath(String assetPath) {
        mAssetPath = assetPath;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public void setResizeMode(ResizeMode resizeMode) {
        mResizeMode = resizeMode;
    }

    public void setBitmapDecorator(BitmapDecorator bitmapDecorator) {
        mBitmapDecorator = bitmapDecorator;
    }

    public void setFlushCache(boolean flushCache) {
        mFlushCache = flushCache;
    }

    public abstract Drawable create();

    protected Bitmap createBitmap() {
        if (mFile == null
                && mResId <= 0
                && Checker.isEmpty(mBytes)
                && Checker.isEmpty(mAssetPath)) {
            LLog.e("Bitmap source is not set.");
            return null;
        }

        Bitmap bitmap = null;
        if (mFile != null) {
            if (Checker.isEmpty(mCacheId)) mCacheId = mFile.getAbsolutePath();
            if (mWidth > 0 && mHeight > 0) {
                bitmap = BitmapCache.loadBitmap(mCacheId, mFile, mWidth, mHeight,
                        (mResizeMode == null ? ResizeMode.Fit : mResizeMode),
                        (mDensity > 0 ? mDensity : -1), mFlushCache);
            } else {
                bitmap = BitmapCache.loadBitmap(mCacheId, mFile,
                        (mDensity > 0 ? mDensity : -1), mFlushCache);
            }

        } else if (!Checker.isEmpty(mBytes)) {
            if (Checker.isEmpty(mCacheId)) mCacheId = DigestUtils.md5Hex(mBytes);
            if (mDensity > 0) {
                if (mWidth > 0 && mHeight > 0) {
                    bitmap = BitmapCache.loadBitmap(mCacheId, mBytes, mWidth, mHeight,
                            (mResizeMode == null ? ResizeMode.Fit : mResizeMode), mDensity);
                } else {
                    bitmap = BitmapCache.loadBitmap(mCacheId, mBytes, mDensity);
                }
            } else {
                if (mWidth > 0 && mHeight > 0) {
                    bitmap = BitmapCache.loadBitmap(mCacheId, mBytes, mWidth, mHeight,
                            (mResizeMode == null ? ResizeMode.Fit : mResizeMode));
                } else {
                    bitmap = BitmapCache.loadBitmap(mCacheId, mBytes);
                }
            }

        } else if (mResId > 0) {
            bitmap = BitmapCache.loadBitmap(mResId);

        } else if (Checker.isEmpty(mAssetPath)) {
            if (mDensity > 0) {
                bitmap = BitmapCache.loadBitmap(mAssetPath, mDensity);
            } else {
                bitmap = BitmapCache.loadBitmap(mAssetPath);
            }
        }

        if (mBitmapDecorator != null && bitmap != null) {
            Bitmap originalBitmap = bitmap;
            bitmap = mBitmapDecorator.mask(originalBitmap);
//            BitmapCache.put(BitmapCache.getCacheKey(mCacheId, mWidth, mHeight, mResizeMode), bitmap);
//            originalBitmap.recycle();
        }

        return bitmap;
    }

    @SuppressWarnings("unchecked")
    protected <T extends BitmapDrawableFactory> T cloneMe() {
        try {
            return (T) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new UnHandledException("Failed to clone BitmapDrawableFactory");
        }
    }

    //*************************************************************************
    //  Bitmap Decorator
    //*************************************************************************

    public interface BitmapDecorator {
        public Bitmap mask(Bitmap origianlBitmap);
    }

}
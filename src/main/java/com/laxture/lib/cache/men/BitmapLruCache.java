package com.laxture.lib.cache.men;

import android.graphics.Bitmap;
import androidx.collection.LruCache;

import com.laxture.lib.util.BitmapUtil;
import com.laxture.lib.util.DeviceUtil;

public class BitmapLruCache extends LruCache<String, Bitmap> {

    public BitmapLruCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return BitmapUtil.getBitmapSize(value);
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
        oldValue.recycle();
        if (BitmapCache.DEBUG) DeviceUtil.logHeap();
    }

}



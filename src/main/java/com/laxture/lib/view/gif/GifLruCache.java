package com.laxture.lib.view.gif;

import android.support.v4.util.LruCache;

public class GifLruCache extends LruCache<String, GifDecoder> {

    public GifLruCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(String key, GifDecoder value) {
        return value.size();
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, GifDecoder oldValue, GifDecoder newValue) {
        oldValue.recycle();
    }

}



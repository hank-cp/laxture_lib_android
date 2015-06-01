package com.laxture.lib.cache.men;

import java.io.File;

import android.graphics.Bitmap;

import com.laxture.lib.Configuration;
import com.laxture.lib.util.BitmapUtil;
import com.laxture.lib.util.LLog;

/**
 * Global Bitmap Cache. Bitmap that get from this cache should be used by company with
 * {@link SafeBitmapDrawable}, which will reload bitmap before drawing if it's recycled.
 */
public class BitmapCache {

    static final boolean DEBUG = false;

    public static final String CACHE_PREFIX_RESORUCE = "res_";
    public static final String CACHE_PREFIX_ASSET = "asset_";

    /**
     * Bitmap that bigger than 300K will be cached to aggressive cache, which
     * will be recycle more aggressively.
     */
    public static final int CACHE_WATERSHED = 300 * 1024;

    /**
     * define 1/4 heap size , up to 16M, as conservative cache size
     */
    public static final int CONSERVATIVE_BITMAP_CACHE_SIZE = (int) Math.min(
            Runtime.getRuntime().maxMemory()/8, 8 * 1024 * 1024);

    /**
     * define 1/8 heap size , up to 8M, as aggressive cache size
     */
    public static final int AGGRESSIVE_BITMAP_CACHE_SIZE = (int) Math.min(
            Runtime.getRuntime().maxMemory()/8, 8 * 1024 * 1024);

    private static BitmapLruCache sConservativeCache = new BitmapLruCache(
            CONSERVATIVE_BITMAP_CACHE_SIZE);

    private static BitmapLruCache sAggressiveCache = new BitmapLruCache(
            AGGRESSIVE_BITMAP_CACHE_SIZE);

    public static Bitmap get(String cacheId) {
        Bitmap bitmap = sConservativeCache.get(cacheId);
        if (bitmap == null) bitmap = sAggressiveCache.get(cacheId);
        return bitmap;
    }

    public static Bitmap put(String cacheId, Bitmap bitmap) {
        if (BitmapUtil.getBitmapSize(bitmap) > CACHE_WATERSHED)
            return sAggressiveCache.put(cacheId, bitmap);
        else
            return sConservativeCache.put(cacheId, bitmap);
    }

    /**
     * Resize Conservative Cache. When you call this method, all previous cache will all
     * be cleared.
     *
     * @param size
     */
    public static void resizeConservativeCache(int size) {
        sConservativeCache.evictAll();
        sConservativeCache = new BitmapLruCache(size);
    }

    /**
     * Resize Aggressive Cache. When you call this method, all previous cache will all
     * be cleared.
     *
     * @param size
     */
    public static void resizeAggressiveCache(int size) {
        sAggressiveCache.evictAll();
        sAggressiveCache = new BitmapLruCache(size);
    }

    //*************************************************************************
    // Load Bitmap Methods
    //*************************************************************************

    public static String getCacheKey(String cacheId, int width, int height, BitmapUtil.ResizeMode resizeMode) {
        if (width > 0 && height > 0 && resizeMode != null) {
            return cacheId+"_"+width+"x"+height+"@"+resizeMode;
        } else return cacheId;
    }

    public static Bitmap loadBitmap(String cacheId, File file) {
        return loadBitmap(cacheId, file, -1);
    }

    public static Bitmap loadBitmap(String cacheId, File file, int density) {
        return loadBitmap(cacheId, file, density, false);
    }

    public static Bitmap loadBitmap(String cacheId, File file, int density, boolean flushCache) {
        Bitmap bitmap = get(cacheId);
        if (bitmap != null && !bitmap.isRecycled()) {
            if (DEBUG) LLog.v("Hit bitmap cache %s", cacheId);
            if (!flushCache) return bitmap;
            else {
                bitmap.recycle();
                flushCache = false;
            }
        }

        bitmap = BitmapUtil.loadBitmapFromFile(file, 1);
        if (bitmap != null) {
            if (density > 0) bitmap.setDensity(density);
            put(cacheId, bitmap);
        }
        return bitmap;
    }

    public static Bitmap loadBitmap(String cacheId, File file,
            int width, int height, BitmapUtil.ResizeMode resizeMode) {
        return loadBitmap(cacheId, file, width, height, resizeMode, -1);
    }

    public static Bitmap loadBitmap(String cacheId, File file,
            int width, int height, BitmapUtil.ResizeMode resizeMode, int density) {
        return loadBitmap(cacheId, file, width, height, resizeMode, density, false);
    }

    public static Bitmap loadBitmap(String cacheId, File file,
            int width, int height, BitmapUtil.ResizeMode resizeMode, int density, boolean flushCache) {
        String cacheKey = cacheId+"_"+width+"x"+height+"@"+resizeMode;
        Bitmap bitmap = get(getCacheKey(cacheId, width, height, resizeMode));
        if (bitmap != null && !bitmap.isRecycled()) {
            if (DEBUG) LLog.v("Hit bitmap cache %s", cacheKey);
            if (!flushCache) return bitmap;
            else {
                bitmap.recycle();
            }
        }

        bitmap = BitmapUtil.loadBitmapFromFile(file, width, height, resizeMode);
        if (bitmap != null) {
            if (density > 0) bitmap.setDensity(density);
            put(cacheKey, bitmap);
        }
        return bitmap;
    }

    public static Bitmap loadBitmap(String cacheId, byte[] bytes) {
        return loadBitmap(cacheId, bytes, -1);
    }

    public static Bitmap loadBitmap(String cacheId, byte[] bytes, int density) {
        Bitmap bitmap = get(cacheId);
        if (bitmap != null && !bitmap.isRecycled()) {
            if (DEBUG) LLog.v("Hit bitmap cache %s", cacheId);
            return bitmap;
        }

        bitmap = BitmapUtil.loadBitmapFromBytes(bytes, 1);
        if (bitmap != null) {
            if (density > 0) bitmap.setDensity(density);
            put(cacheId, bitmap);
        }
        return bitmap;
    }

    public static Bitmap loadBitmap(String cacheId, byte[] bytes,
            int width, int height, BitmapUtil.ResizeMode resizeMode) {
        return loadBitmap(cacheId, bytes, width, height, resizeMode, -1);
    }

    public static Bitmap loadBitmap(String cacheId, byte[] bytes,
            int width, int height, BitmapUtil.ResizeMode resizeMode, int density) {
        String cacheKey = cacheId+"x"+width+"y"+"@"+resizeMode;
        Bitmap bitmap = get(cacheKey);
        if (bitmap != null && !bitmap.isRecycled()) {
            if (DEBUG) LLog.v("Hit bitmap cache %s", cacheKey);
            return bitmap;
        }

        bitmap = BitmapUtil.loadBitmapFromBytes(bytes, width, height, resizeMode);
        if (bitmap != null) {
            if (density > 0) bitmap.setDensity(density);
            put(cacheKey, bitmap);
        }
        return bitmap;
    }

    public static Bitmap loadBitmap(int resId) {
        String cacheKey = CACHE_PREFIX_RESORUCE+resId;
        Bitmap bitmap = get(cacheKey);
        if (bitmap != null && !bitmap.isRecycled()) {
            if (DEBUG) LLog.v("Hit bitmap cache %s", cacheKey);
            return bitmap;
        }

        bitmap = BitmapUtil.loadBitmapFromResources(Configuration.getResources(), resId);
        if (bitmap != null) put(cacheKey, bitmap);
        return bitmap;
    }

    public static Bitmap loadBitmap(String assetPath) {
        return loadBitmap(assetPath, -1);
    }

    public static Bitmap loadBitmap(String assetPath, int density) {
        String cacheKey = CACHE_PREFIX_ASSET+assetPath;
        Bitmap bitmap = get(cacheKey);
        if (bitmap != null && !bitmap.isRecycled()) {
            if (DEBUG) LLog.v("Hit bitmap cache %s", cacheKey);
            return bitmap;
        }

        bitmap = BitmapUtil.loadBitmapFromAsset(assetPath);
        if (bitmap != null) {
            if (density > 0) bitmap.setDensity(density);
            put(cacheKey, bitmap);
        }
        return bitmap;
    }

    //*************************************************************************
    // Global Monitoring Methods
    //*************************************************************************

    public static int size() {
        return sConservativeCache.size() + sAggressiveCache.size();
    }

    public static int maxSize() {
        return sConservativeCache.maxSize() + sAggressiveCache.maxSize();
    }

    /**
     * trim partial cache to acquire required memory space. Make sure this method is
     * called before you do memory sensitive operation, e.g. load large size image,
     * allocate large size buffer.
     *
     * @param requiredSize
     * @return return true means available memory enough for required size after trim cache
     */
    public static boolean trimToRequiredSize(int requiredSize) {
        // if available memory is enough for required size, return
        if (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()
                > requiredSize) return true;

        // if all cache is evict, return false to tell that not enough memory, call should
        // stop that it's tending to to next.
        if (size() <= 0) return false;

        // evict all aggressive cache
        if (sAggressiveCache.size() > 0) sAggressiveCache.evictAll();

        // evict half conservative cache
        else sConservativeCache.trimToSize(sConservativeCache.size()/2);

        // recurse self until enough memory is free
        return trimToRequiredSize(requiredSize);
    }

    public static boolean prepareMemoryBeforeLoadBitmap(File imageFile) {
        return trimToRequiredSize(BitmapUtil.estimateBitmapMemorySize(imageFile));
    }

    public static boolean prepareMemoryBeforeLoadBitmap(int width, int height) {
        return trimToRequiredSize(BitmapUtil.estimateBitmapMemorySize(width, height));
    }

    public static void evictAggressiveCache(){
        // evict all aggressive cache
        if (sAggressiveCache.size() > 0) sAggressiveCache.evictAll();
    }

    public static void evictConservativeCache(){
        // evict all conservative cache
        if (sConservativeCache.size() > 0) sConservativeCache.evictAll();
    }


    public static void evictAll() {
        sAggressiveCache.evictAll();
        sConservativeCache.evictAll();
    }

    public static void remove(String cacheId) {
        sAggressiveCache.remove(cacheId);
        sConservativeCache.remove(cacheId);
    }

}

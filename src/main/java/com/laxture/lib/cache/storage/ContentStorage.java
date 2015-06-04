package com.laxture.lib.cache.storage;

import android.util.LruCache;

import com.laxture.lib.util.Checker;
import com.laxture.lib.util.ResourceUtil;

import java.io.File;
import java.io.Serializable;

public class ContentStorage implements Storage, Serializable {

    public static final int LRUCACHE_SIZE = 150;

    private static final long serialVersionUID = -2556174770329584664L;

    // url反查路径和cache
    private static final LruCache<String, String> sFilePathCache =
            new LruCache<String, String>(LRUCACHE_SIZE);

    // 需要更新最后访问时间的栈. 在弹出缓存栈的时候批量存入数据库
    private static final MyLruCache sFileLastUsedCache =
            new MyLruCache(LRUCACHE_SIZE);

    public String url;

    public String localPath;

    public ContentStorage() {}

    public ContentStorage(String localPath, String url) {
        if (Checker.isEmpty(localPath) && Checker.isEmpty(url))
            throw new IllegalArgumentException("" +
                    "localPath and url cannot be null at the same time");

        this.localPath = localPath;
        this.url = url;
    }

    protected String preCheckCacheKey(String key) {
        return key;
    }

    public File getLocalFile() {
        File file = null;
        String key = url;

        key = preCheckCacheKey(key);

        // 不同来源生成的ContentStorage, 可能url是相同的, 但不一定都有localPath.
        // 如果localPath为空, 尝试找出有url的localPath.
        if (Checker.isEmpty(localPath)) {
            if (Checker.isEmpty(url)) return null;

            //从LruCache里面找
            file = !Checker.isEmpty(getFromCache(key))
                    ? new File(getFromCache(key)) : null;

            //LruCache找不到, 所有规格的图片都先从无规格图片的缓存路径查找图片
            if (Checker.isEmpty(file))
                file = new HashCacheStorage(key).getFile();

        } else {
             //有localPath的情况：直接获取本地文件
             file = new File(localPath);
        }

        // 加入到LurCache里面去
        if (!Checker.isEmpty(file) && !Checker.isEmpty(key)) {
            addCacehRecord(key, file.getAbsolutePath());
        }
        return file;
    }

    public File getCacheFile() {
        if (Checker.isEmpty(url)) return null;

        //先从LruCache里面找
        File file = !Checker.isEmpty(getFromCache(url)) ? new File(
                getFromCache(url)) : null;

        //800的大图按照无图片规格路径保存到缓存
        if (file == null) {
             if (url.endsWith("/" + ResourceUtil.UPP_SIZE_WIDTH_MAX)) {
                 file = new HashCacheStorage(url.substring(0, url.length() - 3)).getFile();
                 addCacehRecord(url, file.getAbsolutePath());
                 return file;
            }
        }

        if (file == null) {
            //LruCache找不到，Hash出路径并放到LurCache和数据库
            file = new HashCacheStorage(url).getFile();
            addCacehRecord(url, file.getAbsolutePath());
        } else {
            //更新最后访问时间, 放在这里做是为了避免频繁操作子线程
            putInCache(url, file.getAbsolutePath(), System.currentTimeMillis());
        }

        return file;
    }

    private void addCacehRecord(String key, String path) {
        StorageCacheRecord cacheRecord = new StorageCacheRecord();
        cacheRecord.url = key;
        cacheRecord.path = path;
        cacheRecord.lastUsed = System.currentTimeMillis();
        StorageCacheManageThread.getInstance().insertStorageCache(cacheRecord);
    }

    @Override
    public File getFile() {
        File contentFile = getLocalFile();
        if (Checker.isEmpty(contentFile)) contentFile = getCacheFile();
        return contentFile;
    }

    /**
     * 上传文件成功之后, 把localPath里面的东西移动从url哈希出来的新路径里面去统一管理
     *
     * @return
     */
    public boolean moveToHashCacheStorage() {
        if (Checker.isEmpty(url) || Checker.isEmpty(localPath)) return false;
        File newFile = new HashCacheStorage(url).getFile();
        ResourceUtil.prepareParentDir(newFile);
        if ((new File(localPath)).renameTo(newFile)) {
            StorageCacheRecord.insert(url, newFile.getAbsolutePath(), null);
            localPath = newFile.getAbsolutePath();
            return true;
        }
        return false;
    }

    public static void putInCache(String url ,String path , long lastUsed) {
        if (null == url || null == path) return;
        sFilePathCache.put(url, path);
        sFileLastUsedCache.put(url, lastUsed);
    }

    public static String getFromCache(String url) {
        if (null == url) return null;
        return sFilePathCache.get(url);
    }

    public static Long getFromMyCache(String url) {
        if (null == url) return 0l;
        return sFileLastUsedCache.get(url);
    }

    public static void clearLruCache(String url) {
        if (null == url) return;
        sFilePathCache.remove(url);
        sFileLastUsedCache.remove(url);
    }

    public static void removeAllLruCache() {
        sFilePathCache.evictAll();
        sFileLastUsedCache.evictAll();
    }

    public static class MyLruCache extends LruCache<String, Long> {

        public MyLruCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected void entryRemoved(boolean evicted, String key, Long oldValue, Long newValue) {
            //evicted true---为释放空间被删除；false---put或remove导致
            //当文件被移出LruCache时更新最后使用时间，减少子线程更新数据库的次数
            if (evicted) {
                StorageCacheRecord storageCacheRecord = new StorageCacheRecord();
                storageCacheRecord.url = key;
                storageCacheRecord.lastUsed = oldValue != null ? oldValue : 0;
                StorageCacheManageThread.getInstance().updateLastUsed(storageCacheRecord);
            }
        }
    }
}

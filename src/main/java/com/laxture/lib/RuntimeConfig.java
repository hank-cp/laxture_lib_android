package com.laxture.lib;

import com.laxture.lib.util.DateUtil;

/**
 * Config arguments used in this library.
 */
public class RuntimeConfig {

    private String storageHomeName;
    private boolean noMedia;
    private int taskQueueConsumingLimit;
    private long cacheStorageMaxCapacity;
    private long recycleCacheInterval;
    private int maxCaptureImageSize;

    public RuntimeConfig(Builder builder) {
        storageHomeName = builder.storageHomeName;
        noMedia = builder.noMedia;
        taskQueueConsumingLimit = builder.taskQueueConsumingLimit;
        cacheStorageMaxCapacity = builder.cacheFolderCapacity;
        recycleCacheInterval = builder.getCacheClearIntervalTime;
        maxCaptureImageSize = builder.maxCaptureImageSize;
    }


    public String getStorageHomeName() {
        return storageHomeName;
    }

    public boolean isNoMedia() {
        return noMedia;
    }

    public int getTaskQueueConsumingLimit() {
        return taskQueueConsumingLimit;
    }

    public long getCacheStorageMaxCapacity() {
        return cacheStorageMaxCapacity;
    }

    public long getRecycleCacheInterval() {
        return recycleCacheInterval;
    }

    public int getMaxCaptureImageSize() {
        return maxCaptureImageSize;
    }

    //*************************************************************************
    // Config Builder
    //*************************************************************************

    public static class Builder {
        private String storageHomeName = "laxture";
        private boolean noMedia = true;
        private int taskQueueConsumingLimit = 5;
        private long cacheFolderCapacity = 100 * 1024 * 1024;
        private long getCacheClearIntervalTime = DateUtil.DAY_MILLIS * 7;
        private int maxCaptureImageSize = 1024;

        public void setStorageHomeName(String storageHomeName) {
            this.storageHomeName = storageHomeName;
        }

        public void setNoMedia(boolean noMedia) {
            this.noMedia = noMedia;
        }

        public void setTaskQueueConsumingLimit(int taskQueueConsumingLimit) {
            this.taskQueueConsumingLimit = taskQueueConsumingLimit;
        }

        public void setCacheFolderCapacity(long cacheFolderCapacity) {
            this.cacheFolderCapacity = cacheFolderCapacity;
        }

        public void setGetCacheClearIntervalTime(long getCacheClearIntervalTime) {
            this.getCacheClearIntervalTime = getCacheClearIntervalTime;
        }

        public void setMaxCaptureImageSize(int maxCaptureImageSize) {
            this.maxCaptureImageSize = maxCaptureImageSize;
        }
    }
}

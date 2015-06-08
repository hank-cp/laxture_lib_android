package com.laxture.lib;

import com.laxture.lib.util.DateUtil;

/**
 * This Class is designated for two purposes:
 * <ol>
 * <li>Hold ApplicationContext and package information. It could match to App
 * projects that include LaxtureLib project automatically.  </li>
 * <li>Allow App projects to specify configurations that used in LaxtureLib.</li>
 * </ol>
 * <br/>
 *
 * NOTE: Concrete RuntimeContext class must be put under root package folder as
 * described as in AndroidManifest.xml.
 */
public class RuntimeConfig {

    private String storageHomeName;
    private boolean noMedia;
    private int taskQueueCosumingLimit;
    private long cacheFolderCapacity;
    private long getCacheClearIntervalTime;

    public RuntimeConfig(Builder builder) {
        storageHomeName = builder.storageHomeName;
        noMedia = builder.noMedia;
        taskQueueCosumingLimit = builder.taskQueueCosumingLimit;
        cacheFolderCapacity = builder.cacheFolderCapacity;
        getCacheClearIntervalTime = builder.getCacheClearIntervalTime;
    }


    public String getStorageHomeName() {
        return storageHomeName;
    }

    public boolean isNoMedia() {
        return noMedia;
    }

    public int getTaskQueueCosumingLimit() {
        return taskQueueCosumingLimit;
    }

    public long getCacheFolderCapacity() {
        return cacheFolderCapacity;
    }

    public long getGetCacheClearIntervalTime() {
        return getCacheClearIntervalTime;
    }

    //*************************************************************************
    // Config Builder
    //*************************************************************************

    public static class Builder {
        private String storageHomeName = "laxture";
        private boolean noMedia = true;
        private int taskQueueCosumingLimit = 5;
        private long cacheFolderCapacity = 100 * 1024 * 1024;
        private long getCacheClearIntervalTime = DateUtil.DAY_MILLIS * 7;

        public void setStorageHomeName(String storageHomeName) {
            this.storageHomeName = storageHomeName;
        }

        public void setNoMedia(boolean noMedia) {
            this.noMedia = noMedia;
        }

        public void setTaskQueueCosumingLimit(int taskQueueCosumingLimit) {
            this.taskQueueCosumingLimit = taskQueueCosumingLimit;
        }

        public void setCacheFolderCapacity(long cacheFolderCapacity) {
            this.cacheFolderCapacity = cacheFolderCapacity;
        }

        public void setGetCacheClearIntervalTime(long getCacheClearIntervalTime) {
            this.getCacheClearIntervalTime = getCacheClearIntervalTime;
        }
    }
}

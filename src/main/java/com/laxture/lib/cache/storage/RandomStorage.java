package com.laxture.lib.cache.storage;

import com.laxture.lib.Configuration;

import java.io.File;
import java.util.UUID;

public class RandomStorage implements Storage {

    File mFile;

    RandomStorage() {}

    public RandomStorage(String prefix, String extName) {
        mFile = new File(getStorageHome(),
                prefix+"_"+UUID.randomUUID().toString()+"."+extName);
    }

    @Override
    public File getFile() {
        return mFile;
    }

    public HashCacheStorage convertToHashCacheStorage(String hashKey) {
        HashCacheStorage cacheFile = new HashCacheStorage(hashKey);
        boolean ret = mFile.renameTo(cacheFile.getFile());
        return ret ? cacheFile : null;
    }

    /**
     * Override this method to speicify storage location
     *
     * @return
     */
    protected File getStorageHome() {
        return Configuration.getInstance().getStorageHome();
    }

}

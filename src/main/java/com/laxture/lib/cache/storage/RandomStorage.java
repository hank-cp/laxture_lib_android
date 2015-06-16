package com.laxture.lib.cache.storage;

import com.laxture.lib.RuntimeContext;

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

    /**
     * Override this method to speicify storage location
     *
     * @return
     */
    protected File getStorageHome() {
        return RuntimeContext.getStorageHome();
    }

}

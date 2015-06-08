package com.laxture.lib.cache.storage;

import com.laxture.lib.RuntimeContext;

import java.io.File;

public class UploadStorage extends RandomStorage {

    public UploadStorage(String prefix, String extName) {
        super(prefix, extName);
    }

    public UploadStorage(String fileName) {
        mFile = new File(getStorageHome(), fileName);
    }

    @Override
    protected File getStorageHome() {
        File cacheHome = new File(RuntimeContext.getStorageHome(), ".upload");
        if (!cacheHome.exists()) cacheHome.mkdirs();
        return cacheHome;
    }
}

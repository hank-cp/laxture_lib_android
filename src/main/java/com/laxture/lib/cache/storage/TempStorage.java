package com.laxture.lib.cache.storage;

import com.laxture.lib.Configuration;
import com.laxture.lib.util.FileUtil;

import java.io.File;

public class TempStorage extends RandomStorage {

    public TempStorage(String prefix, String extName) {
        super(prefix, extName);
    }

    public TempStorage(String fileName) {
        mFile = new File(getStorageHome(), fileName);
    }

    @Override
    protected File getStorageHome() {
        File cacheHome = new File(Configuration.getInstance().getStorageHome(), ".tmp");
        if (!cacheHome.exists()) cacheHome.mkdirs();
        return cacheHome;
    }

    public static void cleanTempFolder() {
        FileUtil.deleteFolder(new File(Configuration.getInstance().getStorageHome(), ".tmp"));
    }

}

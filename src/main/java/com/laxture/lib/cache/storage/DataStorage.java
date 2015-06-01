package com.laxture.lib.cache.storage;

import com.laxture.lib.Configuration;
import com.laxture.lib.util.Checker;

import java.io.File;

public class DataStorage implements Storage {

    private String mFileName;
    private File mFile;

    public DataStorage(String fileName) {
        if (Checker.isEmpty(fileName))
            throw new IllegalArgumentException("cache key cannot be empty");
        mFileName = fileName;
    }

    @Override
    public File getFile() {
        if (mFile == null) {
            mFile = new File(getDataHome(), mFileName);
        }
        return mFile;
    }

    protected File getDataHome() {
        return Configuration.getInstance().getAppContext().getFilesDir();
    }

}

package com.laxture.lib.cache.storage;

import com.laxture.lib.Configuration;
import com.laxture.lib.util.Checker;

import java.io.File;

public class PrivateStorage implements Storage {

    private String mUin;
    private String mFileName;
    private File mFile;

    public PrivateStorage(String uin, String fileName) {
        if (Checker.isEmpty(uin))
            throw new IllegalArgumentException("uin cannot be empty.");
        if (Checker.isEmpty(fileName))
            throw new IllegalArgumentException("cache key cannot be empty");
        mUin = uin;
        mFileName = fileName;
    }

    @Override
    public File getFile() {
        if (mFile == null) {
            mFile = new File(getPrivateHome(mUin), mFileName);
        }
        return mFile;
    }

    protected File getPrivateHome(String uin) {
        return new File(Configuration.getInstance().getAppContext().getFilesDir(), uin);
    }

}

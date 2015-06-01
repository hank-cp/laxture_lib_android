package com.laxture.lib.cache.storage;

import android.text.TextUtils;
import com.laxture.lib.util.DigestUtils;

import java.io.File;

public class HashCacheStorage extends CacheStorage {

    public HashCacheStorage(String key) {
        super(key);
    }

    @Override
    public File getFile() {
        if (mFile == null) {
            String md5 = DigestUtils.md5Hex(mKey);
            String path = TextUtils.join(File.separator, new String[]{
                    md5.substring(0,2),
                    md5.substring(2,4), md5});
            mFile = new File(getCacheHome(), path);
        }
        return mFile;
    }

}

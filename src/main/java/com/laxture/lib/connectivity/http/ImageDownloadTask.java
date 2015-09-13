package com.laxture.lib.connectivity.http;

import com.laxture.lib.cache.storage.CacheStorage;
import com.laxture.lib.cache.storage.CacheStorageManager;
import com.laxture.lib.util.BitmapUtil;
import com.laxture.lib.util.Checker;
import com.laxture.lib.util.LLog;
import com.laxture.lib.util.UnHandledException;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

public class ImageDownloadTask extends HttpDownloadTask<ImageDownloadTask.ImageInfo> {

    private String mCacheId;

    public ImageDownloadTask(String cacheId, String url, File downloadFile) {
        super(url, downloadFile);
        mCacheId = cacheId;
    }

    @Override
    protected ImageInfo generateResult() {
        // validate downloaded image.
        try {
            if (getContentType() != null
                    && !HttpHelper.CONTENT_TYPE_JPEG.equals(getContentType())
                    && !HttpHelper.CONTENT_TYPE_PNG.equals(getContentType())
                    && !HttpHelper.CONTENT_TYPE_GIF.equals(getContentType())
                    && connection.getResponseCode() != HttpURLConnection.HTTP_NOT_MODIFIED) {
                LLog.e("Download image failed, not valid content type: %s", getContentType());
                setErrorDetails(new HttpTaskException(
                        HttpTaskException.HTTP_ERR_CODE_DOWNLOAD_ERROR));

            } else if (!BitmapUtil.isValidImageFile(getDownloadFile())) {
                LLog.e("Download image failed, not valid image.");
                setErrorDetails(new HttpTaskException(
                        HttpTaskException.HTTP_ERR_CODE_DOWNLOAD_ERROR));
            }
        } catch (IOException e) {
            throw new UnHandledException(e);
        }

        if (getErrorDetails() != null) return null;

        ImageInfo imageInfo = new ImageInfo();
        imageInfo.contentType = getContentType();
        imageInfo.contentLength = getContentLength();
        imageInfo.downloadedFile = getDownloadFile();
        imageInfo.cacheId = mCacheId;

        // update last modify tag
        if (connection != null) {
            String lastModified = connection.getHeaderField("Last-Modified");
            if (!Checker.isEmpty(lastModified)) imageInfo.lastModify = lastModified;
            CacheStorage cache = CacheStorageManager.getInstance().getCache(url);
            if (cache != null) cache.setLastModify(lastModified);
        } else {
            imageInfo.lastModify = CacheStorageManager.getInstance().getCache(mCacheId).getLastModify();
        }

        return imageInfo;
    }

    public static class ImageInfo {
        public String contentType;
        public long contentLength;
        public File downloadedFile;
        public String cacheId;
        public String lastModify;
    }
}

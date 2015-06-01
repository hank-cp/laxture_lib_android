package com.laxture.lib.connectivity.http;

import com.laxture.lib.util.BitmapUtil;
import com.laxture.lib.util.LLog;

import java.io.File;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

public class ImageDownloadTask extends HttpDownloadTask<ImageDownloadTask.ImageInfo> {

    private String mCacheId;

    public ImageDownloadTask(String cacheId, String url, File downloadFile) {
        super(url, downloadFile);
        mCacheId = cacheId;
    }

    @Override
    protected ImageInfo processResponse(HttpResponse response) {
        // validate downloaded image.
        if (!HttpHelper.CONTENT_TYPE_JPEG.equals(getContentType())
                && !HttpHelper.CONTENT_TYPE_PNG.equals(getContentType())
                && !HttpHelper.CONTENT_TYPE_GIF.equals(getContentType())
                && response.getStatusLine().getStatusCode() != HttpStatus.SC_NOT_MODIFIED) {
            LLog.e("Download image failed, not valid content type: %s", getContentType());
            setErrorDetails(new HttpTaskException(
                    HttpTaskException.HTTP_ERR_CODE_DOWNLOAD_ERROR));

        } else if (!BitmapUtil.isValidImageFile(getDownloadFile())) {
            LLog.e("Download image failed, not valid image.");
            setErrorDetails(new HttpTaskException(
                    HttpTaskException.HTTP_ERR_CODE_DOWNLOAD_ERROR));
        }

        if (getErrorDetails() != null) return null;

        ImageInfo imageInfo = new ImageInfo();
        imageInfo.contentType = getContentType();
        imageInfo.contentLength = getContentLength();
        imageInfo.downloadedFile = getDownloadFile();
        imageInfo.cacheId = mCacheId;
        Header header = response.getFirstHeader("Last-Modified");
        if (header != null) imageInfo.lastModify = header.getValue();
        addCache(url, response);
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

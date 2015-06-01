package com.laxture.lib.connectivity.http;

import com.laxture.lib.cache.storage.StorageCacheManageThread;
import com.laxture.lib.cache.storage.StorageCacheRecord;
import com.laxture.lib.util.Checker;
import com.laxture.lib.util.LLog;
import com.laxture.lib.util.StreamUtil;
import com.laxture.lib.util.UnHandledException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;

public abstract class HttpDownloadTask<Result> extends HttpTask<Result> {

    public boolean enableResumeFromBreakPoint;

    public boolean avoidDuplicated;

    private long mContentLength;
    public long getContentLength() { return mContentLength; }

    private File mDownloadFile;
    public File getDownloadFile() { return mDownloadFile; }

    private String mContentType;
    public String getContentType() { return mContentType; }

    private long mDownloadedLength;
    private File mTempFile;

    private String mModifyTimestamp;
    public void setModifyTimestamp(String value) {
        mModifyTimestamp = value;
    }

    public HttpDownloadTask(String url, File downloadFile) {
        init(url, downloadFile);
    }

    protected void init(String url, File downloadFile) {
        this.url = url;
        mDownloadFile = downloadFile;
        mTempFile = new File(downloadFile.getParentFile(), downloadFile.getName()+".part");
    }

    public HttpDownloadTask() {}

    @Override
    protected Result run() {
        if (avoidDuplicated && !Checker.isEmpty(mDownloadFile)) {
            return processResponse(null);
        } else return super.run();
    }

    @Override
    protected HttpUriRequest buildRequest() {
        LLog.d("Initializing Binary Http request...");

        HttpUriRequest httpRequest;
        if (Checker.isEmpty(arguments)) {
            httpRequest = new HttpGet(url);
        } else {
            HttpPost httpPost = new HttpPost(url);
            HttpEntity entity = null;
            try {
                entity = new UrlEncodedFormEntity(arguments);
                httpPost.setEntity(entity);
            } catch (UnsupportedEncodingException e) {
                new UnHandledException(e);
            }
            httpRequest = httpPost;
        }

        if (!Checker.isEmpty(headers)) {
            for (String key : headers.keySet())
                httpRequest.setHeader(key, headers.get(key));
        }

        if (!Checker.isEmpty(mModifyTimestamp)) {
            httpRequest.setHeader("If-Modified-Since", mModifyTimestamp);
        }

        if (!Checker.isEmpty(mTempFile)) {
            mDownloadedLength = mTempFile.length();
            httpRequest.setHeader("Range", "bytes="+mDownloadedLength+"-");
        }
        return httpRequest;
    }

    protected void addCache(String cacheId, HttpResponse response) {
        Header header = response.getFirstHeader("Last-Modified");
        StorageCacheRecord cRecord = new StorageCacheRecord();
        cRecord.url = cacheId;
        cRecord.path = getDownloadFile().getAbsolutePath();
        if (header != null) cRecord.lastModify = header.getValue();
        cRecord.lastUsed = System.currentTimeMillis();
        //下载文件后更新缓存表的lastModify
        StorageCacheManageThread.getInstance().InsertDownloadStorageCache(cRecord);
    }

    @Override
    public void onReceived() {
        super.onReceived();

        // If 304, no need to consume entity
        if (!Checker.isEmpty(mDownloadFile)
                && httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
            LLog.d("Requested Content not modified, skip downloading");
            return;
        }

        // example response: Content-Range: bytes 302736-2156918/2156919
        Header rangeHeader = httpResponse.getFirstHeader("Content-Range");
        Header lengthHeader = httpResponse.getFirstHeader("Content-Length");
        mContentType = httpResponse.getEntity().getContentType().getValue();
        if (rangeHeader != null) {
            mContentLength = Integer.parseInt(rangeHeader.getValue().split("/")[1]);
        } else if (lengthHeader != null) {
            mContentLength = Integer.parseInt(lengthHeader.getValue());
        }

        try {
            long downloadLength = saveBinaryFile();
            if (mContentLength > 0 && downloadLength != mContentLength) {
                LLog.e("Downloaded file size is not matched. Expected %s, but received %s",
                        mContentLength, downloadLength);
                setErrorDetails(new HttpTaskException(
                        HttpTaskException.HTTP_ERR_CODE_DOWNLOAD_ERROR));
            } else {
                LLog.d("Download file %s successfully.", mDownloadFile.getAbsolutePath());
            }

        } catch (IOException e) {
            LLog.w("Download file "+mDownloadFile.getName()+" failed.", e);
            setErrorDetails(new HttpTaskException(
                    HttpTaskException.HTTP_ERR_CODE_SAVE_DOWNLOAD_ERROR, e));
        }
    }

    private long saveBinaryFile() throws IOException {
        HttpEntity entity = httpResponse.getEntity();
        InputStream is = null;
        FileOutputStream fos = null;

        if (entity == null)
            throw new IllegalArgumentException("HTTP entity should not be null.");

        //ref: http://stackoverflow.com/questions/4339082/android-decoder-decode-returned-false-for-bitmap-download
//            BufferedHttpEntity bufferedEntity = new BufferedHttpEntity(entity);
//            if (bufferedEntity.getContent() == null) return 0;
//            is = new FlushedInputStream(bufferedEntity.getContent());
        is = entity.getContent();
        if (is == null) return 0;

        // Create parent folder to avoid IOException
        if (!mDownloadFile.getParentFile().exists())
            mDownloadFile.getParentFile().mkdirs();

        // Clear temp file if no need to support resume download
        if (!enableResumeFromBreakPoint) {
            mTempFile.delete();
            mDownloadedLength = 0;
        }

        // If downloaded file bigger than the file to be downloaded, overwrite it.
        // If server doesn't support resuming download, start from head again.
        if (mDownloadedLength >= mContentLength || mContentLength == 0) {
            mDownloadFile.delete();
            mDownloadedLength = 0;
        }

        try {
            fos = new FileOutputStream(mTempFile, mDownloadedLength != 0);

            byte[] tmp = new byte[8192];
            int bufLength;
            while((bufLength = is.read(tmp)) != -1) {
                fos.write(tmp, 0, bufLength);
                if (mContentLength > 0) {
                    mDownloadedLength += bufLength;
                    postProgress((int) mContentLength, (int) mDownloadedLength);
                }
            }
        } finally {
            if (fos != null) fos.close();
        }

        LLog.v("Received binary response in length %s", mTempFile.length());

        if (mContentLength > 0 && mTempFile.length() == mContentLength) {
            // verify download length, move to output file if match
            mTempFile.renameTo(mDownloadFile);
        } else {
            // delete temp file if length verify unmatch
            mTempFile.delete();
        }

        StreamUtil.closeStream(is);
        StreamUtil.closeStream(fos);
        return mDownloadFile.length();
    }

}

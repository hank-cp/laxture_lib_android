package com.laxture.lib.connectivity.http;

import com.laxture.lib.cache.storage.StorageCacheManageThread;
import com.laxture.lib.cache.storage.StorageCacheRecord;
import com.laxture.lib.util.Checker;
import com.laxture.lib.util.LLog;
import com.laxture.lib.util.StreamUtil;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public abstract class HttpDownloadTask<Result> extends HttpTask<Result> {

    public boolean enableResumeFromBreakPoint;

    public boolean avoidDuplicatedDownload = true;

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
        super(url);
        init(downloadFile);
    }

    public HttpDownloadTask(String url, File downloadFile, HttpTaskConfig config) {
        super(url, config);
        init(downloadFile);
    }

    protected void init(File downloadFile) {
        mDownloadFile = downloadFile;
        mTempFile = new File(downloadFile.getParentFile(), downloadFile.getName()+".part");
    }

    @Override
    protected Result run() {
        if (avoidDuplicatedDownload && !Checker.isEmpty(mDownloadFile)) {
            return generateResult();
        } else return super.run();
    }

    @Override
    protected HttpURLConnection createConnection(String url) throws IOException {
        HttpURLConnection connection = super.createConnection(url);

        if (!Checker.isEmpty(mModifyTimestamp)) {
            connection.setRequestProperty("If-Modified-Since", mModifyTimestamp);
        }

        if (!Checker.isEmpty(mTempFile)) {
            mDownloadedLength = mTempFile.length();
            connection.setRequestProperty("Range", "bytes=" + mDownloadedLength + "-");
        }

        return connection;
    }

    protected void addCache(String cacheId, String lastModified) {
        StorageCacheRecord cRecord = new StorageCacheRecord();
        cRecord.url = cacheId;
        cRecord.path = getDownloadFile().getAbsolutePath();
        // 下载文件后更新缓存表的lastModify
        cRecord.lastModify = lastModified;
        cRecord.lastUsed = System.currentTimeMillis();
        StorageCacheManageThread.getInstance().InsertDownloadStorageCache(cRecord);
    }

    @Override
    protected void processResponse(InputStream inputStream) throws IOException {
        super.processResponse(inputStream);

        // If 304, no need to consume entity
        if (!Checker.isEmpty(mDownloadFile)
                && connection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
            LLog.d("Requested Content not modified, skip downloading");
            return;
        }

        // example response: Content-Range: bytes 302736-2156918/2156919
        String rangeHeader = connection.getHeaderField("Content-Range");
        String lengthHeader = connection.getHeaderField("Content-Length");
        mContentType = connection.getContentType();
        if (Checker.isEmpty(rangeHeader)) {
            mContentLength = Integer.parseInt(rangeHeader.split("/")[1]);
        } else if (lengthHeader != null) {
            mContentLength = Integer.parseInt(lengthHeader);
        }

        try {
            long downloadLength = saveBinaryFile(inputStream);
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

    private long saveBinaryFile(InputStream is) throws IOException {
        FileOutputStream fos = null;

        //ref: http://stackoverflow.com/questions/4339082/
//            BufferedHttpEntity bufferedEntity = new BufferedHttpEntity(entity);
//            if (bufferedEntity.getContent() == null) return 0;
//            is = new FlushedInputStream(bufferedEntity.getContent());
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

        StreamUtil.closeStream(fos);
        return mDownloadFile.length();
    }

}

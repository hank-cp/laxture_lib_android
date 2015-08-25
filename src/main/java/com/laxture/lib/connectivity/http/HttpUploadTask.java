package com.laxture.lib.connectivity.http;

import android.net.Uri;

import com.laxture.lib.task.TaskException;
import com.laxture.lib.util.Checker;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;

import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class HttpUploadTask extends HttpSimpleTask {

    private File mUploadFile;

    public HttpUploadTask(String url, File file) {
        super(url);
        mUploadFile = file;
    }

    public HttpUploadTask(String url, File file, HttpTaskConfig config) {
        super(url, config);
        mUploadFile = file;
    }

    @Override
    protected String run() {
        if (Checker.isEmpty(mUploadFile)) {
            setErrorDetails(new TaskException(
                    HttpTaskException.HTTP_ERR_CODE_INVALID_UPLOAD_FILE,
                    "Invalid upload file."));
            return null;
        }
        return super.run();
    }

    @Override
    protected HttpURLConnection createConnection(String url) throws IOException {
        String encodedUrl = Uri.encode(url, config.allowedUriChars);
        HttpURLConnection conn = (HttpURLConnection) new URL(encodedUrl).openConnection();

        // Setup connection
        setHeader(conn);
        conn.setRequestMethod(HttpHelper.HTTP_METHOD_POST);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setConnectTimeout(config.connectionTimeout);
        conn.setReadTimeout(config.socketTimeout);

        return conn;
    }

    @Override
    protected void sendRequest() throws IOException {
        // Create a new MultipartEntity
        CountingMultipartEntity reqEntity = new CountingMultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        ContentBody contentBody = new FileBody(mUploadFile);
        reqEntity.addPart("fileUpload", contentBody);

        connection.setFixedLengthStreamingMode((int)reqEntity.getContentLength());
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.addRequestProperty("Content-length", reqEntity.getContentLength()+"");
        connection.addRequestProperty(reqEntity.getContentType().getName(), reqEntity.getContentType().getValue());

        OutputStream os = connection.getOutputStream();
        reqEntity.writeTo(connection.getOutputStream());
        os.close();
        connection.connect();
    }

    public class CountingMultipartEntity extends MultipartEntity {

        public CountingMultipartEntity(HttpMultipartMode mode, String boundary, Charset charset) {
            super(mode, boundary, charset);
        }

        public CountingMultipartEntity(HttpMultipartMode mode) {
            super(mode);
        }

        public CountingMultipartEntity() {
        }

        @Override
        public void writeTo(final OutputStream outstream) throws IOException {
            super.writeTo(new CountingOutputStream(outstream, (int) getContentLength()));
        }
    }

    public class CountingOutputStream extends FilterOutputStream {

        private int mContentLength;
        private int mUploaded;

        public CountingOutputStream(final OutputStream out,
                                    final int contentLength) {
            super(out);
            mContentLength = contentLength;
            mUploaded = 0;
        }

        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            mUploaded += len;
            postProgress(mContentLength, mUploaded);
        }

        public void write(int b) throws IOException {
            out.write(b);
            mUploaded++;
            postProgress(mContentLength, mUploaded);
        }
    }
}

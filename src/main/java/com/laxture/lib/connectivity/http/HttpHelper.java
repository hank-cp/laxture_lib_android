package com.laxture.lib.connectivity.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import org.apache.http.HttpStatus;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.laxture.lib.util.Checker;
import com.laxture.lib.util.StreamUtil;
import com.laxture.lib.util.UnHandledException;

public class HttpHelper {

    public static final int HTTP_ERR_CODE_SUCCESSFUL = 0;

    public static final int HTTP_ERR_CODE_NETWORK_NOT_AVAILABLE = 90001;

    public static final int HTTP_ERR_CODE_CONNECTION_ERROR = 90002;

    public static final int HTTP_ERR_CODE_CONNECTION_TIME_OUT = 90003;

    public static final int HTTP_ERR_CODE_UNKNOW_ERROR = 90004;

    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_TEXT = "text/plain";
    public static final String CONTENT_TYPE_XML = "text/xml";
    public static final String CONTENT_TYPE_JPEG = "image/jpeg";
    public static final String CONTENT_TYPE_PNG = "image/png";
    public static final String CONTENT_TYPE_GIF = "image/gif";
    public static final String CONTENT_TYPE_GZIP = "application/x-gzip-compressed";

    public static final String UTF_8 = "UTF-8";
    public static final Charset DEFAULT_CHARSET = Charset.forName(UTF_8);

    public static final String HTTP_METHOD_POST = "POST";
    public static final String HTTP_METHOD_GET = "GET";

    public static boolean isBadHttpStatusCode(int code) {
        return code < HttpStatus.SC_OK
                || code > HttpStatus.SC_TEMPORARY_REDIRECT;
    }

    public static String simpleHttpRequestString(String url) {
        try {
            return new String(simpleHttpRequest(url), UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new UnHandledException("Failed to decode response from " + url, e);
        }
    }

    public static Bitmap simpleHttpRequestBitmap(String urlString) {
        if (Checker.isEmpty(urlString)) return null;

        InputStream httpStream = null;
        InputStream in = null;
        OutputStream out = null;

        try {
            URL url = new URL(urlString);
            if (url.getProtocol().equals("https"))
                url = new URL(urlString.replaceAll("https://", "http://"));

            httpStream = url.openStream();
            in = new FlushedInputStream(httpStream);
            return BitmapFactory.decodeStream(in);

        } catch (MalformedURLException e) {
            throw new UnHandledException("Bad url "+urlString, e);
        } catch (IOException e) {
            throw new UnHandledException(e.getMessage(), e);
        } finally {
            StreamUtil.closeStream(httpStream);
            StreamUtil.closeStream(in);
            StreamUtil.closeStream(out);
        }
    }

    public static byte[] simpleHttpRequest(String urlString) {
        if (Checker.isEmpty(urlString)) return null;

        InputStream httpStream = null;
        InputStream in = null;
        OutputStream out = null;

        try {
            URL url = new URL(urlString);
            if (url.getProtocol().equals("https"))
                url = new URL(urlString.replaceAll("https://", "http://"));

            httpStream = url.openStream();
            in = new BufferedInputStream(httpStream, StreamUtil.IO_BUFFER_SIZE);

            ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, StreamUtil.IO_BUFFER_SIZE);
            StreamUtil.copy(in, out);
            out.flush();

            return dataStream.toByteArray();
        } catch (MalformedURLException e) {
            throw new UnHandledException("Bad url "+urlString, e);
        } catch (IOException e) {
            throw new UnHandledException(e.getMessage(), e);
        } finally {
            StreamUtil.closeStream(httpStream);
            StreamUtil.closeStream(in);
            StreamUtil.closeStream(out);
        }
    }

    /**
     * a bug in the previous versions of BitmapFactory.decodeStream may prevent this code
     * from working over a slow connection. Decode a new FlushedInputStream(inputStream)
     * instead to fix the problem.
     *
     * ref: http://android-developers.blogspot.com/2010/07/multithreading-for-performance.html
     */
    public static class FlushedInputStream extends FilterInputStream {

        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                      int b = read();
                      if (b < 0) {
                          break;  // we reached EOF
                      } else {
                          bytesSkipped = 1; // we read one byte
                      }
               }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }

    /**
     * Copy from Uri.getQueryParameter() and omit NOT_HIERARCHICAL exception.
     */
    public static String getQueryParameter(Uri uri, String key) {
        if (uri == null) return null;

        String query = uri.getEncodedQuery();

        if (query == null) return null;

        String encodedKey;
        try {
            encodedKey = URLEncoder.encode(key, UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }

        String prefix = encodedKey + "=";

        if (query.length() < prefix.length()) return null;

        int start;
        if (query.startsWith(prefix)) {
            // It's the first parameter.
            start = prefix.length();
        } else {
            // It must be later in the query string.
            prefix = "&" + prefix;
            start = query.indexOf(prefix);

            // Not found.
            if (start == -1) return null;

            start += prefix.length();
        }

        // Find end of value.
        int end = query.indexOf('&', start);
        if (end == -1) end = query.length();

        String value = query.substring(start, end);
        return Uri.decode(value);
    }

}

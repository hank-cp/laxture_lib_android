package com.laxture.lib.connectivity.http;

import com.laxture.lib.task.AbstractAsyncTask;
import com.laxture.lib.util.Checker;
import com.laxture.lib.util.LLog;
import com.laxture.lib.util.NetworkUtil;
import com.laxture.lib.util.UnHandledException;

import org.apache.http.*;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public abstract class HttpTask<Result> extends AbstractAsyncTask<Result> {

    private static final boolean DEBUG = false;

    private static final int HTTP_CONNECTION_TIMEOUT = 10 * 1000; // 10 seconds
    private static final int HTTP_SOCKET_TIMEOUT = 20 * 1000; // 20 seconds
    private static final int HTTP_SOCKET_BUFFER_SIZE = 8192; // http的缓冲区大小设置

    private static final int RETRY_TIMES = 1;

    private static DefaultHttpClient sHttpClient;

    volatile HttpUriRequest httpRequest;
    HttpResponse httpResponse;
    int retries;

    // request arguments
    public String url;

    protected List<NameValuePair> arguments;

    protected Map<String, String> headers;

    private synchronized DefaultHttpClient getHttpClient() {
        if (sHttpClient == null) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, HTTP_CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpParams, HTTP_SOCKET_TIMEOUT);
            HttpProtocolParams.setContentCharset(httpParams, HttpHelper.UTF_8);
            HttpConnectionParams.setSocketBufferSize(httpParams, HTTP_SOCKET_BUFFER_SIZE);
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
            ClientConnectionManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
            sHttpClient = new DefaultHttpClient(cm, httpParams);
            GZipResponseInterceptor gzipInterceptor = new GZipResponseInterceptor();
            sHttpClient.addResponseInterceptor(gzipInterceptor);
            sHttpClient.setRedirectHandler(new DefaultRedirectHandler() {
                @Override
                public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
                    boolean isRedirect = super.isRedirectRequested(response, context);
                    if (!isRedirect) {
                        int responseCode = response.getStatusLine().getStatusCode();
                        if (responseCode == HttpStatus.SC_MOVED_PERMANENTLY
                                || responseCode == HttpStatus.SC_MOVED_TEMPORARILY) {
                            return true;
                        }
                    }
                    return isRedirect;
                }
            });
        }
        return sHttpClient;
    }

    public void addArgument(String key, String value) {
        if (Checker.isEmpty(arguments)) arguments = new ArrayList<NameValuePair>();
        arguments.add(new BasicNameValuePair(key, value));
    }

    public void addHeader(String key, String value) {
        if (Checker.isEmpty(headers)) headers = new HashMap<String, String>();
        headers.put(key, value);
    }

    //*************************************************************************
    // Implementation of AbstractTask
    //*************************************************************************

    @Override
    protected Result run() {
        // if mContext is set, check network status first
        if (!NetworkUtil.isNetworkAvailable()) {
            int errorCode = HttpTaskException.HTTP_ERR_CODE_NETWORK_NOT_AVAILABLE;
            setErrorDetails(new HttpTaskException(errorCode));
            return null;
        }

        try {
            httpRequest = buildRequest();

            onSend();
            httpResponse = getHttpClient().execute(httpRequest);
            // if user cancel the connection, onError should handle it
            if (isCancelled()) return onErrorOrCancel(null);

            // if response status code is not 200, or content type (MIME type) is not json
            // go to onError() method to retry or terminate
            if (httpResponse == null
                    || HttpHelper.isBadHttpStatusCode(httpResponse.getStatusLine().getStatusCode()))
                return onErrorOrCancel(null);
            onReceived();

            return processResponse(httpResponse);

        } catch (Exception e) {
            // exception should be log in onError();
            LLog.w("error:" + e.getMessage());
            return onErrorOrCancel(e);
        }
    }

    @Override
    public boolean cancel() {
        try {
            if (httpRequest != null) httpRequest.abort();
        } catch (Throwable e) {
            LLog.w("Abort http connection failed : %s", url);
        }
        return super.cancel();
    }

    //*************************************************************************
    // Http Callback
    //*************************************************************************

    protected abstract HttpUriRequest buildRequest();

    protected abstract Result processResponse(HttpResponse response);

    protected int getRetries() {
        return retries;
    }

    protected void onSend() {
        // Log request info
        if (DEBUG) {
            LLog.v("Sending Http Request to " + httpRequest.getURI());
            LLog.v("Http Request Header Line :: " + httpRequest.getRequestLine().toString());
            for (Header header : httpRequest.getAllHeaders()) {
                LLog.v("Http Request Header :: " + header.toString());
            }
        }
    }

    protected void onReceived() {
        // Log response info
        if (DEBUG) {
            LLog.v("Receive Http Response from " + httpRequest.getURI());
            LLog.v("Http Response Header Line :: " + httpResponse.getStatusLine().toString());
            for (Header header : httpResponse.getAllHeaders()) {
                LLog.v("Http Response Header :: " + header.toString());
            }
        }
    }

    /**
     * call {@link #cancel()} will cause Exception. Need to catch and return
     * cancelled status.
     */
    protected Result onErrorOrCancel(Exception ex) {
        if (isCancelled()) return null;

        // logic/fatal exception, throw it out.
        if (ex != null && ex instanceof UnHandledException) {
            UnHandledException exception = (UnHandledException) ex;
            throw exception;
        }

        // Log response info
        String errorMsg = null;
        if (ex != null) {
            errorMsg = "Http Connection to %s failed with exception :: " +
                    (!Checker.isEmpty(ex.getMessage()) ? ex.getMessage() : ex.getClass().getName());

        } else if (httpResponse == null) {
            errorMsg = "Http Connection to %s failed empty response";

        } else if (HttpHelper.isBadHttpStatusCode(httpResponse.getStatusLine().getStatusCode())) {
            errorMsg = "Http Connection to %s failed with unexcepted http status code :: "
                    + httpResponse.getStatusLine().getStatusCode();

        } else errorMsg = "Http Connection to %s failed with unexcepted Error!";

        LLog.e(errorMsg, ex, url);

        // retry
        retries++;
        if (retries <= RETRY_TIMES) {
            // Android's implemenration of ThreadSafeClientConnManager keep only 2
            // connection in pool. Need to abort failed httpRequest before retry.
            try {
                if (httpRequest != null) httpRequest.abort();
            } catch (Throwable e) {
                // abort failed, skip retry
                LLog.w("Abort http connection failed : %s", url);
                return null;
            }
            LLog.e("Retries Count :: " + retries);
            LLog.d("ConnectionInPool=%d",
                    ((ThreadSafeClientConnManager) getHttpClient().
                            getConnectionManager()).getConnectionsInPool());
            return run();
        }

        if (ex != null)
            setErrorDetails(new HttpTaskException(
                    HttpTaskException.HTTP_ERR_CODE_CONNECTION_ERROR, ex));

        else if (httpResponse == null)
            setErrorDetails(new HttpTaskException(
                    HttpTaskException.HTTP_ERR_CODE_SERVER_ERROR));

        else if (HttpHelper.isBadHttpStatusCode(httpResponse.getStatusLine().getStatusCode())) {
            // server reply status error, for example 404, 403
            setErrorDetails(new HttpTaskException(
                    httpResponse.getStatusLine().getStatusCode()));

        } else {
            // unknow error, shouldn't reach here
            setErrorDetails(new HttpTaskException(
                    HttpTaskException.HTTP_ERR_CODE_UNKNOW_ERROR));
        }

        return null;
    }

    //*************************************************************************
    //  Inner Class Definition
    //*************************************************************************

    private static class GZipResponseInterceptor implements HttpResponseInterceptor {

        @Override
        public void process(final HttpResponse response,
                            final HttpContext context) throws HttpException, IOException {
            HttpEntity entity = response.getEntity();
            if (entity == null) return;
            Header ceheader = entity.getContentEncoding();
            if (ceheader == null) return;
            HeaderElement[] codecs = ceheader.getElements();
            for (HeaderElement codec : codecs) {
                if (codec.getName().equalsIgnoreCase("gzip")) {
                    response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                    return;
                }
            }
        }
    }

    private static class GzipDecompressingEntity extends HttpEntityWrapper {

        public GzipDecompressingEntity(final HttpEntity entity) {
            super(entity);
        }

        @Override
        public InputStream getContent() throws IOException, IllegalStateException {
            // the wrapped entity's getContent() decides about repeatability
            InputStream wrappedin = wrappedEntity.getContent();
            return new GZIPInputStream(wrappedin);
        }

        @Override
        public long getContentLength() {
            // length of ungzipped content is not known
            return -1;
        }
    }

}

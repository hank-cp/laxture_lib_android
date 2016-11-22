package com.laxture.lib.connectivity.http;

import android.net.Uri;
import android.text.TextUtils;

import com.laxture.lib.task.AbstractAsyncTask;
import com.laxture.lib.util.Checker;
import com.laxture.lib.util.LLog;
import com.laxture.lib.util.NetworkUtil;
import com.laxture.lib.util.UnHandledException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class HttpTask<Result> extends AbstractAsyncTask<Result> {

    private static final boolean DEBUG = true;

    int retries;

    // request arguments
    public String url;

    public String method = HttpHelper.HTTP_METHOD_GET;

    protected HashMap<String, String> arguments = new HashMap<>();

    protected String requestJsonBody;

    protected HttpTaskConfig config;

    protected HttpURLConnection connection;

    protected int responseCode;

    //*************************************************************************
    // Http Support
    //*************************************************************************

    public HttpTask(String url) {
        this.url = url;
        this.config = new HttpTaskConfig();
    }

    public HttpTask(String url, HttpTaskConfig config) {
        this.url = url;
        this.config = config;
    }

    public void addArgument(String key, Object value) {
        arguments.put(key, value.toString());
    }

    public void addArgument(String key, int value) {
        arguments.put(key, Integer.toString(value));
    }

    public void addArgument(String key, boolean value) {
        arguments.put(key, Boolean.toString(value));
    }

    public void addArgument(String key, long value) {
        arguments.put(key, Long.toString(value));
    }

    public void addArgument(String key, float value) {
        arguments.put(key, Float.toString(value));
    }

    public void addArgument(String key, double value) {
        arguments.put(key, Double.toString(value));
    }

    public void addArgument(String key , long[] value) {
        arguments.put(key, Arrays.toString(value));
    }

    public void addArgument(String key , String[] value) {
        arguments.put(key, Arrays.toString(value));
    }

    //*************************************************************************
    // Implementation of AbstractTask
    //*************************************************************************

    @Override
    public Result run() {
        // if mContext is set, check network status first
        if (!NetworkUtil.isNetworkAvailable()) {
            int errorCode = HttpTaskException.HTTP_ERR_CODE_NETWORK_NOT_AVAILABLE;
            setErrorDetails(new HttpTaskException(errorCode));
            return null;
        }
        // if user cancel the connection, onError should handle it
        if (isCancelled()) return onErrorOrCancel(null);

        try {
            connection = createConnection(url);

            sendRequest();

            int redirectCount = 0;
            while (connection.getResponseCode() / 100 == 3 && redirectCount < config.maxRedirectCount) {
                connection = createConnection(connection.getHeaderField("Location"));
                redirectCount++;
            }

            // if user cancel the connection, onError should handle it
            if (isCancelled()) return onErrorOrCancel(null);

            responseCode = connection.getResponseCode();
            // if response status code is not 200, or content type (MIME type) is not json
            // go to onError() method to retry or terminate
            if (HttpHelper.isBadHttpStatusCode(responseCode))
                return onErrorOrCancel(null);

            processResponse(connection.getInputStream());
            if (getErrorDetails() != null) return null;

            return generateResult();

        } catch (Exception e) {
            // exception should be log in onError();
            return onErrorOrCancel(e);

        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    @Override
    public boolean cancel() {
        try {
            if (connection != null) connection.disconnect();
        } catch (Throwable e) {
            LLog.i("Abort http connection %s", url);
        }
        return super.cancel();
    }

    //*************************************************************************
    // Http Callback
    //*************************************************************************

    protected HttpURLConnection createConnection(String url) throws IOException {
        String encodedUrl = Uri.encode(url, config.allowedUriChars);
        HttpURLConnection conn;

        StringBuilder postData = new StringBuilder();
        if (!Checker.isEmpty(requestJsonBody)) {
            postData.append(requestJsonBody);

        } else {
            for (Map.Entry<String, String> param : arguments.entrySet()) {
                if (param.getValue().startsWith("[") && param.getValue().endsWith("]")) {
                    String[] array = param.getValue().substring(1, param.getValue().length()-1).split(", ");
                    for (String p : array) {
                        if (postData.length() != 0) postData.append('&');
                        postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                        postData.append('=');
                        postData.append(URLEncoder.encode(p, "UTF-8"));
                    }
                } else {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(param.getValue(), "UTF-8"));
                }
            }
        }

        if (method.equals(HttpHelper.HTTP_METHOD_POST)) {
            if (DEBUG) LLog.d("Posting data : "+postData.toString());

            conn = (HttpURLConnection) new URL(encodedUrl).openConnection();
            setHeader(conn);
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");
            conn.setRequestMethod(HttpHelper.HTTP_METHOD_POST);
            conn.setRequestProperty("Content-Type", !Checker.isEmpty(requestJsonBody)
                    ? "application/json"
                    : "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setRequestProperty("charset", "utf-8");
            conn.setInstanceFollowRedirects(false);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.getOutputStream().write(postDataBytes);

        } else if (postData.length() > 0) {
            if (postData.indexOf("?") == -1) {
                encodedUrl += "?" + postData.toString();
            } else {
                encodedUrl += "&" + postData.toString();
            }
            conn = (HttpURLConnection) new URL(encodedUrl).openConnection();
            setHeader(conn);

        } else {
            conn = (HttpURLConnection) new URL(encodedUrl).openConnection();
            setHeader(conn);
        }

        conn.setConnectTimeout(config.connectionTimeout);
        conn.setReadTimeout(config.socketTimeout);

        return conn;
    }

    protected void setHeader(HttpURLConnection conn) {
        if (config.headers == null) return;
        for (Map.Entry<String, String> header : config.headers.entrySet()) {
            conn.setRequestProperty(header.getKey(), header.getValue());
        }

        // Set cookies
        if (HttpHelper.sharedCookieManager.getCookieStore().getCookies().size() > 0) {
            //While joining the Cookies, use ',' or ';' as needed. Most of the server are using ';'
            conn.setRequestProperty("Cookie",
                    TextUtils.join(";", HttpHelper.sharedCookieManager.getCookieStore().getCookies()));
        }
    }

    protected int getRetries() {
        return retries;
    }

    protected void sendRequest() throws IOException {
        // Log request info
        if (DEBUG) {
            LLog.v("Requesting Http URL (%s) : %s", connection.getRequestMethod(), connection.getURL());
        }
    }

    protected void processResponse(InputStream inputStream) throws IOException {
        // Load/Save cookies
        Map<String, List<String>> headerFields = connection.getHeaderFields();
        List<String> cookiesHeader = headerFields.get(HttpHelper.COOKIES_HEADER);
        if (cookiesHeader != null) {
            for (String cookie : cookiesHeader) {
                HttpHelper.sharedCookieManager.getCookieStore().add(
                        null, HttpCookie.parse(cookie).get(0));
            }
        }

        // Log response info
        if (DEBUG) {
            LLog.v("Receiving Http Response from " + connection.getURL());
        }
    }

    protected abstract Result generateResult();

    /**
     * call {@link #cancel()} will cause Exception. Need to catch and return
     * cancelled status.
     */
    protected Result onErrorOrCancel(Exception ex) {
        if (isCancelled()) return null;

        // logic/fatal exception, throw it out.
        if (ex != null && ex instanceof UnHandledException) {
            throw (UnHandledException) ex;
        }

        // generate error message
        String errorMsg;
        if (ex != null) {
            errorMsg = "Http Connection to %s failed with exception :: " +
                    (!Checker.isEmpty(ex.getMessage()) ? ex.getMessage() : ex.getClass().getName());

        } else if (responseCode == 0) {
            errorMsg = "Http Connection to %s failed empty response";

        } else if (HttpHelper.isBadHttpStatusCode(responseCode)) {
            errorMsg = "Http Connection to %s failed with unexpected http status code :: "
                    + responseCode;

        } else errorMsg = "Http Connection to %s failed with unexpected Error!";

        LLog.e(errorMsg, ex, url);

        // retry
        retries++;
        if (retries <= config.maxRetryCount) {
            return run();
        }

        if (ex != null) {
            setErrorDetails(new HttpTaskException(
                    HttpTaskException.HTTP_ERR_CODE_CONNECTION_ERROR, ex));

        } else if (responseCode == 0) {
            setErrorDetails(new HttpTaskException(
                    HttpTaskException.HTTP_ERR_CODE_SERVER_ERROR));

        } else if (HttpHelper.isBadHttpStatusCode(responseCode)) {
            // server reply status error, for example 404, 403
            setErrorDetails(new HttpTaskException(responseCode));

        } else {
            // unknown error, shouldn't reach here
            setErrorDetails(new HttpTaskException(
                    HttpTaskException.HTTP_ERR_CODE_UNKNOWN_ERROR));
        }

        return null;
    }

}

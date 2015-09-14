package com.laxture.lib.connectivity.http;

import com.laxture.lib.util.CollectionUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * HttpTask configuration holder to shared common setting between different {@link HttpTask}
 */
public class HttpTaskConfig {

    public String allowedUriChars = "@#&=*+-_.,:!?()/~'%";
    public int connectionTimeout = 10 * 1000; // 10 seconds
    public int socketTimeout = 20 * 1000; // 20 seconds
    public int socketBufferSize = 8192; // http的缓冲区大小设置
    public int maxRedirectCount = 5;
    public int maxRetryCount = 0;
    public Map<String, String> headers;

    public HttpTaskConfig immutableCopy() {
        HttpTaskConfig config = new HttpTaskConfig();
        config.allowedUriChars = allowedUriChars;
        config.connectionTimeout = connectionTimeout;
        config.socketTimeout = socketTimeout;
        config.socketBufferSize = socketBufferSize;
        config.maxRetryCount = maxRetryCount;
        if (headers != null) {
            headers = new HashMap<>();
            CollectionUtil.copyMap(headers, config.headers);
        }
        return config;
    }

}

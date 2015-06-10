package com.laxture.lib.connectivity.http;

import com.laxture.lib.util.StreamUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HttpSimpleTask extends HttpTask<String> {

    private String responseText;

    public HttpSimpleTask(String url) {
        super(url);
    }

    public HttpSimpleTask(String url, HttpTaskConfig config) {
        super(url, config);
    }

    @Override
    protected void processResponse(InputStream inputStream) throws IOException {
        super.processResponse(inputStream);

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String str = reader.readLine();
        while (str != null) {
            sb.append(str);
            str = reader.readLine();
        }
        StreamUtil.closeStream(reader);
        responseText = sb.toString();
    }

    @Override
    protected String generateResult() {
        return responseText;
    }

}

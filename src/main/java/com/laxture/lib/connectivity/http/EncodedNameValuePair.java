package com.laxture.lib.connectivity.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.NameValuePair;

import android.text.TextUtils;

import com.laxture.lib.util.UnHandledException;

public class EncodedNameValuePair implements NameValuePair {

    private String name;

    private String value;

    public EncodedNameValuePair(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString(){
        if (TextUtils.isEmpty(this.name)) {
            return "";
        }
        String v=(this.value==null)?"":this.value;
        try {
            return this.name+"="+URLEncoder.encode(v, HttpHelper.UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new UnHandledException(e);
        }
    }

}

package com.laxture.lib.connectivity.push;

public class PushEvent {

    public <T> PushEvent(T data, PushProvider provider) {
        mData = data;
        mPushProvider = provider;
    }

    private PushProvider mPushProvider;
    public PushProvider getPushProvider() { return mPushProvider; }

    private Object mData;
    @SuppressWarnings("unchecked")
    public <T> T getData() { return (T) mData; }

}

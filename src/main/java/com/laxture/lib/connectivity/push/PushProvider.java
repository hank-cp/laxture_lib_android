package com.laxture.lib.connectivity.push;

public interface PushProvider {

    public String getProviderId();

    /**
     * Don't call this method, PushManager shall handle it.
     */
    public void registerPushService();

    /**
     * Don't call this method, PushManager shall handle it.
     */
    public void unRegisterPushService();

    /**
     * If set to true, and after registration, PushManager will hold
     * this PushProvider but not really all {@link #registerPushService()}
     * until the first {@link PushListener} is added. And {@link #unRegisterPushService()}
     * will be called after the last {@link PushListener} is removed.
     *
     * Set this option to true will help reduce network traffic.
     */
    public boolean lazyRegistration();

}
package com.laxture.lib.connectivity.push;

public interface PushListener {

    public boolean test(PushEvent pushEvent);

    public boolean onPushReceived(PushEvent pushEvent);

    public boolean shouldPostToMainThread();

}

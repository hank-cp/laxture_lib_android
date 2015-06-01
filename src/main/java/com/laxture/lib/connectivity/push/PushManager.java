package com.laxture.lib.connectivity.push;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;

import com.laxture.lib.util.ArrayUtil;
import com.laxture.lib.util.LLog;
import com.laxture.lib.util.UnHandledException;

public class PushManager extends Thread {

    private static PushManager sInstance;

    private volatile boolean mHandled;
    private Hashtable<String, PushProvider> mProviders
            = new Hashtable<String, PushProvider>();

    private PushManager() {
        setName("Push Manager");
    }

    public static synchronized PushManager getInstance() {
        if (sInstance == null) {
            LLog.d("Initializing PushManager...");
            sInstance = new PushManager();
            sInstance.start();
        }
        return sInstance;
    }

    private LinkedBlockingQueue<PushEvent> mPushQueue =
            new LinkedBlockingQueue<PushEvent>();

    private SparseArray<PushListener> mPushListeners
        = new SparseArray<PushListener>();

    public void addPushListener(PushListener listener, int level) {
        addPushListener(listener, level, null);
    }

    public void addPushListener(PushListener listener, int level, String providerID) {
        synchronized (mPushListeners) {
            // Before first listener is added, register lazing loading
            // PushProvider
            if (mPushListeners.size() == 0 && mProviders.size() > 0) {
                if (null == providerID) {
                    for (PushProvider provider : new ArrayList<PushProvider>(mProviders.values())) {
                        if (provider.lazyRegistration()) provider.registerPushService();
                    }
                } else {
                    for (PushProvider provider : new ArrayList<PushProvider>(mProviders.values())) {
                        if (provider.lazyRegistration()
                                && provider.getProviderId().equals(providerID)) {
                            provider.registerPushService();
                        }
                    }
                }
            }

            mPushListeners.append(level, listener);
        }
    }

    public void removePushListener(int level) {
        mPushListeners.remove(level);

        // After last listener is removed, unregister lazing loading
        // PushProvider
        if (mPushListeners.size() == 0 && mProviders.size() > 0) {
            for (PushProvider provider : new ArrayList<PushProvider>(mProviders.values())) {
                if (provider.lazyRegistration()) provider.unRegisterPushService();
            }
        }
    }

    public void removePushListener(PushListener listener) {
        synchronized (mPushListeners) {
            int index = mPushListeners.indexOfValue(listener);
            if (index < 0) return;
            removePushListener(mPushListeners.keyAt(index));
        }
    }

    /**
     * This method should only be called by PushProvider
     */
    public void receivePushEvent(PushEvent pushEvent) {
        mPushQueue.offer(pushEvent);
    }

    /**
     * Start listening specific PushProvider
     *
     * @param pushProvider
     */
    public void registerPushProvider(PushProvider pushProvider) {
        // avoid duplicated register
        if (!mProviders.contains(pushProvider.getProviderId())) {
            mProviders.put(pushProvider.getProviderId(), pushProvider);
            if (mPushListeners.size() != 0 || !pushProvider.lazyRegistration()) {
                pushProvider.registerPushService();
            }
        }
    }

    /**
     * Stop listening specific PushProvider
     *
     * @param providerId defined in PushProvider's constants, could be
     *                   return by {@link PushProvider#getProviderId()}
     */
    public void unRegisterPushProvider(String providerId) {
        PushProvider provider = mProviders.get(providerId);
        if (provider != null) {
            provider.unRegisterPushService();
            mProviders.remove(providerId);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                // block manager thread until PushEvent available
                final PushEvent pushEvent = mPushQueue.take();

                // pop pushEvent to listeners, if one of the handler return true,
                // the event will stop propagating
                // order: UI -> Notification -> Silent
                mHandled = false;

                // Copy a safe array of PushListeners to notify, since SparesArray
                // is not thread-safe
                SparseArray<PushListener> tmpListeners =
                        ArrayUtil.cloneSparseArray(mPushListeners);

                for (int i=tmpListeners.size()-1; i>=0; i--) {
                    PushListener listener = tmpListeners.get(tmpListeners.keyAt(i));
                    if (!listener.test(pushEvent)) continue;

                    if (listener.shouldPostToMainThread()) {
                        MainRunnable runnable = new MainRunnable(listener, pushEvent);
                        new Handler(Looper.getMainLooper()).post(runnable);
                        runnable.latch.await();
                    } else {
                        mHandled = listener.onPushReceived(pushEvent);
                    }

                    // quit this push event is handled
                    if (mHandled) break;
                }
            }

        } catch (InterruptedException e) {
            throw new UnHandledException(e);
        }
    }

    /**
     * This runnable will wait current thread until execute finished
     * on MainThreads
     */
    class MainRunnable implements Runnable {

        PushListener listener;
        PushEvent event;
        CountDownLatch latch = new CountDownLatch(1);

        MainRunnable(PushListener listener, PushEvent event) {
            this.listener = listener;
            this.event = event;
        }

        @Override
        public void run() {
            try {
                mHandled = listener.onPushReceived(event);
            } catch (Throwable e) {
                LLog.w("Call onPushReceived failed", e);
            }
            latch.countDown();
        }
    }

}

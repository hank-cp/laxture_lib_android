package com.laxture.lib.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.laxture.lib.R;
import com.laxture.lib.cache.storage.CacheStorage;

import java.io.File;

public class AsyncTouchImageView extends TouchImageView {

    private AsyncImageAdapter mAdapter = new AsyncImageAdapter(this);

    public AsyncImageAdapter getAdapter() { return mAdapter; }

    private int mProgressViewResId;

    public AsyncTouchImageView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public AsyncTouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.AsyncImageView);
        mAdapter.setLoadingImageRes(a.getResourceId(R.styleable.AsyncImageView_loadingDrawable, 0));
        mAdapter.setFailedImageRes(a.getResourceId(R.styleable.AsyncImageView_failedDrawable, 0));
        mAdapter.setBoundWidth(a.getDimensionPixelSize(R.styleable.AsyncImageView_boundWidth, 0));
        mAdapter.setBoundHeight(a.getDimensionPixelSize(R.styleable.AsyncImageView_boundHeight, 0));
        mAdapter.setDensity(a.getInt(R.styleable.AsyncImageView_density, -1));
        mProgressViewResId = a.getResourceId(R.styleable.AsyncImageView_progressView, 0);
        a.recycle();
    }

    public AsyncTouchImageView(Context context) {
        super(context);

        if (context instanceof Activity) {
            ProgressBar progressBar = (ProgressBar) ((Activity)context).findViewById(mProgressViewResId);
            if (progressBar != null) {
                getAdapter().setProgressView(progressBar);
                progressBar.bringToFront();
            }
        }
    }

    public void setImage(String imageUrl) {
        getAdapter().setImage(imageUrl);
    }

    public void setImage(String taskTag, String imageUrl) {
        getAdapter().setImage(taskTag, imageUrl);
    }

    public void setImage(CacheStorage cache) {
        getAdapter().setImage(cache);
    }

    public void setImage(String taskTag, CacheStorage cache) {
        getAdapter().setImage(taskTag, cache);
    }

    public void setImage(String cacheId, String taskTag, File localImageFile, String imageUrl, File downloadFile) {
        getAdapter().setImage(cacheId, taskTag, localImageFile, imageUrl, downloadFile);
    }

    /**
     * Call this method in {@link Activity}.onCreate() or {@link Fragment}.onCreateView()
     * to setup ProgressView that specified in layout xml.
     *
     * @param activity
     */
    public void installProgressView(Activity activity) {
        ProgressBar progressBar = (ProgressBar) activity.findViewById(mProgressViewResId);
        if (progressBar != null) getAdapter().setProgressView(progressBar);
    }

    public void setProgressView(ProgressBar progressView) {
        getAdapter().setProgressView(progressView);
    }

}

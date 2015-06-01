package com.laxture.lib.view;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.laxture.lib.R;
import com.laxture.lib.cache.storage.ContentStorage;

public class AsyncImageView extends ImageView {

    private AsyncImageAdapter mAdapter = new AsyncImageAdapter(this);
    public AsyncImageAdapter getAdapter() { return mAdapter; }

    private int mProgressViewResId;

    public AsyncImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AsyncImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.AsyncImageView);
        mAdapter.setLoadingImageRes(a.getResourceId(R.styleable.AsyncImageView_loadingDrawable, 0));
        mAdapter.setFailedImageRes(a.getResourceId(R.styleable.AsyncImageView_failedDrawable, 0));
        mAdapter.setBoundWidth(a.getDimensionPixelSize(R.styleable.AsyncImageView_boundWidth, 0));
        mAdapter.setBoundHeight(a.getDimensionPixelSize(R.styleable.AsyncImageView_boundHeight, 0));
        mAdapter.setDensity(a.getInt(R.styleable.AsyncImageView_density, -1));
        mProgressViewResId = a.getResourceId(R.styleable.AsyncImageView_progressView, 0);
        a.recycle();
    }

    public AsyncImageView(Context context) {
        super(context);
    }

    public void setImage(String imageUrl) {
        getAdapter().setImage(imageUrl);
    }

    public void setImage(String taskTag, String imageUrl) {
        getAdapter().setImage(taskTag, imageUrl);
    }

    public void setImage(ContentStorage storage) {
        getAdapter().setImage(storage);
    }

    public void setImage(String taskTag, ContentStorage storage) {
        getAdapter().setImage(taskTag, storage);
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
        if (progressBar != null) {
            getAdapter().setProgressView(progressBar);
            progressBar.bringToFront();
        }
    }

    public void setProgressView(ProgressBar progressView) {
        getAdapter().setProgressView(progressView);
    }


}

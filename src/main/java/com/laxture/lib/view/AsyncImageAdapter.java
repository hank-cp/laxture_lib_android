package com.laxture.lib.view;

import android.annotation.TargetApi;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.laxture.lib.cache.storage.CacheStorage;
import com.laxture.lib.cache.storage.CacheStorageManager;
import com.laxture.lib.connectivity.http.ImageDownloadTask;
import com.laxture.lib.connectivity.http.ImageDownloadTask.ImageInfo;
import com.laxture.lib.task.TaskException;
import com.laxture.lib.task.TaskListener.TaskCancelledListener;
import com.laxture.lib.task.TaskListener.TaskFailedListener;
import com.laxture.lib.task.TaskListener.TaskFinishedListener;
import com.laxture.lib.task.TaskListener.TaskProgressUpdatedListener;
import com.laxture.lib.task.TaskManager;
import com.laxture.lib.util.BitmapUtil;
import com.laxture.lib.util.BitmapUtil.ResizeMode;
import com.laxture.lib.util.Checker;
import com.laxture.lib.util.LLog;

import java.io.File;

public class AsyncImageAdapter implements TaskProgressUpdatedListener,
        TaskFinishedListener<ImageInfo>, TaskCancelledListener<ImageInfo>,
        TaskFailedListener<ImageInfo> {

    public static final boolean DEBUG = false;

    public enum Status {
        Idle, Loading, Finished, Failed,
    }
    private Status mStatus = Status.Idle;
    public Status getStatus() { return mStatus; }

    private ImageView mImageView;
    private File mLoadingImageFile;
    private String mLastModified;

    /**
     * This image file will be shown during download
     *
     * @param file
     */
    public void setLoadingImage(File file) {
        mLoadingImageFile = file;
    }

    private int mLoadingResId;

    /**
     * This image resource will be shown during download
     *
     * @param resId
     */
    public void setLoadingImageRes(int resId) {
        mLoadingResId = resId;
    }

    private int mFailedResId;
    public void setFailedImageRes(int resId) {
        mFailedResId = resId;
    }

    private int mBoundWidth;
    public void setBoundWidth(int value) {
        mBoundWidth = value;
    }

    private int mBoundHeight;
    public void setBoundHeight(int value) {
        mBoundHeight = value;
    }

    private ResizeMode mResizeMode = ResizeMode.Fill;
    public void setResizeMode(ResizeMode value) {
        mResizeMode = value;
    }

    public static final int DENSITY_DEFAULT = -1;
    public static final int DENSITY_MEDIUM  = 0;
    public static final int DENSITY_HIGH    = 1;
    public static final int DENSITY_XHIGH   = 2;

    private int mDensity = DENSITY_DEFAULT;
    public void setDensity(int density) { mDensity = density; }

    private ProgressBar mProgressView;
    public void setProgressView(ProgressBar progressBar) {
        mProgressView = progressBar;
    }

    private BitmapDrawableFactory mDrawableFactory;
    public void setDrawableFactory(BitmapDrawableFactory drawableFactory) {
        mDrawableFactory = drawableFactory;
    }
    public BitmapDrawableFactory getBitmapDrawableFactory() {
        if (mDrawableFactory == null) {
            mDrawableFactory = new SafeBitmapDrawable.SafeBitmapDrawableFactory();
        }
        return mDrawableFactory;
    }

    private File mDownloadFile;

    private OnImageDownloadedListener mOnImageDownloadedListener;
    public void setOnImageDownloadedListener(OnImageDownloadedListener listener) {
        mOnImageDownloadedListener = listener;
    }

    //*************************************************************************
    // Public Methods
    //*************************************************************************

    public AsyncImageAdapter(ImageView imageView) {
        mImageView = imageView;
    }

    public void setImage(String imageUrl) {
        setImage(null, imageUrl);
    }

    public void setImage(String taskTag, String imageUrl) {
        CacheStorage cacheStorage = CacheStorageManager.getInstance().getCache(imageUrl);
        setImage(taskTag, cacheStorage);
    }

    public void setImage(CacheStorage cache) {
        setImage(null, cache);
    }

    public void setImage(String taskTag, CacheStorage cache) {
        if (cache == null) {
            getBitmapDrawableFactory().setCacheId(null);
            getBitmapDrawableFactory().setFile(null);
            getBitmapDrawableFactory().setBytes(null);
            // storage is not set, reset ImageView
            if (mFailedResId != 0) {
                setDrawableRes(mFailedResId);
            } else setLoadingImage();

        } else {
            String cacheId = !Checker.isEmpty(cache.getKey()) ? cache.getKey()
                             : cache.getFile() != null        ? cache.getFile().getAbsolutePath()
                                                              : "INVALID_CONTENT_STORAGE";
            mLastModified = cache.getLastModify();
            setImage(cacheId, taskTag, cache.getFile(), cache.getKey(), cache.getCacheFile());
        }
    }

    public void setImage(String cacheId, String taskTag, File localImageFile, String imageUrl, File downloadFile) {
        // Set tag to this view so later could compare with Executor callback result.
        //  - It aims to prevent setting old image to view if this view is
        // reused by list/grid.
        //  - It is also used to reconnect executor to view if Activity/Fragment
        // switched.
        mImageView.setTag(cacheId);
        mDownloadFile = downloadFile;

        // try to load from local imageFile
        if (hasLocalCache(localImageFile)) {
            setBitmap(cacheId, localImageFile);

            // no need to check update
            if (Checker.isEmpty(mLastModified)) {
                mStatus = Status.Finished;
                if (DEBUG) LLog.v("Found image file %s", localImageFile);
                return;
            }
        }

        // image file not exist, start a async request to
        // download image. it will later be save to imageFile.
        if (!Checker.isEmpty(imageUrl) && downloadFile != null) {
            if (hasLocalCache(localImageFile)) {
                if (DEBUG) LLog.v("Query image update %s, initialize download process...", localImageFile);
            } else {
                if (DEBUG) LLog.v("Cannot find image file %s, initialize download process...", localImageFile);
                setLoadingImage();
            }

            // put executor to a pool to avoid duplicate image downloading
            ImageDownloadTask task = (ImageDownloadTask) TaskManager.findTask(cacheId);
            if (task != null) {
                if (DEBUG) LLog.v("Reuse ImageDownloadExecutor "+task.getId());

            // initialize a new request executor
            } else {
                task = new ImageDownloadTask(cacheId, imageUrl, mDownloadFile);
                task.setId(cacheId);

                // set modify timestamp only if local cache file is still there, in case
                // user delete cache folder manually.
                if (hasLocalCache(localImageFile)) {
                    task.setLastModified(mLastModified);
                }
            }
            task.addProgressUpdatedListener(this);
            task.addFinishedListener(this);
            task.addCancelledListener(this);
            task.addFailedListener(this);
            task.setTag(taskTag);
            TaskManager.push(task);
            mStatus = Status.Loading;
            if (mProgressView != null) mProgressView.setVisibility(View.VISIBLE);

        // request key is not given, display the failed image
        } else if (mFailedResId != 0) {
            setDrawableRes(mFailedResId);
            mStatus = Status.Failed;
            if (DEBUG) LLog.v("Image file %s not found and no download requested.", localImageFile);
        }
    }

    private boolean hasLocalCache(File cacheImageFile) {
        return !Checker.isEmpty(cacheImageFile)
                && BitmapUtil.isValidImageFile(cacheImageFile);
    }

    private void setLoadingImage() {
        if (!Checker.isEmpty(mLoadingImageFile)) {
            setBitmap(mLoadingImageFile.getName(), mLoadingImageFile);
        } else if (mLoadingResId > 0) {
            setDrawableRes(mLoadingResId);
        } else {
            mImageView.setImageDrawable(null);
        }
    }

    private void setBitmap(String cacheId, File imageFile) {
        BitmapDrawableFactory drawableFactory = getBitmapDrawableFactory();
        drawableFactory.setCacheId(cacheId);
        drawableFactory.setFile(imageFile);
        drawableFactory.setDensity(getDensity());
        drawableFactory.setWidth(mBoundWidth);
        drawableFactory.setHeight(mBoundHeight);
        drawableFactory.setResizeMode(mResizeMode);
        mImageView.setImageDrawable(getBitmapDrawableFactory().create());

        if (mImageView.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) mImageView.getDrawable()).start();
        }
    }

    private void setDrawableRes(int resId) {
        Drawable drawable = mImageView.getResources().getDrawable(resId);

        // Resize <shape> drawable to match bounds
        if ((drawable instanceof LayerDrawable)
                && mBoundWidth > 0 && mBoundHeight > 0) {
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            for (int i=0; i<layerDrawable.getNumberOfLayers(); i++) {
                if (layerDrawable.getDrawable(i) instanceof GradientDrawable) {
                    GradientDrawable shapeDrawable = (GradientDrawable) layerDrawable.getDrawable(i);
                    shapeDrawable.mutate();
                    shapeDrawable.setSize(mBoundWidth, mBoundHeight);
                }
            }
        }
        mImageView.setImageDrawable(drawable);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private int getDensity() {
        return mDensity == DENSITY_XHIGH  ? android.util.DisplayMetrics.DENSITY_XHIGH
             : mDensity == DENSITY_HIGH   ? android.util.DisplayMetrics.DENSITY_HIGH
             : mDensity == DENSITY_MEDIUM ? android.util.DisplayMetrics.DENSITY_MEDIUM
                                          : -1;
    }

    public void setDensityByDeviceScreen() {
        float density = mImageView.getResources().getDisplayMetrics().density;
        setDensity((int)(android.util.DisplayMetrics.DENSITY_MEDIUM * density));
    }

    //*************************************************************************
    //  Task Callback
    //*************************************************************************

    @Override
    public void onTaskProgressUpdated(int totalSize, int currentSize) {
        if (mProgressView == null) return;
        mProgressView.setMax(totalSize);
        mProgressView.setProgress(currentSize);
    }

    @Override
    public void onTaskFinished(ImageInfo result) {
        // Compare the view tag. if it is the same one that launch
        // the download request, set bitmap to view.
        // bitmap should have been validate by ImageDownloadRequest
        if (mImageView.getTag() == null
                || !mImageView.getTag().equals(result.cacheId)) return;

        // if server retrieve valid content, and request for modified checking (304),
        // flush cache with latest image.
        if (result.contentLength > 0 && !Checker.isEmpty(mLastModified)) {
            getBitmapDrawableFactory().setFlushCache(true);
        }

        // update ImageView with latest content. It's possible that multiple tasks
        // with modified checking (304) for same key. Later task might not update
        // content because server return zero content (304). So here need to update
        // ImageView no matter server return valid content or not (onTaskFinished)
        setBitmap(result.cacheId, result.downloadedFile);

        mStatus = Status.Finished;
        hideProgressView();
        mLastModified = null;

        if (mOnImageDownloadedListener != null) {
            mOnImageDownloadedListener.onImageDownloaded(result);
        }
    }

    @Override
    public void onTaskCancelled(ImageInfo result) {
        mStatus = Status.Failed;
        hideProgressView();
        mLastModified = null;
    }

    @Override
    public void onTaskFailed(ImageInfo result, TaskException ex) {
        LLog.e("Falied to download image file %s with error <%s:%s>.",
                mDownloadFile, ex.getErrorCode(), ex.getMessage());
        mStatus = Status.Failed;

        if (mFailedResId != 0) {
            setDrawableRes(mFailedResId);
        }

        hideProgressView();
        mLastModified = null;
    }

    private void hideProgressView() {
        try {
            if (mProgressView != null) mProgressView.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            LLog.w("hide Prgoress for AsyncImage failed.", e);
        }
    }

    public interface OnImageDownloadedListener {
        void onImageDownloaded(ImageInfo imageInfo);
    }

}

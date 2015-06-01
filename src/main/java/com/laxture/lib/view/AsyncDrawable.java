package com.laxture.lib.view;

import android.graphics.drawable.BitmapDrawable;

import com.laxture.lib.cache.storage.ContentStorage;
import com.laxture.lib.connectivity.http.ImageDownloadTask;
import com.laxture.lib.connectivity.http.ImageDownloadTask.ImageInfo;
import com.laxture.lib.task.TaskException;
import com.laxture.lib.task.TaskListener;
import com.laxture.lib.task.TaskManager;
import com.laxture.lib.util.BitmapUtil;
import com.laxture.lib.util.Checker;
import com.laxture.lib.util.LLog;

import java.io.File;

public class AsyncDrawable extends BitmapDrawable implements TaskListener<ImageInfo> {

    public interface OnQQImageDownloadedListener {
        public void onQQImageDownloaded(ImageInfo imageInfo);
    }

    public interface OnDrawableUpdateListener {
        // Call this method when drawable was changed.
        public void onDrawableUpdate(BitmapDrawable drawable);
    }

	public static final boolean DEBUG = false;
	
    private String mUrl;
    private File mDownloadFile;
    private BitmapDrawable mBitmapDrawable;
    private BitmapDrawableFactory mDrawableFactory;
    private OnQQImageDownloadedListener mOnQQImageDownloadedListener;
    private OnDrawableUpdateListener mOnDrawableUpdateListener;

    public void setOnDrawableUpdateListener(OnDrawableUpdateListener listener) {
        mOnDrawableUpdateListener = listener;
    }

    public void setOnQQImageDownloadedListener(OnQQImageDownloadedListener listener) {
        mOnQQImageDownloadedListener = listener;
    }

    public BitmapDrawable getDrawable(){
    	return mBitmapDrawable;
    }
    //*************************************************************************
    // Public Methods
    //*************************************************************************

	public AsyncDrawable(String url) {
        mUrl = url;
        LLog.v("Photo URL = %s", url);
    }

    public void loadDrawable() {
        ContentStorage cacheStorage = new ContentStorage(null, mUrl);
        mDownloadFile = cacheStorage.getCacheFile();

        // try to load from local imageFile
        if (hasLocalCache(mDownloadFile)) {
            if (DEBUG) LLog.v("Found image file %s", mDownloadFile);
            setDrawable(mUrl, cacheStorage.getFile());
            return;
        }

        // image file not exist, start a async request to
        // download image. it will later be save to imageFile.
        if (!Checker.isEmpty(mUrl) && mDownloadFile != null) {
            if (DEBUG) LLog.v("Cannot find image file %s, initialize download process...", mDownloadFile);

            // put executor to a pool to avoid duplicate image downloading
            ImageDownloadTask task = (ImageDownloadTask) TaskManager.findTask(mUrl);
            if (task != null) {
                if (DEBUG) LLog.v("Reuse ImageDownloadExecutor "+task.getId());

            // initialize a new request executor
            } else {
                task = new ImageDownloadTask(mUrl, mUrl, mDownloadFile);
                task.setId(mUrl);
                task.setTag("native");
            }
            task.addTaskListener(this);
            TaskManager.push(task);
        } 
    }

    private boolean hasLocalCache(File cacheImageFile) {
        return !Checker.isEmpty(cacheImageFile)
                && BitmapUtil.isValidImageFile(cacheImageFile);
    }

    //*************************************************************************
    //  Task Callback
    //*************************************************************************

    @Override
    public void onTaskStart() {} // do nothing

    @Override
    public void onTaskProgressUpdated(int totalSize, int currentSize) {} // do nothing

    @Override
    public void onTaskCancelled(ImageInfo result) {} // do nothing

    @Override
    public void onTaskFinished(final ImageInfo result) {
    	setDrawable(result.cacheId, result.downloadedFile);
    	if(null != mOnQQImageDownloadedListener){
    		mOnQQImageDownloadedListener.onQQImageDownloaded(result);
    	}
    }

    @Override
    public void onTaskFailed(ImageInfo result, final TaskException ex) {
        LLog.e("Falied to download image file %s with error <%s:%s>.",
                mDownloadFile, ex.getErrorCode(), ex.getMessage());
    }

    public BitmapDrawableFactory getBitmapDrawableFactory() {
        if (mDrawableFactory == null) {
            mDrawableFactory = new SafeBitmapDrawable.SafeBitmapDrawableFactory();
        }
        return mDrawableFactory;
    }
    
    private void setDrawable(String cacheId, File imageFile){
    	getBitmapDrawableFactory().setCacheId(cacheId);
        getBitmapDrawableFactory().setFile(imageFile);
        mBitmapDrawable = (BitmapDrawable) getBitmapDrawableFactory().create();
        if (null != mOnDrawableUpdateListener) {
            mOnDrawableUpdateListener.onDrawableUpdate(mBitmapDrawable);
        }
    }
}

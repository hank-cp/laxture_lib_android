package com.laxture.lib.util;

import java.io.File;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.laxture.lib.R;
import com.laxture.lib.RuntimeContext;
import com.laxture.lib.cache.men.BitmapCache;

public class CameraLauncher {

    private static final String PHOTO_FILE = "PhotoFile";
    private static final String OUTPUT_DIR = "OutputDir";
    private static final String OUTPUT_FILE = "OutputFile";
    private static final String WIDTH_LIMIT = "WidthLimit";
    private static final String HEIGHT_LIMIT = "HeightLimit";
    private static final String QUALITY = "Quality";
    private Activity mActivity;
    private Fragment mFragment;
    private int mResultCode;
    private CameraLauncherListener mCameraLauncherListener;

    public File mPhotoFile;

    public static final int DEFAULT_CAPTURED_QUALITY = 85;

    public File mOutputFile;
    public void setOutputFile(File file) {
        mOutputFile = file;
    }

    public File mOutputDir;
    public void setOutputDir(File file) {
        mOutputDir = file;
    }

    private int mWidthLimit;
    public void setWidthLimit(int widthLimit) {
        mWidthLimit = widthLimit;
    }
    public int getWidthLimit() {
        return mWidthLimit > 0 ? mWidthLimit : RuntimeContext.getConfig().getMaxCaptureImageSize();
    }

    private int mHeightLimit;
    public void setHeightLimit(int heightLimit) {
        mHeightLimit = heightLimit;
    }
    public int getHeightLimit() {
        return mHeightLimit > 0 ? mHeightLimit : RuntimeContext.getConfig().getMaxCaptureImageSize();
    }

    private int mQuality;
    public void setQuality(int quality) {
        mQuality = quality;
    }
    public int getQuality() {
        return mQuality > 0 ? mQuality : DEFAULT_CAPTURED_QUALITY;
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(PHOTO_FILE, mPhotoFile);
        outState.putSerializable(OUTPUT_DIR, mOutputDir);
        outState.putSerializable(OUTPUT_FILE, mOutputFile);
        outState.putInt(WIDTH_LIMIT, mWidthLimit);
        outState.putInt(HEIGHT_LIMIT, mHeightLimit);
        outState.putInt(QUALITY, mQuality);
    }

    public void onRestoreInstanceState(Bundle inState) {
        mPhotoFile = (File) inState.getSerializable(PHOTO_FILE);
        mOutputDir = (File) inState.getSerializable(OUTPUT_DIR);
        mOutputFile = (File) inState.getSerializable(OUTPUT_FILE);
        mWidthLimit = inState.getInt(WIDTH_LIMIT);
        mHeightLimit = inState.getInt(HEIGHT_LIMIT);
        mQuality = inState.getInt(QUALITY);
    }

    private Vector<File> mNewAddedImageFiles = new Vector<File>();
    private FileObserver mCameraRollObserver = new FileObserver(
            Environment.DIRECTORY_DCIM, FileObserver.CREATE) {

        @Override
        public void onEvent(int event, String path) {
            File newAddedFile = new File(path);
            if (!FileUtil.getFileExtName(newAddedFile).equalsIgnoreCase("jpg")) return;
            mNewAddedImageFiles.add(newAddedFile);
            LLog.d("Detected a image file captured by camera", path);
        }
    };

    public CameraLauncher(Activity activity, int resultCode,
            CameraLauncherListener listener) {
        mActivity = activity;
        mResultCode = resultCode;
        mCameraLauncherListener = listener;
    }

    public CameraLauncher(Fragment fragment, int resultCode,
            CameraLauncherListener listener) {
        mFragment = fragment;
        mActivity = fragment.getActivity();
        mResultCode = resultCode;
        mCameraLauncherListener = listener;
    }

    public void launchCamera() {
        launchCamera(null);
    }

    public void launchCamera(Location location) {
        mOutputFile = null;
        mPhotoFile = IntentUtil.getOutputImageFile();
        Intent cameraIntent = IntentUtil.getCameraIntent(mPhotoFile, 0);

        mCameraRollObserver.startWatching();
        if (mFragment != null) {
            IntentUtil.startActivityWrapper(mFragment, cameraIntent, mResultCode);
        } else {
            IntentUtil.startActivityWrapper(mActivity, cameraIntent, mResultCode);
        }
    }

    public void onCameraCancel() {
        if (Checker.isExistedFile(mPhotoFile)) mPhotoFile.delete();
    }

    public File onCameraReturn() {
        mCameraRollObserver.stopWatching();
        if (Checker.isEmpty(mPhotoFile)) {
            LLog.w("Cannot found captured image file, try to load from new added files (size=%s).",
                    mNewAddedImageFiles.size());
            if (mNewAddedImageFiles.size() == 1)
                mPhotoFile = mNewAddedImageFiles.get(0);
        }
        mNewAddedImageFiles.clear();

        if (Checker.isEmpty(mPhotoFile)) {
            LLog.e("Failed to found photo file captured by camera.");
            Toast.makeText(mActivity, R.string.msg_cannotFoundPhoto, Toast.LENGTH_SHORT).show();
            return null;
        }

        // create output file based on output dir
        if (mOutputFile == null && mOutputDir != null) {
            if (!mOutputDir.exists()) mOutputDir.mkdirs();
            if (mOutputDir.isDirectory()) {
                mOutputFile = new File(mOutputDir, mPhotoFile.getName());
            }
        }

        // copy a scaled image to home dir.
        if (mOutputFile != null && getWidthLimit() > 0 && getHeightLimit() > 0) {
            BitmapUtil.Size imageSize = BitmapUtil.getImageSizeFromFile(mPhotoFile);
            if (imageSize.width > getWidthLimit() || imageSize.height > getHeightLimit()) {

                BitmapCache.prepareMemoryBeforeLoadBitmap(getWidthLimit(), getHeightLimit());

                // resize image to limit size
                Bitmap documentedBitmap = BitmapUtil.loadBitmapFromFile(
                        mPhotoFile, getWidthLimit(), getHeightLimit(),
                        BitmapUtil.ResizeMode.Fit);
                if (documentedBitmap == null) {
                    LLog.e("Failed to read captured photo data.");
                    return null;
                }
                // Save captured photo data to documented photo file with scaled size.
                BitmapUtil.storeBitmapToFile(documentedBitmap, mOutputFile, getQuality());
                documentedBitmap.recycle();

            // no need resize, copy directly. be careful of loading full size image that cause OOM.
            } else  FileUtil.copyFile(mPhotoFile, mOutputFile);

            if (Checker.isEmpty(mOutputFile)) {
                LLog.e("Failed to save documented photo file %s.", mOutputFile);
                return null;
            }

            mCameraLauncherListener.onImageReady(mOutputFile);
            return mOutputFile;

        } else {
            mCameraLauncherListener.onImageReady(mPhotoFile);
            return mPhotoFile;
        }
    }

    public interface CameraLauncherListener {
        void onImageReady(File imageFile);
    }

}

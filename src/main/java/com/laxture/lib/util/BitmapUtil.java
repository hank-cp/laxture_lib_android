package com.laxture.lib.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

import com.laxture.lib.BuildConfig;
import com.laxture.lib.RuntimeContext;

public final class BitmapUtil {

    public static final String CACHE_PREFIX_RESORUCE = "res_";
    public static final String CACHE_PREFIX_ASSET = "asset_";

    private BitmapUtil() {}

    public static final boolean DEBUG = false;

    public static class Size {
        public int width;
        public int height;
        public Size(int w, int h) {
            width = w;
            height = h;
        }
    };

    public static boolean isValidImageFile(File file) {
        if (Checker.isEmpty(file)) return false;

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), opt);
        if (opt.outWidth <= 0 && opt.outHeight <= 0) return false;
        return true;
    }

    public static String getMimeType(File file) {
        if (Checker.isEmpty(file)) return null;

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), opt);
        return opt.outMimeType;
    }

    public static boolean isValidImageBytes(byte[] bytes) {
        if (bytes == null || bytes.length <= 0) return false;

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opt);
        if (opt.outWidth <= 0 && opt.outHeight <= 0) return false;
        return true;
    }

    public static Size getImageSizeFromFile(File file) {
        if (file == null || !file.exists()) return null;

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), opt);
        return new Size(opt.outWidth, opt.outHeight);
    }

    public static int estimateBitmapMemorySize(File file) {
        if (file == null || !file.exists()) return 0;

        Size size = getImageSizeFromFile(file);

        // double the result size for possible resizing
        // and add little more buffer
        return (int) (size.width * size.height * 4 * 2.2);
    }

    public static int estimateBitmapMemorySize(int width, int height) {
        // double the result size for possible resizing
        // and add little more buffer
        return (int) (width * height * 4 * 2.2);
    }

    public static Drawable resizeDrawable(Drawable drawable, int w, int h) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        // drawable转换成bitmap
        Bitmap oldbmp = drawableToBitmap(drawable);
        // 创建操作图片用的Matrix对象
        Matrix matrix = new Matrix();
        float scaleWidth = ((float)w / width); // 计算缩放比例
        float scaleHeight = ((float)h / height);
        matrix.postScale(scaleWidth, scaleHeight); // 设置缩放比例
        // 建立新的bitmap，其内容是对原bitmap的缩放后的图
        Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true);
        oldbmp.recycle();
        // 把bitmap转换成drawable并返回
        return new BitmapDrawable(RuntimeContext.getResources(), newbmp);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = null;
        if (width > 0 && height > 0) {
            bitmap = Bitmap.createBitmap(width, height,
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, width, height);
            drawable.draw(canvas);
        }
        return bitmap;
    }


    /**
     * rotate the bitmap according to EXIF info.
     */
    public static Bitmap rotateBitmapByEXIF(File file, Bitmap bitmap) {
        // rotate the bitmap according to EXIF info.
        return rotate(bitmap, getExifOrientation(file));
    }

    /**
     * rotate the bitmap by degree
     */
    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        // rotate the bitmap by degree
        if (bitmap == null) return null;
        if (degrees == 0) return bitmap;
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);

        return transform(bitmap, matrix);
    }

    /**
     * mirror the bitmap
     */
    public static Bitmap mirror(Bitmap bitmap, boolean horizental) {
        if (bitmap == null) return null;
        Matrix matrix = new Matrix();
        if(horizental) {
            matrix.postScale(-1f, 1f);
        } else {
            matrix.postScale(1f, -1f);
        }

        return transform(bitmap, matrix);
    }

    public static Bitmap transform(Bitmap bitmap, Matrix matrix) {
        if (bitmap == null) return null;

        Bitmap newBitmap = null;
        try {
            newBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e1) {
            LLog.e("Out of memory when reading image file. Try cause gc and load again", e1);
            if (BuildConfig.DEBUG) DeviceUtil.logHeap();
            System.gc();
            try {
                newBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            } catch (OutOfMemoryError e2) {
                LLog.e("Second tried failed, return null");
                if (BuildConfig.DEBUG) DeviceUtil.logHeap();
                return null;
            }
        }
        return newBitmap;
    }

    //Ref: http://stackoverflow.com/questions/8807799/android-find-the-orientation-of-photo-was-took-by-camera
    public static int getExifOrientation(File imageFile) {
        if (Checker.isEmpty(imageFile)) return 0;
        int orientationTag = 0;
        try {
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            orientationTag = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        } catch (IOException e) {
            LLog.w("Get image orientation tag failed.", e);
        }

        switch (orientationTag) {
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }

    //*************************************************************************
    // Load Bitmap From File/Resource/byte[]
    //*************************************************************************

    /**
     * Retrieve a Bitmap object from an image file.
     *
     * @param file image file.
     * @param scale scale radio of return image. For example scale=10 will
     *              return 1/10 size Bitmap object of original image.
     * @return
     */
    public static Bitmap loadBitmapFromFile(File file, int scale) {
        if (file == null || !file.exists()) return null;

        BitmapFactory.Options opt = null;
        if (scale != 1) {
            opt = new BitmapFactory.Options();
            opt.inSampleSize = scale;
        }

        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), opt);
        } catch (OutOfMemoryError e1) {
            LLog.e("Out of memory when reading image file. Try cause gc and load again", e1);
            if (BuildConfig.DEBUG) DeviceUtil.logHeap();
            System.gc();
            try {
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), opt);
            } catch (OutOfMemoryError e2) {
                if (BuildConfig.DEBUG) DeviceUtil.logHeap();
                LLog.e("Second tried failed, return null");
                return null;
            }
        }

        return rotateBitmapByEXIF(file, bitmap);
    }

    /**
     * Retrieve a Bitmap object from an image file. The ratio of image file
     * will be kept in the returned bitmap.
     *
     * @param file image file.
     * @param width expected width of the return bitmap
     * @param height expected height of the return bitmap
     * @param resizeMode
     * @return
     */
    public static Bitmap loadBitmapFromFile(File file, int width, int height,
            ResizeMode resizeMode) {
        if (Checker.isEmpty(file)) return null;

        int scale = getBitmapScaleFromFile(file, width, height);

        Bitmap roughBitmap = loadBitmapFromFile(file, scale);
        Bitmap accurateBitmap = resizeBitmap(roughBitmap,
                width, height, resizeMode);
        if (roughBitmap!= null && roughBitmap != accurateBitmap)
            roughBitmap.recycle();
        return accurateBitmap;
    }

    public static int getBitmapScaleFromFile(File file,int width,int height){
        BitmapFactory.Options opt1 = new BitmapFactory.Options();
        opt1.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), opt1);
        int originalWidth = opt1.outWidth;
        int originalHeight = opt1.outHeight;

        return Math.max((int)Math.round(originalHeight/(double)height),
                (int)Math.round(originalWidth/(double)width));
    }

    public static Bitmap loadBitmapFromBytes(byte[] bytes, int scale) {
        if (bytes == null || bytes.length <= 0) return null;

        BitmapFactory.Options opt = null;
        if (scale != 1) {
            opt = new BitmapFactory.Options();
            opt.inSampleSize = scale;
        }

        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opt);
        } catch (OutOfMemoryError e1) {
            LLog.e("Out of memory when reading image file. Try cause gc and load again", e1);
            if (BuildConfig.DEBUG) DeviceUtil.logHeap();
            System.gc();
            try {
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opt);
            } catch (OutOfMemoryError e2) {
                if (BuildConfig.DEBUG) DeviceUtil.logHeap();
                LLog.e("Second tried failed, return null");
                return null;
            }
        }
        return bitmap;
    }

    public static Bitmap loadBitmapFromBytes(byte[] bytes, int width,
            int height, ResizeMode resizeMode) {
        if (bytes == null || bytes.length <= 0) return null;

        BitmapFactory.Options opt1 = new BitmapFactory.Options();
        opt1.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opt1);
        int originalWidth = opt1.outWidth;
        int originalHeight = opt1.outHeight;

        int scale = 1;
        if (originalWidth/originalHeight > width/height) {
            scale = (int)Math.round(originalHeight/(double)height);
        } else {
            scale = (int)Math.round(originalWidth/(double)width);
        }

        Bitmap roughBitmap = loadBitmapFromBytes(bytes, scale);
        Bitmap accurateBitmap = resizeBitmap(roughBitmap,
                width, height, resizeMode);
        if (roughBitmap!= null && roughBitmap != accurateBitmap)
            roughBitmap.recycle();
        return accurateBitmap;
    }

    public static Bitmap loadBitmapFromResources(Resources res, int resId) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeResource(res, resId);
        } catch (OutOfMemoryError e1) {
            LLog.e("Out of memory when reading image bytes. Try cause gc and load again", e1);
            if (BuildConfig.DEBUG) DeviceUtil.logHeap();
            System.gc();
            try {
                bitmap = BitmapFactory.decodeResource(res, resId);
            } catch (OutOfMemoryError e2) {
                LLog.e("Second tried failed, return null");
                if (BuildConfig.DEBUG) DeviceUtil.logHeap();
                return null;
            }
        }
        return bitmap;
    }

    public static Bitmap loadBitmapFromAsset(String assetPath) {
        Bitmap bitmap = null;
        InputStream is = AssetLoader.openAsset(assetPath);
        if (is == null) return null;
        try {
            bitmap = BitmapFactory.decodeStream(is);
            StreamUtil.closeStream(is);
            return bitmap;

        } catch (OutOfMemoryError e1) {
            LLog.e("Out of memory when reading image bytes. Try cause gc and load again", e1);
            if (BuildConfig.DEBUG) DeviceUtil.logHeap();
            System.gc();
            try {
                bitmap = BitmapFactory.decodeStream(is);
                StreamUtil.closeStream(is);
                return bitmap;

            } catch (OutOfMemoryError e2) {
                LLog.e("Second tried failed, return null");
                if (BuildConfig.DEBUG) DeviceUtil.logHeap();
                return null;
            }
        }
    }


    //*************************************************************************
    // Load Bitmap From Cache
    //*************************************************************************


    public static File getRealPathFromUri(ContentResolver cr, Uri contentUri) {
        if (contentUri == null) return null;

        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = cr.query(contentUri, projection, null, null, null);
        if (cursor == null) return null;
        File result = null;
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            result = new File(cursor.getString(columnIndex));
        }
        cursor.close();
        return result;
    }

    public static File queryRealPathFromFileName(ContentResolver cr, String fileName) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                String.format("%s = '%s'", MediaStore.Images.Media.DISPLAY_NAME, fileName),
                null, null);
        if (cursor == null) return null;
        File result = null;
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            result = new File(cursor.getString(columnIndex));
        }
        cursor.close();
        return result;
    }

    /**
     * Store a bitmap object to file. It's always be good to recycle the
     * bitmap after using it to save memory.
     *
     * @param bitmap
     * @param file
     * @return
     */
    public static boolean storeBitmapToFile(Bitmap bitmap, File file, int quality) {
        if (bitmap == null || file == null) return false;

        FileOutputStream fos = null;
        try {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            if (!file.exists()) file.createNewFile();
            fos = new FileOutputStream(file);
            bitmap.compress(CompressFormat.JPEG, quality, fos);
            fos.flush();
        } catch (FileNotFoundException e) {
            throw new UnHandledException(e);
        } catch (IOException e) {
            throw new UnHandledException(e);
        } finally {
            StreamUtil.closeStream(fos);
        }
        return true;
    }

    /**
     * Store a bitmap object to byte array. It's always be good to recycle the
     * bitmap after using it to save memory.
     *
     * @param bitmap
     * @return
     */
    public static byte[] storeBitmapToByteArray(Bitmap bitmap, int quality) {
        if (bitmap == null) return null;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, quality, bos);
        return bos.toByteArray();
    }

    public enum ResizeMode {
        /** Keep original ratio and scale to fit the given bounds. */
        Fit,

        /** Fill the given bounds, part of image might be cropped. */
        Fill,

        /** if image is landscape, it will be sacle to fit given bounds.
         *  if image is portrait, it will be crop to match given bounds. */
        CropVertical
    };

    /**
     * Resizes the given bitmap while preserving the aspect ratio.
     * Two resize options are given.
     *
     * @param bitmap Source image
     * @param width Maximum width of resized image
     * @param height Maximum height of resized image
     * @param resizeMode
     * @return the resized bitmap.
     */
    public static Bitmap resizeBitmap(Bitmap bitmap, int width, int height, ResizeMode resizeMode) {
        if (bitmap == null) return null;

        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        // no need to resize
        if (bitmapWidth <= width && bitmapHeight <= height) return bitmap;

        // resizes the image so it *fits* within the given bounds (while maintaining aspect ratio)
        // unless the aspect ratio of the source an destination match, this will result in an image smaller than the specified bounds
        if (resizeMode == ResizeMode.Fit
                || (resizeMode == ResizeMode.CropVertical && bitmapWidth >= bitmapHeight)) {
            // ported from net.jtank.image.ImageUtil
            // Calculate the height and width - this is done automatically if not constrained
            int im_width = bitmapWidth;
            int im_height = bitmapHeight;

            int tmp_w;
            int tmp_h;

            tmp_h = height;
            tmp_w = (int) (( (double) height / (double) im_height) * im_width);

            if (tmp_w > width) {
                tmp_w = width;
                tmp_h = (int) (( (double) width / (double) im_width) * im_height);
            }

            int canvasWidth = Math.max(tmp_w, 1);
            int canvasHeight = Math.max(tmp_h, 1);

            try {
                return Bitmap.createScaledBitmap(bitmap, canvasWidth, canvasHeight, true);
            } catch (OutOfMemoryError e1) {
                LLog.e("Out of memory when resizing image file. InputSize=%s/%s, OutputSize=%s/%s. Try cause gc and load again",
                        e1, bitmapWidth, bitmapHeight, width, height);
                if (BuildConfig.DEBUG) DeviceUtil.logHeap();
                System.gc();
                try {
                    // run the same code again.
                    return Bitmap.createScaledBitmap(bitmap, canvasWidth, canvasHeight, true);
                } catch (OutOfMemoryError e2) {
                    LLog.e("Second tried failed, return null");
                    if (BuildConfig.DEBUG) DeviceUtil.logHeap();
                    return null;
                }
            }

        // resizes the image so it fills the given bounds
        // unless the aspect ratio of the source and destination matches, this will result in some cropping (the image will be centered)
        } else {
            int scaledWidth, scaledHeight, excessM2X=0, excessM2Y=0;
            if (bitmapWidth/(double)bitmapHeight > width/(double)height) {
                scaledHeight = height;
                scaledWidth = (int) Math.round(height*bitmapWidth/(double)bitmapHeight);
                int excess = scaledWidth - width;
                excessM2X = (excess/2) * 2;  // get as multiple of 2  17  16  -8
                if (excessM2X < excess) {
                    excessM2X += 2;
                    scaledWidth +=1;
                }
                excessM2X = Math.max(excessM2X, 0);

            } else {
                scaledWidth = width;
                scaledHeight = (int) Math.round(width*bitmapHeight/(double)bitmapWidth);
                int excess = scaledHeight - height;
                excessM2Y = (excess/2) * 2;  // get as multiple of 2
                if (excessM2Y < excess) {
                    excessM2Y += 2;
                    scaledHeight +=1;
                }
            }
            excessM2Y = Math.max(excessM2Y, 0);

            Bitmap resized=null, result=null;
            try {
                // resizes maintaining aspect ratio
                resized = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);

                // crops to size
                result = Bitmap.createBitmap(resized, excessM2X/2, excessM2Y/2, width, height);

            } catch (OutOfMemoryError e1) {
                LLog.e("Out of memory when resizing image file. InputSize=%s/%s, OutputSize=%s/%s. Try cause gc and load again",
                        e1, bitmapWidth, bitmapHeight, width, height);
                if (BuildConfig.DEBUG) DeviceUtil.logHeap();
                System.gc();
                try {
                    // run the same code again.
                    if (resized == null)
                        resized = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
                    result = Bitmap.createBitmap(resized, excessM2X/2, excessM2Y/2, width, height);
                } catch (OutOfMemoryError e2) {
                    LLog.e("Second tried failed, return null");
                    if (BuildConfig.DEBUG) DeviceUtil.logHeap();
                    return null;
                }
            }
            if (resized != null && resized != result) resized.recycle();
            return result;
        }
    }

    public static int getBitmapSize(Bitmap bitmap) {
        if (bitmap == null) return 0;
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    public static Bitmap decodeYUV(byte[] yuvFrame, int width, int height) {
        int sz = width * height;
        int[] colors = new int[yuvFrame.length];
        int i, j;
        int Y, Cr = 0, Cb = 0;
        for (j = 0; j < height; j++) {
            int pixPtr = j * width;
            final int jDiv2 = j >> 1;
            for (i = 0; i < width; i++) {
                Y = yuvFrame[pixPtr];
                if (Y < 0)
                    Y += 255;
                if ((i & 0x1) != 1) {
                    final int cOff = sz + jDiv2 * width + (i >> 1) * 2;
                    Cb = yuvFrame[cOff];
                    if (Cb < 0)
                        Cb += 127;
                    else
                        Cb -= 128;
                    Cr = yuvFrame[cOff + 1];
                    if (Cr < 0)
                        Cr += 127;
                    else
                        Cr -= 128;
                }
                int R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
                if (R < 0)
                    R = 0;
                else if (R > 255)
                    R = 255;
                int G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1)
                        + (Cr >> 3) + (Cr >> 4) + (Cr >> 5);
                if (G < 0)
                    G = 0;
                else if (G > 255)
                    G = 255;
                int B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);
                if (B < 0)
                    B = 0;
                else if (B > 255)
                    B = 255;
                colors[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
            }
        }

        return Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888);
    }

}

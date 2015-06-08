package com.laxture.lib.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract.Contacts;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.laxture.lib.RuntimeContext;
import com.laxture.lib.R;

import java.io.File;
import java.util.Date;

public class IntentUtil {

    public static final String MIME_IMAGE = "image/*";
    public static final String MIME_IMAGE_JPEG = "image/jpeg";
    public static final String MIME_VIDEO = "video/*";
    public static final String MIME_AUDIO = "audio/*";
    public static final String MIME_GEO = "geo:";
    public static final String MIME_TEXT = "text/plain";
    public static final String MIME_PDF = "application/pdf";
    public static final String MIME_ALL = "*/*";

    public static final String DATA_TYPE_APK_INSTALLER = "application/vnd.android.package-archive";
    public static final String PACKAGE_NAME_MARKET = "com.android.vending";

    /**
     * If the Intent rely on external app, this method should be call rather
     * than calling Activity.startActivity().
     *
     * @param activity
     * @param intent
     * @return
     */
    public static boolean startActivityWrapper(Activity activity, Intent intent) {
        return startActivityWrapper(activity, intent, -1);
    }

    /**
     * If the Intent rely on external app, this method should be call rather
     * than calling Activity.startActivity().
     *
     * @param intent
     * @return
     */
    public static boolean startActivityWrapper(Fragment fragment, Intent intent) {
        return startActivityWrapper(fragment, intent, -1);
    }

    /**
     * If the Intent rely on external app, this method should be call rather
     * than calling Activity.startActivity().
     *
     * @param activity
     * @param intent
     * @return
     */
    public static boolean startActivityWrapper(Activity activity, Intent intent, int requestCode) {
        try {
            if (requestCode <= 0) activity.startActivity(intent);
            else activity.startActivityForResult(intent, requestCode);
            return true;
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(activity,
                    activity.getString(R.string.msg_notSupportOperation),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * If the Intent rely on external app, this method should be call rather
     * than calling Activity.startActivity().
     *
     * @param fregment
     * @param intent
     * @param requestCode
     * @return
     */
    public static boolean startActivityWrapper(Fragment fregment, Intent intent, int requestCode) {
        try {
            if (requestCode <= 0) fregment.startActivity(intent);
            else fregment.startActivityForResult(intent, requestCode);
            return true;
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(fregment.getActivity(),
                    fregment.getString(R.string.msg_notSupportOperation),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static Intent getCameraIntent(File imageFile) {
        return getCameraIntent(imageFile, -1);
    }

    public static Intent getCameraIntent(File imageFile, int sizeLimit) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        if (sizeLimit != -1) intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, sizeLimit);
        LLog.d("Launch camera for photo: " + imageFile.getAbsolutePath());
        return intent;
    }

    public static Intent getCameraIntent(Uri imageUri) {
        return getCameraIntent(imageUri, -1);
    }

    public static Intent getCameraIntent(Uri imageUri, int sizeLimit) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        if (sizeLimit != -1) intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, sizeLimit);
        LLog.d("Launch camera for photo: " + imageUri.toString());
        return intent;
    }

    private final static String IMAGE_FILE_NAME_FORMAT = "IMG_%s.jpg";
    private final static String IMAGE_FILE_NAME_DATE_FORMAT = "yyyyMMdd_HHmmss";

    public static File getOutputImageFile(){
        File cameraRollDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        if (!cameraRollDir.exists()) cameraRollDir.mkdirs();
        return new File(cameraRollDir, String.format(IMAGE_FILE_NAME_FORMAT,
                DateUtil.formatDate(new Date(), IMAGE_FILE_NAME_DATE_FORMAT)));
    }

    public static Uri createImageUri(ContentResolver cr, Location location) {
        long nowLong = new Date().getTime();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_ADDED, nowLong);
        values.put(MediaStore.Images.Media.DATE_TAKEN, nowLong);
        values.put(MediaStore.Images.Media.DATE_MODIFIED, nowLong);
        values.put(MediaStore.Images.Media.MIME_TYPE, MIME_IMAGE_JPEG);
        values.put(MediaStore.Images.Media.DATA, getOutputImageFile().getAbsolutePath());
        if (location != null ) {
            values.put(MediaStore.Images.Media.LATITUDE, location.getLatitude());
            values.put(MediaStore.Images.Media.LONGITUDE, location.getLongitude());
        }
        return cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public static void updateImageUri(ContentResolver cr, Uri imageUri, File imageFile) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.SIZE, imageFile.length());
        cr.update(imageUri, values, null, null);
    }

    public static Intent getGalleryIntent(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, MIME_IMAGE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        return intent;
    }

    public static Intent getChooseImageIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        intent.setType(MIME_IMAGE);
        return intent;
    }

    public static Intent getShareIntent(String title, String text,
            String shareChooserTitle) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(MIME_TEXT);
        intent.putExtra(Intent.EXTRA_TITLE, title);
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        return Intent.createChooser(intent, shareChooserTitle);
    }

    public static Intent getGPSSettingIntent() {
        return new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    }

    public static Intent getDefaultViewIntent(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setDataAndType(Uri.fromFile(file),
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        FileUtil.getFileExtName(file)));
        return intent;
    }

    public static Intent getBrowserIntent(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        return intent;
    }

    public static Intent getApkInstallerIntent(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apkFile), DATA_TYPE_APK_INSTALLER);
        return intent;
    }

    public static Intent getContactsIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(Contacts.CONTENT_URI, Contacts.CONTENT_TYPE);
        return intent;
    }

    public static Intent getSendSMSIntent(String number, String content) {
        Uri uri = Uri.fromParts("sms", number, null);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra("sms_body", content);
        return intent;
    }

    /**
     * Main Activity is the one you specify in AndroidManifext.xml.
     *
     * @param context
     * @return
     */
    public static Intent getMainIntent(Context context, Class<?> mainActivityClazz) {
        Intent intent = new Intent(context, mainActivityClazz);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        return intent;
    }

    public static boolean isInstalledByMarket(Context context) {
        try {
            int allowUnknowSource = Settings.Secure.getInt(
                    context.getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS);
            if (allowUnknowSource == 1) return false;

            return RuntimeContext.getPackageName().equals(
                    context.getPackageManager().getInstallerPackageName(PACKAGE_NAME_MARKET));
        } catch (Exception e) {
            return false;
        }
    }

}

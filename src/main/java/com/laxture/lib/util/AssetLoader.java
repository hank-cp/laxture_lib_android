package com.laxture.lib.util;

import com.laxture.lib.RuntimeContext;

import java.io.IOException;
import java.io.InputStream;

public class AssetLoader {

    public static InputStream openAsset(String assetPath) {
        if (Checker.isEmpty(assetPath)) return null;
        InputStream is = null;
        try {
            is = RuntimeContext.getApplication().getAssets().open(assetPath);
            return is;
        } catch (IOException e) {
            LLog.e("Failed to read file %s", assetPath, e);
            return null;
        }
    }

    /**
     * Read asset file to byte array
     * @param assetPath "config/xx.txt"
     * @return
     */
    public static byte[] readAssetToBytes(String assetPath) {
        InputStream is = openAsset(assetPath);
        if (is == null) return null;
        try {
            long length = is.available();
            if (length > Integer.MAX_VALUE) throw new UnHandledException("File is too large");

            // Create the byte array to hold the data
            byte[] bytes = new byte[(int)length];

            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length
                   && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0)
                offset += numRead;

            // Ensure all the bytes have been read in
            if (offset < bytes.length)
                LLog.w("Could not completely read file %s", assetPath);
            return bytes;
        } catch (IOException e) {
            LLog.e("Failed to read file %s", assetPath, e);
            return null;
        } finally {
            StreamUtil.closeStream(is);
        }
    }

    /**
     * Read asset file to string
     * @param assetPath "config/xx.txt"
     * @return
     */
    public static String readAssetToString(String assetPath) {
        if (Checker.isEmpty(assetPath)) return null;
        return new String(readAssetToBytes(assetPath));
    }

    public static String[] listFiles(String assetPath) {
        try {
            return RuntimeContext.getApplication().getAssets().list(assetPath);
        } catch (IOException e) {
            LLog.e("Failed to read file %s", assetPath, e);
            return null;
        }
    }
}

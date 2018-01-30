package com.laxture.lib.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CompressionHelper {

    public static final String ZIP_EXTENSION = ".zip";

    /**
     * Extract a zip file to expected location.
     *
     * @param zipFile
     * @param targetLocation
     */
    public static String[] extractZipFile(File zipFile, String targetLocation) throws IOException {
        if (Checker.isEmpty(zipFile) || zipFile.isDirectory()) {
            LLog.w("invalid zip file");
            return null;
        }

        File targetFolder = new File(targetLocation);
        if (!targetFolder.isDirectory()) targetFolder.mkdirs();

        byte[] buffer = new byte[1024];
        int nrBytesRead;
        ArrayList<String> files = new ArrayList<String>();
        ZipInputStream zis = null;
        OutputStream os = null;

        try {
            zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File extractedFile = new File(targetFolder + File.separator + zipEntry.getName());
                if (!extractedFile.getParentFile().exists()) extractedFile.getParentFile().mkdirs();
                files.add(extractedFile.toString());
                os = new FileOutputStream(extractedFile);
                while ((nrBytesRead = zis.read(buffer)) > 0) {
                    os.write(buffer, 0, nrBytesRead);
                }

                //Finish off by closing the streams
                os.flush();
                StreamUtil.closeStream(os);
                zis.closeEntry();
                zipEntry = zis.getNextEntry();
            }
            return files.toArray(new String[] {});

        } catch (IOException ex) {
            LLog.e("Extract zip file %s failed.", ex, zipFile);
            throw ex;

        } finally {
            StreamUtil.closeStream(os);
            StreamUtil.closeStream(zis);
        }
    }

}

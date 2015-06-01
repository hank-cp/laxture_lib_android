package com.laxture.lib.util;

import android.content.res.AssetFileDescriptor;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtil {

    public static final int IO_BUFFER_SIZE = 4 * 1024;

    private StreamUtil() {}

    public static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                LLog.e("Could not close stream", e);
            }
        }
    }

    public static void closeStream(AssetFileDescriptor fd) {
        if (fd != null) {
            try {
                fd.close();
            } catch (IOException e) {
                LLog.e("Could not close stream", e);
            }
        }
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }

}

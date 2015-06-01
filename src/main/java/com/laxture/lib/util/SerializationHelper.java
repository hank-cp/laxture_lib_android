package com.laxture.lib.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializationHelper {

    public static <T> byte[] serialize(T object) {
        if (object == null) throw new UnHandledException("Cannot serialize a null object.");

        byte[] byteArray = null;
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;

        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            byteArray = bos.toByteArray();

            oos.flush();
            oos.close();
        } catch (IOException e) {
            throw new UnHandledException(e);
        } finally {
            StreamUtil.closeStream(bos);
            StreamUtil.closeStream(oos);
        }

        return byteArray;
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(byte[] byteArray) {
        T object = null;

        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;

        try {
            bis = new ByteArrayInputStream(byteArray);
            ois = new ObjectInputStream(bis);
            object = (T) ois.readObject();
        } catch (IOException e) {
            throw new UnHandledException(e);
        } catch (ClassNotFoundException e) {
            throw new UnHandledException(e);
        } catch (Exception e) {
            // Mostly because Class def is changed, return null
            LLog.w("Deserialize Object failed", e);
        } finally {
            StreamUtil.closeStream(bis);
            StreamUtil.closeStream(ois);
        }

        return object;
    }


}

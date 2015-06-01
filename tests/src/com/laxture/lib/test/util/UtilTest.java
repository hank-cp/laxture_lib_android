package com.laxture.lib.test.util;

import com.laxture.lib.util.LLog;
import com.laxture.lib.util.SecurityUtil;
import com.laxture.lib.util.SerializationHelper;

import junit.framework.TestCase;

import java.util.ArrayList;

public class UtilTest extends TestCase {

    public UtilTest() {}

    public void testEncyptAndDecrypt() {
        ArrayList<String> testData = new ArrayList<String>();
        testData.add("a");
        testData.add("b");
        testData.add("c");

        // test serialization
        byte[] serializedData = SerializationHelper.serialize(testData);
        assertNotNull(serializedData);
        LLog.d("SerializedData = %s", serializedData);

        // test encryption
        byte[] encryptedData = SecurityUtil.encrypt(serializedData);
        assertNotNull(encryptedData);
        LLog.d("EncryptedData = %s", encryptedData);

        // test decryption
        byte[] decryptedData = SecurityUtil.decrypt(encryptedData);
        assertNotNull(decryptedData);
        LLog.d("DecryptedData = %s", decryptedData);

        // test deserialization
        testData = SerializationHelper.deserialize(decryptedData);
        assertNotNull(testData);
        assertEquals(testData.size(), 3);
        assertEquals(testData.get(0), "a");
        assertEquals(testData.get(1), "b");
        assertEquals(testData.get(2), "c");
    }
}

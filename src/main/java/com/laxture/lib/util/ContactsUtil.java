package com.laxture.lib.util;

import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;

public class ContactsUtil {

    private static final String[] PROJECTION_CONTACT = {
        Contacts._ID,
        Contacts.DISPLAY_NAME,
    };

    private static final String[] PROJECTION_PHONE = {
        Phone.TYPE,
        Phone.NUMBER
    };

    private ContactsUtil() {}

    public static HashMap<String, String> resolvePhoneNumber(Context context, Uri uri) {
        HashMap<String, String> phoneList = new HashMap<String, String>();
        Cursor cursor = context.getContentResolver().query(uri, PROJECTION_CONTACT, null, null, null);
        if (cursor.moveToFirst()) {
            String contactId = cursor.getString(cursor.getColumnIndexOrThrow(Contacts._ID));
            String contactName = cursor.getString(cursor.getColumnIndexOrThrow(Contacts.DISPLAY_NAME));
            LLog.d("Selected Contact id/name :: %s/%s", contactId, contactName);

            Cursor phoneCursor = context.getContentResolver().query(Phone.CONTENT_URI, PROJECTION_PHONE,
                    Phone.CONTACT_ID+"="+contactId, null, null);
            while (phoneCursor.moveToNext()) {
                int type = phoneCursor.getInt(phoneCursor.getColumnIndexOrThrow(Phone.TYPE));
                String typeLabel = Phone.getTypeLabel(context.getResources(), type, "Unknow").toString();
                phoneList.put(typeLabel, phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(Phone.NUMBER)));
            }
            phoneCursor.close();
        }
        cursor.close();

        return phoneList;
    }
}

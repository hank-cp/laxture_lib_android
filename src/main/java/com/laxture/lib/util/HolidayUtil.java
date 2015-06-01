package com.laxture.lib.util;

import android.annotation.SuppressLint;

import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;

public class HolidayUtil {

    private static HolidayUtil instance = null;

    public static HolidayUtil getInstance(){
        if (instance == null){
            instance = new HolidayUtil();

            initFromFile();
        }
        return instance;
    }


    private static HashMap<Integer, Boolean> mListData = null;


    private static HashMap<Integer, Boolean> mDarkWeekends = null;

    @SuppressLint("UseSparseArrays")
    private static void initFromFile(){
        Calendar mCalendar = Calendar.getInstance();
        int nowYear = mCalendar.get(Calendar.YEAR);

        mListData = new HashMap<Integer, Boolean>();
        mDarkWeekends = new HashMap<Integer, Boolean>();

        byte[] bytes = AssetLoader.readAssetToBytes("config/holidays.txt");
        if (Checker.isEmpty(bytes)) return;
        try {
            JSONObject json = new JSONObject(EncodingUtils.getString(bytes, "UTF-8"));

            for (int i = 0; i< 2; i++) {
                nowYear+=i;

                JSONObject year = json.getJSONObject(String.valueOf(nowYear));
                JSONArray holiday = year.getJSONArray("holidays");

                int len = holiday.length();
                for (int j = 0; j < len; j++) {
                    String[] s = holiday.getString(j).split("-");

                    if (s.length == 0) continue;

                    int d1 = Integer.valueOf(s[0]);
                    int d2 = Integer.valueOf(s.length==1 ? s[0] : s[1]);
                    for (int k=d1; k<=d2; k++){
                        if (k % 100 > 31){k = (k/100+1)*100+1;}
                        mListData.put(nowYear * 10000 + k, true);
                    }
                }

                JSONArray darkweekends = year.getJSONArray("darkWeekends");
                len = darkweekends.length();
                for (int j = 0; j < len; j++) {
                    String[] s = darkweekends.getString(j).split("-");

                    if (s.length == 0) continue;

                    int d1 = Integer.valueOf(s[0]);
                    int d2 = Integer.valueOf(s.length==1 ? s[0] : s[1]);
                    for (int k=d1; k<=d2; k++){
                        if (k % 100 > 31){k = (k/100+1)*100+1;}
                        mDarkWeekends.put(nowYear * 10000 + k, true);
                    }
                }
            }
        } catch (JSONException e) {
            LLog.e("Parse holidays json failed.", e);
        }
    }

    public boolean checkIsHoliday(int year, int month, int day){
        int d = year * 10000 + month * 100 + day;
        Boolean b = mListData.get(d);
        return (b==null)?false : b;
    }

    public boolean checkIsDarkWeekends(int year, int month, int day){
        int d = year * 10000 + month * 100 + day;
        Boolean b = mDarkWeekends.get(d);
        return b != null;
    }
}

package com.laxture.lib.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;

import com.laxture.lib.Configuration;
import com.laxture.lib.R;

public class FestivalUtil {

    private ArrayList<Festival> mArraySolarFestival = null;

    private ArrayList<Festival> mArrayLunarFestival = null;

    private static FestivalUtil mFestivalDictionary = null;

    private FestivalUtil() {
    }

    public static FestivalUtil getInstance() {
        if (null == mFestivalDictionary) {
            mFestivalDictionary = new FestivalUtil();
            try {
                mFestivalDictionary.parse(Configuration.getInstance().getAppContext(), R.xml.festival);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                mFestivalDictionary = null;
                return mFestivalDictionary;
            } catch (IOException e) {
                e.printStackTrace();
                mFestivalDictionary = null;
                return mFestivalDictionary;
            }
        }

        return mFestivalDictionary;
    }

    private void parse(Context context, int resId) throws XmlPullParserException, IOException {

        XmlPullParser parser = context.getResources().getXml(resId);

        boolean isLoadingSolarFestivals = false;
        boolean isloadingLunarFestivals = false;
        Festival festival = null;

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT: {
                    break;
                }
                case XmlPullParser.START_TAG: {
                    if (parser.getName().equals("types")) {
                        String types = parser.nextText();
                        if (types.equals("solarFestivals")) {
                            mArraySolarFestival = new ArrayList<Festival>();
                            isLoadingSolarFestivals = true;
                            isloadingLunarFestivals = false;
                        } else if (types.equals("lunarFestivals")) {
                            mArrayLunarFestival = new ArrayList<Festival>();
                            isLoadingSolarFestivals = false;
                            isloadingLunarFestivals = true;
                        }
                    }

                    if (parser.getName().equals("dict")) {
                        festival = new Festival();
                    } else if (parser.getName().equals("name")) {
                        festival.setName(parser.nextText());
                    } else if (parser.getName().equals("type")) {
                        festival.setType(parser.nextText());
                    } else if (parser.getName().equals("priority")) {
                        festival.setPriority(Integer.parseInt(parser.nextText()));
                    } else if (parser.getName().equals("month")) {
                        festival.setMonth(Integer.parseInt(parser.nextText()));
                    } else if (parser.getName().equals("day")) {
                        festival.setDay(Integer.parseInt(parser.nextText()));
                    } else if (parser.getName().equals("weekOrder")) {
                        festival.setWeekOrder(Integer.parseInt(parser.nextText()));
                    } else if (parser.getName().equals("weekday")) {
                        festival.setWeekday(Integer.parseInt(parser.nextText()));
                    }

                    break;
                }
                case XmlPullParser.END_TAG: {
                    if (parser.getName().equals("dict")) {
                        if (isLoadingSolarFestivals) {
                            mArraySolarFestival.add(festival);
                        } else if (isloadingLunarFestivals) {
                            mArrayLunarFestival.add(festival);
                        }
                    }
                    break;
                }
            }
            eventType = parser.next();
        }

        isLoadingSolarFestivals = false;
        isloadingLunarFestivals = false;

    }

    private ArrayList<Festival> _getFestival(Date date) {

        // 查找节气信息,如果有则封装成一个Festival对象
        /*
         * if (containsTerms) { NSString *solarTerm = [self
         * solarTermWithSolarDateYear:solarDateCompnents.year
         * month:solarDateCompnents.month andDay:solarDateCompnents.day]; if
         * (solarTerm != nil) { [allFestivals addObject:[[[Festival alloc]
         * initWithName:solarTerm type:FestivalInSolarDate
         * withMonth:solarDateCompnents.month day:solarDateCompnents.day]
         * autorelease]]; } }
         */

        ArrayList<Festival> allFestival = new ArrayList<Festival>();

        SLLunarDate lunarDate = new SLLunarDate(date);

        // 查找匹配的农历节日
        for (int i = 0; i < mArrayLunarFestival.size(); i++) {
            Festival festival = mArrayLunarFestival.get(i);
            if (festival.getMonth() != lunarDate.getMonth()) {
                continue;
            }

            switch (festival.getType()) {
                case Festival.SLFESTIVAL_IN_LUNAR_DATE:
                    if (festival.getDay() == lunarDate.getDay()) {
                        allFestival.add(festival.clone());
                    }
                    break;

                case Festival.SLFESTIVAL_IN_LUNAR_DATE_WITH_SPECIAL_DAY_IN_MONTH:
                    if (festival.getDay() == 0) { //

                        //
                        Date datetomorrow = (Date)date.clone();
                        datetomorrow.setDate(datetomorrow.getDate() + 1);

                        SLLunarDate lunartomorrow = new SLLunarDate(datetomorrow);

                        if (lunartomorrow.getMonth() != lunarDate.getMonth()) {
                            allFestival.add(festival.clone());
                        }

                    }
                    break;

                default:
                    break;
            }

        }

        // 查找匹配的阳历节日
        for (int i = 0; i < mArraySolarFestival.size(); i++) {
            Festival festival = mArraySolarFestival.get(i);
            switch (festival.getType()) {
                case Festival.SLFESTIVAL_IN_SOLAR_DATE:
                    if (festival.getMonth() == date.getMonth() + 1 && festival.getDay() == date.getDate()) {
                        allFestival.add(festival.clone());
                    }
                    break;

                case Festival.SLFESTIVAL_IN_SOLAR_DATE_WITH_WEEK_AND_DAY:
                    if (festival.getMonth() == date.getMonth() + 1) {

                        int dayInWeeks = date.getDay() + 1; //为什么+1？因为getDay返回的周日是0，而calendar返回的周日是1，可以看源码

                        if (festival.getWeekday() == dayInWeeks) { //
                            if ((date.getDate() + 6) / 7 == festival.getWeekOrder()) { //
                                allFestival.add(festival.clone());
                            }
                        }

                    }
                    break;

                default:
                    break;
            }
        }

        // 不是节日则返回空
        if (allFestival.size() == 0) {
            return null;
        }

        // 对查找到的节日进行排序
        Comparator<Festival> comparator = new Comparator<Festival>() {

            public int compare(Festival lhs, Festival rhs) {
                return lhs.getPriority() - rhs.getPriority();
            }
        };

        Collections.sort(allFestival, comparator);

        return allFestival;
    }

    public String getFestival(Date date){

        ArrayList<Festival> festivals = _getFestival(date);

        if (festivals != null && festivals.size() >= 0){
            return festivals.get(0).getName();
        }
        return null;
    }


    class Festival {

        /**
         * 节日名
         */
        private String mName;

        /**
         * 节日类型
         */
        private int mType;

        /**
         * 节日优先级
         */
        private int mPriority;

        /**
         * 月份
         */
        private int mMonth;

        /**
         * 日,0代表该月的最后一天
         */
        private int mDay;

        /**
         * 节日是某月的第几个weekday,如父亲节是6月第三个周日,则mWeekOrder=3,mWeekday=1
         */
        private int mWeekOrder;

        /**
         * 节日是某月某个星期的第几天,周日为1,周一为2,依次类推
         */
        private int mWeekday;

        /**
         * 阳历节日
         */
        static final int SLFESTIVAL_IN_SOLAR_DATE = 1;

        /**
         * 农历节日
         */
        static final int SLFESTIVAL_IN_LUNAR_DATE = 2;

        /**
         * 按星期和周几来过的节日,如父亲节(6月第三个周日)
         */
        static final int SLFESTIVAL_IN_SOLAR_DATE_WITH_WEEK_AND_DAY = 3;

        /**
         * 节日是当月一个特殊日子,如除夕是腊月的最后一天(腊月可能有29天也可能30天)
         */
        static final int SLFESTIVAL_IN_LUNAR_DATE_WITH_SPECIAL_DAY_IN_MONTH = 4;

        /**
         * 节日优先级低
         */
        static final int SLFESTIVAL_PRIORITY_LOW = 1;

        /**
         * 节日优先级中
         */
        static final int SLFESTIVAL_PRIORITY_NORMAL = 5;

        /**
         * 节日优先级高
         */
        static final int SLFESTIVAL_PRIORITY_HIGH = 9;

        public Festival() {

        }

        public Festival(Festival festival) {
            this.mName = festival.getName();
            this.mType = festival.getType();
            this.mPriority = festival.getPriority();
            this.mMonth = festival.getMonth();
            this.mDay = festival.getDay();
            this.mWeekOrder = festival.getWeekOrder();
            this.mWeekday = festival.getWeekday();
        }

        public Festival clone() {
            return new Festival(this);
        }

        /**
         * 通过节日名称,类型,月,日来初始化
         *
         * @param name
         * @param type
         * @param month
         * @param day
         */
        public void initWithName(String name, int type, int month, int day) {
            this.mName = name;
            this.mType = type;
            this.mMonth = month;
            this.mDay = day;
            this.mPriority = SLFESTIVAL_PRIORITY_NORMAL;
        }

        public void initWithName(String name, int type, int month, int day, int priority) {
            this.mName = name;
            this.mType = type;
            this.mMonth = month;
            this.mDay = day;
            this.mPriority = priority;
        }

        /**
         * 通过节日名,类型,月,星期数,星期几来初始化
         *
         * @param name
         * @param type
         * @param month
         * @param weekOrder
         * @param weekDay
         * @param priority
         */
        public void initWithName(String name, int type, int month, int weekOrder, int weekDay, int priority) {
            this.mName = name;
            this.mType = type;
            this.mMonth = month;
            this.mWeekOrder = weekOrder;
            this.mWeekday = weekDay;
            this.mPriority = priority;

        }

        public void initWithName(String name, int type, int month, int day, int weekOrder, int weekDay, int priority) {
            this.mName = name;
            this.mType = type;
            this.mMonth = month;
            this.mWeekOrder = weekOrder;
            this.mWeekday = weekDay;
            this.mPriority = priority;
            this.mDay = day;
        }

        public String getName() {
            return mName;
        }

        public void setName(String mName) {
            this.mName = mName;
        }

        public int getType() {
            return mType;
        }

        public void setType(int mType) {
            this.mType = mType;
        }

        public void setType(String type) {
            if (type.equals("solar")) {
                this.mType = SLFESTIVAL_IN_SOLAR_DATE;
            } else if (type.equals("lunar")) {
                this.mType = SLFESTIVAL_IN_LUNAR_DATE;
            } else if (type.equals("solarWithWeekAndDay")) {
                this.mType = SLFESTIVAL_IN_SOLAR_DATE_WITH_WEEK_AND_DAY;
            } else if (type.equals("lunarWithSpecialDayInMonth")) {
                this.mType = SLFESTIVAL_IN_LUNAR_DATE_WITH_SPECIAL_DAY_IN_MONTH;
            }
        }

        public int getPriority() {
            return mPriority;
        }

        public void setPriority(int mPriority) {
            this.mPriority = mPriority;
        }

        public int getMonth() {
            return mMonth;
        }

        public void setMonth(int mMonth) {
            this.mMonth = mMonth;
        }

        public int getDay() {
            return mDay;
        }

        public void setDay(int mDay) {
            this.mDay = mDay;
        }

        public int getWeekOrder() {
            return mWeekOrder;
        }

        public void setWeekOrder(int mWeekOrder) {
            this.mWeekOrder = mWeekOrder;
        }

        public int getWeekday() {
            return mWeekday;
        }

        public void setWeekday(int mWeekday) {
            this.mWeekday = mWeekday;
        }

        public void dump() {
            Log.e("Holiday", mName + " " + mType + " " + mPriority + " " + mMonth + " " + mDay + " " + mWeekOrder + " "
                    + mWeekday);
        }
    }


}


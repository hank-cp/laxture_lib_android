package com.laxture.lib.util;

import java.io.Serializable;
import java.util.Date;

import org.joda.time.DateMidnight;

@SuppressWarnings("deprecation")
public class SLLunarDate implements Serializable {

    private static final long serialVersionUID = 8989078124612481142L;

    private int mYear = 0;

    private int mMonth = 0;

    private int mDay = 0;

    private boolean isLeapMonth = false;


    // 天干名
    static final String[] LUNAR_YEAR_PART1 = {
            "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"
    };

    // 地支名
    static final String[] LUNAR_YEAR_PART2 = {
            "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"
    };

    // 属相名
    static final String[] LUNAR_ANIMAL = {
            "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"
    };

    // 农历月份名
    static final String[] LUNAR_MONTH_NAMES = {
            "闰*", "正月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "冬月", "腊月"
    };;

    // 农历日名
    static final String[] LUNAR_DAYS_NAMES = {
            "*", "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十", "十一", "十二", "十三", "十四", "十五", "十六", "十七",
            "十八", "十九", "二十", "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    };

    static final int BEGIN_YEAR = 1891;

    static final int NUMBER_YEAR = 159;

    // 用比特位记录每年的情况

    // 0~4共5bit 春节日分
    // 5~6共2bit 春节月份
    // 7~19共13bit 13个月的大小情况(如无闰月,最后位无效),大月为1,小月为0
    // 20~23共4bit 记录闰月的月份,如果没有则闰月为0
    static final int[] LUNAR_YEARS = {
            0x06d549, 0x6ed23e, 0x075251, 0x06a546, 0x554aba, 0x054bcd, 0x02abc2, 0x355ab6, 0x056aca, 0x8b693f, // 1891-1900
            0x03a953, 0x0752c8, 0x5b253d, 0x0325d0, 0x054dc4, 0x4aab39, 0x02b54d, 0x05acc2, 0x2ba936, 0x03a94a, // 1901-1910
            0x6d92be, 0x0592d2, 0x0525c6, 0x5a55ba, 0x0156ce, 0x02b5c3, 0x25b4b7, 0x06d4cb, 0x7ec941, 0x074954, // 1911-1920
            0x0692c8, 0x5d26bc, 0x052b50, 0x015b45, 0x4adab8, 0x036a4d, 0x0754c2, 0x2f4937, 0x07494a, 0x66933e, // 1921-1930
            0x0295d1, 0x052bc6, 0x596b3a, 0x05ad4e, 0x036a44, 0x3764b8, 0x03a4cb, 0x7b49bf, 0x0549d3, 0x0295c8, // 1931-1940
            0x652dbb, 0x0556cf, 0x02b545, 0x4daab9, 0x05d24d, 0x05a4c2, 0x2d49b6, 0x054aca, 0x7a96bd, 0x029b51, // 1941-1950
            0x0556c6, 0x5ad53b, 0x02d94e, 0x06d2c3, 0x3ea538, 0x06a54c, 0x854abf, 0x054bd2, 0x02ab48, 0x655abc, // 1951-1960
            0x056acf, 0x036945, 0x4752b9, 0x0752cd, 0x032542, 0x364bb5, 0x054dc9, 0x7aad3e, 0x02b551, 0x05b4c6, // 1961-1970
            0x5ba93b, 0x05a94f, 0x0592c3, 0x4b25b7, 0x0525cb, 0x8a55bf, 0x0156d2, 0x02b6c7, 0x65b4bc, 0x06d4d0, // 1971-1980
            0x06c945, 0x4e92b9, 0x0692cd, 0xad26c2, 0x052b54, 0x015b49, 0x62dabd, 0x036ad1, 0x0754c6, 0x5f493b, // 1981-1990
            0x07494f, 0x069344, 0x352b37, 0x052bca, 0x8a6b3f, 0x01ad53, 0x036ac7, 0x5b64bc, 0x03a4d0, 0x0349c5, // 1991-2000
            0x4a95b8, 0x0295cc, 0x052dc1, 0x2aad36, 0x02b549, 0x7daabd, 0x05d252, 0x05a4c7, 0x5d49ba, 0x054ace, // 2001-2010
            0x0296c3, 0x4556b7, 0x055aca, 0x9ad53f, 0x02e953, 0x06d2c8, 0x6ea53c, 0x06a550, 0x064ac5, 0x4a9739, // 2011-2020
            0x02ab4c, 0x055ac1, 0x2ad936, 0x03694a, 0x6752bd, 0x0392d1, 0x0325c6, 0x564bba, 0x0655cd, 0x02ad43, // 2021-2030
            0x356b37, 0x05b4cb, 0x7ba93f, 0x05a953, 0x0592c8, 0x6d25bc, 0x0525cf, 0x0255c4, 0x52adb8, 0x02d6cc, // 2031-2040
            0x05b541, 0x2da936, 0x06c94a, 0x7e92be, 0x0692d1, 0x052ac6, 0x5a56ba, 0x025b4e, 0x02dac2
    // 2041-2049

    };

    public SLLunarDate(Date date) {
        initFromDate(date);
    }

    public SLLunarDate(DateMidnight dateTime) {
        initFromDate(dateTime.toDate());
    }

    public SLLunarDate(int year, int month, int day, boolean leap) {
        setYear(year);
        setMonth(month);
        setDay(day);
        setLeapMonth(leap);
    }

    /**
     * @return 0 or leap month 1-12
     */
    public static int getLunarYearBit(int year) {
        year -= BEGIN_YEAR;

        if (year < 0 || year >= NUMBER_YEAR)
            return 0;
        return LUNAR_YEARS[year];

    }

    public int getYear() {
        return mYear;
    }

    public void setYear(int mYear) {
        this.mYear = mYear;
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

    public boolean isLeapMonth() {
        return isLeapMonth;
    }

    public void setLeapMonth(boolean mIsLeapMonth) {
        this.isLeapMonth = mIsLeapMonth;
    }

    /**
     * 将公历日期转换为农历
     *
     * @param date
     * @return
     */
    public int initFromDate(Date date) {
        int solarYear = date.getYear() + 1900;
        int solarMonth = date.getMonth() + 1;
        int solarDay = date.getDate();


        int lunarYear = solarYear;
        int lunarMonth = 0;
        int lunarDay = 0;
        boolean isLeapYear = false;

        if (solarYear < BEGIN_YEAR || solarYear > BEGIN_YEAR + NUMBER_YEAR - 1) {
            return -1; // 返回无效日期
        }

        int yearIndex = solarYear - BEGIN_YEAR;

        // 记录春节的公历日期
        int springMonthInLunarYear = (LUNAR_YEARS[yearIndex] & 0x60) >> 5;
        int springDayInLunarYear = (LUNAR_YEARS[yearIndex] & 0x1f);

        // 计算今天是公历年的第几天
        int daysSinceSolarYearBegin = dayOfSolarYear(solarYear, solarMonth, solarDay);

        // 计算春节是公历年的第几天
        int daysSinceSolarYearBeginToSpringFestival = dayOfSolarYear(solarYear, springMonthInLunarYear,
                springDayInLunarYear);

        // 计算今天是农历年的第几天
        int daysSinceLunarYearBegin = daysSinceSolarYearBegin - daysSinceSolarYearBeginToSpringFestival + 1;

        // 如果今天在春节前面,重新计算daysSinceLunarYearBegin
        if (daysSinceLunarYearBegin <= 0) {
            // 农历年比当前公历年小1
            yearIndex--;
            lunarYear--;

            // 如果越界,返回无效日期
            if (yearIndex < 0) {
                return -2;
            }

            springMonthInLunarYear = (LUNAR_YEARS[yearIndex] & 0x60) >> 5;
            springDayInLunarYear = (LUNAR_YEARS[yearIndex] & 0x1f);

            daysSinceSolarYearBeginToSpringFestival = dayOfSolarYear(solarYear, springMonthInLunarYear,
                    springDayInLunarYear);

            // 计算上个公历年总天数
            int totalDaysInLastSolarYear = dayOfSolarYear(lunarYear, 12, 31);

            // 计算这种情况下，今天是农历年的第多少天：今天是公历年的多少天+去年全年天数-去年春节是第多少天+1
            daysSinceLunarYearBegin = daysSinceSolarYearBegin + totalDaysInLastSolarYear
                    - daysSinceSolarYearBeginToSpringFestival + 1;

        }

        // 计算月份和日期
        for (lunarMonth = 1; lunarMonth <= 13; lunarMonth++) {
            int daysInMonth = 29;
            if (0 != ((LUNAR_YEARS[yearIndex] >> (6 + lunarMonth)) & 0x1)) {
                daysInMonth = 30;
            }

            if (daysSinceLunarYearBegin <= daysInMonth) {
                break;
            } else {
                daysSinceLunarYearBegin -= daysInMonth;
            }

        }
        lunarDay = daysSinceLunarYearBegin;

        // 处理闰月
        int leapMonth = (LUNAR_YEARS[yearIndex] >> 20) & 0xf;
        if (leapMonth > 0 && leapMonth < lunarMonth) {
            lunarMonth--;

            // 如果当前月是闰月,则设置闰月标记
            if (lunarMonth == leapMonth) {
                isLeapYear = true;
            }
        }

        if (leapMonth > 12) {
            return -3;
        }

        mYear = lunarYear;
        mMonth = lunarMonth;
        mDay = lunarDay;
        isLeapMonth = isLeapYear;


        return 0;
    }

    /**
     * 将农历转成公历
     *
     * @return
     */
    public Date getDate() {

        int solarYear = mYear - 1900;
        int yearIndex = mYear - BEGIN_YEAR;

        int springMonthInLunarYear = (LUNAR_YEARS[yearIndex] & 0x60) >> 5;
        int springDayInLunarYear = (LUNAR_YEARS[yearIndex] & 0x1f);
        int solarMonth = springMonthInLunarYear;
        int solarDay = springDayInLunarYear;

        int distance = 0;
        int leapMonth = (LUNAR_YEARS[yearIndex] >> 20) & 0xf;

        int m = 0; // 要计算到第几月的天数
        if (leapMonth == 0 || (mMonth <= leapMonth && !isLeapMonth)) {
            m = mMonth;
        } else {
            m = mMonth + 1;
        }

        for (int i = 1; i < m; i++) {
            // 大月30天,小月29天
            boolean isBigMonth = ((LUNAR_YEARS[yearIndex] >> (6 + i)) & 0x1) != 0 ? true : false;
            if (isBigMonth) {
                distance += 30;
            } else {
                distance += 29;
            }
        }
        distance += mDay;

        solarDay += distance - 1;

        return new Date(solarYear, solarMonth - 1, solarDay);
    }

    /**
     * 计算这个公历日期是一年中的第几天
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    int dayOfSolarYear(int year, int month, int day) {
        // 为了提高效率,记录每月1日是一年中的第几天
        int NORMAL_YDAY[] = {
                1, 32, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335
        };

        // 闰年的情况
        int LEAP_YDAY[] = {
                1, 32, 61, 92, 122, 153, 183, 214, 245, 275, 306, 336
        };
        int[] t_year_yday_ = NORMAL_YDAY;

        // 判断是否是公历闰年
        if (year % 4 == 0) {
            if (year % 100 != 0)
                t_year_yday_ = LEAP_YDAY;
            if (year % 400 == 0)
                t_year_yday_ = LEAP_YDAY;
        }
        return t_year_yday_[month - 1] + (day - 1);
    }

    /**
     * 获得农历月的名字
     *
     * @return
     */
    public String lunarMonthName() {
        int lunarMonth = mMonth;
        String monthName = "";
        if (lunarMonth >= 0 && lunarMonth < 13) {
            monthName = LUNAR_MONTH_NAMES[lunarMonth];
            if (isLeapMonth) {
                monthName = "闰" + monthName;
            }
            return monthName;
        } else {
            return null;
        }
    }

    /**
     * 获得农历天的名字
     *
     * @return
     */
    public String lunarDayName() {
        int lunarDay = mDay;
        String dayName = "";
        if (lunarDay > 0 && lunarDay < 31) {
            dayName = LUNAR_DAYS_NAMES[lunarDay];
            return dayName;
        } else {
            return null;
        }
    }

    public String amazingLunarDayName(){
        int lunarDay = mDay;
        String dayName = "";
        dayName = lunarMonthName();
        if (lunarDay == 1 && dayName != null){
            return dayName;
        }else if (lunarDay > 0 && lunarDay < 31) {
            dayName = LUNAR_DAYS_NAMES[lunarDay];
            return dayName;
        } else {
            return null;
        }
    }

    /**
     * 获得农历生肖
     *
     * @return
     */
    public String animalOfLunarYear() {
        String animalYear = "";
        animalYear = LUNAR_ANIMAL[(mYear - 4) % 60 % 12];
        return animalYear;
    }

    /**
     * 生成农历年字符串
     *
     * @return
     */
    public String lunarYearName() {
        String lunarYear = "";
        String par1 = LUNAR_YEAR_PART1[(mYear - 4) % 60 % 10];
        String par2 = LUNAR_YEAR_PART2[(mYear - 4) % 60 % 12];
        lunarYear = par1 + par2 + "年";
        return lunarYear;
    }

    /**
     * 生成汉语字符串
     *
     * @return
     */
    public String yearNameInChinese() {
        String chineseNumber = "零一二三四五六七八九";
        StringBuffer chineseYearName = new StringBuffer();
        int num = 0;
        int year = mYear;
        while (year > 0) {
            num = year % 10;
            chineseYearName.append(chineseNumber.substring(num, num + 1));
            year = year / 10;
        }

        return chineseYearName.toString();
    }

    public SLLunarDate clone(SLLunarDate date){
        return new SLLunarDate(getYear(), getMonth(), getDay(), isLeapMonth());
    }
}


package com.laxture.lib.view.date;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateWheelAdapter implements WheelAdapter {

    private final static int mItemNum = (0x7FFFFFFF / 86400); // 总秒数/一天的秒数

    private String fmt = "MM月dd日 E";

    private int invalidbIndex = -1;

    private int invalideIndex = -1;

    private Calendar c = Calendar.getInstance();

    public DateWheelAdapter(String fmt) {
        this.setFmt(fmt);
    }

    public DateWheelAdapter() {
    }

    public int getItemsCount() {
        return mItemNum;
    }

    public String getItem(int index) {
        Date d = new Date();
        d.setTime((long)index * 86400 * 1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(fmt);

        String s = simpleDateFormat.format(d);
        if (s.equals(simpleDateFormat.format(c.getTime()))) {
            return "今天";
        }
        return simpleDateFormat.format(d);
    }

    public int getMaximumLength() {
        return this.fmt.getBytes().length + 2; // MM月dd日(8) (1) E(4) (=13)
    }

    public void setFmt(String fmt) {
        this.fmt = fmt;
    }

    public boolean isValidIndex(int index) {

        if (this.invalidbIndex >= 0 && index <= invalidbIndex) {
            return false;
        }

        if (this.invalideIndex >= 0 && index >= invalideIndex) {
            return false;
        }

        return true;

    }

    public void setInvalidIndex(int bIndex, int eIndex) {
        this.invalidbIndex = bIndex;
        this.invalideIndex = eIndex;
    }

}

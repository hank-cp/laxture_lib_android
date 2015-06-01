package com.laxture.lib.view.date;

public class LunarYearWheelAdapter implements WheelAdapter {

    private int invalidbIndex = -1;

    private int invalideIndex = -1;

    private int iBeginYear;

    private int iEndYear;

    public LunarYearWheelAdapter(int begin, int end) {
        iBeginYear = begin;
        iEndYear = end;
    }

    private final static String year[] = {
            "零", "一", "二", "三", "四", "五", "六", "七", "八", "九"
    };

    public int getItemsCount() {
        return iEndYear - iBeginYear + 1;
    }

    public String getItem(int index) {
        if (index >= 0 && index < getItemsCount()) {
            int value = iBeginYear + index;
            return year[value / 1000] + year[value % 1000 / 100] + year[value % 100 / 10] + year[value % 10] + "年";
        }
        return null;
    }

    public int getMaximumLength() {
        return 10;
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

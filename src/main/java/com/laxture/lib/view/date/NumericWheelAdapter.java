package com.laxture.lib.view.date;

/**
 * Numeric Wheel adapter.
 */
public class NumericWheelAdapter implements WheelAdapter {

    /** The default min value */
    public static final int DEFAULT_MAX_VALUE = 9;

    /** The default max value */
    public static final int DEFAULT_MIN_VALUE = 0;

    // Values
    private int minValue;

    private int maxValue;

    // format
    private String format;

    private String label = null;

    private int invalidBegin = -1;

    private int invalidEnd = -1;

    /**
     * Constructor
     *
     * @param minValue the wheel min value
     * @param maxValue the wheel max value
     */
    public NumericWheelAdapter(int minValue, int maxValue) {
        this(minValue, maxValue, null);
    }

    /**
     * Constructor
     *
     * @param minValue the wheel min value
     * @param maxValue the wheel max value
     * @param format the format string
     */
    public NumericWheelAdapter(int minValue, int maxValue, String format) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.format = format;
    }

    public NumericWheelAdapter(int minValue, int maxValue, String format, String label) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.format = format;
        this.label = label;
    }

    public String getItem(int index) {
        if (index >= 0 && index < getItemsCount()) {
            int value = minValue + index;

            String v = (format != null ? String.format(format, value) : Integer.toString(value));
            if (label != null)
                v += label;
            return v;
        }
        return null;
    }

    public int getItemsCount() {
        return maxValue - minValue + 1;
    }

    public int getMaximumLength() {
        int max = Math.max(Math.abs(maxValue), Math.abs(minValue));
        int maxLen = Integer.toString(max).length();
        if (minValue < 0) {
            maxLen++;
        }
        if (label != null) {
            maxLen += label.getBytes().length;
        }
        return maxLen;
    }

    public boolean isValidIndex(int index) {
        if (this.invalidBegin >= 0 && index <= invalidBegin) {
            //LogUtil.d(String.format("Adapter less than begin:%d,%d", index, this.invalidBegin));
            return false;
        }

        if (this.invalidEnd >= 0 && index >= invalidEnd) {
            //LogUtil.d(String.format("Adapter more than end:%d,%d", index, this.invalidEnd));
            return false;
        }

        return true;

    }

    public void setInvalidIndex(int bIndex, int eIndex) {
        //LogUtil.d(String.format("set invalid index %d,%d", bIndex, eIndex));
        this.invalidBegin = bIndex;
        this.invalidEnd = eIndex;
    }
}

package com.laxture.lib.view.date;

/**
 * The simple Array wheel adapter
 * 
 * @param <T> the element type
 */
public class ArrayWheelAdapter<T> implements WheelAdapter {

    /** The default items length */
    public static final int DEFAULT_LENGTH = -1;

    // items
    private T items[];

    // length
    private int length;

    private int invalidbIndex = -1;

    private int invalideIndex = -1;

    /**
     * Constructor
     * 
     * @param items the items
     * @param length the max items length
     */
    public ArrayWheelAdapter(T items[], int length) {
        this.items = items;
        this.length = length;
    }

    /**
     * Contructor
     * 
     * @param items the items
     */
    public ArrayWheelAdapter(T items[]) {
        this(items, DEFAULT_LENGTH);
    }

    public String getItem(int index) {
        if (index >= 0 && index < items.length) {
            return items[index].toString();
        }
        return null;
    }

    public int getItemsCount() {
        return items.length;
    }

    public int getMaximumLength() {
        return length;
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

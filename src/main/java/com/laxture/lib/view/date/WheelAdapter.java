
package com.laxture.lib.view.date;

public interface WheelAdapter {
    /**
     * Gets items count
     * 
     * @return the count of wheel items
     */
    public int getItemsCount();

    /**
     * Gets a wheel item by index.
     * 
     * @param index the item index
     * @return the wheel item text or null
     */
    public String getItem(int index);

    /**
     * Gets maximum item length. It is used to determine the wheel width. If -1
     * is returned there will be used the default wheel width.
     * 
     * @return the maximum item length or -1
     */
    public int getMaximumLength();

    /**
     * Gets invalid minValue. 应对两个情景。 选择了开始时间，则结束时间必须要大于开始时间
     * 上午下午两个选择项，必须得是个奇数，可以把第一个空元素给扔掉。 如果不需要invalid，返回-1；
     */
    public boolean isValidIndex(int index);

    public void setInvalidIndex(int bIndex, int eIndex);
}

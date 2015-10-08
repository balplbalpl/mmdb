package com.mmdb.model.database.bean;

/**
 * 数据库分页类
 * Created by xiepeng on 15/3/30.
 */
public class Page {
    private int rowTotal;// 总记录数
    private int pageSize = 10;// 每页记录数

    private int count;// 当前页码

    private int total;// 总页数
    private int beginIndex;//起始记录下标
    private int endIndex;//截止记录下标

    /**
     * 使用总记录数、当前页码构造、默认每页10条
     *
     * @param totalRow 总条数
     * @param count    页码，从1开始
     */
    public Page(int totalRow, int count) {
        this.rowTotal = totalRow;
        this.count = count;
        calculate();
    }

    /**
     * 使用总记录数、当前页码和每页记录数构造
     *
     * @param totalRow 总条数
     * @param count    页码，从1开始
     * @param pageSize 默认10条
     */
    public Page(int totalRow, int count, int pageSize) {
        this.rowTotal = totalRow;
        this.count = count;
        this.pageSize = pageSize;
        calculate();
    }

    /**
     * 计算起始下标和结束下标
     */
    private void calculate() {
        total = rowTotal / pageSize + ((rowTotal % pageSize) > 0 ? 1 : 0);

        if (count > total) {
            count = total;
        } else if (count < 1) {
            count = 1;
        }

        beginIndex = (count - 1) * pageSize;
        endIndex = beginIndex + pageSize;
        if (endIndex > rowTotal) {
            endIndex = rowTotal;
        }
    }

    public int getCount() {
        return count;
    }

    public int getTotal() {
        return total;
    }

    public int getTotalRow() {
        return rowTotal;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getBeginIndex() {
        return beginIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }
}

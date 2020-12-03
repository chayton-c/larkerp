package com.yingda.lkj.beans.system;

import lombok.Data;

import java.math.BigInteger;
import java.util.List;

/**
 * 分页
 *
 * @author hood  2019/12/27
 */
public class Page {
    private Long dataTotal;
    private Integer pageSize;
    private Integer currentPage;

    public Page() {
    }

    public Page(Integer pageSize, Integer currentPage) {
        this.pageSize = pageSize;
        this.currentPage = currentPage;
    }

    public void setDataTotal(List<BigInteger> count) {
        this.dataTotal = count.isEmpty() ? 0 : count.get(0).longValue();
    }

    public Long getDataTotal() {
        return dataTotal;
    }

    public void setDataTotal(Long dataTotal) {
        this.dataTotal = dataTotal;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }
}

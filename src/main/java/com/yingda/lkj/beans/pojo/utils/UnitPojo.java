package com.yingda.lkj.beans.pojo.utils;

import lombok.Data;

/**
 * @author hood  2020/6/10
 */
@Data
public class UnitPojo {
    private String unitName;
    private double value;

    public UnitPojo() {
    }

    public UnitPojo(String unitName, double value) {
        this.unitName = unitName;
        this.value = value;
    }
}

package com.yingda.lkj.beans.enums;

/**
 * 是否出现异常
 *
 * @author hood  2020/3/30
 */
public enum Abnormal {
    // abnormal字段
    NORMAL(0), // 无异常
    ABNORMAL(1), // 异常
    NO_DATA(2); // 无数据

    private byte abnormal;

    Abnormal(int abnormal) {
        this.abnormal = (byte) abnormal;
    }

    public byte getAbnormal() {
        return abnormal;
    }
}

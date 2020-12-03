package com.yingda.lkj.beans.system;

import lombok.Data;

import java.io.Serializable;

/**
 * 由多个部分组成的数据结构
 * @author hood  2019/12/27
 */
@Data
public class Pair<K, V> implements Serializable {

    private static final long serialVersionUID = 1L;

    public K firstValue;
    public V secondValue;

    public Pair() {
        super();
    }

    public Pair(K firstValue, V secondValue) {
        super();
        this.firstValue = firstValue;
        this.secondValue = secondValue;
    }

    @Override
    public String toString() {
        return "Pair [firstValue=" + firstValue + ", secondValue=" + secondValue + "]";
    }

}

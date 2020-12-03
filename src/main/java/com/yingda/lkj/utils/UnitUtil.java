package com.yingda.lkj.utils;

import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.utils.UnitPojo;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.beans.system.Pair;

/**
 * @author hood  2020/6/10
 */
public class UnitUtil {
    /**
     * 把未经处理的单位和数值转换成最小的单位和数值，如rawUnitName="A",rawUnitValue=3 => UnitPojo("mA", 3000)
     */
    public static UnitPojo convertToSmallestUnit(String rawUnitName, double rawUnitValue) {
        if (StringUtils.isEmpty(rawUnitName))
            return new UnitPojo("", rawUnitValue);
        switch (rawUnitName) {
            case "V":
                return new UnitPojo("mV", rawUnitValue * 1000);
            case "A":
                return new UnitPojo("mA", rawUnitValue * 1000);
            case "KΩ":
                return new UnitPojo("Ω", rawUnitValue * 1000);
            case "MΩ":
                return new UnitPojo("Ω", rawUnitValue * 1000 * 1000);
            case "nF":
                return new UnitPojo("pF", rawUnitValue * 1000);
            case "uF":
                return new UnitPojo("pF", rawUnitValue * 1000 * 1000);
            default:
                return new UnitPojo(rawUnitName, rawUnitValue);
        }
    }
}

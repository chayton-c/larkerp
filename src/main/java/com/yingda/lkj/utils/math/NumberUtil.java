package com.yingda.lkj.utils.math;

import com.yingda.lkj.utils.StringUtils;

import java.util.regex.Pattern;

/**
 * @author hood  2019/12/11
 */
@SuppressWarnings("unused")
public class NumberUtil {

    public static boolean isDouble(String str) {
        if (StringUtils.isEmpty(str))
            return false;

        Pattern pattern = Pattern.compile("^[-\\+]?\\d+(\\.\\d*)?|\\.\\d+$");
        return pattern.matcher(str).matches();
    }

    /**
     * 是否为整数
     */
    public static boolean isInteger(String str) {
        if (StringUtils.isEmpty(str))
            return false;

        Pattern pattern = Pattern.compile("^[-+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    public static void main(String[] args) {
        System.out.println(isInteger("1"));
        System.out.println(isInteger("-1"));
        System.out.println(isInteger("+1.0"));
    }
}

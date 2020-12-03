package com.yingda.lkj.utils.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author hood  2019/11/11
 */
@SuppressWarnings("unused")
public class Arithmetic {
    /**
     * 加法
     *
     * @param augend 被加数
     * @param addend 加数
     * @param scale  保留几位，scale = 2 : 保留两位
     */
    public static double add(double augend, double addend, int scale) {
        return new BigDecimal(augend).add(new BigDecimal(addend)).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 加法
     *
     * @param augend 被加数
     * @param addend 加数
     * @return 默认保留两位
     */
    public static double add(double augend, double addend) {
        return add(augend, addend, 2);
    }

    /**
     * 加法
     *
     * @param augend 被加数
     * @param addend 加数
     * @param scale  保留几位，scale = 2 : 保留两位
     */
    public static double jia(double augend, double addend, int scale) {
        return add(augend, addend, scale);
    }

    /**
     * 加法
     *
     * @param augend 被加数
     * @param addend 加数
     * @return 默认保留两位
     */
    public static double jia(double augend, double addend) {
        return add(augend, addend, 2);
    }


    /**
     * 减法
     *
     * @param subtracted  被减数
     * @param subtraction 减数
     * @param scale       保留几位，scale = 2 : 保留两位
     */
    public static double subtract(double subtracted, double subtraction, int scale) {
        return new BigDecimal(subtracted).subtract(new BigDecimal(subtraction)).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 减法
     *
     * @param subtracted  被减数
     * @param subtraction 减数
     * @return 默认保留两位
     */
    public static double subtract(double subtracted, double subtraction) {
        return subtract(subtracted, subtraction, 2);
    }

    /**
     * 减法
     *
     * @param subtracted  被减数
     * @param subtraction 减数
     * @param scale       保留几位，scale = 2 : 保留两位
     */
    public static double jian(double subtracted, double subtraction, int scale) {
        return subtract(subtracted, subtraction, scale);
    }

    /**
     * 减法
     *
     * @param subtracted  被减数
     * @param subtraction 减数
     * @return 默认保留两位
     */
    public static double jian(double subtracted, double subtraction) {
        return subtract(subtracted, subtraction, 2);
    }


    /**
     * 乘法
     *
     * @param multiplicand 乘数
     * @param multiplier   被乘数
     * @param scale        保留几位，scale = 2 : 保留两位
     */
    public static double multiply(double multiplicand, double multiplier, int scale) {
        return new BigDecimal(multiplicand).multiply(new BigDecimal(multiplier)).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 乘法
     *
     * @param multiplicand 乘数
     * @param multiplier   被乘数
     * @return 默认保留两位
     */
    public static double multiply(double multiplicand, double multiplier) {
        return multiply(multiplicand, multiplier, 2);
    }


    /**
     * 乘法
     *
     * @param multiplicand 乘数
     * @param multiplier   被乘数
     * @param scale        保留几位，scale = 2 : 保留两位
     */
    public static double cheng(double multiplicand, double multiplier, int scale) {
        return multiply(multiplicand, multiplier, scale);
    }

    /**
     * 乘法
     *
     * @param multiplicand 乘数
     * @param multiplier   被乘数
     * @return 默认保留两位
     */
    public static double cheng(double multiplicand, double multiplier) {
        return multiply(multiplicand, multiplier, 2);
    }

    /**
     * 除法
     *
     * @param dividend 被除数
     * @param divisor  除数
     * @param scale    保留几位，scale = 2 : 保留两位
     */
    public static double divide(double dividend, double divisor, int scale) {
        return new BigDecimal(dividend).divide(new BigDecimal(divisor), scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 除法
     *
     * @param dividend 被除数
     * @param divisor  除数
     * @return 默认保留两位
     */
    public static double divide(double dividend, double divisor) {
        return divide(dividend, divisor, 2);
    }

    /**
     * 除法
     *
     * @param dividend 被除数
     * @param divisor  除数
     * @param scale    保留几位，scale = 2 : 保留两位
     */
    public static double chu(double dividend, double divisor, int scale) {
        return divide(dividend, divisor, scale);
    }

    /**
     * 除法
     *
     * @param dividend 被除数
     * @param divisor  除数
     * @return 默认保留两位
     */
    public static double chu(double dividend, double divisor) {
        return divide(dividend, divisor, 2);
    }

    public static void main(String[] args) {
        System.out.println(Arithmetic.add(1, 1));
        System.out.println(Arithmetic.subtract(1.1111111, 1.1111111, 3));
        System.out.println(Arithmetic.multiply(1.1111111, 1.1111111, 15));
        System.out.println(Arithmetic.divide(Math.PI, 2, 15));
    }

}

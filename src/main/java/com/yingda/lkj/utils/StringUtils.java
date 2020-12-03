package com.yingda.lkj.utils;

import org.springframework.util.Assert;

import java.util.*;

/**
 * @author hood  2019/12/13
 */
@SuppressWarnings("unused")
public class StringUtils {
    /**
     * 判断对象是否为null或""(条件成立则返回ture,否则返回false)
     */
    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Object objects) {
        if (objects == null)
            return true;

        if (objects instanceof Number)
            return ((Number) objects).doubleValue() == 0;

        if (objects instanceof Collection)
            return ((Collection) objects).isEmpty();

        return "".equals(objects) || "null".equalsIgnoreCase(objects + "") || "undefined".equalsIgnoreCase(objects + "");
    }

    /**
     * 判断对象是否不为null或""(条件成立则返回ture,否则返回false)
     */
    public static boolean isNotEmpty(Object objects) {
        return !isEmpty(objects);
    }

    /**
     * <p>判断所有对象是否都不是空</p>
     *
     * <p>注意：如果传的是null，因为这里没有不是空的数据，所以方法返回true</p>
     * <p>      stream().allMatch也是这么判断的</p>
     */
    public static boolean isNotEmpty(Object... objects) {
        return objects == null || Arrays.stream(objects).allMatch(StringUtils::isNotEmpty);
    }

    /**
     * 全角转半角
     * @param input String.
     * @return 半角字符串
     */
    public static String toDBC(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == '\u3000') {
                c[i] = ' ';
            } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
                c[i] = (char) (c[i] - 65248);

            }
        }

        return new String(c);
    }

    public static void main(String[] args) {
        List<String> strings = new ArrayList<>();
    }
}

package com.yingda.lkj.utils;

import java.util.Optional;

/**
 * @author hood  2020/7/8
 */
public class OptionalUtil {
    public static String removeNull(String raw) {
        return Optional.ofNullable(raw).orElse("");
    }
}

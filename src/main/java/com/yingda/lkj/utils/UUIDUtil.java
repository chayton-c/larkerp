package com.yingda.lkj.utils;

import java.util.UUID;

/**
 * @author hood  2020/7/9
 */
public class UUIDUtil {
    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static long getUUID2Long(String uuid) {
        for (String s : uuid.split(".")) {
            System.out.println(s);
        }
        return 0;
    }

    public static void main(String[] args) {
        getUUID2Long(UUID.randomUUID().toString());
    }
}

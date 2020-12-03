package com.yingda.lkj.utils;

import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;

/**
 * @author hood  2020/4/10
 */
public class Assert {
    public static void notNull(Object object) throws CustomException {
        notNull(object, "");
    }

    public static void notNull(Object object, String message) throws CustomException {
        if (object == null)
            throw new CustomException(new Json(JsonMessage.SYS_ERROR, message));
    }
}

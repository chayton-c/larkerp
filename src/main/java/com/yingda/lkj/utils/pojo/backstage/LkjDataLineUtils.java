package com.yingda.lkj.utils.pojo.backstage;

import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;

/**
 * @author hood  2020/3/1
 */
public class LkjDataLineUtils {
    public static String createUniqueCode(LkjDataLine lkjDataLine) {
        if (lkjDataLine == null)
            return null;

        String uniqueCode = lkjDataLine.getDownriver() + lkjDataLine.getRetrograde() + lkjDataLine.getLeftDeviceId() + lkjDataLine.getRightDeviceId();
        return uniqueCode;
    }
}

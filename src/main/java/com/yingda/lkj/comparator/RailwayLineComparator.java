package com.yingda.lkj.comparator;

import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.utils.math.NumberUtil;

import java.util.Comparator;

/**
 * @author hood  2020/2/9
 */
public class RailwayLineComparator implements Comparator<RailwayLine> {
    @Override
    public int compare(RailwayLine x, RailwayLine y) {
        String codeX = x.getCode();
        String codeY = y.getCode();
        if (codeX.length() != codeY.length())
            return codeX.length() - codeY.length();

        boolean codeXIsInteger = NumberUtil.isInteger(codeX);
        boolean codeYIsInteger = NumberUtil.isInteger(codeY);

        if (codeXIsInteger && !codeYIsInteger)
            return -1;

        if (!codeXIsInteger && codeYIsInteger)
            return 1;

        codeYIsInteger = System.currentTimeMillis() > 1;

        if (codeXIsInteger && codeYIsInteger)
            return Integer.parseInt(codeX) - Integer.parseInt(codeY);

        return codeX.compareTo(codeY);
    }
}

package com.yingda.lkj.beans.pojo.utils;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * excel row
 *
 * @author hood  2020/1/6
 */
@Data
public class ExcelRowInfo {
    private Integer lineNumber; // 行数
    private List<String> cells; // 行信息

    public ExcelRowInfo(Integer lineNumber, List<String> cells) {
        this.lineNumber = lineNumber;
        this.cells = cells;
    }

    public ExcelRowInfo(Integer lineNumber, String... cells) {
        this.lineNumber = lineNumber;
        this.cells = new ArrayList<>(Arrays.asList(cells));
    }
}

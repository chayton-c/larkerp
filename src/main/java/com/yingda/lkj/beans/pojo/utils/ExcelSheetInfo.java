package com.yingda.lkj.beans.pojo.utils;

import lombok.Data;

import java.util.*;

/**
 * @author hood  2020/1/6
 */
@Data
public class ExcelSheetInfo {
    private String sheetName;
    private Map<Integer, ExcelRowInfo> rowInfoMap; // key:行号，value:行信息

    public ExcelSheetInfo(String sheetName, Map<Integer, ExcelRowInfo> rowInfoMap) {
        this.sheetName = sheetName;
        this.rowInfoMap = rowInfoMap;
    }

}

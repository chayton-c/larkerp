package com.yingda.lkj.utils.excel.excelClient;

import com.yingda.lkj.beans.pojo.device.SemaphoreFromExcel;
import com.yingda.lkj.beans.pojo.utils.ExcelRowInfo;
import com.yingda.lkj.beans.pojo.utils.ExcelSheetInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/5/10
 */
public class SemaphoreExcelParser {
    private String bureauCode; // 局编码，根据局编码的不同，调用不同的解析策略

    public SemaphoreExcelParser(String bureauCode) {
        this.bureauCode = bureauCode;
    }


    public List<SemaphoreFromExcel> getSemaphoreFromExcel(List<ExcelSheetInfo> excelSheetInfos) {
        List<SemaphoreFromExcel> result = new ArrayList<>();

        for (ExcelSheetInfo excelSheetInfo : excelSheetInfos) {

            Map<Integer, ExcelRowInfo> rowInfoMap = excelSheetInfo.getRowInfoMap();
            List<ExcelRowInfo> excelRowInfos = new ArrayList<>(rowInfoMap.values());

            Map<Integer, SemaphoreFromExcel> semaphoresFromExcel = getSemaphoresFromExcel(excelRowInfos);
            result.addAll(new ArrayList<>(semaphoresFromExcel.values()));
        }
        return result;
    }


    /**
     * key：行数，value：通过excel封装的设备
     */
    public Map<Integer, SemaphoreFromExcel> getSemaphoresFromExcel(List<ExcelRowInfo> excelRowInfos) {
        Map<Integer, SemaphoreFromExcel> result = new LinkedHashMap<>();
        for (int i = 0; i < excelRowInfos.size(); i++) {
            ExcelRowInfo excelRowInfo = excelRowInfos.get(i);

            List<String> cells = excelRowInfo.getCells();
            // TODO 这里应该写局策略，没时间先这样了
            if ("01".equals(bureauCode) || "1".equals(bureauCode)) { // 哈局
                if (cells.size() > 14 && bureauCode.equals(cells.get(1)))
                    result.put(i, SemaphoreFromExcel.getInstance(excelRowInfo));
            }
            else { // 云南，广州局
                if (cells.size() > 14 && bureauCode.equals(cells.get(1)))
                    result.put(i, SemaphoreFromExcel.getInstance4Yunnan(excelRowInfo));
            }
        }
        return result;
    }
}

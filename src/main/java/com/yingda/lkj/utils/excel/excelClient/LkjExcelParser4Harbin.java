package com.yingda.lkj.utils.excel.excelClient;

import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.device.Semaphore;
import com.yingda.lkj.beans.pojo.device.SemaphoreFromExcel;
import com.yingda.lkj.beans.pojo.lkj.LkjDataLineFromExcel;
import com.yingda.lkj.beans.pojo.utils.ExcelRowInfo;
import com.yingda.lkj.beans.pojo.utils.ExcelSheetInfo;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author hood  2020/5/10
 */
public class LkjExcelParser4Harbin {

    /** 距离数据所在列 */
    public static final Integer DISTANCE_INDEX = 7;

    public static final String RETROGRADE_STR = "信号机和轨道电路分布表（逆向）";
    public static final String FORWARD_STR = "信号机和轨道电路分布表（正向）";

    // 表格类型
    public static final String TABLE_1 = "表1";
    public static final String TABLE_2 = "表2";
    public static final String TABLE_3 = "表3";
    public static final String TABLE_4 = "表4";

    private int seq = 0;

    private SemaphoreExcelParser semaphoreExcelParser;

    public LkjExcelParser4Harbin(String bureauCode) {
        this.semaphoreExcelParser = new SemaphoreExcelParser(bureauCode);
    }

    /**
     * excel中解析LkjDataLineFromExcel
     */
    public List<LkjDataLineFromExcel> getLkjDataLineFromExcel(List<ExcelSheetInfo> excelSheetInfos) throws CustomException {
        List<LkjDataLineFromExcel> result = new ArrayList<>();

        // 遍历每一页
        for (ExcelSheetInfo excelSheetInfo : excelSheetInfos) {
            // 行信息list
            List<ExcelRowInfo> excelRowInfos = new ArrayList<>(excelSheetInfo.getRowInfoMap().values());

            // key:行数，value：excel中的封装信号机
            Map<Integer, SemaphoreFromExcel> semaphoresFromExcel = semaphoreExcelParser.getSemaphoresFromExcel(excelRowInfos);

            // indexes：信号机所在行数
            List<Integer> indexes = semaphoresFromExcel.keySet().stream().sorted().collect(Collectors.toList());
            // 根据每个信号机所在行数，辨别生成的lkj数是否为一组
            List<List<Integer>> indexGroups = subscriptSplit(indexes);
            // 获取上行还是下行
            byte downriver = getDownriver(excelSheetInfo);
            List<LkjDataLineFromExcel> lkjDataExcelUtils = getLkjDataExcel(semaphoresFromExcel, indexGroups, excelRowInfos, downriver);
            result.addAll(lkjDataExcelUtils);
        }
        return result;
    }


    /**
     * 生成lkj数据
     * @param semaphoresFromExcel  key:行数，value：excel中的封装信号机
     * @param indexGroups 分好组的行数，如：[[4, 6, 8], [11, 13, 15]]，其中4,6,8会生成两个lkj数，主要根据excel信号机和这个行数生成lkj
     * @param excelRowInfos ExcelRowInfo为一行excel，excelRowInfos为这页所有的excel行集合
     * @param downriver 正向还是逆向
     */
    protected List<LkjDataLineFromExcel> getLkjDataExcel (
            Map<Integer, SemaphoreFromExcel> semaphoresFromExcel, List<List<Integer>> indexGroups, List<ExcelRowInfo> excelRowInfos, byte downriver) throws CustomException {
        List<LkjDataLineFromExcel> results = new ArrayList<>();

        // batchIndexes为同一批次下的多个Lkj数据使用的信号机下标
        // 遍历分组行数，如：indexGroups为[[4, 6, 8], [11, 13, 15]] -> batchIndexes为[4,6,8]，能生成两个lkj数
        for (List<Integer> batchIndexes : indexGroups) {
            List<LkjDataLineFromExcel> batches = new ArrayList<>();
            // 批次序号分组，如[4,6,8] -> [[4,6],[6,8]]
            List<List<Integer>> indexList = groupIndex(batchIndexes);
            // indexes为一条lkj数据使用使用的excel行数(4,6)
            for (List<Integer> indexes : indexList) {
                LkjDataLineFromExcel lkjDataLineFromExcel = new LkjDataLineFromExcel(downriver, getRetrograde(excelRowInfos, indexes.get(0)));
                lkjDataLineFromExcel.setTableType(getTableType(excelRowInfos, indexes.get(0)));
                lkjDataLineFromExcel.setSeq(seq++); // 记录插入顺序，每组lkj数能区分开就行

                // 左节点行数
                Integer leftNodeLineNumber = indexes.get(0);
                // 通过行数，从semaphoresFromExcel中获取信号机
                SemaphoreFromExcel leftNode = semaphoresFromExcel.get(leftNodeLineNumber);
                lkjDataLineFromExcel.setLeftNode(leftNode);

                // 距离
                String distanceStr = excelRowInfos.get(leftNodeLineNumber + 1).getCells().get(DISTANCE_INDEX);
                if (StringUtils.isEmpty(distanceStr))
                    throw new CustomException(new Json(JsonMessage.PARAM_INVALID, "第" + (leftNodeLineNumber + 1) + "行缺少信号机间距离"));

                try {
                    double distance = Double.parseDouble(distanceStr);
                    lkjDataLineFromExcel.setDistance(distance);
                } catch (NumberFormatException n) {
                    throw new CustomException(new Json(JsonMessage.PARAM_INVALID, "第" + leftNodeLineNumber + "行距离格式错误"));
                }

                // 右节点行数
                Integer rightNodeLineNumber = indexes.get(1);
                SemaphoreFromExcel rightNode = semaphoresFromExcel.get(rightNodeLineNumber);
                lkjDataLineFromExcel.setRightNode(rightNode);

                batches.add(lkjDataLineFromExcel);
            }

            // 暂时放一个uuid当唯一码，导入数据时，会在service中查询持久化的唯一码
            String temporaryUniqueCode = UUID.randomUUID().toString();
            batches.forEach(x -> x.setUniqueCode(temporaryUniqueCode));
            results.addAll(batches);
        }
        return results;
    }

    /**
     * <p>差是2的，连起来</p>
     * <p>{4, 6, 8, 11, 13, 15, 18, 20, 22, 25, 27, 29, 32, 34, 36, 43, 45, 47, 50, 52, 54, 57, 59, 61, 64, 66, 68, 71, 73, 75}</p>
     * <p> -> </p>
     * <p>[[4, 6, 8], [11, 13, 15], [18, 20, 22], [25, 27, 29], [32, 34, 36], [43, 45, 47], [50, 52, 54], [57, 59, 61], [64, 66, 68], [71, 73, 75]]</p>
     */
    private List<List<Integer>> subscriptSplit(List<Integer> raw) {
        List<List<Integer>> returnList = new ArrayList<>();
        for (Integer current : raw) {
            if (returnList.isEmpty()) {
                returnList.add(new ArrayList<>(List.of(current)));
                continue;
            }

            List<Integer> integers = returnList.get(returnList.size() - 1);
            int last = integers.get(integers.size() - 1);
            if (current - last == 2) {
                integers.add(current);
                continue;
            }

            returnList.add(new ArrayList<>(List.of(current)));
        }
        return returnList;
    }

    /**
     * <p>[4, 6, 8, 10]</p>
     * <p> -> </p>
     * <p>[[4, 6], [6, 8], [8, 10]]</p>
     */
    private List<List<Integer>> groupIndex(List<Integer> raw) {
        List<List<Integer>> result = new ArrayList<>();
        for (int current : raw) {
            if (result.isEmpty()) {
                result.add(new ArrayList<>(List.of(current)));
                continue;
            }

            result.get(result.size() - 1).add(current);
            result.add(new ArrayList<>(List.of(current)));
        }
        result.remove(result.size() - 1);
        return result;
    }

    private byte getDownriver(ExcelSheetInfo excelSheetInfo) {
        String sheetName = excelSheetInfo.getSheetName();
        // 如果sheetName包含"上行"，则下面的lkjDataLine.downriver为上行,上下行都不包含，为半自闭
        if (sheetName.contains(Semaphore.DOWNRIVER))
            return LkjDataLine.DOWNRIVER;
        if (sheetName.contains(Semaphore.UPRIVER))
            return LkjDataLine.UPRIVER;
        return LkjDataLine.SEME_CLOSED_INTERVAL;
    }

    private byte getRetrograde(List<ExcelRowInfo> excelRowInfos, int lineNumber) {
        for (int i = lineNumber; i > 0; i--) {
            List<String> cells = excelRowInfos.get(i).getCells();
            if (cells.contains(RETROGRADE_STR))
                return LkjDataLine.RETROGRADE;
            if (cells.contains(FORWARD_STR))
                return LkjDataLine.FORWARD;
        }
        return LkjDataLine.SEME_CLOSED_INTERVAL;
    }

    private Byte getTableType(List<ExcelRowInfo> excelRowInfos, int lineNumber) {
        for (int i = lineNumber; i > 0; i--) {
            List<String> cells = excelRowInfos.get(i).getCells();
            if (cells.contains(TABLE_1))
                return LkjDataLine.TABLE_1;
            if (cells.contains(TABLE_2))
                return LkjDataLine.TABLE_2;
            if (cells.contains(TABLE_3))
                return LkjDataLine.TABLE_3;
            if (cells.contains(TABLE_4))
                return LkjDataLine.TABLE_4;
        }
        return null;
    }
}

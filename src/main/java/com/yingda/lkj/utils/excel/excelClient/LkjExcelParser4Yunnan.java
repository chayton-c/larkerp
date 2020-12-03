package com.yingda.lkj.utils.excel.excelClient;

import com.yingda.lkj.beans.entity.backstage.line.Fragment;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.device.SemaphoreFromExcel;
import com.yingda.lkj.beans.pojo.lkj.LkjDataLineFromExcel;
import com.yingda.lkj.beans.pojo.utils.ExcelRowInfo;
import com.yingda.lkj.beans.pojo.utils.ExcelSheetInfo;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.service.backstage.line.FragmentService;
import com.yingda.lkj.utils.SpringContextUtil;
import com.yingda.lkj.utils.StringUtils;

import java.util.*;

/**
 * @author hood  2020/5/10
 */
public class LkjExcelParser4Yunnan {

    public static final String RETROGRADE_DOWNRIVER_STR = "下行反向";
    public static final String RETROGRADE_UPRIVER_STR = "上行反向";
    public static final String FORWARD_DOWNRIVER_STR = "下行正向";
    public static final String FORWARD_UPRIVER_STR = "上行正向";

    private int seq = 0;

    private SemaphoreExcelParser semaphoreExcelParser;

    public LkjExcelParser4Yunnan(String bureauCode) {
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

            // 根据每个信号机所在行数，辨别生成的lkj数是否为一组
            List<List<Integer>> indexGroups = groupLineNumber(semaphoresFromExcel);
            // 获取上行还是下行
            List<LkjDataLineFromExcel> lkjDataExcelUtils = getLkjDataExcel(semaphoresFromExcel, indexGroups, excelRowInfos);
            result.addAll(lkjDataExcelUtils);
        }
        return result;
    }

    private List<List<Integer>> groupLineNumber(Map<Integer, SemaphoreFromExcel> semaphoresFromExcel) throws CustomException {
        List<List<Integer>> returnList = new LinkedList<>();
        if (semaphoresFromExcel.size() == 0)
            return new LinkedList<>();

        // key: 行号，value：excel第p行标志位
        Set<Integer> lineNumbers = semaphoresFromExcel.keySet();
        LinkedList<Integer> lineNumberList = new LinkedList<>(lineNumbers);
        SemaphoreFromExcel headSemaphore = semaphoresFromExcel.get(lineNumberList.get(0));
        String previousFragmentName = headSemaphore.getFragmentName();
        String previousFlag = headSemaphore.getLkjFlag();

        Integer rearLineNumber = lineNumberList.get(lineNumbers.size() - 1);
        Integer previousLineNumber = 0; // 上次读取的行号

        Map<Integer, String> lineNumberFlag = new LinkedHashMap<>();
        for (Map.Entry<Integer, SemaphoreFromExcel> entry : semaphoresFromExcel.entrySet()) {
            Integer currentLineNumber = entry.getKey();
            SemaphoreFromExcel semaphore = entry.getValue();

            String lkjFlag = semaphore.getLkjFlag().trim();
            String fragmentName = semaphore.getFragmentName();

            if (    "↑".equals(lkjFlag) &&
                    (
                            (!fragmentName.equals(previousFragmentName) && StringUtils.isNotEmpty(fragmentName)
                    ) || // 区间名变了，即使标识符还是↑，也表示不同区间
                    (
                            !previousLineNumber.equals(0) && !previousLineNumber.equals(currentLineNumber - 1))
                    ) // 行号不连续，也表示不同区间
               )
            {
                previousFragmentName = fragmentName; // 更新区间名指针
                returnList.addAll(groupFragment(lineNumberFlag, previousFlag)); // 把内存中的行号拼到一起，然后组成多组区间
                lineNumberFlag = new LinkedHashMap<>(); // 清空内存中的行号
                previousFlag = lkjFlag; // 更新标识位指针
            }
            if ("1".equals(lkjFlag)) {
                returnList.addAll(groupFragment(lineNumberFlag, previousFlag));
                lineNumberFlag = new LinkedHashMap<>();
                previousFlag = lkjFlag;
            }

            previousLineNumber = currentLineNumber; // 更新行号
            lineNumberFlag.put(currentLineNumber, lkjFlag);

            // 收尾
            if (currentLineNumber.equals(rearLineNumber)) {
                returnList.addAll(groupFragment(lineNumberFlag, previousFlag));
            }
        }
        return returnList;
    }

    private List<List<Integer>> groupFragment(Map<Integer, String> lineNumberFlag, String previousFlag) throws CustomException {
        if (lineNumberFlag.size() == 0)
            return new LinkedList<>();
        if ("1".equals(previousFlag))
            return groupingWithinTheStationFragment(lineNumberFlag);
        if ("↑".equals(previousFlag))
            return groupingBetweenStationsFragment(lineNumberFlag);
        return new LinkedList<>(); // 不可能
    }


    private List<List<Integer>> groupingBetweenStationsFragment(Map<Integer, String> lineNumberFlag) {
        List<List<Integer>> returnList = new LinkedList<>();
        List<Integer> collect = new LinkedList<>(lineNumberFlag.keySet());
        returnList.add(collect);
        return returnList;
    }

    /**
     * 站内区间分组
     */
    private List<List<Integer>> groupingWithinTheStationFragment(Map<Integer, String> lineNumberFlag) throws CustomException {
        List<List<Integer>> returnList = new LinkedList<>();
        List<Integer> lines = new LinkedList<>(lineNumberFlag.keySet());
        Integer flag1Line = lines.get(0); // 找标志位为1的行(第一行)
        lines.remove(flag1Line);

        int count = 0;
        while (true) {
            if (lines.isEmpty())
                return returnList;

            if (count++ == 1000000)
                throw new CustomException(new Json(JsonMessage.PARAM_INVALID, String.format("解析lkjExcel%d行时出现异常，请稍后再试", lines.get(0) + 1)));

            // 找标志位为2
            List<Integer> flag2Lines = new LinkedList<>();
            for (Integer line : lines) {
                String flag = lineNumberFlag.get(line);
                if ("2".equals(flag))
                    flag2Lines.add(line);

                if (!"2".equals(flag))
                    break;
            }
            lines.removeAll(flag2Lines);

            // 找标志位为3
            List<Integer> flag3Lines = new LinkedList<>();
            for (Integer line : lines) {
                String flag = lineNumberFlag.get(line);
                if ("3".equals(flag))
                    flag3Lines.add(line);

                if (!"3".equals(flag))
                    break;
            }
            lines.removeAll(flag3Lines);

            // 找标志位为4
            List<Integer> flag4Lines = new LinkedList<>();
            for (Integer line : lines) {
                String flag = lineNumberFlag.get(line);
                if ("4".equals(flag))
                    flag4Lines.add(line);

                if (!"4".equals(flag))
                    break;
            }
            lines.removeAll(flag4Lines);

            if (flag2Lines.size() > 1 && flag3Lines.size() > 1 ||
                flag2Lines.size() > 1 && flag4Lines.size() > 1 ||
                flag3Lines.size() > 1 && flag4Lines.size() > 1)
                throw new CustomException(new Json(JsonMessage.PARAM_INVALID, String.format("第%d行所在进路标志位包含多个连续的结束标志位，无法解析", flag1Line)));

            if (flag2Lines.size() == 1 && flag3Lines.size() == 1) {
                if (flag4Lines.isEmpty()) {
                    List<Integer> batch = new ArrayList<>();
                    batch.add(flag1Line);
                    batch.add(flag2Lines.get(0));
                    batch.add(flag3Lines.get(0));
                    returnList.add(batch);
                    continue;
                }

                for (Integer flag4Line : flag4Lines) {
                    List<Integer> batch = new ArrayList<>();
                    batch.add(flag1Line);
                    batch.add(flag2Lines.get(0));
                    batch.add(flag3Lines.get(0));
                    batch.add(flag4Line);
                    returnList.add(batch);
                }
            }

            if (flag2Lines.size() == 1 && flag3Lines.isEmpty()) {
                List<Integer> batch = new ArrayList<>();
                batch.add(flag1Line);
                batch.add(flag2Lines.get(0));
                if (!flag4Lines.isEmpty())
                    throw new CustomException(new Json(JsonMessage.PARAM_INVALID, String.format("第%d行所在进路缺少3标志位，无法解析", flag1Line)));

                returnList.add(batch);
                continue;
            }

            if (flag2Lines.size() > 1) {
                for (Integer flag2Line : flag2Lines) {
                    List<Integer> batch = new ArrayList<>();
                    batch.add(flag1Line);
                    batch.add(flag2Line);
                    if (!flag3Lines.isEmpty())
                        batch.add(flag3Lines.get(0));
                    if (!flag4Lines.isEmpty())
                        batch.add(flag4Lines.get(0));
                    returnList.add(batch);
                }
                continue;
            }

            if (flag3Lines.size() > 1) {
                for (Integer flag3Line : flag3Lines) {
                    List<Integer> batch = new ArrayList<>();
                    batch.add(flag1Line);

                    if (flag2Lines.isEmpty())
                        throw new CustomException(new Json(JsonMessage.PARAM_INVALID, String.format("第%d行所在进路缺少2标志位，无法解析", flag1Line)));
                    batch.add(flag2Lines.get(0));

                    batch.add(flag3Line);
                    if (!flag4Lines.isEmpty())
                        batch.add(flag4Lines.get(0));
                    returnList.add(batch);
                }
            }
        }
    }


    /**
     * 生成lkj数据
     *
     * @param semaphoresFromExcel key:行数，value：excel中的封装信号机
     * @param indexGroups         分好组的行数，如：[[4, 6, 8], [11, 13, 15]]，其中4,6,8会生成两个lkj数，主要根据excel信号机和这个行数生成lkj
     * @param excelRowInfos       ExcelRowInfo为一行excel，excelRowInfos为这页所有的excel行集合
     */
    private List<LkjDataLineFromExcel> getLkjDataExcel(
            Map<Integer, SemaphoreFromExcel> semaphoresFromExcel, List<List<Integer>> indexGroups, List<ExcelRowInfo> excelRowInfos) throws CustomException {
        List<LkjDataLineFromExcel> results = new ArrayList<>();

        // batchIndexes为同一批次下的多个Lkj数据使用的信号机下标
        // 遍历分组行数，如：indexGroups为[[4, 6, 8], [11, 13, 15]] -> batchIndexes为[4,6,8]，能生成两个lkj数
        for (List<Integer> batchIndexes : indexGroups) {
            List<LkjDataLineFromExcel> batches = new ArrayList<>();
            // 批次序号分组，如[4,6,8] -> [[4,6],[6,8]]
            List<List<Integer>> indexList = groupIndex(batchIndexes);
            // indexes为一条lkj数据使用使用的excel行数(4,6)
            for (List<Integer> indexes : indexList) {
                Integer headLineNumber = indexes.get(0); // 起始信号机行号

                // 区分正向反向
                LkjDataLineFromExcel lkjDataLineFromExcel = new LkjDataLineFromExcel();
                setDownriverANDRetrograde(lkjDataLineFromExcel, excelRowInfos, headLineNumber);

                lkjDataLineFromExcel.setSeq(seq++); // 记录插入顺序，每组lkj数能区分开就行

                // 左节点行数
                int leftNodeLineNumber = headLineNumber;
                // 通过行数，从semaphoresFromExcel中获取信号机
                SemaphoreFromExcel leftNode = semaphoresFromExcel.get(leftNodeLineNumber);
                lkjDataLineFromExcel.setLeftNode(leftNode);

                // 右节点行数
                Integer rightNodeLineNumber = indexes.get(1);
                SemaphoreFromExcel rightNode = semaphoresFromExcel.get(rightNodeLineNumber);
                lkjDataLineFromExcel.setRightNode(rightNode);

                batches.add(lkjDataLineFromExcel);

                // 所属区间
                FragmentService fragmentService = (FragmentService) SpringContextUtil.getBean("fragmentService");
                String fragmentName = semaphoresFromExcel.get(indexList.get(0).get(0)).getFragmentName(); // 该区间第一条信号机所在区间名
                Fragment fragment = fragmentService.getFramentsByName(fragmentName);
                if (fragment == null)
                    throw new CustomException(new Json(JsonMessage.PARAM_INVALID, String.format("找不到第%d行中名为'%s'的区间", (leftNodeLineNumber + 1), fragmentName)));

                lkjDataLineFromExcel.setFragmentId(fragment.getId());

                // 距离
                String distanceStr = rightNode.getDistanceStr();
                if (StringUtils.isEmpty(distanceStr))
                    throw new CustomException(new Json(JsonMessage.PARAM_INVALID, "第" + (leftNodeLineNumber + 1) + "行缺少信号机间距离"));

                try {
                    double distance = Double.parseDouble(distanceStr);
                    lkjDataLineFromExcel.setDistance(distance);
                } catch (NumberFormatException n) {
                    throw new CustomException(new Json(JsonMessage.PARAM_INVALID, "第" + leftNodeLineNumber + "行距离格式错误"));
                }
            }

            // 暂时放一个uuid当唯一码，导入数据时，会在service中查询持久化的唯一码
            String temporaryUniqueCode = UUID.randomUUID().toString();
            batches.forEach(x -> x.setUniqueCode(temporaryUniqueCode));
            results.addAll(batches);
        }
        return results;
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

    private void setDownriverANDRetrograde(LkjDataLineFromExcel lkjDataLineFromExcel, List<ExcelRowInfo> excelRowInfos, int lineNumber) {
        for (int i = lineNumber; i > 0; i--) {
            List<String> cells = excelRowInfos.get(i).getCells();
            if (cells.stream().anyMatch(x -> x.contains(RETROGRADE_DOWNRIVER_STR))) {
                lkjDataLineFromExcel.setDownriver(LkjDataLine.DOWNRIVER);
                lkjDataLineFromExcel.setRetrograde(LkjDataLine.RETROGRADE);
                return;
            }
            if (cells.stream().anyMatch(x -> x.contains(RETROGRADE_UPRIVER_STR))) {
                lkjDataLineFromExcel.setDownriver(LkjDataLine.UPRIVER);
                lkjDataLineFromExcel.setRetrograde(LkjDataLine.RETROGRADE);
                return;
            }
            if (cells.stream().anyMatch(x -> x.contains(FORWARD_DOWNRIVER_STR))) {
                lkjDataLineFromExcel.setDownriver(LkjDataLine.DOWNRIVER);
                lkjDataLineFromExcel.setRetrograde(LkjDataLine.FORWARD);
                return;
            }
            if (cells.stream().anyMatch(x -> x.contains(FORWARD_UPRIVER_STR))) {
                lkjDataLineFromExcel.setDownriver(LkjDataLine.UPRIVER);
                lkjDataLineFromExcel.setRetrograde(LkjDataLine.FORWARD);
                return;
            }
        }
        lkjDataLineFromExcel.setDownriver(LkjDataLine.SEME_CLOSED_INTERVAL);
        lkjDataLineFromExcel.setRetrograde(LkjDataLine.SEME_CLOSED_INTERVAL);
    }
}

package com.yingda.lkj.service.impl.backstage.lkjdataline;

import com.yingda.lkj.beans.entity.backstage.line.Fragment;
import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.device.Semaphore;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.lkjdataline.LkjDataLineExcelService;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.excel.ExcelUtil;
import com.yingda.lkj.utils.excel.excelClient.LkjDataExcelParser;
import com.yingda.lkj.utils.pojo.CollectUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hood  2020/2/12
 */
@Service("lkjDataLineExcelYunnanServiceImpl")
public class LkjDataLineExcelYunnanServiceImpl implements LkjDataLineExcelService {

    @Autowired
    private BaseDao<Fragment> fragmentBaseDao;
    @Autowired
    private BaseDao<RailwayLine> railwayLineBaseDao;

    private CellStyle cellBorderStyle;
    private Workbook currentWorkbook;

    /**
     * 导出
     */
    public MultipartFile createWorkbook(List<LkjDataLine> lkjDataLines, List<Semaphore> semaphores) throws IOException {
        String fragmentId = lkjDataLines.stream().map(LkjDataLine::getFragmentId).reduce("", (x, y) -> y);
        Fragment fragment = fragmentBaseDao.get(Fragment.class, fragmentId);
        RailwayLine railwayLine = railwayLineBaseDao.get(RailwayLine.class, fragment.getRailwayLineId());


        currentWorkbook = new XSSFWorkbook();
        if (lkjDataLines.isEmpty())
            return ExcelUtil.workbook2File(currentWorkbook, "暂无数据");

        cellBorderStyle = currentWorkbook.createCellStyle();
        cellBorderStyle.setBorderTop(BorderStyle.THIN);//上边框
        cellBorderStyle.setBorderRight(BorderStyle.THIN);//右边框
        cellBorderStyle.setBorderBottom(BorderStyle.THIN); //下边框
        cellBorderStyle.setBorderLeft(BorderStyle.THIN);//左边框
        cellBorderStyle.setAlignment(HorizontalAlignment.CENTER);


        // 上行，下行，和半自闭的要分开，放到不同的sheet中
        List<Semaphore> downRivers = semaphores.stream().filter(x -> Semaphore.DOWNRIVER.equals(x.getDownriver())).collect(Collectors.toList()); // 下行的
        List<Semaphore> upRivers = semaphores.stream().filter(x -> Semaphore.UPRIVER.equals(x.getDownriver())).collect(Collectors.toList()); // 上行的
        List<Semaphore> semeClosedIntervals =
                semaphores.stream().filter(x -> Semaphore.SEME_CLOSED_INTERVAL.equals(x.getDownriver())).collect(Collectors.toList()); // 半自闭

        if (!downRivers.isEmpty()) {
            Sheet downRiverSheet = currentWorkbook.createSheet(Semaphore.DOWNRIVER);
            fillSheetInfo(downRiverSheet, Semaphore.DOWNRIVER, downRivers);
        }


        if (!upRivers.isEmpty()) {
            Sheet upRiverSheet = currentWorkbook.createSheet(Semaphore.UPRIVER);
            fillSheetInfo(upRiverSheet, Semaphore.UPRIVER, upRivers);
        }


        if (!semeClosedIntervals.isEmpty()) {
            Sheet semeClosedIntervalSheet = currentWorkbook.createSheet(Semaphore.SEME_CLOSED_INTERVAL);
            fillSheetInfo(semeClosedIntervalSheet, Semaphore.SEME_CLOSED_INTERVAL, semeClosedIntervals);
        }

        return ExcelUtil.workbook2File(currentWorkbook, railwayLine.getName());
    }

    /**
     * 填写一页lkj信息
     *
     * @param downRiver 站内下行线 / 站内上行线 / ""
     */
    private void fillSheetInfo(Sheet sheet, String downRiver, List<Semaphore> semaphores) {
        if (semaphores.isEmpty())
            return;

        List<Semaphore> forward = semaphores.stream().filter(x -> Semaphore.FORWARD.equals(x.getRetrograde())).collect(Collectors.toList()); // 正向的
        List<Semaphore> retrograde = semaphores.stream().filter(x -> Semaphore.RETROGRADE.equals(x.getRetrograde())).collect(Collectors.toList()); // 逆向的
        List<Semaphore> semeClosedIntervals =
                semaphores.stream().filter(x -> Semaphore.SEME_CLOSED_INTERVAL.equals(x.getRetrograde())).collect(Collectors.toList()); // 半自闭

        int lineNumber = 0;

        // 首行空行
        setRow(sheet, lineNumber++, null, "");

        // 行别
        setRow(sheet, lineNumber++, null, downRiver);

        lineNumber = writeSemaphores(sheet, lineNumber, forward);
        lineNumber = writeSemaphores(sheet, lineNumber, retrograde);
        writeSemaphores(sheet, lineNumber, semeClosedIntervals);
    }


    private int writeSemaphores(Sheet sheet, int lineNumber, List<Semaphore> semaphores) {
        // 正向
        if (semaphores.isEmpty())
            return lineNumber;

        Font font = currentWorkbook.createFont();
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 18);
        CellStyle style = currentWorkbook.createCellStyle();
        style.setFont(font);
        setRow(sheet, lineNumber++, style, Map.of(1, "信号机和轨道电路分布表（" + semaphores.get(0).getRetrograde() + "）"));


        // 副标题(Lkj数-14)
        setRow(sheet, lineNumber++, null, Map.of(12, "LKJ数-14"));
        setRow(sheet, lineNumber++, cellBorderStyle, "局名", "局编号", "线名", "线编号", "站内/区间", "行别", "车站", "信号机编号",
                "信号机间距离(m)", "信号机位置(km)", "信号机类型", "轨道电路制式", "中心频率", "UM71类别", "闭塞方式", "修改标注", "标识位");

        List<List<Semaphore>> forwardGroup = CollectUtils.groupList(Semaphore::getUniqueCode, semaphores);

        // yndianwuduan,wcnm
        // TODO 必须重构
        forwardGroup = group122222List(forwardGroup);
        forwardGroup = group122223List(forwardGroup);
        forwardGroup = group123333List(forwardGroup);

        for (List<Semaphore> groupItem : forwardGroup) {
            Fragment fragment = fragmentBaseDao.get(Fragment.class, groupItem.get(0).getFragmentId());

            for (int i = 0; i < groupItem.size(); i++) {
                Semaphore semaphore = groupItem.get(i);
                String flag = semaphore.getFlag();
                if (StringUtils.isEmpty(flag))
                    flag = Fragment.STATION == fragment.getFragmentType() ? (i + 1) + "" : "↑";
                setRow(sheet, lineNumber++, cellBorderStyle,
                        semaphore.getBureauName(), // 局名
                        semaphore.getBureauCode(), // 局编号
                        semaphore.getRailwayLineName(), // 线名
                        semaphore.getRailwayLineCode(), // 线编号
                        semaphore.getFragmentName(), // 站内/区间
                        semaphore.getDownriver(), // 行别
                        semaphore.getStationName(), // 车站
                        semaphore.getCode(), // 信号机编号
                        semaphore.getDistanceStr(), // 距离
                        semaphore.getPosition(), // 信号机位置(km)
                        semaphore.getSemaphoreType(), // 信号机类型
                        semaphore.getTrackSystem(), // 轨道电路制式
                        semaphore.getCenterFrequency(), // 中心频率
                        semaphore.getUM71Type(), // UM71类别
                        semaphore.getOcclusionMethod(), // 闭塞方式
                        semaphore.getRemark(), // 修改标注
                        flag
                );
            }
        }
        return lineNumber;
    }

    /**
     * 如果semphore中flag
     * @param forwardGroup
     * @return
     */
    private List<List<Semaphore>> group122222List(List<List<Semaphore>> forwardGroup) {
        // key:头尾信号机id拼起来
        List<List<Semaphore>> returnList = new LinkedList<>();

        for (List<Semaphore> semaphores : forwardGroup) {
            if (returnList.isEmpty()) {
                returnList.add(semaphores);
                continue;
            }

            // 队尾list，如果符合条件，合并
            List<Semaphore> rear = returnList.get(returnList.size() - 1);
            // 队尾list的第一个,第二个,倒数第一，第二个信号机
            Semaphore rearFirstSemaphore = rear.get(0);
            Semaphore rearSecondSemaphore = rear.get(1);
            Semaphore rearPenultimateSemaphore = rear.get(rear.size() - 2);
            Semaphore rearLastSemaphore = rear.get(rear.size() - 1);
            // 如果队尾是长度为3，第一个第二个相同的的lkj合成的，不做处理
            if ("2".equals(rearFirstSemaphore.getFlag())
                    && "2".equals(rearSecondSemaphore.getFlag())) {
                returnList.add(semaphores);
                continue;
            }
            // 如果队尾是长度为3，头尾相同的的lkj合成的，不做处理
            if ("2".equals(rearPenultimateSemaphore.getFlag())
                    && "3".equals(rearLastSemaphore.getFlag())) {
                returnList.add(semaphores);
                continue;
            }

            if (semaphores.size() == 2) {
                String leftDeviceId = rear.get(0).getDeviceId();
                if (leftDeviceId.equals(semaphores.get(0).getDeviceId())) {
                    Semaphore returnSemaphore = semaphores.get(1);
                    returnSemaphore.setFlag("2");
                    rear.add(returnSemaphore);
                    continue;
                }
            }

            returnList.add(semaphores);
        }
        return returnList;
    }

    /**
     * 如果semphore中flag
     * @param forwardGroup
     * @return
     */
    private List<List<Semaphore>> group122223List(List<List<Semaphore>> forwardGroup) {
        // key:头尾信号机id拼起来
        List<List<Semaphore>> returnList = new LinkedList<>();

        for (List<Semaphore> semaphores : forwardGroup) {
            if (returnList.isEmpty()) {
                returnList.add(semaphores);
                continue;
            }

            // 队尾list，如果符合条件，合并
            List<Semaphore> rear = returnList.get(returnList.size() - 1);
            // 队尾list的第一个,第二个,倒数第一，第二个信号机
            Semaphore rearFirstSemaphore = rear.get(0);
            Semaphore rearSecondSemaphore = rear.get(1);
            Semaphore rearPenultimateSemaphore = rear.get(rear.size() - 2);
            Semaphore rearLastSemaphore = rear.get(rear.size() - 1);

            // 如果队尾是长度为2的lkj合成的，不做处理
            if ("2".equals(rearFirstSemaphore.getFlag())) {
                returnList.add(semaphores);
                continue;
            }
            // 如果队尾是长度为3，第一个第二个相同的的lkj合成的，不做处理
            if ("3".equals(rearPenultimateSemaphore.getFlag())
                    && "3".equals(rearLastSemaphore.getFlag())) {
                returnList.add(semaphores);
                continue;
            }

            if (semaphores.size() == 3) {

                // 当前判断的lkjGroup的第一个和最后一个信号机
                Semaphore itemFirstSemaphore = semaphores.get(0);
                Semaphore itemLastSemaphore = semaphores.get(2);

                if (rearFirstSemaphore.getDeviceId().equals(itemFirstSemaphore.getDeviceId()) &&
                    rearLastSemaphore.getDeviceId().equals(itemLastSemaphore.getDeviceId())) {
                    rearLastSemaphore.setFlag("3");
                    Semaphore element = semaphores.get(1);
                    element.setFlag("2");
                    rear.add(rear.size() - 1, element);
                    continue;
                }
            }

            returnList.add(semaphores);
        }
        return returnList;
    }
    /**
     * 如果semphore中flag
     * @param forwardGroup
     * @return
     */
    private List<List<Semaphore>> group123333List(List<List<Semaphore>> forwardGroup) {
        // key:头尾信号机id拼起来
        List<List<Semaphore>> returnList = new LinkedList<>();

        for (List<Semaphore> semaphores : forwardGroup) {
            if (returnList.isEmpty()) {
                returnList.add(semaphores);
                continue;
            }

            // 队尾list，如果符合条件，合并
            List<Semaphore> rear = returnList.get(returnList.size() - 1);
            // 队尾list的第一个,第二个,倒数第一，第二个信号机
            Semaphore rearFirstSemaphore = rear.get(0);
            Semaphore rearSecondSemaphore = rear.get(1);
            Semaphore rearPenultimateSemaphore = rear.get(rear.size() - 2);
            Semaphore rearLastSemaphore = rear.get(rear.size() - 1);

            // 如果队尾是长度为2的lkj合成的，不做处理
            if ("2".equals(rearLastSemaphore.getFlag())) {
                returnList.add(semaphores);
                continue;
            }
            // 如果队尾是长度为3，头尾相同的的lkj合成的，不做处理
            if ("2".equals(rearPenultimateSemaphore.getFlag())
                    && "3".equals(rearLastSemaphore.getFlag())) {
                returnList.add(semaphores);
                continue;
            }

            if (semaphores.size() == 3) {
                // 当前判断的lkjGroup的第一个和最后一个信号机
                Semaphore itemFirstSemaphore = semaphores.get(0);
                Semaphore itemSecondSemaphore = semaphores.get(1);

                if (rearFirstSemaphore.getDeviceId().equals(itemFirstSemaphore.getDeviceId()) &&
                        rearSecondSemaphore.getDeviceId().equals(itemSecondSemaphore.getDeviceId())) {
                    rearLastSemaphore.setFlag("3");
                    Semaphore element = semaphores.get(2);
                    element.setFlag("3");
                    rear.add(element);
                    continue;
                }
            }

            returnList.add(semaphores);
        }
        return returnList;
    }

    private void setRow(Sheet sheet, int lineNumber, CellStyle cellStyle, String... infos) {
        setRow(sheet, lineNumber, cellStyle, Arrays.asList(infos));
    }


    /**
     * @param infos key:列数，value:info
     */
    private void setRow(Sheet sheet, int lineNumber, CellStyle cellStyle, Map<Integer, String> infos) {
        List<String> infoList = new ArrayList<>();
        // 取最大的列
        List<Integer> columnNumbers = infos.keySet().stream().sorted().collect(Collectors.toList());
        int maxColumn = columnNumbers.get(columnNumbers.size() - 1) + 1;

        for (int i = 0; i < maxColumn; i++)
            infoList.add(Optional.ofNullable(infos.get(i)).orElse(""));

        setRow(sheet, lineNumber, cellStyle, infoList);
    }

    private void setRow(Sheet sheet, int lineNumber, CellStyle cellStyle, List<String> infos) {
        Row row = sheet.createRow(lineNumber);

        for (int i = 0; i < infos.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(infos.get(i));
            if (cellStyle != null)
                cell.setCellStyle(cellStyle);
        }

    }


}

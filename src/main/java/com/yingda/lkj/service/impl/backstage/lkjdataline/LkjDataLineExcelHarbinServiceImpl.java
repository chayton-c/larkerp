package com.yingda.lkj.service.impl.backstage.lkjdataline;

import com.yingda.lkj.beans.entity.backstage.line.Fragment;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.pojo.device.Semaphore;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.lkjdataline.LkjDataLineExcelService;
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
@Service("lkjDataLineExcelHarbinServiceImpl")
public class LkjDataLineExcelHarbinServiceImpl implements LkjDataLineExcelService {

    @Autowired
    private BaseDao<Fragment> fragmentBaseDao;

    private CellStyle cellBorderStyle;
    private Workbook currentWorkbook;

    /**
     * 导出
     */
    public MultipartFile createWorkbook(List<LkjDataLine> lkjDataLines, List<Semaphore> semaphores) throws IOException {
        String fragmentId = lkjDataLines.stream().map(LkjDataLine::getFragmentId).reduce("", (x, y) -> y);
        Fragment fragment = fragmentBaseDao.get(Fragment.class, fragmentId);

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

        return ExcelUtil.workbook2File(currentWorkbook, fragment.getName());
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
        setRow(sheet, lineNumber++, cellBorderStyle, "局名", "局编号", "线名", "线编号", "行别", "车站", "信号机编号",
                "信号机间距离(m)", "信号机位置(km)", "信号机类型", "轨道电路制式", "中心频率", "UM71类别", "闭塞方式", "修改标注");

        List<List<Semaphore>> forwardGroup = CollectUtils.groupList(Semaphore::getUniqueCode, semaphores);

        for (List<Semaphore> groupItem : forwardGroup) {
            for (int i = 0; i < groupItem.size(); i++) {
                Semaphore semaphore = groupItem.get(i);
                if (i > 0) {
                    setRow(sheet, lineNumber++, cellBorderStyle, Map.of(LkjDataExcelParser.DISTANCE_INDEX, semaphore.getDistanceStr(), 14, "")); // 距离行
                    CellRangeAddress region = new CellRangeAddress(lineNumber - 1, lineNumber, LkjDataExcelParser.DISTANCE_INDEX,  LkjDataExcelParser.DISTANCE_INDEX);
                    sheet.addMergedRegion(region);
                }

                setRow(sheet, lineNumber++, cellBorderStyle,
                        semaphore.getBureauName(), // 局名
                        semaphore.getBureauCode(), // 局编号
                        semaphore.getRailwayLineName(), // 线名
                        semaphore.getRailwayLineCode(), // 线编号
                        semaphore.getDownriver(), // 行别
                        semaphore.getStationName(), // 车站
                        semaphore.getCode(), // 信号机编号
                        "", // 距离空行
                        semaphore.getPosition(), // 信号机位置(km)
                        semaphore.getSemaphoreType(), // 信号机类型
                        semaphore.getTrackSystem(), // 轨道电路制式
                        semaphore.getCenterFrequency(), // 中心频率
                        semaphore.getUM71Type(), // UM71类别
                        semaphore.getOcclusionMethod(), // 闭塞方式
                        semaphore.getRemark() // 修改标注
                );

                for (int j = 0; j < 15; j++) {
                    if (j == LkjDataExcelParser.DISTANCE_INDEX)
                        continue;
                    CellRangeAddress region = new CellRangeAddress(lineNumber - 1, lineNumber, j, j);
                    sheet.addMergedRegion(region);
                }
            }

            // 不同组插入空行
            setRow(sheet, lineNumber++, cellBorderStyle, Map.of(14, ""));
            setRow(sheet, lineNumber++, null, "");
        }
        return lineNumber;
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

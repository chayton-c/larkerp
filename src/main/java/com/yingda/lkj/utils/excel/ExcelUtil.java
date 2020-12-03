package com.yingda.lkj.utils.excel;

import com.yingda.lkj.beans.pojo.utils.ExcelRowInfo;
import com.yingda.lkj.beans.pojo.utils.ExcelSheetInfo;
import com.yingda.lkj.utils.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * POI工具类
 */
public class ExcelUtil {

    // 扩展名
    public final static String XLS = "xls";
    public final static String XLSX = "xlsx";


    /**
     * * 读取excel文件
     *
     * @param excelFile excel文件
     * @return
     * @throws IOException
     */
    public static List<ExcelSheetInfo> readExcelFile(MultipartFile excelFile) throws IOException {
        // 检查文件
        checkFile(excelFile);
        // 获得工作簿对象
        Workbook workbook = getWorkBook(excelFile);
        // 创建返回对象，把每行中的值作为一个数组，所有的行作为一个集合返回
        List<ExcelSheetInfo> excelSheetInfos = new ArrayList<>();
        if (workbook == null)
            return new ArrayList<>();

        for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
            // 获取当前sheet工作表
            Sheet sheet = workbook.getSheetAt(sheetNum);
            if (sheet == null)
                continue;
            String sheetName = sheet.getSheetName();

            List<ExcelRowInfo> excelRowInfos = getExcelRowInfo(sheet);
            Map<Integer, ExcelRowInfo> rowInfoMap = excelRowInfos.stream().collect(Collectors.toMap(ExcelRowInfo::getLineNumber, x -> x));

            excelSheetInfos.add(new ExcelSheetInfo(sheetName, rowInfoMap));
        }
        return excelSheetInfos;
    }

    private static List<ExcelRowInfo> getExcelRowInfo(Sheet sheet) {
        List<ExcelRowInfo> excelRowInfos = new ArrayList<>();

        int lastRowNum = sheet.getLastRowNum();
        for (int lineNumber = 0; lineNumber <= lastRowNum; lineNumber++) {
            // 获得当前行
            Row row = sheet.getRow(lineNumber);
            if (row == null) {
                excelRowInfos.add(new ExcelRowInfo(lineNumber, Collections.singletonList("")));
                continue;
            }

            // 获得当前行的开始列
            int firstCellNum = row.getFirstCellNum();
            // 获得当前行的列数
            int lastCellNum = row.getPhysicalNumberOfCells();
            String[] cells = new String[lastCellNum + 1];
            // 循环当前行
            for (int cellNum = firstCellNum; cellNum <= lastCellNum; cellNum++) {
                if (cellNum == -1)
                    continue;

                Cell cell = row.getCell(cellNum);
                cells[cellNum] = getCellValue(cell);
            }
            excelRowInfos.add(new ExcelRowInfo(lineNumber, Arrays.asList(cells)));
        }

        return excelRowInfos;
    }

    /**
     * 生成excel文件
     */
    public static Workbook createExcelFile(List<ExcelSheetInfo> excelSheetInfos) {
        // 1. 创建workbook
        Workbook workbook = new XSSFWorkbook();

        for (ExcelSheetInfo excelSheetInfo : excelSheetInfos) {
            Map<Integer, ExcelRowInfo> rowInfoMap = excelSheetInfo.getRowInfoMap();
            var values = rowInfoMap.values();
            List<List<String>> data = values.stream().map(ExcelRowInfo::getCells).collect(Collectors.toList());

            // 创建sheet
            String sheetName = excelSheetInfo.getSheetName();
            Sheet sheet = workbook.createSheet(sheetName);

            // 插入数据
            for (int i = 0; i < data.size(); i++) {
                List<String> rowInfo = data.get(i);
                Row row = sheet.createRow(i + 1);
                // 添加数据
                for (int j = 0; j < rowInfo.size(); j++)
                    row.createCell(j).setCellValue(rowInfo.get(j));
            }
        }


        return workbook;
    }

    public static MultipartFile workbook2File(Workbook workbook, String fileName) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return new MockMultipartFile(fileName, new ByteArrayInputStream(outputStream.toByteArray()));
    }

    /**
     * 获取当前列数据
     *
     * @param cell 列
     * @return 列值
     */
    private static String getCellValue(Cell cell) {
        String cellValue = "";

        if (cell == null)
            return cellValue;

        // 把数字当成String来读，避免出现1读成1.0的情况
        if (cell.getCellType() == CellType.NUMERIC)
            cell.setCellType(CellType.STRING);
        // 判断数据的类型
        switch (cell.getCellType()) {
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case STRING:
                return String.valueOf(cell.getStringCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return "";
//                    cellValue = String.valueOf(cell.getStringCellValue());
                }
            case BLANK:
                return "";
            case ERROR:
                return "非法字符";
            default:
                return "未知类型";
        }
    }


    /**
     * 获得工作簿对象
     *
     * @param excelFile excel文件
     * @return 工作簿对象
     */
    public static Workbook getWorkBook(MultipartFile excelFile) {
        // 获得文件名
        String fileName = excelFile.getOriginalFilename();
        // 创建Workbook工作簿对象，表示整个excel
        Workbook workbook = null;
        try {
            // 获得excel文件的io流
            InputStream is = excelFile.getInputStream();
            // 根据文件后缀名不同(xls和xlsx)获得不同的workbook实现类对象
            if (fileName.endsWith(XLS)) {
                // 2003版本
                workbook = new HSSFWorkbook(is);
            } else if (fileName.endsWith(XLSX)) {
                // 2007版本
                workbook = new XSSFWorkbook(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return workbook;
    }

    /**
     * 检查文件
     *
     * @param excelFile excel文件
     */
    private static void checkFile(MultipartFile excelFile) throws IOException {
        //判断文件是否存在
        if (null == excelFile) {
            throw new FileNotFoundException("文件不存在");
        }
        //获得文件名
        String fileName = excelFile.getOriginalFilename();
        //判断文件是否是excel文件
        if (!fileName.endsWith(XLS) && !fileName.endsWith(XLSX)) {
            throw new IOException(fileName + "不是excel文件");
        }
    }
}
package com.yingda.lkj.utils.excel.excelClient;

import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.device.Semaphore;
import com.yingda.lkj.beans.pojo.lkj.LkjDataLineFromExcel;
import com.yingda.lkj.beans.pojo.utils.ExcelSheetInfo;
import com.yingda.lkj.service.backstage.lkjdataline.LkjDataLineExcelService;
import com.yingda.lkj.utils.SpringContextUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author hood  2020/5/10
 */
public class LkjDataExcelParser {

    /** 距离数据所在列 */
    public static final Integer DISTANCE_INDEX = 7;

    private String bureauCode; // 局编码，根据局编码的不同，调用不同的解析策略

    public LkjDataExcelParser(String bureauCode) {
        this.bureauCode = bureauCode;
    }

    /**
     * excel中解析LkjDataLineFromExcel
     */
    public List<LkjDataLineFromExcel> getLkjDataLineFromExcel(List<ExcelSheetInfo> excelSheetInfos) throws CustomException {
        // TODO 这里应该写局策略，没时间先这样了
        if (bureauCode.equals("01") || bureauCode.equals("1")) // 哈局策略
            return new LkjExcelParser4Harbin(bureauCode).getLkjDataLineFromExcel(excelSheetInfos);
        else // 云南，广州局
            return new LkjExcelParser4Yunnan(bureauCode).getLkjDataLineFromExcel(excelSheetInfos);
    }

    public MultipartFile createWorkbook(List<LkjDataLine> lkjDataLines, List<Semaphore> semaphores) throws IOException {
        LkjDataLineExcelService lkjDataLineExcelHarbinServiceImpl =
                (LkjDataLineExcelService) SpringContextUtil.getBean("lkjDataLineExcelHarbinServiceImpl");
        LkjDataLineExcelService lkjDataLineExcelYunnanServiceImpl =
                (LkjDataLineExcelService) SpringContextUtil.getBean("lkjDataLineExcelYunnanServiceImpl");

        if (bureauCode.equals("01") || bureauCode.equals("1")) // 哈局策略
            return lkjDataLineExcelHarbinServiceImpl.createWorkbook(lkjDataLines, semaphores);
        else
//            return lkjDataLineExcelHarbinServiceImpl.createWorkbook(lkjDataLines, semaphores);
            return lkjDataLineExcelYunnanServiceImpl.createWorkbook(lkjDataLines, semaphores);
    }

    /**
     * 获取导入lkj模板
     */
    public MultipartFile getLkjImportTemplate() {
        ClassPathResource templateFileResource = null;
        if (bureauCode.equals("01") || bureauCode.equals("1")) // 哈局策略
            templateFileResource = new ClassPathResource("/static/uploadimg/semaphoresTemplate4Harbin.xlsx");
        else // 哈局策略
            templateFileResource = new ClassPathResource("/static/uploadimg/semaphoresTemplate4Yunnan.xlsx");

        File templateFile = null;
        try {
            templateFile = templateFileResource.getFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileInputStream fileInputStream = new FileInputStream(templateFile)) {
            return new MockMultipartFile(templateFile.getName(), fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

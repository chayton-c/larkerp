package com.yingda.lkj.service.backstage.lkjdataline;

import com.yingda.lkj.beans.entity.backstage.line.Fragment;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.pojo.device.Semaphore;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author hood  2020/2/12
 */
public interface LkjDataLineExcelService {
    MultipartFile createWorkbook(List<LkjDataLine> lkjDataLines, List<Semaphore> semaphores) throws IOException;
}

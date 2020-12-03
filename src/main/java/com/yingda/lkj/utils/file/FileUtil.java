package com.yingda.lkj.utils.file;

import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author hood  2020/6/5
 */
public class FileUtil {
    /**
     *
     * @param filePath eg: "/static/uploadimg/lineExcelTemplate.xlsx"
     */
    public static MultipartFile readFiles(String filePath) throws CustomException {
        ClassPathResource excelTemplate = new ClassPathResource(filePath);
        try (FileInputStream fileInputStream = new FileInputStream(excelTemplate.getFile())){
            return new MockMultipartFile("线路模板", fileInputStream.readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new CustomException(new Json(JsonMessage.SYS_ERROR));
    }
}

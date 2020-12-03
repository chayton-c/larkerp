package com.yingda.lkj.utils.file;

import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import org.springframework.util.ClassUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author hood  2020/1/9
 */
public class UploadUtil {
    private static String uploadPath = ClassUtils.getDefaultClassLoader().getResource("static/uploadimg").getPath();

    public static String saveToUploadPath(MultipartFile rawFile) throws CustomException, IOException {
        File baseDir = new File(uploadPath);
        if (!baseDir.exists())
            throw new CustomException(new Json(JsonMessage.SYS_ERROR, "服务器上传目录设置不正确，请联系管理员检查 config.properties.upload_path"));

        // 月目录
        String month = new SimpleDateFormat("yyyy_MM").format(new Date());
        File monthDir = new File(baseDir, month);
        if (!monthDir.exists())
            monthDir.mkdir();

        // 日目录
        String day = new SimpleDateFormat("dd").format(new Date());
        File uploadDir = new File(monthDir, day);
        if (!uploadDir.exists())
            uploadDir.mkdir();

        // 源文件后缀
        String originalFilename = rawFile.getOriginalFilename();
        String uploadFileSuffix = originalFilename.split("\\.")[1];

        // 生成文件
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + uploadFileSuffix;
        File uploadFile = new File(uploadDir, fileName);
        uploadFile.createNewFile();

        // 读取源文件到生成的文件
        rawFile.transferTo(uploadFile);

        return "/uploadimg/" + month + "/" + day + "/" + fileName;
    }

    public static String saveToUploadPath(byte[] rawData, String originalFilename) throws CustomException, IOException {
        File baseDir = new File(uploadPath);
        if (!baseDir.exists())
            throw new CustomException(new Json(JsonMessage.SYS_ERROR, "服务器上传目录设置不正确，请联系管理员检查 config.properties.upload_path"));

        // 月目录
        String month = new SimpleDateFormat("yyyy_MM").format(new Date());
        File monthDir = new File(baseDir, month);
        if (!monthDir.exists())
            monthDir.mkdir();

        // 日目录
        String day = new SimpleDateFormat("dd").format(new Date());
        File uploadDir = new File(monthDir, day);
        if (!uploadDir.exists())
            uploadDir.mkdir();

        // 源文件后缀
        String uploadFileSuffix = getAppUploadImageSuffix(originalFilename);

        // 生成文件
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + uploadFileSuffix;
        File uploadFile = new File(uploadDir, fileName);
        uploadFile.createNewFile();

        // 读取源文件到生成的文件
        new FileOutputStream(uploadFile).write(rawData);

        return "/uploadimg/" + month + "/" + day + "/" + fileName;
    }

    public static String getAppUploadImageSuffix(String fileName) {
        fileName = getImageName(fileName);
        return fileName.split("\\.")[1];
    }

    public static String getAppUploadImageFileName(String fileName) {
        fileName = getImageName(fileName);
        return fileName.split("\\.")[0];
    }

    public static String saveToUploadPath(MultipartFile rawFile, String filePath, String fileName) throws CustomException, IOException {
        File baseDir = new File(uploadPath);
        if (!baseDir.exists())
            throw new CustomException(new Json(JsonMessage.SYS_ERROR, "服务器上传目录设置不正确，请联系管理员检查 config.properties.upload_path"));

        // 月目录
        File uploadDir = new File(baseDir, filePath);
        if (!uploadDir.exists())
            uploadDir.mkdir();

        File uploadFile = new File(uploadDir, fileName);
        if (!uploadFile.exists())
            uploadFile.mkdir();

        // 读取源文件到生成的文件
        rawFile.transferTo(uploadFile);

        return filePath + "/" + fileName;
    }

    private static String getImageName(String rawPictureName) {
        String[] rawPictureNameArr = rawPictureName.split("/");
        return rawPictureNameArr[rawPictureNameArr.length - 1];
    }

}

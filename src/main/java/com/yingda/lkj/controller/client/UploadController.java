package com.yingda.lkj.controller.client;

import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.utils.file.UploadUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * @author hood  2020/1/9
 */
@Controller
@RequestMapping("/client/upload")
public class UploadController {

    @RequestMapping("")
    @ResponseBody
    public Json upload(MultipartFile file) throws IOException, CustomException {
        String path = UploadUtil.saveToUploadPath(file);
        return new Json(JsonMessage.SUCCESS, Map.of("path", path), "");
    }

}

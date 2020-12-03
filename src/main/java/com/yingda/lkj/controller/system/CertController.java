package com.yingda.lkj.controller.system;

import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.utils.LicensingUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * @author hood  2020/5/6
 */
@Controller
@RequestMapping("/auth")
public class CertController extends BaseController {

    @RequestMapping("/cert")
    public ModelAndView certUpload() {
        String errorMsg = req.getParameter("errorMsg");
        String machineCode = LicensingUtil.getSystemCode();
        return new ModelAndView("/upload-cert", Map.of("errorMsg", errorMsg, "machineCode", machineCode));
    }

    @RequestMapping("/certUpload")
    @ResponseBody
    public Json uploadCert(MultipartFile file)  {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".") || !"cert".equals(originalFilename.split("\\.")[1]))
            return new Json(JsonMessage.PARAM_INVALID, "选择的文件中包含不支持的格式");

        ClassPathResource certificate = new ClassPathResource("/static/uploadimg/akagi.cert");
        try {
            File cert = certificate.getFile();
            if (!cert.exists())
                cert.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileWriter fileWriter = new FileWriter(certificate.getFile(), false)) {
            String certInfo = new String(file.getInputStream().readAllBytes());
            fileWriter.write(certInfo);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Json validate = LicensingUtil.validate();
        if (!validate.isSuccess())
            return validate;

        return new Json(JsonMessage.SUCCESS);
    }
}

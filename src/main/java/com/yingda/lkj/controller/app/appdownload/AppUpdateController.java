package com.yingda.lkj.controller.app.appdownload;

import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.utils.file.UploadUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

/**
 * @author hood  2020/8/11
 */
@RequestMapping("/app/update")
@Controller
public class AppUpdateController extends BaseController {

    private static final String APP_UPLOAD_PATH = "app";
    private static final String APP_UPLOAD_NAME = "powerPlant.apk";

    @RequestMapping("/uploadPage")
    public ModelAndView uploadPage() {
        return new ModelAndView("/backstage/app/app-upload");
    }

    @RequestMapping("/upload")
    @ResponseBody
    public Json upload(MultipartFile file) throws Exception {
        UploadUtil.saveToUploadPath(file, APP_UPLOAD_PATH, APP_UPLOAD_NAME);
        return new Json(JsonMessage.SUCCESS);
    }
}

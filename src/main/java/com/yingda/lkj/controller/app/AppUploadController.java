package com.yingda.lkj.controller.app;

import com.yingda.lkj.beans.entity.system.UploadImage;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.file.UploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author hood  2020/6/25
 */
@Controller
@RequestMapping("/app/upload")
public class AppUploadController extends BaseController {

    @Autowired
    private BaseService<UploadImage> uploadImageBaseService;

    @RequestMapping("/uploadImage")
    @ResponseBody
    public Json uploadImage() throws Exception {
        String base64Image = req.getParameter("file");
        byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image);

        String fileName = req.getParameter("fileName"); // fileName使用uuid拼成的
        String path = UploadUtil.saveToUploadPath(imageBytes, fileName);
        fileName = UploadUtil.getAppUploadImageFileName(fileName);

        UploadImage uploadImage = new UploadImage();
        uploadImage.setId(fileName);
        uploadImage.setUrl(path);
        uploadImage.setAddTime(current());
        uploadImageBaseService.saveOrUpdate(uploadImage);

        return new Json(JsonMessage.SUCCESS);
    }
}

package com.yingda.lkj.controller.init;

import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.system.I18NService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * @author hood  2020/11/10
 */
@RequestMapping("/init/i18N")
@Controller
public class I18NController extends BaseController {
    @Autowired
    private I18NService i18NService;

    @RequestMapping("")
    @ResponseBody
    public Map<String, String> i18N() {
        Map<String, String> cn = i18NService.getCN();
        return cn;
    }
}

package com.yingda.lkj.controller.client;

import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author hood  2020/1/31
 */
@Controller
@RequestMapping("/appTest")
public class AppTestController extends BaseController {

    @GetMapping("/get")
    @ResponseBody
    public Json get() {
        return new Json(JsonMessage.SUCCESS, req.getParameterMap());
    }

    @PostMapping("/post")
    @ResponseBody
    public Json post() {
        return new Json(JsonMessage.SUCCESS, req.getParameterMap());
    }

}

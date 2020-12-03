package com.yingda.lkj.controller.system;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hood  2020/11/11
 */
@RequestMapping("/chart")
@Controller
public class ChartController {

    @RequestMapping("")
    @ResponseBody
    public Map<String, Object> demo() {
        Map<String, Object> attributes = new HashMap<>();


        return attributes;
    }

}

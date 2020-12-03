package com.yingda.lkj.controller.system;

import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.service.backstage.measurement.MeasurementTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author hood  2020/8/6
 */
@Controller
@RequestMapping("/test")
public class TimerTestController {
    @Autowired
    private MeasurementTaskService measurementTaskService;

    @RequestMapping("/checkMissedMeasurementTask")
    @ResponseBody
    public Json checkMissedMeasurementTask() {
        System.out.println("checkMissedMeasurementTask");
        measurementTaskService.checkMissed();
        return new Json(JsonMessage.SUCCESS);
    }
}

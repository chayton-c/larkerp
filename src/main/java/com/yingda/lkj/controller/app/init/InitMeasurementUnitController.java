package com.yingda.lkj.controller.app.init;

import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementUnit;
import com.yingda.lkj.beans.pojo.app.AppMeasurementUnit;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.service.base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 获取测量项
 * @author hood  2020/4/14
 */
@Controller
@RequestMapping("/app/init/measurementUnit")
public class InitMeasurementUnitController {

    @Autowired
    private BaseService<MeasurementUnit> measurementUnitBaseService;

    @RequestMapping("")
    @ResponseBody
    public Json getData() throws Exception {
        List<AppMeasurementUnit> appMeasurementUnits = measurementUnitBaseService.getAllObjects(MeasurementUnit.class).stream()
                .map(AppMeasurementUnit::new).collect(Collectors.toList());

        return new Json(JsonMessage.SUCCESS, appMeasurementUnits);
    }
}

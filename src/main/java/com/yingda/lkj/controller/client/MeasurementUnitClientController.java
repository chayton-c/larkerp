package com.yingda.lkj.controller.client;

import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementUnit;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/5/30
 */
@Controller
@RequestMapping("/client/measurementUnitClient")
public class MeasurementUnitClientController extends BaseController {

    @Autowired
    private BaseService<MeasurementUnit> measurementUnitBaseService;

    @RequestMapping("/getMeasurementUnitsByGroupName")
    @ResponseBody
    public Json getMeasurementUnitsByGroupName() throws Exception {
        String groupName = req.getParameter("groupName");

        List<MeasurementUnit> measurementUnits = measurementUnitBaseService.find(
                "from MeasurementUnit where groupName = :groupName",
                Map.of("groupName", groupName)
        );

        return new Json(JsonMessage.SUCCESS, measurementUnits);
    }

}

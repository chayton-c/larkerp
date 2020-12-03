package com.yingda.lkj.controller.client;

import com.yingda.lkj.beans.entity.backstage.device.DeviceMaintenancePlan;
import com.yingda.lkj.beans.entity.backstage.device.DeviceMeasurementItem;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementUnit;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.device.DeviceMeasurementItemService;
import com.yingda.lkj.service.backstage.measurement.MeasurementUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author hood  2020/6/15
 */
@RequestMapping("/client/deviceMeasurementItem")
@Controller
public class DeviceMeasurementItemClientController extends BaseController {
    @Autowired
    private DeviceMeasurementItemService deviceMeasurementItemService;
    @Autowired
    private MeasurementUnitService measurementUnitService;

    @RequestMapping("/getByDeviceTypeId")
    @ResponseBody
    public Json getByDeviceTypeId() {
        String deviceTypeId = req.getParameter("deviceTypeId");
        List<DeviceMeasurementItem> deviceMeasurementItems = deviceMeasurementItemService.getByDeviceTypeId(deviceTypeId);
        return new Json(JsonMessage.SUCCESS, deviceMeasurementItems);
    }

    @RequestMapping("/getById")
    @ResponseBody
    public Json getById() {
        String id = req.getParameter("id");
        DeviceMeasurementItem deviceMeasurementItem = deviceMeasurementItemService.getById(id);
        MeasurementUnit measurementUnit = measurementUnitService.getById(deviceMeasurementItem.getMeasurementUnitId());
        deviceMeasurementItem.setMeasurementUnit(measurementUnit);

        return new Json(JsonMessage.SUCCESS, deviceMeasurementItem);
    }
}

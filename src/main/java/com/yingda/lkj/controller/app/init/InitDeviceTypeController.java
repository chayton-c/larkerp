package com.yingda.lkj.controller.app.init;

import com.yingda.lkj.beans.entity.backstage.device.DeviceMeasurementItem;
import com.yingda.lkj.beans.entity.backstage.device.DeviceType;
import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.pojo.app.AppDeviceMeasurementItem;
import com.yingda.lkj.beans.pojo.app.AppDeviceType;
import com.yingda.lkj.beans.pojo.app.AppRailwayLine;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.service.backstage.device.DeviceMeasurementItemService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author hood  2020/4/13
 */
@Controller
@RequestMapping("/app/init/deviceType")
public class InitDeviceTypeController {
    @Autowired
    private BaseService<DeviceType> deviceTypeBaseService;
    @Autowired
    private DeviceMeasurementItemService deviceMeasurementItemService;

    @RequestMapping("")
    @ResponseBody
    public Json getData() throws Exception {
        List<AppDeviceType> appDeviceTypes = deviceTypeBaseService.getAllObjects(DeviceType.class).stream().map(AppDeviceType::new).collect(Collectors.toList());

        for (AppDeviceType appDeviceType : appDeviceTypes) {
            String deviceTypeId = appDeviceType.getDeviceTypeId();
            List<DeviceMeasurementItem> deviceMeasurementItems = deviceMeasurementItemService.getByDeviceTypeId(deviceTypeId);

            if (deviceMeasurementItems.isEmpty()) {
                appDeviceType.setAppDeviceMeasurementItemListStr("");
                continue;
            }

            StringBuilder jsonStr = new StringBuilder("[");
//            List<AppDeviceMeasurementItem> appDeviceMeasurementItems = deviceMeasurementItems.stream().map(AppDeviceMeasurementItem::new).collect(Collectors.toList());
//            String.join(appDeviceMeasurementItems.stream().map())

            String collect =
                    deviceMeasurementItems.stream().map(AppDeviceMeasurementItem::new).map(AppDeviceMeasurementItem::toString).collect(Collectors.joining(","));
            jsonStr.append(collect);
            jsonStr.append("]");
            appDeviceType.setAppDeviceMeasurementItemListStr(jsonStr.toString());
        }

        return new Json(JsonMessage.SUCCESS, appDeviceTypes);
    }
}

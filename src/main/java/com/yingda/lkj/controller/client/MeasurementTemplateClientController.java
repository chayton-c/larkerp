package com.yingda.lkj.controller.client;

import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.backstage.device.DeviceSubType;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItem;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTemplate;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

/**
 * @author hood  2020/3/16
 */
@Controller
@RequestMapping("/client/measurementTemplate")
public class MeasurementTemplateClientController extends BaseController {

    @Autowired
    private BaseService<MeasurementTemplate> measurementTemplateBaseService;
    @Autowired
    private BaseService<MeasurementItem> measurementItemBaseService;
    @Autowired
    private BaseService<DeviceSubType> deviceSubTypeBaseService;

    @RequestMapping("/getMeasurementItems")
    @ResponseBody
    public Json getMeasurementItemByMeasurementTemplateId(String measurementTemplateId) throws Exception {
        List<MeasurementItem> measurementItems = measurementItemBaseService.find("from MeasurementItem where measurementTemplateId", Map.of(
                "measurementTemplateId", measurementTemplateId));

        return new Json(JsonMessage.SUCCESS, measurementItems);
    }

    @RequestMapping("/getMeasurementTemplatesByDeviceType")
    @ResponseBody
    public Json getMeasurementTemplatesByDeviceType(String deviceTypeId, String repairClass) throws Exception {
        byte repairClassByte = 0;
        if (StringUtils.isNotEmpty(repairClass))
            repairClassByte = Byte.parseByte(repairClass);

        List<DeviceSubType> deviceSubTypes = deviceSubTypeBaseService.find(
                "from DeviceSubType where deviceTypeId = :deviceTypeId",
                Map.of("deviceTypeId", deviceTypeId)
        );
        List<String> deviceSubTypeIds = StreamUtil.getList(deviceSubTypes, DeviceSubType::getId);

        List<MeasurementTemplate> measurementTemplates = measurementTemplateBaseService.find(
                "from MeasurementTemplate where deviceSubTypeId in :deviceSubTypeIds and repairClass = :repairClass and hide = :hide" ,
                Map.of("deviceSubTypeIds", deviceSubTypeIds, "repairClass", repairClassByte, "hide", Constant.SHOW)
        );

        return new Json(JsonMessage.SUCCESS, measurementTemplates);
    }
}

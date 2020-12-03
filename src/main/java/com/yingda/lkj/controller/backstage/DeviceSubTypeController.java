package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.entity.backstage.device.DeviceSubType;
import com.yingda.lkj.beans.entity.backstage.device.DeviceType;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.device.DeviceSubTypeService;
import com.yingda.lkj.service.backstage.device.DeviceTypeService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/11/22
 */
@RequestMapping("/backstage/deviceSubType")
@Controller
public class DeviceSubTypeController extends BaseController {
    @Autowired
    private DeviceTypeService deviceTypeService;
    @Autowired
    private DeviceSubTypeService deviceSubTypeService;
    @Autowired
    private BaseService<DeviceSubType> deviceSubTypeBaseService;

    private DeviceSubType pageDeviceSubType;

    @RequestMapping("/getDeviceSubTypesByDeviceTypeId")
    @ResponseBody
    public Json getDeviceSubTypesByDeviceTypeId() throws Exception {
        Map<String, Object> attributes = new HashMap<>();

        String deviceTypeId = pageDeviceSubType.getDeviceTypeId();

        Map<String, Object> params = new HashMap<>();
        Map<String, String> conditions = new HashMap<>();

        params.put("deviceTypeId", deviceTypeId);
        conditions.put("deviceTypeId", "=");

        List<DeviceSubType> deviceSubTypes = deviceSubTypeBaseService.getObjcetPagination(
                DeviceSubType.class, params, conditions, page.getCurrentPage(), page.getPageSize(), ""
        );
        page.setDataTotal(deviceSubTypeBaseService.getObjectNum(DeviceSubType.class, params, conditions));

        attributes.put("page", page);
        attributes.put("deviceSubTypes", deviceSubTypes);

        return new Json(JsonMessage.SUCCESS, attributes);
    }

    @RequestMapping("/info")
    @ResponseBody
    public Json info() {
        Map<String, Object> attributes = new HashMap<>();

        String deviceTypeId = pageDeviceSubType.getDeviceTypeId();
        String id = pageDeviceSubType.getId();

        DeviceSubType deviceSubType = StringUtils.isNotEmpty(id) ? deviceSubTypeService.getById(id) : new DeviceSubType();
        if (StringUtils.isNotEmpty(id))
            deviceTypeId = deviceSubType.getDeviceTypeId();

        DeviceType deviceType = deviceTypeService.getDeviceType(deviceTypeId);
        deviceSubType.setDeviceTypeId(deviceTypeId);
        deviceSubType.setDeviceTypeName(deviceType.getName());
        attributes.put("deviceSubType", deviceSubType);

        return new Json(JsonMessage.SUCCESS, attributes);
    }

    @RequestMapping("/saveOrUpdate")
    @ResponseBody
    public Json saveOrUpdate() throws Exception {
        String id = pageDeviceSubType.getId();

        DeviceSubType deviceSubType = StringUtils.isNotEmpty(id) ? deviceSubTypeService.getById(id) : new DeviceSubType(pageDeviceSubType);
        BeanUtils.copyProperties(pageDeviceSubType, deviceSubType, "id", "hide", "addTime");
        deviceSubType.setUpdateTime(current());

        deviceSubTypeBaseService.saveOrUpdate(deviceSubType);

        return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public Json delete() throws CustomException {
        String ids = req.getParameter("ids");
        deviceSubTypeService.delete(Arrays.asList(ids.split(",")));

        return new Json(JsonMessage.SUCCESS);
    }

    @ModelAttribute
    public void setPageDeviceSubType(DeviceSubType pageDeviceSubType) {
        this.pageDeviceSubType = pageDeviceSubType;
    }
}

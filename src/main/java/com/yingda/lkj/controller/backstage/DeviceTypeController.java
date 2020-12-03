package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.entity.backstage.device.*;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.device.devicetype.DeviceTypeNodePojo;
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

import java.sql.Timestamp;
import java.util.*;

/**
 * 后台设备类型管理业
 *
 * @author hood  2019/12/30
 */
@RequestMapping("/backstage/deviceType")
@Controller
public class DeviceTypeController extends BaseController {

    @Autowired
    private BaseService<DeviceType> deviceTypeBaseService;
    @Autowired
    private DeviceTypeService deviceTypeService;
    @Autowired
    private DeviceSubTypeService deviceSubTypeService;

    private DeviceType pageDeviceType;

    @RequestMapping("")
    @ResponseBody
    public Json getList() {
        Map<String, Object> attributes = new HashMap<>();

        List<DeviceTypeNodePojo> deviceTypeNodePojos = new ArrayList<>();
        // 因为数据量不大，为了美观暂时循环查询，如果卡了改成关联查询，全是这种页面的话
        List<DeviceType> deviceTypes = deviceTypeService.getAllDeviceTypes();
        for (DeviceType deviceType : deviceTypes) {
            List<DeviceSubType> deviceSubTypes = deviceSubTypeService.getByDeviceTypeId(deviceType.getId());
            DeviceTypeNodePojo deviceTypeNodePojo = new DeviceTypeNodePojo(deviceType, deviceSubTypes);
            deviceTypeNodePojos.add(deviceTypeNodePojo);
        }
        attributes.put("deviceTypeNodePojos", deviceTypeNodePojos);

        return new Json(JsonMessage.SUCCESS, attributes);
    }

    @RequestMapping("/info")
    @ResponseBody
    public Json info() {
        Map<String, Object> attributes = new HashMap<>();
        String deviceTypeId = pageDeviceType.getId();
        DeviceType deviceType = StringUtils.isNotEmpty(deviceTypeId) ? deviceTypeService.getDeviceType(deviceTypeId) : new DeviceType();

        attributes.put("deviceType", deviceType);
        return new Json(JsonMessage.SUCCESS, attributes);
    }

    @RequestMapping("/saveOrUpdate")
    @ResponseBody
    public Json saveOrUpdate() throws Exception {
        String deviceTypeId = pageDeviceType.getId();

        DeviceType deviceType = StringUtils.isNotEmpty(deviceTypeId) ? deviceTypeService.getDeviceType(deviceTypeId) : new DeviceType(pageDeviceType);
        BeanUtils.copyProperties(pageDeviceType, deviceType, "id", "expired", "seq", "addTime");
        deviceType.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        deviceTypeBaseService.saveOrUpdate(deviceType);

        return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public Json delete() throws CustomException {
        String ids = req.getParameter("ids");

        deviceTypeService.delete(Arrays.asList(ids.split(",")));

        return new Json(JsonMessage.SUCCESS);
    }

    @ModelAttribute
    public void setPageDeviceType(DeviceType pageDeviceType) {
        this.pageDeviceType = pageDeviceType;
    }

}

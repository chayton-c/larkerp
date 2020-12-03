package com.yingda.lkj.controller.client;

import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.backstage.device.DeviceSubType;
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
 * @author hood  2020/3/16
 */
@Controller
@RequestMapping("/client/deviceSubType")
public class DeviceSubTypeClientController extends BaseController {

    @Autowired
    private BaseService<DeviceSubType> deviceSubTypeBaseService;

    @ResponseBody
    @RequestMapping("/getDeviceSubType")
    public Json getDeviceSubType(String deviceTypeId) throws Exception {
        List<DeviceSubType> deviceSubTypes = deviceSubTypeBaseService.find(
                "from DeviceSubType where deviceTypeId = :deviceTypeId and hide = :hide",
                Map.of("deviceTypeId", deviceTypeId, "hide", Constant.SHOW)
        );

        return new Json(JsonMessage.SUCCESS, deviceSubTypes);
    }

}

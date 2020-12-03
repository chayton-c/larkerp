package com.yingda.lkj.controller.client;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.line.Station;
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
 * @author hood  2020/1/3
 */
@RequestMapping("/client/device")
@Controller
public class DeviceClientController extends BaseController {
    @Autowired
    private BaseService<Device> deviceBaseService;

    @RequestMapping("/getDevicesByStationId")
    @ResponseBody
    public Json getDevicesByStationId(String stationId) throws Exception {
        List<Device> devices = deviceBaseService.find(
                "from Device where stationId = :stationId",
                Map.of("stationId", stationId)
        );

        return new Json(JsonMessage.SUCCESS, devices);
    }


}

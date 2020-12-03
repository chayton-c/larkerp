package com.yingda.lkj.controller.app.init;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.app.AppStation;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.line.StationService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.math.NumberUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hood  2020/4/13
 */
@Controller
@RequestMapping("/app/init/stations")
public class InitStationController extends BaseController {
    @Autowired
    private BaseService<Station> stationBaseService;
    @Autowired
    private StationService stationService;
    @Autowired
    private BaseService<User> userBaseService;
    @Autowired
    private BaseService<Device> deviceBaseService;

    @RequestMapping("")
    @ResponseBody
    public Json getStations() throws Exception {
        String userId = req.getParameter("userId");

        Map<String, Object> params = new HashMap<>();
        Map<String, String> conditions = new HashMap<>();

        // 查询用户所在车间下的所有车站下的设备
        User user = userBaseService.get(User.class, userId);
        List<Station> stations = stationService.getStationsByWorkshopId(user.getWorkshopId()); // 用户所在车间下的车站list
        params.put("stationId", stations.stream().map(Station::getId).collect(Collectors.toList()));
        conditions.put("stationId", "in");

        // 所有可能用到的设备
        List<Device> devices = deviceBaseService.getObjcetPagination(Device.class, params, conditions, 1, 999999, "order by addTime desc");

        List<String> deviceIds = StreamUtil.getList(devices, Device::getStationId);
        List<Station> usefulStations = stationService.getByIds(deviceIds);

        List<AppStation> appStations = usefulStations.stream().map(AppStation::new).collect(Collectors.toList());

        return new Json(JsonMessage.SUCCESS, appStations);
    }
}

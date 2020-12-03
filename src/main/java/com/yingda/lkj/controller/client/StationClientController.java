package com.yingda.lkj.controller.client;

import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.entity.backstage.line.StationRailwayLine;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.line.StationRailwayLineService;
import com.yingda.lkj.service.backstage.line.StationService;
import com.yingda.lkj.service.base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hood  2020/1/3
 */
@RequestMapping("/client/station")
@Controller
public class StationClientController extends BaseController {
    @Autowired
    private StationService stationService;
    @Autowired
    private StationRailwayLineService stationRailwayLineService;

    @RequestMapping("/getStationsByRailwayLineId")
    @ResponseBody
    public Json getStationsByRailwayLineId() {
        String railwayLineId = req.getParameter("railwayLineId");
        List<String> stationIdsByRailwayLineId = stationRailwayLineService.getStationIdsByRailwayLineId(railwayLineId);
        List<Station> stations = stationService.getByIds(stationIdsByRailwayLineId);
        stations = stations.stream().filter(x -> x.getHide() == Constant.SHOW).collect(Collectors.toList());
        return new Json(JsonMessage.SUCCESS, stations);
    }

}

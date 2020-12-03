package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.entity.backstage.line.StationRailwayLine;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.line.LineNodePojo;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.line.RailwayLineService;
import com.yingda.lkj.service.backstage.line.StationRailwayLineService;
import com.yingda.lkj.service.backstage.line.StationService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/11/21
 */
@RequestMapping("/backstage/railwayLine")
@Controller
public class RailwayLineController extends BaseController {
    @Autowired
    private RailwayLineService railwayLineService;
    @Autowired
    private StationService stationService;
    @Autowired
    private StationRailwayLineService stationRailwayLineService;
    @Autowired
    private BaseService<RailwayLine> railwayLineBaseService;

    private RailwayLine pageRailwayLine;

    // 以后他妈的所有页面都是三层
    @RequestMapping("")
    @ResponseBody
    public Json getList() {
        Map<String, Object> attributes = new HashMap<>();
        List<RailwayLine> railwayLines = railwayLineService.getAll();

        List<LineNodePojo> lineNodePojos = new ArrayList<>();
        // 因为数据量不大，为了美观暂时循环查询，如果卡了改成关联查询，全是这种页面的话
        for (RailwayLine railwayLine : railwayLines) {
            String railwayLineId = railwayLine.getId();
            List<String> stationIds = stationRailwayLineService.getStationIdsByRailwayLineId(railwayLineId);
            List<Station> stations = stationService.getByIds(stationIds);
            LineNodePojo lineNodePojo = new LineNodePojo(railwayLine, stations);
            lineNodePojos.add(lineNodePojo);
        }
        attributes.put("lineNodePojos", lineNodePojos);

        return new Json(JsonMessage.SUCCESS, attributes);
    }

    @RequestMapping("/info")
    @ResponseBody
    public Json info() {
        Map<String, Object> attributes = new HashMap<>();
        String railwayLineId = pageRailwayLine.getId();
        RailwayLine railwayLine = StringUtils.isNotEmpty(railwayLineId) ? railwayLineService.getById(railwayLineId) : new RailwayLine();

        attributes.put("railwayLine", railwayLine);
        return new Json(JsonMessage.SUCCESS, attributes);
    }

    @RequestMapping("/saveOrUpdate")
    @ResponseBody
    public Json saveOrUpdate() throws Exception {
        String railwayLineId = pageRailwayLine.getId();

        RailwayLine railwayLine = StringUtils.isNotEmpty(railwayLineId) ? railwayLineService.getById(railwayLineId) : new RailwayLine(pageRailwayLine);
        BeanUtils.copyProperties(pageRailwayLine, railwayLine, "id", "addTime", "bureauId");
        // 线路所属局与用户相同
        String bureauId = getUser().getBureauId();
        railwayLine.setBureauId(bureauId);
        railwayLine.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        railwayLineBaseService.saveOrUpdate(railwayLine);

        return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public Json delete() throws Exception {
        String railwayLineId = pageRailwayLine.getId();

        RailwayLine railwayLine = StringUtils.isNotEmpty(railwayLineId) ? railwayLineService.getById(railwayLineId) : new RailwayLine(pageRailwayLine);
        List<String> stationIds = stationRailwayLineService.getStationIdsByRailwayLineId(railwayLineId);
        if (!stationIds.isEmpty())
            throw new CustomException(JsonMessage.CONTAINING_ASSOCIATED_DATA, "选中环境下包含未删除的位置，请在修改对应位置后再进行删除");

        railwayLineBaseService.delete(railwayLine);

        return new Json(JsonMessage.SUCCESS);
    }

    @ModelAttribute
    public void setPageRailwayLine(RailwayLine pageRailwayLine) {
        this.pageRailwayLine = pageRailwayLine;
    }
}

package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.entity.backstage.location.TaskLocation;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.line.StationService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/5/30
 */
@Controller
@RequestMapping("/backstage/taskLocation")
public class TaskLocationController extends BaseController {

    @Autowired
    private BaseService<TaskLocation> taskLocationBaseService;
    @Autowired
    private StationService stationService;

    private TaskLocation pageTaskLocation;

    @RequestMapping("")
    public ModelAndView getList() throws Exception {
        Map<String, Object> attributes = new HashMap<>();

        String sectionId = getSectionId();
        String stationId = req.getParameter("stationId");
        String name = req.getParameter("name");
        attributes.put("stationId", stationId);
        attributes.put("name", name);

        // 查询用车站
        List<Station> stations = stationService.getStationsBySectionId(sectionId);
        attributes.put("stations", stations);

        Map<String, Object> params = new HashMap<>();
        Map<String, String> conditions = new HashMap<>();

        params.put("sectionId", sectionId);
        conditions.put("sectionId", "=");
        if (StringUtils.isNotEmpty(stationId)) {
            params.put("stationId", sectionId);
            conditions.put("stationId", "=");
        }
        if (StringUtils.isNotEmpty(name)) {
            params.put("name", "%" + name + "%");
            conditions.put("name", "like");
        }

        List<TaskLocation> taskLocations = taskLocationBaseService.getObjcetPagination(
                TaskLocation.class, params, conditions, page.getCurrentPage(), page.getPageSize(), "order by updateTime desc");
        attributes.put("taskLocations", taskLocations);

        return new ModelAndView("/backstage/task-location/task-location-list", attributes);
    }

    @RequestMapping("/infoPage")
    public ModelAndView infoPage() {
        return null;
    }

    @RequestMapping("/saveOrUpdate")
    @ResponseBody
    public Json saveOrUpdate() {
        return null;
    }

    @RequestMapping("/hide")
    @ResponseBody
    public ModelAndView hide() {
        return null;
    }




    @ModelAttribute
    public void setPageTaskLocation(TaskLocation pageTaskLocation) {
        this.pageTaskLocation = pageTaskLocation;
    }
}

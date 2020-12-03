package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.entity.backstage.device.DeviceMaintenancePlan;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTask;
import com.yingda.lkj.beans.pojo.measurement.MeasurementTaskGanttPojo;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.device.DeviceMaintenancePlanService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTaskService;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.date.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.unit.DataUnit;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hood  2020/5/8
 */
@Controller
@RequestMapping("/backstage/measurementTaskGantt")
public class MeasurementTaskGanttController extends BaseController {

    @Autowired
    private DeviceMaintenancePlanService deviceMaintenancePlanService;
    @Autowired
    private MeasurementTaskService measurementTaskService;

    @RequestMapping("")
    public ModelAndView gantt() throws ParseException {
        Map<String, Object> attributes = new HashMap<>();

        String startTime = req.getParameter("startTime");
        String endTime = req.getParameter("endTime");
        String workshopName = req.getParameter("workshopName");
        String repairClassStr = req.getParameter("repairClassStr");

        attributes.put("startTime", startTime);
        attributes.put("endTime", endTime);
        attributes.put("workshopName", workshopName);
        attributes.put("repairClassStr", repairClassStr);

        byte repairClass = Byte.parseByte(repairClassStr);

        List<DeviceMaintenancePlan> deviceMaintenancePlans = deviceMaintenancePlanService.getByName(workshopName, getSectionId());
        deviceMaintenancePlans = deviceMaintenancePlans.stream().filter(x -> repairClass == x.getRepairClass()).collect(Collectors.toList());

        List<MeasurementTask> tasksGantt = measurementTaskService.getTasksGantt(
                deviceMaintenancePlans, DateUtil.toTimestamp(startTime, "yyyy-MM-dd"), DateUtil.toTimestamp(endTime, "yyyy-MM-dd")
        );
        if (StringUtils.isNotEmpty(repairClassStr))
            tasksGantt = tasksGantt.stream().filter(x -> repairClass == x.getTaskType()).collect(Collectors.toList());
        List<MeasurementTaskGanttPojo> ganttPojos = tasksGantt.stream().map(MeasurementTaskGanttPojo::getInstance).collect(Collectors.toList());

        attributes.put("ganttPojos", ganttPojos);

        return new ModelAndView("/backstage/measurement-task-gantt/measurement-task-gantt", attributes);
    }
}

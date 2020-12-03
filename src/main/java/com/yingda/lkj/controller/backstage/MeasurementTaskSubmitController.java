package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTask;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTaskDetail;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTaskExecuteUser;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTemplate;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.pojo.measurement.MeasurementTaskPojo;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.measurement.MeasurementTaskService;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.RequestUtil;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.date.CalendarUtil;
import com.yingda.lkj.utils.date.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hood  2020/3/11
 */
@Controller
@RequestMapping("/backstage/measurementTaskSubmit")
public class MeasurementTaskSubmitController extends BaseController {

    @Autowired
    private BaseService<MeasurementTask> measurementTaskBaseService;
    @Autowired
    private BaseService<MeasurementTaskDetail> measurementTaskDetailBaseService;
    @Autowired
    private BaseService<User> userBaseService;
    @Autowired
    private BaseService<Device> deviceBaseService;
    @Autowired
    private MeasurementTaskService measurementTaskService;
    @Autowired
    private BaseService<MeasurementTemplate> measurementTemplateBaseService;
    @Autowired
    private BaseService<MeasurementTaskExecuteUser> measurementTaskExecuteUserBaseService;
    @Autowired
    private OrganizationClientService organizationClientService;

    private MeasurementTask pageMeasurementTask;

    @RequestMapping("")
    public ModelAndView getList() throws Exception {
        Map<String, Object> attributes = new HashMap<>();

        String startTimeStr = req.getParameter("startTime");
        String endTimeStr = req.getParameter("endTime");
        String finishedStatusStr = req.getParameter("finishedStatusStr");
        String workshopId = req.getParameter("workshopId");
        String repairClass = req.getParameter("repairClass");

        if (StringUtils.isEmpty(startTimeStr))
            startTimeStr = DateUtil.format(CalendarUtil.getBeginningOfTheYear(), "yyyy-MM-dd");
        if (StringUtils.isEmpty(endTimeStr))
            endTimeStr = DateUtil.format(CalendarUtil.getEndOfTheYear(), "yyyy-MM-dd");

        attributes.put("startTime", startTimeStr);
        attributes.put("endTime", endTimeStr);
        attributes.put("finishedStatusStr", finishedStatusStr);
        attributes.put("workshopId", workshopId);

        // 查询使用到的车间
        attributes.put("workshops",
                organizationClientService.getWorkshops().stream().filter(x -> getSectionId().equals(x.getParentId())).collect(Collectors.toList()));

        User user = RequestUtil.getUser(req);

        Map<String, Object> params = new HashMap<>();
        Map<String, String> conditions = new HashMap<>();

//        params.put("submitUserId", user.getId());
//        conditions.put("submitUserId", "=");

        if (StringUtils.isNotEmpty(startTimeStr)) {
            params.put("startTime", DateUtil.toTimestamp(startTimeStr, "yyyy-MM-dd"));
            conditions.put("startTime", ">");
        }
        if (StringUtils.isNotEmpty(endTimeStr)) {
            params.put("endTime", DateUtil.toTimestamp(endTimeStr, "yyyy-MM-dd"));
            conditions.put("endTime", "<");
        }
        if (StringUtils.isNotEmpty(finishedStatusStr)) {
            params.put("finishedStatus", Byte.valueOf(finishedStatusStr));
            conditions.put("finishedStatus", "=");
        }
        if (StringUtils.isNotEmpty(workshopId)) {
            List<String> workAreaIds = organizationClientService.getSlave(workshopId).stream().map(Organization::getId).collect(Collectors.toList());
            params.put("workAreaId", workAreaIds);
            conditions.put("workAreaId", "in");
        }
        if (StringUtils.isNotEmpty(repairClass)){
            params.put("taskType", Byte.valueOf(repairClass));
            conditions.put("taskType", "=");
        }

        List<MeasurementTask> measurementTasks =
                measurementTaskBaseService.getObjcetPagination(MeasurementTask.class, params, conditions, page.getCurrentPage()
                , page.getPageSize(), "order by startTime desc, deviceMaintenancePlanId");
        page.setDataTotal(measurementTaskBaseService.getObjectNum(MeasurementTask.class, params, conditions));

        List<String> measurementTaskIds = StreamUtil.getList(measurementTasks, MeasurementTask::getId);

        // 执行人扩展表
        List<MeasurementTaskExecuteUser> measurementTaskExecuteUsers = measurementTaskExecuteUserBaseService.find(
                "from MeasurementTaskExecuteUser where measurementTaskId in :measurementTaskIds",
                Map.of("measurementTaskIds", measurementTaskIds)
        );
        Map<String, List<MeasurementTaskExecuteUser>> measurementTaskExecuteUserMap =
                measurementTaskExecuteUsers.stream().collect(Collectors.groupingBy(MeasurementTaskExecuteUser::getMeasurementTaskId));
        List<String> executorIds = StreamUtil.getList(measurementTaskExecuteUsers, MeasurementTaskExecuteUser::getExecuteUserId);

        List<MeasurementTaskPojo> measurementTaskPojos = new ArrayList<>();

        // 需要查询名称的用户
        List<String> userIds = new ArrayList<>();
        userIds.addAll(executorIds); // 执行人
        userIds.addAll(StreamUtil.getList(measurementTasks, MeasurementTask::getSubmitUserId)); // 提交人
        List<User> users = userBaseService.find("from User where id in :userIds", Map.of("userIds", userIds));
        Map<String, String> userNameMap = users.stream().collect(Collectors.toMap(User::getId, User::getDisplayName)); // key:用户id value:用户显示名

        // 所有的任务详情
        List<MeasurementTaskDetail> measurementTaskDetails = measurementTaskDetailBaseService.find("from MeasurementTaskDetail where measurementTaskId in " +
                ":measurementTaskIds", Map.of("measurementTaskIds", measurementTaskIds));
        Map<String, List<MeasurementTaskDetail>> measurementTaskDetailMap =
                measurementTaskDetails.stream().collect(Collectors.groupingBy(MeasurementTaskDetail::getMeasurementTaskId)); // key: 测量任务id，value: 相同id的测量任务list

        // 需要查询名称的设备
        List<String> deviceIds = StreamUtil.getList(measurementTaskDetails, MeasurementTaskDetail::getDeviceId); // 用到的设备
        List<Device> devices = deviceBaseService.find("from Device where id in :deviceIds", Map.of("deviceIds", deviceIds));
        Map<String, String> deviceNameMap = devices.stream().collect(Collectors.toMap(Device::getId, Device::getName)); // key:设备id，value:设备名称

        // 需要查询名称的测量模板
        List<String> measurementTemplateIds = StreamUtil.getList(measurementTaskDetails, MeasurementTaskDetail::getMeasurementTemplateId); // 用到的测量模板
        List<MeasurementTemplate> measurementTemplates = measurementTemplateBaseService.find("from MeasurementTemplate where id in " +
                ":measurementTemplateIds", Map.of("measurementTemplateIds", measurementTemplateIds));

        Map<String, String> measurementTemplateNameMap = measurementTemplates.stream().collect(Collectors.toMap(MeasurementTemplate::getId,
                MeasurementTemplate::getName)); // key:测量模板id,value:测量模板名称

        for (MeasurementTaskDetail measurementTaskDetail : measurementTaskDetails) {
            String deviceId = measurementTaskDetail.getDeviceId();
            String measurementTemplateId = measurementTaskDetail.getMeasurementTemplateId();

            measurementTaskDetail.setDeviceName(deviceNameMap.get(deviceId));
            measurementTaskDetail.setTemplateName(measurementTemplateNameMap.get(measurementTemplateId));
        }

        for (MeasurementTask measurementTask : measurementTasks) {
            String measurementTaskId = measurementTask.getId();
            Organization workshop = organizationClientService.getParent(measurementTask.getWorkAreaId());

            measurementTask.setSubmitUserName(userNameMap.get(measurementTask.getSubmitUserId()));
            measurementTask.setWorkshopName(workshop.getName());

            // 执行人姓名
            List<MeasurementTaskExecuteUser> measurementTaskExecuteUserList = measurementTaskExecuteUserMap.get(measurementTaskId);
            measurementTaskExecuteUserList = measurementTaskExecuteUserList == null ? new ArrayList<>() : measurementTaskExecuteUserList;
            String executorName = measurementTaskExecuteUserList.stream()
                    .map(x -> userNameMap.get(x.getExecuteUserId()))
                    .collect(Collectors.joining(", "));
            measurementTask.setExecuteUserName(executorName);

            measurementTask.setSubmitUserName(userNameMap.get(measurementTask.getSubmitUserId()));
            measurementTaskPojos.add(new MeasurementTaskPojo(measurementTask));

            // 测量任务下的任务详情
            List<MeasurementTaskDetail> measurementTaskDetailList = measurementTaskDetailMap.get(measurementTaskId);
            if (measurementTaskDetailList == null)
                continue;
            List<MeasurementTaskPojo> measurementTaskDetailPojos = new ArrayList<>();
            for (MeasurementTaskDetail measurementTaskDetail : measurementTaskDetailList) {
                String deviceId = measurementTaskDetail.getDeviceId();
                String deviceName = deviceNameMap.get(deviceId);
                measurementTaskDetailPojos.add(new MeasurementTaskPojo(measurementTaskDetail, measurementTask, deviceName));
            }

            measurementTaskPojos.addAll(measurementTaskDetailPojos);
        }

        attributes.put("measurementTaskPojos", measurementTaskPojos);
        attributes.put("repairClass", repairClass);

        return new ModelAndView("/backstage/measurementtasksubmit/measurement-task-submit-list", attributes);
    }

    @RequestMapping("/infoPage")
    public ModelAndView infoPage(String measurementTaskId) throws Exception {
        User user = RequestUtil.getUser(req);

        String repairClass = req.getParameter("repairClass");
        String sectionId = user.getSectionId();

        List<User> users = userBaseService.find("from User where sectionId = :sectionId", Map.of("sectionId", sectionId));
        List<Organization> workAreas = organizationClientService.getWorkAreasBySectionId(sectionId);

        MeasurementTask measurementTask = new MeasurementTask();
        if (StringUtils.isNotEmpty(measurementTaskId)) {
            measurementTask = measurementTaskBaseService.get(MeasurementTask.class, measurementTaskId);
            List<MeasurementTaskExecuteUser> measurementTaskExecuteUsers = measurementTaskExecuteUserBaseService.find(
                    "from MeasurementTaskExecuteUser where measurementTaskId = :measurementTaskId",
                    Map.of("measurementTaskId", measurementTask.getId())
            );
            List<String> executeUserIds = StreamUtil.getList(measurementTaskExecuteUsers, MeasurementTaskExecuteUser::getExecuteUserId);
            users.forEach(x -> x.setPassword(null));
            return new ModelAndView("/backstage/measurementtasksubmit/measurement-task-info",
                    Map.of("users", users, "workAreas", workAreas, "measurementTask", measurementTask,
                            "executeUserIds", executeUserIds, "repairClass", repairClass));
        } else {
            measurementTask.setNotCurrentExecuted(MeasurementTask.CURRENT_EXECUTED);
        }

        users.forEach(x -> x.setPassword(null));

        return new ModelAndView("/backstage/measurementtasksubmit/measurement-task-info",
                Map.of("users", users, "workAreas", workAreas, "measurementTask", measurementTask, "repairClass", repairClass));
    }

    @RequestMapping("/saveOrUpdate")
    @ResponseBody
    public Json saveOrUpdate() {
        User user = RequestUtil.getUser(req);
        String[] executeUserIds = req.getParameterMap().get("executeUserId");
        measurementTaskService.saveOrUpdate(pageMeasurementTask, user, executeUserIds);

        return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/close")
    @ResponseBody
    public Json close(String measurementTaskId) {
        measurementTaskService.closeTask(measurementTaskId);
        return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/submit")
    @ResponseBody
    public Json submit(String measurementTaskId) {
        measurementTaskService.submitTask(measurementTaskId);
        return new Json(JsonMessage.SUCCESS);
    }

    @ModelAttribute
    public void setPageMeasurementTask(MeasurementTask pageMeasurementTask) {
        this.pageMeasurementTask = pageMeasurementTask;
    }

}

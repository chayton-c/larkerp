package com.yingda.lkj.controller.app.task;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTask;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTaskDetail;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTaskExecuteUser;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.pojo.device.DeviceStatistics;
import com.yingda.lkj.beans.pojo.measurement.MeasurementTaskPojo;
import com.yingda.lkj.beans.pojo.measurement.UserMeasurementTaskDetail;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.device.DeviceService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTaskDetailService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTaskService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.service.system.UserService;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.date.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * app任务
 *
 * @author hood  2020/7/8
 */
@Controller
@RequestMapping("/app/task")
public class AppTaskController extends BaseController {

    @Autowired
    private UserService userService;
    @Autowired
    private MeasurementTaskService measurementTaskService;
    @Autowired
    private BaseService<MeasurementTask> measurementTaskBaseService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private BaseService<MeasurementTaskExecuteUser> measurementTaskExecuteUserBaseService;
    @Autowired
    private BaseService<User> userBaseService;
    @Autowired
    private BaseService<MeasurementTaskDetail> measurementTaskDetailBaseService;

    @RequestMapping("/taskOverview")
    @ResponseBody
    public Json taskOverview() {
        Map<String, Object> attributes = new HashMap<>();

        String userId = req.getParameter("userId");
        User user = userService.getById(userId);
        UserMeasurementTaskDetail userMeasurementTaskDetail = measurementTaskService.getUserMeasurementTaskDetail(userId);

        List<Device> devices = deviceService.getBySectionId(user.getSectionId());
        DeviceStatistics deviceStatistics = new DeviceStatistics(devices);

        attributes.put("totalDevice", deviceStatistics.getTotal());
        attributes.put("errorDeviceCount", deviceStatistics.getError());
        attributes.put("pendingHandleTaskCount", userMeasurementTaskDetail.getPendingHandleTasks().size());

        return new Json(JsonMessage.SUCCESS, attributes);
    }

    @RequestMapping("")
    @ResponseBody
    public Json measurementTasks() throws Exception {
        checkParameters("userId", "startTime", "endTime", "pageSize", "currentPage");

        String userId = req.getParameter("userId");
        String startTimeStr = req.getParameter("startTime");
        String endTimeStr = req.getParameter("endTime");
        String pageSizeStr = req.getParameter("pageSize");
        String currentPageStr = req.getParameter("currentPage");

        int currentPage = Integer.parseInt(currentPageStr);
        int pageSize = Integer.parseInt(pageSizeStr);
        long startTimestamp = Long.parseLong(startTimeStr);
        long endTimestamp = Long.parseLong(endTimeStr);
        Timestamp startTime = new Timestamp(startTimestamp);
        Timestamp endTime = new Timestamp(endTimestamp);
        User user = userService.getById(userId);

        Map<String, Object> params = new HashMap<>();
        Map<String, String> conditions = new HashMap<>();

        // 查询登录用户的可用任务
        List<MeasurementTaskExecuteUser> measurementTaskExecuteUsers = measurementTaskExecuteUserBaseService.find(
                "from MeasurementTaskExecuteUser where executeUserId = :executeUserId",
                Map.of("executeUserId", user.getId())
        );

        List<String> measurementTaskIds = StreamUtil.getList(measurementTaskExecuteUsers, MeasurementTaskExecuteUser::getMeasurementTaskId);

        params.put("id", measurementTaskIds);
        conditions.put("id", "in");

        // 任务的结束时间在参数的startTime后，且任务开始时间在参数的endTime前
        params.put("startTime", endTime);
        conditions.put("startTime", "<=");
        params.put("endTime", startTime);
        conditions.put("endTime", ">=");

        List<MeasurementTask> measurementTasks = measurementTaskBaseService.getObjcetPagination(
                MeasurementTask.class, params, conditions, currentPage, pageSize, "order by endTime desc");
        long total = measurementTaskBaseService.getObjectNum(MeasurementTask.class, params, conditions);

        // 执行人扩展表
        measurementTaskExecuteUsers = measurementTaskExecuteUserBaseService.find(
                "from MeasurementTaskExecuteUser where measurementTaskId in :measurementTaskIds",
                Map.of("measurementTaskIds", measurementTaskIds)
        );
        Map<String, List<MeasurementTaskExecuteUser>> measurementTaskExecuteUserMap =
                measurementTaskExecuteUsers.stream().collect(Collectors.groupingBy(MeasurementTaskExecuteUser::getMeasurementTaskId));
        List<String> executorIds = StreamUtil.getList(measurementTaskExecuteUsers, MeasurementTaskExecuteUser::getExecuteUserId);

        // 需要查询名称的用户
        List<String> userIds = new ArrayList<>();
        userIds.addAll(executorIds); // 执行人
        userIds.addAll(StreamUtil.getList(measurementTasks, MeasurementTask::getSubmitUserId)); // 提交人
        List<User> users = userBaseService.find("from User where id in :userIds", Map.of("userIds", userIds));
        Map<String, String> userNameMap = users.stream().collect(Collectors.toMap(User::getId, User::getDisplayName)); // key:用户id value:用户显示名

        // 所有的任务详情
        measurementTaskIds = StreamUtil.getList(measurementTasks, MeasurementTask::getId);
        List<MeasurementTaskDetail> measurementTaskDetails = measurementTaskDetailBaseService.find("from MeasurementTaskDetail where measurementTaskId in " +
                ":measurementTaskIds", Map.of("measurementTaskIds", measurementTaskIds));


        Map<String, List<MeasurementTaskDetail>> measurementTaskDetailMap =
                measurementTaskDetails.stream().collect(Collectors.groupingBy(MeasurementTaskDetail::getMeasurementTaskId)); // key: 测量任务id，value: 相同id的测量任务list

        for (MeasurementTask measurementTask : measurementTasks) {
            String measurementTaskId = measurementTask.getId();
            List<MeasurementTaskDetail> measurementTaskDetailList = measurementTaskDetailMap.get(measurementTaskId);

            if (measurementTaskDetailList == null || measurementTaskDetailList.isEmpty()) {
                measurementTask.setMeasurementTaskDetailList(new ArrayList<>());
                continue;
            }

            // 给前端的数据不能有null,用string类型的executeTimeStr代替timestamp
            Timestamp executeTime = measurementTask.getExecuteTime();
            measurementTask.setExecuteTimeStr(executeTime == null ? "" : DateUtil.format(executeTime));

            // 执行人姓名
            List<MeasurementTaskExecuteUser> measurementTaskExecuteUserList = measurementTaskExecuteUserMap.get(measurementTaskId);
            measurementTaskExecuteUserList = measurementTaskExecuteUserList == null ? new ArrayList<>() : measurementTaskExecuteUserList;
            String executorName = measurementTaskExecuteUserList.stream()
                    .map(x -> userNameMap.get(x.getExecuteUserId()))
                    .collect(Collectors.joining(", "));
            measurementTask.setExecuteUserName(executorName);

            measurementTask.setSubmitUserName(userNameMap.get(measurementTask.getSubmitUserId()));

            measurementTask.setMeasurementTaskDetailList(measurementTaskDetailList);
        }

        return new Json(JsonMessage.SUCCESS, Map.of("measurementTasks", measurementTasks, "total", total));
    }
}

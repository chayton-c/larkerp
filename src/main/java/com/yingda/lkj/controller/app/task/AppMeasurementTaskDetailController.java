package com.yingda.lkj.controller.app.task;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.measurement.*;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.app.AppMeasurementTaskDetail;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.device.DeviceService;
import com.yingda.lkj.service.backstage.measurement.*;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.service.system.UserService;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.hql.HqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/7/8
 */
@Controller
@RequestMapping("/app/measurementTaskDetail")
public class AppMeasurementTaskDetailController extends BaseController {

    @Autowired
    private MeasurementTaskDetailService measurementTaskDetailService;
    @Autowired
    private MeasurementTaskService measurementTaskService;
    @Autowired
    private BaseService<AppMeasurementTaskDetail> appMeasurementTaskDetailBaseService;
    @Autowired
    private BaseService<BigInteger> bigIntegerBaseService;
    @Autowired
    private MeasurementTaskExecuteUserService measurementTaskExecuteUserService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private MeasurementItemFieldValueService measurementItemFieldValueService;
    @Autowired
    private MeasurementItemFieldService measurementItemFieldService;
    @Autowired
    private UserService userService;

    @RequestMapping("/getMeasurementTaskDetails")
    @ResponseBody
    public Json getMeasurementTasksByUserId() throws Exception {
        checkParameters("finishedStatus", "pageSize", "currentPage", "measurementTaskId");

        String measurementTaskId = req.getParameter("measurementTaskId");
        String finishedStatusStr = req.getParameter("finishedStatus");
        String currentPageStr = req.getParameter("currentPage");
        String pageSizeStr = req.getParameter("pageSize");

        int currentPage = Integer.parseInt(currentPageStr);
        int pageSize = Integer.parseInt(pageSizeStr);


        Map<String, Object> params = new HashMap<>();
        params.put("finishedStatus", Byte.parseByte(finishedStatusStr));
        params.put("measurementTaskId", measurementTaskId);

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT\n");
        sqlBuilder.append("  taskDetail.id as measurementTaskDetailId,\n");
        sqlBuilder.append("  taskDetail.measurement_template_id as measurementTemplateId,\n");
        sqlBuilder.append("  device.id as deviceId,\n");
        sqlBuilder.append("  device.name as deviceName,\n");
        sqlBuilder.append("  device.code as deviceCode,\n");
        sqlBuilder.append("  task.id as measurementTaskId,\n");
        sqlBuilder.append("  task.name as measurementTaskName,\n");
        sqlBuilder.append("  task.start_time as measurementTaskStartTime,\n");
        sqlBuilder.append("  task.execute_time as measurementTaskExecuteTime\n");
        sqlBuilder.append("FROM\n");
        sqlBuilder.append("  measurement_task_detail taskDetail\n");
        sqlBuilder.append("  LEFT JOIN device ON device.id = taskDetail.device_id \n");
        sqlBuilder.append("  left join measurement_task task on task.id = taskDetail.measurement_task_id\n");
        sqlBuilder.append("WHERE\n");
        sqlBuilder.append("  taskDetail.measurement_task_id = :measurementTaskId\n");
        sqlBuilder.append("  AND taskDetail.finished_status = :finishedStatus\n");
        sqlBuilder.append("ORDER BY task.end_time desc");

        String sql = sqlBuilder.toString();

        List<AppMeasurementTaskDetail> appMeasurementTaskDetails = appMeasurementTaskDetailBaseService.findSQL(
                sql, params, AppMeasurementTaskDetail.class, currentPage, pageSize
        );

        String countSql = HqlUtils.getCountSql(sql);
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        long total = count.isEmpty() ? 0 : count.get(0).longValue();

        Map<String, List<MeasurementItemFieldValue>> measurementItemFieldValueMap =
                measurementItemFieldValueService.getMeasurementItemFieldValues(
                        StreamUtil.getList(appMeasurementTaskDetails, AppMeasurementTaskDetail::getMeasurementTaskDetailId)
                );

        Map<String, List<MeasurementItemField>> itemFieldMap = measurementItemFieldService.getFieldsByTemplateIds(
                StreamUtil.getList(appMeasurementTaskDetails, AppMeasurementTaskDetail::getMeasurementTemplateId)
        );

        for (AppMeasurementTaskDetail appMeasurementTaskDetail : appMeasurementTaskDetails) {
            String measurementTaskDetailId = appMeasurementTaskDetail.getMeasurementTaskDetailId();
            String measurementTemplateId = appMeasurementTaskDetail.getMeasurementTemplateId();

            List<MeasurementItemField> measurementItemFields = itemFieldMap.get(measurementTemplateId);
            List<MeasurementItemFieldValue> measurementItemFieldValues = measurementItemFieldValueMap.get(measurementTaskDetailId);

            appMeasurementTaskDetail.setItemFieldCount(measurementItemFields == null ? 0 : measurementItemFields.size());
            appMeasurementTaskDetail.setItemFieldValueCount(measurementItemFieldValues == null ? 0 : measurementItemFieldValues.size());
        }

        return new Json(JsonMessage.SUCCESS, Map.of("appMeasurementTaskDetails", appMeasurementTaskDetails, "total", total));
    }

    /**
     * 设备维护记录(查询对应deviceId的measurementTaskDetail)
     */
    @RequestMapping("/deviceRecord")
    @ResponseBody
    public Json deviceRecord() throws Exception {
        checkParameters("deviceId", "pageSize", "currentPage");

        String deviceId = req.getParameter("deviceId");
        String currentPageStr = req.getParameter("currentPage");
        String pageSizeStr = req.getParameter("pageSize");

        int currentPage = Integer.parseInt(currentPageStr);
        int pageSize = Integer.parseInt(pageSizeStr);

        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", deviceId);
        params.put("finishedStatus", MeasurementTaskDetail.COMPLETED);

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT\n");
        sqlBuilder.append("  taskDetail.id as measurementTaskDetailId,\n");
        sqlBuilder.append("  device.name as deviceName,\n");
        sqlBuilder.append("  task.id as measurementTaskId,\n");
        sqlBuilder.append("  task.name as measurementTaskName,\n");
        sqlBuilder.append("  task.start_time as measurementTaskStartTime,\n");
        sqlBuilder.append("  task.execute_time as measurementTaskExecuteTime\n");
        sqlBuilder.append("FROM\n");
        sqlBuilder.append("  measurement_task_detail taskDetail\n");
        sqlBuilder.append("  LEFT JOIN device ON device.id = taskDetail.device_id \n");
        sqlBuilder.append("  left join measurement_task task on task.id = taskDetail.measurement_task_id\n");
        sqlBuilder.append("WHERE\n");
        sqlBuilder.append("  device.id = :deviceId \n");
        sqlBuilder.append("  AND taskDetail.finished_status = :finishedStatus \n");
        sqlBuilder.append("ORDER BY task.execute_time desc");

        String sql = sqlBuilder.toString();

        List<AppMeasurementTaskDetail> appMeasurementTaskDetails = appMeasurementTaskDetailBaseService.findSQL(
                sql, params, AppMeasurementTaskDetail.class, currentPage, pageSize
        );

        String countSql = HqlUtils.getCountSql(sql);
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        long total = count.isEmpty() ? 0 : count.get(0).longValue();


        List<String> measurementTaskIds = StreamUtil.getList(appMeasurementTaskDetails, AppMeasurementTaskDetail::getMeasurementTaskId);
        // key:任务id，value:执行人名称
        Map<String, String> executeUserNameMap = measurementTaskDetailService.getExecuteUserNamesByTaskIds(measurementTaskIds);
        for (AppMeasurementTaskDetail appMeasurementTaskDetail : appMeasurementTaskDetails) {
            String measurementTaskId = appMeasurementTaskDetail.getMeasurementTaskId();
            String executeUserName = executeUserNameMap.get(measurementTaskId);
            appMeasurementTaskDetail.setExecuteUserName(executeUserName);
        }

        return new Json(JsonMessage.SUCCESS, Map.of("appMeasurementTaskDetails", appMeasurementTaskDetails, "total", total));
    }

    @RequestMapping("/getMeasurementTaskDetail")
    @ResponseBody
    public Json getMeasurementTaskDetail() throws CustomException {
        checkParameters("measurementTaskDetailId");
        String measurementTaskDetailId = req.getParameter("measurementTaskDetailId");
        MeasurementTaskDetail measurementTaskDetail = measurementTaskDetailService.getById(measurementTaskDetailId);

        String measurementTaskId = measurementTaskDetail.getMeasurementTaskId();
        MeasurementTask measurementTask = measurementTaskService.getById(measurementTaskId);

        String executeUserNames = measurementTaskExecuteUserService.getUserNamesByMeasurementTaskId(measurementTaskId);
        String submitUserName = userService.getById(measurementTask.getSubmitUserId()).getDisplayName();

        Device device = deviceService.getById(measurementTaskDetail.getDeviceId());



        return new Json(JsonMessage.SUCCESS,
                new AppMeasurementTaskDetail(measurementTask, measurementTaskDetail, submitUserName, executeUserNames, device));
    }
}

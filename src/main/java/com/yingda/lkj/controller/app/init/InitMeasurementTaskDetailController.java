package com.yingda.lkj.controller.app.init;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItem;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTask;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTaskDetail;
import com.yingda.lkj.beans.pojo.app.AppMeasurementTaskDetail;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.measurement.MeasurementTaskDetailService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTaskExecuteUserService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.StreamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 任务列表获取
 *
 * @author hood  2020/4/13
 */
@Controller
@RequestMapping("/app/init/measurementTaskDetails")
public class InitMeasurementTaskDetailController extends BaseController {
    @Autowired
    private BaseService<MeasurementTaskDetail> measurementTaskDetailBaseService;
    @Autowired
    private MeasurementTaskDetailService measurementTaskDetailService;
    @Autowired
    private BaseService<MeasurementItem> measurementItemBaseService;
    @Autowired
    private MeasurementTaskExecuteUserService measurementTaskExecuteUserService;
    @Autowired
    private BaseService<Device> deviceBaseService;

    @RequestMapping("")
    @ResponseBody
    public Json getData(String userId) throws Exception {
        Map<String, Object> params = new HashMap<>();
        Map<String, String> conditions = new HashMap<>();

        List<String> measurementTaskIds = measurementTaskExecuteUserService.getMeasurementTaskIdsByUserId(userId);

        params.put("finishedStatus", MeasurementTaskDetail.PENDING_HANDLE);
        conditions.put("finishedStatus", "=");
        params.put("measurementTaskId", measurementTaskIds);
        conditions.put("measurementTaskId", "in");

        // 所有子任务
        List<MeasurementTaskDetail> measurementTaskDetails = measurementTaskDetailBaseService.getObjcetPagination(
                MeasurementTaskDetail.class, params, conditions, 1, 999999, "order by addTime desc");

        // 查询子任务上面的主任务，作为measurementTaskDetails中的measurementTask部分字段
        // 任务map key:measurementTaskDetail.id(子任务id) value:对应的主任务
        Map<String, MeasurementTask> measurementTaskMap = measurementTaskDetailService.getMeasurementTaskMap(measurementTaskDetails);
        // 执行人map key:measuermeentTask.id(任务id) value:执行人id
        Map<String, String> executeUserIdMap = measurementTaskDetailService.getExecuteUserIds(measurementTaskDetails);

        // 查询测量项
        List<String> measurementTemplateIds = StreamUtil.getList(measurementTaskDetails, MeasurementTaskDetail::getMeasurementTemplateId);
        List<MeasurementItem> measurementItems = measurementItemBaseService.find(
                "from MeasurementItem where measurementTemplateId in :ids",
                Map.of("ids", measurementTemplateIds)
        );
        // key:测量模板id，value:测量项
        Map<String, List<MeasurementItem>> measurementItemMap =
                measurementItems.stream().collect(Collectors.groupingBy(MeasurementItem::getMeasurementTemplateId));

        List<AppMeasurementTaskDetail> appMeasurementTaskDetails = new ArrayList<>();
        for (MeasurementTaskDetail measurementTaskDetail : measurementTaskDetails) {
            String measurementTaskDetailId = measurementTaskDetail.getId();
            MeasurementTask measurementTask = measurementTaskMap.get(measurementTaskDetailId);
            if (measurementTask == null)
                continue;

            String measurementTaskId = measurementTaskDetail.getMeasurementTaskId();
            String userIds = executeUserIdMap.get(measurementTaskId);

            String measurementTemplateId = measurementTaskDetail.getMeasurementTemplateId();
            // 子任务对应的测量项
            List<MeasurementItem> measurementItemList = measurementItemMap.get(measurementTemplateId);
            if (measurementItemList == null) // 如果没有测量项，不返给app
                continue;
            // 注意：虽然叫measurementItemIds，但是这里取的是主模板id
            String measurementItemIds = measurementItemList.stream().map(MeasurementItem::getMeasurementTemplateId).distinct().collect(Collectors.joining(","));

            Device device = deviceBaseService.get(Device.class, measurementTaskDetail.getDeviceId());

            AppMeasurementTaskDetail appMeasurementTaskDetail = new AppMeasurementTaskDetail(measurementTask, measurementTaskDetail, userIds,
                    measurementItemIds, device);
            appMeasurementTaskDetails.add(appMeasurementTaskDetail);
        }

        return new Json(JsonMessage.SUCCESS, appMeasurementTaskDetails);
    }

}

package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.entity.backstage.measurement.*;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.pojo.measurement.MeasurementItemFieldPojo;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.measurement.MeasurementItemFieldValueService;
import com.yingda.lkj.service.backstage.measurement.MeasurementItemFieldService;
import com.yingda.lkj.service.backstage.measurement.MeasurementUnitService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.RequestUtil;
import com.yingda.lkj.utils.StreamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 子任务下的数据(measurementItemFieldValue)
 *
 * @author hood  2020/3/19
 */
@Controller
@RequestMapping("/backstage/measurementItemFieldValue")
public class MeasurementItemFieldValueController extends BaseController {

    @Autowired
    private BaseService<MeasurementTaskDetail> measurementTaskDetailBaseService;
    @Autowired
    private MeasurementItemFieldService measurementItemFieldService;
    @Autowired
    private BaseService<MeasurementItem> measurementItemBaseService;
    @Autowired
    private BaseService<MeasurementItemFieldValue> measurementItemFieldValueBaseSerivce;
    @Autowired
    private BaseService<MeasurementTask> measurementTaskBaseService;
    @Autowired
    private MeasurementItemFieldValueService measurementItemFieldValueService;
    @Autowired
    private BaseService<MeasurementTaskExecuteUser> measurementTaskExecuteUserBaseService;
    @Autowired
    private MeasurementUnitService measurementUnitService;

    /**
     * 测量任务数据详情(测量任务所有字段的值)，测量任务查看页
     */
    @RequestMapping("/fieldList")
    public ModelAndView fieldList(String measurementTaskDetailId) throws Exception {
        User user = RequestUtil.getUser(req);

        MeasurementTaskDetail measurementTaskDetail = measurementTaskDetailBaseService.get(MeasurementTaskDetail.class, measurementTaskDetailId);
        String measurementTaskId = measurementTaskDetail.getMeasurementTaskId(); // 测量任务id
        String measurementTemplateId = measurementTaskDetail.getMeasurementTemplateId(); // 测量模板id

        MeasurementTask measurementTask = measurementTaskBaseService.get(MeasurementTask.class, measurementTaskId);

        // 查询提交人
        List<MeasurementTaskExecuteUser> measurementTaskExecuteUsers = measurementTaskExecuteUserBaseService.find(
                "from MeasurementTaskExecuteUser where measurementTaskId = :measurementTaskId",
                Map.of("measurementTaskId", measurementTaskId)
        );
        List<String> executeUserIds = StreamUtil.getList(measurementTaskExecuteUsers, MeasurementTaskExecuteUser::getExecuteUserId);
        boolean editable = executeUserIds.contains(user.getId()) && MeasurementTask.PENDING_HANDLE == measurementTask.getFinishedStatus();

        // 子模板
        List<MeasurementItem> measurementItems = measurementItemBaseService.find(
                "from MeasurementItem where measurementTemplateId = :measurementTemplateId",
                Map.of("measurementTemplateId", measurementTemplateId)
        );
        Map<String, MeasurementItem> measurementItemMap = measurementItems.stream().collect(Collectors.toMap(
                MeasurementItem::getId, x -> x
        )); // key:id, value:measurementItem

        // 字段
        List<MeasurementItemField> measurementItemFields = measurementItemFieldService.getFieldsByDetailId(measurementTaskDetailId);

        // 字段的值
        List<MeasurementItemFieldValue> measurementItemFieldValues =
                measurementItemFieldValueService.getByMeasurementTaskDetailId(measurementTaskDetailId);
        Map<String, MeasurementItemFieldValue> measurementItemFieldValueMap = measurementItemFieldValues.stream().collect(Collectors.toMap(
                MeasurementItemFieldValue::getMeasurementItemFieldId, x -> x
        )); // key: MeasurementItemFieldValue.measurementItemField, value: measurementItemFieldValue

        List<MeasurementItemFieldPojo> measurementItemFieldPojos = new ArrayList<>();
        for (MeasurementItemField measurementItemField : measurementItemFields) {
            String measurementItemId = measurementItemField.getMeasurementItemId();
            String measurementItemFieldId = measurementItemField.getId();

            MeasurementItem measurementItem = measurementItemMap.get(measurementItemId);
            MeasurementItemFieldValue measurementItemFieldValue = measurementItemFieldValueMap.get(measurementItemFieldId);

            MeasurementUnit measurementUnit = measurementUnitService.getById(measurementItemField.getMeasurementUnitId());

            measurementItemFieldPojos.add(new MeasurementItemFieldPojo(measurementItemField, measurementItem, measurementItemFieldValue, measurementUnit));
        }

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("measurementItemFieldPojos", measurementItemFieldPojos);
        attributes.put("editable", editable);
        attributes.put("measurementTaskDetailId", measurementTaskDetailId);

        return new ModelAndView("/backstage/measurement-task-detail/measurement-task-detail-value-list", attributes);
    }

    /**
     * 添加/修改测量值
     */
    @RequestMapping("/saveOrUpdateFileValue")
    @ResponseBody
    public Json saveOrUpdateFileValue(String measurementItemFieldId, String measurementTaskDetailId, String value) {
        if (value.contains("\""))
            value = value.replace("\"", "");
        MeasurementItemFieldValue measurementItemFieldValue = measurementItemFieldValueService.saveOrUpdateFieldValue(measurementItemFieldId,
                measurementTaskDetailId, value);
        return new Json(JsonMessage.SUCCESS, measurementItemFieldValue);
    }
}

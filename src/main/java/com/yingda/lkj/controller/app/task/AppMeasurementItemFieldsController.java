package com.yingda.lkj.controller.app.task;

import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItemField;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItemFieldValue;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTaskDetail;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementUnit;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.measurement.MeasurementItemFieldService;
import com.yingda.lkj.service.backstage.measurement.MeasurementItemFieldValueService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTaskDetailService;
import com.yingda.lkj.service.backstage.measurement.MeasurementUnitService;
import com.yingda.lkj.utils.StreamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/7/9
 */
@RequestMapping("/app/measurementItemFields")
@Controller
public class AppMeasurementItemFieldsController extends BaseController {

    @Autowired
    private MeasurementItemFieldValueService measurementItemFieldValueService;
    @Autowired
    private MeasurementItemFieldService measurementItemFieldService;
    @Autowired
    private MeasurementTaskDetailService measurementTaskDetailService;
    @Autowired
    private MeasurementUnitService measurementUnitService;

    @RequestMapping("")
    @ResponseBody
    public Json getFields() throws CustomException {
        checkParameters("measurementTaskDetailId");
        String measurementTaskDetailId = req.getParameter("measurementTaskDetailId");

        // 测量字段
        List<MeasurementItemFieldValue> measurementItemFieldValues =
                measurementItemFieldValueService.getByMeasurementTaskDetailId(measurementTaskDetailId);
        Map<String, MeasurementItemFieldValue> measurementItemFieldValueMap =
                StreamUtil.getMap(measurementItemFieldValues, MeasurementItemFieldValue::getMeasurementItemFieldId, x -> x);

        List<MeasurementItemField> measurementItemFields = measurementItemFieldService.getFieldsByDetailId(measurementTaskDetailId);
        for (MeasurementItemField measurementItemField : measurementItemFields) {
            String measurementItemFieldId = measurementItemField.getId();

            MeasurementUnit measurementUnit = measurementUnitService.getById(measurementItemField.getMeasurementUnitId());
            measurementItemField.setMeasurementUnit(measurementUnit);

            MeasurementItemFieldValue measurementItemFieldValue = measurementItemFieldValueMap.get(measurementItemFieldId);
            measurementItemField.setMeasurementItemFieldValue(measurementItemFieldValue);
        }

        return new Json(JsonMessage.SUCCESS, measurementItemFields);
    }

}

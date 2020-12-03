package com.yingda.lkj.controller.app.init;

import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItem;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItemField;
import com.yingda.lkj.beans.pojo.app.AppMeasurementItemField;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.measurement.MeasurementItemFieldService;
import com.yingda.lkj.service.base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 测量内容列表获取
 *
 * @author hood  2020/4/14
 */
@Controller
@RequestMapping("/app/init/measurementItemField")
public class InitMeasurementItemFieldController extends BaseController {

    @Autowired
    private BaseService<MeasurementItemField> measurementItemFieldBaseService;
    @Autowired
    private BaseService<MeasurementItem> measurementItemBaseService;

    @RequestMapping("")
    @ResponseBody
    public Json getData() throws Exception {
        Map<String, Object> params = new HashMap<>();
        Map<String, String> conditions = new HashMap<>();

        List<MeasurementItemField> measurementItemFields = measurementItemFieldBaseService.getObjcetPagination(
                MeasurementItemField.class, params, conditions, 1, 9999999, "order by addTime desc");

        List<AppMeasurementItemField> appMeasurementItemFields = measurementItemFields.stream()
                .map(AppMeasurementItemField::new).collect(Collectors.toList());

        for (AppMeasurementItemField appMeasurementItemField : appMeasurementItemFields) {
            String measurementItemFieldId = appMeasurementItemField.getMeasurementItemId();
            MeasurementItem measurementItem = measurementItemBaseService.get(MeasurementItem.class, measurementItemFieldId);
            appMeasurementItemField.setMeasurementItemName(measurementItem.getName());
        }

        for (int i = 0; i < appMeasurementItemFields.size(); i++)
            appMeasurementItemFields.get(i).setSeq(i);

        return new Json(JsonMessage.SUCCESS, appMeasurementItemFields);
    }
}

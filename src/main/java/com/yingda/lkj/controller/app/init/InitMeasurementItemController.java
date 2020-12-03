package com.yingda.lkj.controller.app.init;

import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItem;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItemField;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTemplate;
import com.yingda.lkj.beans.pojo.app.AppMeasurementItem;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.measurement.MeasurementItemFieldService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTemplateService;
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
 * 模板列表获取
 *
 * @author hood  2020/4/14
 */
@Controller
@RequestMapping("/app/init/measurementItem")
public class InitMeasurementItemController extends BaseController {

    @Autowired
    private BaseService<MeasurementItem> measurementItemBaseService;
    @Autowired
    private MeasurementTemplateService measurementTemplateService;
    @Autowired
    private MeasurementItemFieldService measurementItemFieldService;

    @RequestMapping("")
    @ResponseBody
    public Json getData() throws Exception {
        Map<String, Object> params = new HashMap<>();
        Map<String, String> conditions = new HashMap<>();

        List<MeasurementItem> measurementItems = measurementItemBaseService.getObjcetPagination(
                MeasurementItem.class, params, conditions, 1, 999999, "order by addTime desc"
        );

        List<MeasurementTemplate> measurementTemplates = measurementTemplateService.getTemplatesByMeasurementItems(measurementItems);

        // key:主模板id, value:主模板
        Map<String, MeasurementTemplate> measurementTemplateMap = measurementTemplates.stream()
                .collect(Collectors.toMap(MeasurementTemplate::getId, x -> x));

        // key:子模板id, value:子模板下的所有未隐藏字段
        Map<String, List<MeasurementItemField>> fieldsByMeasurmenetItems =
                measurementItemFieldService.getFieldsByMeasurmenetItems(measurementItems);

        List<AppMeasurementItem> appMeasurementItems = new ArrayList<>();
        for (MeasurementItem measurementItem : measurementItems) {
            String measurementTemplateId = measurementItem.getMeasurementTemplateId();
            String measurementItemId = measurementItem.getId();

            MeasurementTemplate measurementTemplate = measurementTemplateMap.get(measurementTemplateId);
            if (measurementTemplate == null)
                continue;

            List<MeasurementItemField> measurementItemFields = fieldsByMeasurmenetItems.get(measurementItemId);
            String measurementItemFieldIds = "";
            if (measurementItemFields != null)
                measurementItemFieldIds = measurementItemFields.stream().map(MeasurementItemField::getId).collect(Collectors.joining(","));

            appMeasurementItems.add(new AppMeasurementItem(measurementItem, measurementTemplate, measurementItemFieldIds));
        }

        // 把子模板list换成主模板的
        List<AppMeasurementItem> returnList = new ArrayList<>();
        Map<String, List<AppMeasurementItem>> collect =
                appMeasurementItems.stream().collect(Collectors.groupingBy(AppMeasurementItem::getMeasurementTemplateId));
        for (Map.Entry<String, List<AppMeasurementItem>> stringListEntry : collect.entrySet()) {
            String measurementTemplateId = stringListEntry.getKey();
            MeasurementTemplate measurementTemplate = measurementTemplateMap.get(measurementTemplateId);
            if (measurementTemplate == null)
                continue;

            List<AppMeasurementItem> value = stringListEntry.getValue();
            String measurementItemFieldIds = String.join(",", StreamUtil.getList(value, AppMeasurementItem::getMeasurementItemFieldIds));
            returnList.add(new AppMeasurementItem(measurementTemplate.getId(), measurementTemplate.getName(), measurementItemFieldIds));
        }

        return new Json(JsonMessage.SUCCESS, returnList);
    }
}

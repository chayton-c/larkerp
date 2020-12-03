package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.backstage.device.DeviceMeasurementItem;
import com.yingda.lkj.beans.entity.backstage.device.DeviceType;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItem;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItemField;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTemplate;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementUnit;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.device.DeviceMeasurementItemService;
import com.yingda.lkj.service.backstage.device.DeviceTypeService;
import com.yingda.lkj.service.backstage.measurement.MeasurementItemFieldService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTemplateService;
import com.yingda.lkj.service.backstage.measurement.MeasurementUnitService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.hql.HqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hood  2020/3/17
 */
@Controller
@RequestMapping("/backstage/measurementItemFiled")
public class MeasurementItemFieldController extends BaseController {

    @Autowired
    private BaseService<MeasurementItemField> measurementItemFieldBaseService;
    @Autowired
    private BaseService<MeasurementUnit> measurementUnitBaseService;
    @Autowired
    private DeviceMeasurementItemService deviceMeasurementItemService;
    @Autowired
    private BaseService<MeasurementItem> measurementItemBaseService;
    @Autowired
    private MeasurementTemplateService measurementTemplateService;
    @Autowired
    private MeasurementItemFieldService measurementItemFieldService;
    @Autowired
    private DeviceTypeService deviceTypeService;
    @Autowired
    private MeasurementUnitService measurementUnitService;

    private MeasurementItemField pageMeasurementItemField;

    @RequestMapping("/getList")
    @ResponseBody
    public Json getList() throws Exception {
        Map<String, Object> attributes = new HashMap<>();

        String measurementTemplateId = req.getParameter("measurementTemplateId");

        Map<String, Object> params = new HashMap<>();
        String sql = """
                SELECT
                	measurementItemField.id AS id,
                	measurementItemField.`name` AS `name`,
                	measurementItemField.seq AS seq,
                	measurementItemField.measurement_template_id AS measurementTemplateId,
                	measurementItemField.measurement_unit_id AS measurementUnitId,
                	measurementItemField.correct_value AS correctValue,
                	measurementItemField.max_value AS `maxValue`,
                	measurementItemField.min_value AS minValue,
                	measurementItemField.man_hour AS manHour,
                	measurementItemField.description AS description,
                	measurementItemField.remark AS remark,
                	measurementUnit.unit_name AS unitName,
                	measurementUnit.name AS measurementUnitName
                FROM
                	measurement_item_field AS measurementItemField
                	LEFT JOIN measurement_unit AS measurementUnit ON measurementItemField.measurement_unit_id = measurementUnit.id
                	LEFT JOIN measurement_template AS measurementTemplate ON measurementItemField.measurement_template_id = measurementTemplate.id
                WHERE
                    measurementTemplate.id = :measurementTemplateId
                """;
        params.put("measurementTemplateId", measurementTemplateId);
        sql += "ORDER BY measurementItemField.seq";

        List<MeasurementItemField> measurementItemFields = measurementItemFieldBaseService.findSQL(
                sql, params, MeasurementItemField.class, page.getCurrentPage(), page.getPageSize()
        );

        List<BigInteger> count = bigIntegerBaseService.findSQL(HqlUtils.getCountSql(sql), params);
        page.setDataTotal(count);

        attributes.put("measurementUnits", measurementUnitService.showdown());
        attributes.put("measurementItemFields", measurementItemFields);
        attributes.put("page", page);

        return new Json(JsonMessage.SUCCESS, attributes);
    }

    @RequestMapping("/groupFields")
    @ResponseBody
    public Json groupFields() throws Exception {
        String fieldIdsStr = req.getParameter("fieldIds");
        List<String> fieldIds = Arrays.asList(fieldIdsStr.split(","));
        measurementItemFieldService.groupFields(fieldIds);
        return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/info")
    public ModelAndView infoPage(String id, String measurementItemId) throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        if (StringUtils.isEmpty(measurementItemId))
            throw new CustomException(new Json(JsonMessage.SYS_ERROR));

        MeasurementItem measurementItem = measurementItemBaseService.get(MeasurementItem.class, measurementItemId);
        MeasurementTemplate measurementTemplate = measurementTemplateService.getById(measurementItem.getMeasurementTemplateId());
        DeviceType deviceType = deviceTypeService.getByDeviceSubTypeId(measurementTemplate.getDeviceSubTypeId());
        List<DeviceMeasurementItem> deviceMeasurementItems = deviceMeasurementItemService.getByDeviceTypeId(deviceType.getId());

        MeasurementItemField measurementItemField = new MeasurementItemField();
        measurementItemField.setMaxValue(0.0);
        measurementItemField.setMinValue(0.0);
        MeasurementUnit measurementUnit = new MeasurementUnit();
        if (StringUtils.isNotEmpty(id)) {
            measurementItemField = measurementItemFieldBaseService.get(MeasurementItemField.class, id);
            measurementUnit = measurementUnitBaseService.get(MeasurementUnit.class, measurementItemField.getMeasurementUnitId());
        }

        // 查询所有单位(不包括隐藏的)
        List<String> measurementUnitGroupNames = measurementUnitBaseService.getAllObjects(MeasurementUnit.class)
                .stream().filter(x -> x.getHide() == Constant.SHOW).map(MeasurementUnit::getGroupName).distinct().collect(Collectors.toList());

        attributes.put("measurementItemField", measurementItemField);
        attributes.put("measurementUnitGroupNames", measurementUnitGroupNames);
        attributes.put("measurementUnit", measurementUnit);
        attributes.put("measurementItemId", measurementItemId);
        // 待选的电气特性
        attributes.put("deviceMeasurementItems", deviceMeasurementItems);

        return new ModelAndView("/backstage/measurement-item-field/measurement-item-field-info", attributes);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public Json delete() {
        measurementItemFieldService.delete(pageMeasurementItemField.getId());
        return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/saveOrUpdate")
    @ResponseBody
    public Json saveOrUpdate() {
        measurementItemFieldService.saveOrUpdate(pageMeasurementItemField);
        return new Json(JsonMessage.SUCCESS);
    }

    @ModelAttribute
    public void setPageMeasurementItemField(MeasurementItemField pageMeasurementItemField) {
        this.pageMeasurementItemField = pageMeasurementItemField;
    }
}

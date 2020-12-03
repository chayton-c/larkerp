package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.backstage.device.DeviceSubType;
import com.yingda.lkj.beans.entity.backstage.device.DeviceType;
import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItem;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItemField;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTemplate;
import com.yingda.lkj.beans.enums.repairclass.RepairClass;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.device.DeviceSubTypeService;
import com.yingda.lkj.service.backstage.device.DeviceTypeService;
import com.yingda.lkj.service.backstage.line.StationService;
import com.yingda.lkj.service.backstage.measurement.MeasurementItemFieldService;
import com.yingda.lkj.service.backstage.measurement.MeasurementItemService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTemplateService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.hql.HqlUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author hood  2020/3/16
 */
@Controller
@RequestMapping("/backstage/measurementTemplate")
public class MeasurementTemplateController extends BaseController {

    @Autowired
    private BaseService<MeasurementTemplate> measurementTemplateBaseService;
    @Autowired
    private BaseService<MeasurementItem> measurementItemBaseService;
    @Autowired
    private BaseService<MeasurementItemField> measurementItemFieldBaseService;
    @Autowired
    private DeviceTypeService deviceTypeService;
    @Autowired
    private DeviceSubTypeService deviceSubTypeService;
    @Autowired
    private MeasurementItemService measurementItemService;
    @Autowired
    private MeasurementItemFieldService measurementItemFieldService;
    @Autowired
    private MeasurementTemplateService measurementTemplateService;
    @Autowired
    private StationService stationService;

    private MeasurementTemplate pageMeasurementTemplate;
    private MeasurementItem pageMeasurementItem;

    /**
     * 测量模板和测量项list
     */
    @RequestMapping("/getList")
    @ResponseBody
    public Json getList() throws Exception {
        Map<String, Object> attributes = new HashMap<>();

        String deviceTypeIdOrDeviceSubTypeId = req.getParameter("deviceTypeIdOrDeviceSubTypeId");
        String stationId = req.getParameter("stationId");
        String name = req.getParameter("name");
        String repairClass = req.getParameter("repairClass");

        Map<String, Object> params = new HashMap<>();
        String sql = """
                SELECT
                	measurementTemplate.id AS id,
                	measurementTemplate.`name` AS `name`,
                	measurementTemplate.description AS description,
                	measurementTemplate.repair_class AS repairClass,
                	measurementTemplate.remark AS remark,
                	measurementTemplate.update_time AS updateTime
                FROM
                	measurement_template AS measurementTemplate
                WHERE
                    1 = 1
                """;
        if (StringUtils.isNotEmpty(stationId)) {
            sql += "AND measurementTemplate.station_id = :stationId\n";
            params.put("stationId", stationId);
            sql += "AND measurementTemplate.type = :type\n";
            params.put("type", MeasurementTemplate.STATION);
        }
        if (StringUtils.isNotEmpty(deviceTypeIdOrDeviceSubTypeId)) {
            sql += "AND (measurementTemplate.device_type_id = :deviceTypeIdOrDeviceSubTypeId OR measurementTemplate.device_sub_type_id = :deviceTypeIdOrDeviceSubTypeId)\n";
            params.put("deviceTypeIdOrDeviceSubTypeId", deviceTypeIdOrDeviceSubTypeId);
            sql += "AND measurementTemplate.type = :type\n";
            params.put("type", MeasurementTemplate.DEVICE);
        }
        if (StringUtils.isNotEmpty(name)) {
            sql += "AND measurementTemplate.name LIKE :name\n";
            params.put("name", "%" + name + "%");
        }
        if (StringUtils.isNotEmpty(repairClass)) {
            sql += "AND measurementTemplate.repairClass = :repairClass\n";
            params.put("repairClass", Byte.valueOf(repairClass));
        }
        sql += "ORDER BY measurementTemplate.update_time DESC";

        List<MeasurementTemplate> measurementTemplates = measurementTemplateBaseService.findSQL(
                sql, params, MeasurementTemplate.class, page.getCurrentPage(), page.getPageSize()
        );
        // 根据repairClass获取修程名称 com.yingda.lkj.beans.enums.repairclass.RepairClass
        measurementTemplates.forEach(x -> {
            RepairClass repairClassPojo = RepairClass.getByType(x.getRepairClass());
            x.setRepairClassName(repairClassPojo.getName());
        });

        List<BigInteger> count = bigIntegerBaseService.findSQL(HqlUtils.getCountSql(sql), params);
        page.setDataTotal(count);

        attributes.put("measurementTemplates", measurementTemplates);
        attributes.put("page", page);

        return new Json(JsonMessage.SUCCESS, attributes);
    }

    @RequestMapping("/info")
    @ResponseBody
    public Json info() {
        Map<String, Object> attributes = new HashMap<>();
        String id = pageMeasurementTemplate.getId();
        String deviceTypeIdOrDeviceSubTypeId = req.getParameter("deviceTypeIdOrDeviceSubTypeId");
        String stationId = req.getParameter("stationId");
        String typeStr = req.getParameter("type");

        MeasurementTemplate measurementTemplate = StringUtils.isNotEmpty(id) ? measurementTemplateService.getById(id) : new MeasurementTemplate();
        measurementTemplate.setType(StringUtils.isNotEmpty(id) ? measurementTemplate.getType() : Byte.parseByte(typeStr));

        DeviceSubType deviceSubType = null;
        DeviceType deviceType = null;
        Station station = null;
        // 查询维表的设备类型名称和设备子类型名称
        if (StringUtils.isEmpty(id)) { // 新增时
            deviceSubType = deviceSubTypeService.getById(deviceTypeIdOrDeviceSubTypeId);
            deviceType = deviceSubType == null ?
                    deviceTypeService.getDeviceType(deviceTypeIdOrDeviceSubTypeId) : deviceTypeService.getByDeviceSubTypeId(deviceSubType.getId());
            station = stationService.getById(stationId);
        } else {
            String deviceTypeId = measurementTemplate.getDeviceTypeId();
            if (StringUtils.isNotEmpty(deviceTypeId))
                deviceType = deviceTypeService.getDeviceType(deviceTypeId);
            String deviceSubTypeId = measurementTemplate.getDeviceSubTypeId();
            if (StringUtils.isNotEmpty(deviceSubTypeId))
                deviceSubType = deviceSubTypeService.getById(deviceSubTypeId);
            stationId = measurementTemplate.getStationId();
            station = stationService.getById(stationId);
        }

        if (deviceType != null) {
            measurementTemplate.setDeviceTypeName(deviceType.getName());
            measurementTemplate.setDeviceTypeId(deviceType.getId());
        }
        if (deviceSubType != null) {
            measurementTemplate.setDeviceSubTypeName(deviceSubType.getName());
            measurementTemplate.setDeviceSubTypeId(deviceSubType.getId());
        }
        if (station != null) {
            measurementTemplate.setStationName(station.getName());
            measurementTemplate.setStationId(station.getId());
        }

        attributes.put("measurementTemplate", measurementTemplate);
        attributes.put("repairClasses", RepairClass.showdown());

        return new Json(JsonMessage.SUCCESS, attributes);
    }

    @RequestMapping("/saveOrUpdate")
    @ResponseBody
    public Json saveOrUpdate() throws Exception {
        String id = pageMeasurementTemplate.getId();

        MeasurementTemplate measurementTemplate = StringUtils.isNotEmpty(id) ?
                measurementTemplateService.getById(id) : new MeasurementTemplate(pageMeasurementTemplate);
        String[] ignoreProperties = {"id", "deviceTypeId", "deviceSubTypeId", "stationId", "addTime", "hide"};
        BeanUtils.copyProperties(pageMeasurementTemplate, measurementTemplate, ignoreProperties);
        measurementTemplate.setUpdateTime(current());

        measurementTemplateBaseService.saveOrUpdate(measurementTemplate);

        return new Json(JsonMessage.SUCCESS, Map.of("measurementTemplate", measurementTemplate));
    }

    @RequestMapping("/delete")
    @ResponseBody
    public Json delete() {
        measurementTemplateService.delete(pageMeasurementTemplate.getId());
        return new Json(JsonMessage.SUCCESS);
    }

    /**
      * 测量模板详情
     stMapping("/measurementTemplateInfo")
    public ModelAndView measurementTemplateInfo(String measurementTemplageId) throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        MeasurementTemplate measurementTemplate = new MeasurementTemplate();

        String repairClass = req.getParameter("repairClass");
        if (StringUtils.isNotEmpty(repairClass))
            measurementTemplate.setRepairClass(Byte.parseByte(repairClass));

        // 查询所有设备类型
        List<DeviceType> deviceTypes = deviceTypeService.getAllDeviceTypes();
        attributes.put("deviceTypes", deviceTypes);

        List<DeviceSubType> deviceSubTypes = new ArrayList<>();
        if (StringUtils.isNotEmpty(measurementTemplageId)) {
            measurementTemplate = measurementTemplateBaseService.get(MeasurementTemplate.class, measurementTemplageId);
            // 查询所在设备类型
            String deviceSubTypeId = measurementTemplate.getDeviceSubTypeId();
            DeviceSubType deviceSubType = deviceSubTypeService.getById(deviceSubTypeId);

            Assert.notNull(deviceSubType, String.format("找不到id为'%s'的deviceSubType", deviceSubTypeId));

            measurementTemplate.setDeviceTypeId(deviceSubType.getDeviceTypeId());
            deviceSubTypes = List.of(deviceSubType);
        }

        attributes.put("measurementTemplate", measurementTemplate);
        attributes.put("deviceSubTypes", deviceSubTypes);

        return null;
    }

    /**
     * 测量模板添加/修改
     */
    @RequestMapping("/measurementTemplateInfo/saveOrUpdate")
    @ResponseBody
    public Json saveOrUpdateMeasurementTemplateInfo() throws Exception {
        String id = pageMeasurementTemplate.getId();

        if (StringUtils.isEmpty(pageMeasurementTemplate.getDeviceSubTypeId()))
            throw new CustomException(new Json(JsonMessage.PARAM_INVALID, "请选择设备子类型"));

        if (StringUtils.isEmpty(id)) {
            pageMeasurementTemplate.setId(UUID.randomUUID().toString());
            pageMeasurementTemplate.setHide(Constant.SHOW);
            pageMeasurementTemplate.setAddTime(current());
            pageMeasurementTemplate.setUpdateTime(current());
        } else {
            MeasurementTemplate measurementTemplate = measurementTemplateBaseService.get(MeasurementTemplate.class, id);
            pageMeasurementTemplate.setAddTime(measurementTemplate.getAddTime());
        }

        measurementTemplateBaseService.saveOrUpdate(pageMeasurementTemplate);
        return new Json(JsonMessage.SUCCESS);
    }

    /**
     * 测量项详情页
     */
    @RequestMapping("/measuremenItemInfo")
    public ModelAndView measuremenItemInfo(String measuremenItemInfoId, String measurementTemplateId) throws Exception {
        MeasurementItem measurementItem = new MeasurementItem();
        measurementItem.setMeasurementTemplateId(measurementTemplateId);

        if (StringUtils.isEmpty(measurementTemplateId))
            throw new CustomException(new Json(JsonMessage.SYS_ERROR, "网络繁忙，请稍后再试"));

        if (StringUtils.isNotEmpty(measuremenItemInfoId))
            measurementItem = measurementItemBaseService.get(MeasurementItem.class, measuremenItemInfoId);

        return new ModelAndView("/backstage/measurement-template/measurement-item-info",
                Map.of("measurementItem", measurementItem, "measurementTemplateId", measurementTemplateId));
    }

    /**
     * 测量项添加/修改
     */
    @RequestMapping("/measurementItemInfo/saveOrUpdate")
    @ResponseBody
    public Json saveOrUpdateMeasurementItemInfo() throws Exception {
        String id = pageMeasurementItem.getId();
        if (StringUtils.isEmpty(id)) {
            pageMeasurementItem.setId(UUID.randomUUID().toString());
            pageMeasurementItem.setHide(Constant.SHOW);
            pageMeasurementItem.setAddTime(current());
            pageMeasurementItem.setUpdateTime(current());
        } else {
            MeasurementItem measurementItem = measurementItemBaseService.get(MeasurementItem.class, id);
            pageMeasurementItem.setAddTime(measurementItem.getAddTime());
        }

        measurementItemBaseService.saveOrUpdate(pageMeasurementItem);

        // 更新测量字段
        String measurementTemplateId = pageMeasurementItem.getMeasurementTemplateId();
        List<MeasurementItem> measurementItems =
                measurementItemService.getMeasurementItemsByMeasurementTemplateId(measurementTemplateId);
        if (measurementItems.isEmpty())
            return new Json(JsonMessage.SUCCESS);

        // 新增时，复制模板
        if (StringUtils.isEmpty(id)) {
            List<MeasurementItemField> measurementItemFields = measurementItemFieldService.getFieldsByMeasurementItem(measurementItems.get(0).getId());
            for (MeasurementItemField measurementItemField : measurementItemFields) {
                MeasurementItemField newFields = new MeasurementItemField();
                BeanUtils.copyProperties(measurementItemField, newFields, "id");
                newFields.setId(UUID.randomUUID().toString());
                newFields.setMeasurementItemId(pageMeasurementItem.getId());
                newFields.setMeasurementTemplateId(measurementTemplateId);
                newFields.setAddTime(current());
                newFields.setUpdateTime(current());
                measurementItemFieldBaseService.saveOrUpdate(newFields);
            }
        }

        return new Json(JsonMessage.SUCCESS);
    }

    @ModelAttribute
    public void setPageMeasurementTemplate(MeasurementTemplate pageMeasurementTemplate) {
        this.pageMeasurementTemplate = pageMeasurementTemplate;
    }

    @ModelAttribute
    public void setPageMeasurementItem(MeasurementItem pageMeasurementItem) {
        this.pageMeasurementItem = pageMeasurementItem;
    }
}

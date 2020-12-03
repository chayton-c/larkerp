package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.device.DeviceSubType;
import com.yingda.lkj.beans.entity.backstage.device.DeviceType;
import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.entity.backstage.measurement.*;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.measurement.MeasurementTemplatePojo;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.device.DeviceSubTypeService;
import com.yingda.lkj.service.backstage.device.DeviceTypeService;
import com.yingda.lkj.service.backstage.line.StationService;
import com.yingda.lkj.service.backstage.measurement.MeasurementItemFieldValueService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTaskDetailService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTemplateService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.RequestUtil;
import com.yingda.lkj.utils.StreamUtil;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务详情(子任务)
 *
 * @author hood  2020/3/17
 */
@Controller
@RequestMapping("/backstage/measurementTaskDetail")
public class MeasurementTaskDetailController extends BaseController {

    @Autowired
    private BaseService<MeasurementTaskDetail> measurementTaskDetailBaseService;
    @Autowired
    private BaseService<MeasurementTemplate> measurementTemplateBaseService;
    @Autowired
    private BaseService<Device> deviceBaseService;
    @Autowired
    private MeasurementTemplateService measurementTemplateService;
    @Autowired
    private StationService stationService;
    @Autowired
    private BaseService<MeasurementItem> measurementItemBaseService;
    @Autowired
    private MeasurementTaskDetailService measurementTaskDetailService;
    @Autowired
    private MeasurementItemFieldValueService measurementItemFieldValueService;
    @Autowired
    private DeviceSubTypeService deviceSubTypeService;
    @Autowired
    private DeviceTypeService deviceTypeService;
    @Autowired
    private BaseService<BigInteger> bigIntegerBaseService;

    private MeasurementTaskDetail pageMeasurementTaskDetail;

    @RequestMapping("")
    public ModelAndView getList() throws Exception {
        Map<String, Object> attributes = new HashMap<>();

        String deviceId = req.getParameter("deviceId");
        String measurementTemplateId = req.getParameter("measurementTemplateId");

        attributes.put("deviceId", deviceId);

        // 查询已有模板
        if (StringUtils.isEmpty(measurementTemplateId)) {
            List<MeasurementTaskDetail> createdMeasurementTaskDetails =
                    measurementTaskDetailService.getMeasurementTaskDetailsByDeviceId(deviceId);
            if (createdMeasurementTaskDetails.size() > 0)
                measurementTemplateId = createdMeasurementTaskDetails.get(0).getMeasurementTemplateId();
        }
        attributes.put("measurementTemplateId", measurementTemplateId);
        attributes.put("titles", new ArrayList<>());

        // 查询供选择的模板
        List<MeasurementTemplate> visiableTemplates = measurementTemplateService.getVisiableTemplates(deviceId);
        attributes.put("visiableTemplates", visiableTemplates);

        if (StringUtils.isEmpty(deviceId))
            throw new CustomException(new Json(JsonMessage.SYS_ERROR));

        if (StringUtils.isEmpty(measurementTemplateId))
            return new ModelAndView("/backstage/measurement-task-detail/measurement-task-detail-list", attributes);

        // 所有扩展字段
        List<MeasurementItem> measurementItems = measurementTemplateService.getItemsAndItemFieldsByTemplateId(measurementTemplateId);

        if (measurementItems.isEmpty())
            return new ModelAndView("/backstage/measurement-task-detail/measurement-task-detail-list", attributes);

        // 查询表头，因为每个measurementItem下面的measurementField一致，所以
        List<MeasurementItemField> titles = new ArrayList<>(measurementItems.get(0).getMeasurementItemFields());

        // 查询符合条件的已完成的子任务
        Map<String, Object> params = new HashMap<>();
        Map<String, String> conditions = new HashMap<>();
        params.put("deviceId", deviceId);
        conditions.put("deviceId", "=");
        params.put("measurementTemplateId", measurementTemplateId);
        conditions.put("measurementTemplateId", "=");
        params.put("finishedStatus", MeasurementTaskDetail.COMPLETED);
        conditions.put("finishedStatus", "=");
        List<MeasurementTaskDetail> measurementTaskDetails = measurementTaskDetailBaseService.getObjcetPagination(MeasurementTaskDetail.class,
                params, conditions, page.getCurrentPage(), page.getPageSize(), "order by executeTime desc");
        page.setDataTotal(measurementTaskDetailBaseService.getObjectNum(MeasurementTaskDetail.class, params, conditions));

        if (measurementTaskDetails.isEmpty())
            return new ModelAndView("/backstage/measurement-task-detail/measurement-task-detail-list", attributes);

        // 查询执行人
        // key:measuermeentTask.id(任务id) value:执行人姓名
        Map<String, String> executeUserNameMap = measurementTaskDetailService.getExecuteUserNames(measurementTaskDetails);
        for (MeasurementTaskDetail measurementTaskDetail : measurementTaskDetails) {
            String taskId = measurementTaskDetail.getMeasurementTaskId();
            String executeUserNames = executeUserNameMap.get(taskId);
            measurementTaskDetail.setExecuteUserNames(executeUserNames);
        }

        List<String> measurementTaskDetailIds = StreamUtil.getList(measurementTaskDetails, MeasurementTaskDetail::getId);

        // 查询所有任务的扩展字段
        // key:测量子任务id，value:子任务id对应的测量值
        Map<String, List<MeasurementItemFieldValue>> measurementItemFieldValueMap =
                measurementItemFieldValueService.getMeasurementItemFieldValues(measurementTemplateId, measurementTaskDetailIds);

        // 使用executeUserNameMap为任务添加执行人
        for (MeasurementTaskDetail measurementTaskDetail : measurementTaskDetails) {
            String measurementTaskId = measurementTaskDetail.getMeasurementTaskId();

            // 在model上添加执行人
            String executeUserNames = executeUserNameMap.get(measurementTaskId);
            measurementTaskDetail.setExecuteUserNames(executeUserNames);
        }

        List<MeasurementTaskDetail> returnList = new ArrayList<>();
        // 使用measurementItemFieldValueMap填写扩展字段的值
        for (MeasurementTaskDetail measurementTaskDetail : measurementTaskDetails) {
            String measurementTaskDetailId = measurementTaskDetail.getId();
            // measurementItemFieldValueMap.get(measurementTaskDetailId)为这个任务的所有扩展字段
            List<MeasurementItemFieldValue> measurementItemFieldValueList = measurementItemFieldValueMap.get(measurementTaskDetailId);

            if (measurementItemFieldValueList == null)
                continue;
            // key:扩展字段id(measurementItemFieldId), value:测量值
            Map<String, MeasurementItemFieldValue> fieldValueMap = measurementItemFieldValueList.stream()
                    .collect(Collectors.toMap(MeasurementItemFieldValue::getMeasurementItemFieldId, x -> x));

            // 每个任务，模板下有几个测量项，返回的列表上就有几条数据
            for (MeasurementItem measurementItem : measurementItems) {
                MeasurementTaskDetail detail = new MeasurementTaskDetail();
                BeanUtils.copyProperties(measurementTaskDetail, detail);

                // 测量项名称(u灯、黄灯。。)
                detail.setMeasurementItemName(measurementItem.getName());

                List<MeasurementItemFieldValue> measurementItemFieldValues = new ArrayList<>();
                for (MeasurementItemField measurementItemField : measurementItem.getMeasurementItemFields()) {
                    MeasurementItemFieldValue measurementItemFieldValue = fieldValueMap.get(measurementItemField.getId());
                    if (measurementItemFieldValue != null)
                        measurementItemFieldValues.add(measurementItemFieldValue);
                }
                // 一个值也没有的不要
                if (measurementItemFieldValues.isEmpty())
                    continue;
                detail.setMeasurementItemFieldValues(measurementItemFieldValues);
                returnList.add(detail);
            }
        }

        attributes.put("measurementItems", measurementItems);
        attributes.put("measurementTaskDetails", returnList);
        attributes.put("titles", titles);
        attributes.put("page", page);

        return new ModelAndView("/backstage/measurement-task-detail/measurement-task-detail-list", attributes);
    }

    /**
     * 测量任务编辑页，子任务详情
     */
    @RequestMapping("/info")
    public ModelAndView info(String measurementTaskDetailId, String measurementTaskId) throws Exception {
        String workAreaId = req.getParameter("workAreaId"); // 被选中任务执行人所在工区  从进入任务管理处传过来的
        if (StringUtils.isEmpty(measurementTaskId))
            throw new CustomException(new Json(JsonMessage.SYS_ERROR));

        User user = RequestUtil.getUser(req);
        String sectionId = user.getSectionId();

        MeasurementTaskDetail measurementTaskDetail = new MeasurementTaskDetail();

        if (StringUtils.isNotEmpty(measurementTaskDetailId)) {
            measurementTaskDetail = measurementTaskDetailBaseService.get(MeasurementTaskDetail.class, measurementTaskDetailId);

            // 填写车站id用于表单回显
            String deviceId = measurementTaskDetail.getDeviceId();
            Device device = deviceBaseService.get(Device.class, deviceId);
            measurementTaskDetail.setStationId(device.getStationId());
            measurementTaskDetail.setDeviceName(device.getName());
        }

        // 查询所有的测量模板
        List<MeasurementTemplate> measurementTemplates = measurementTemplateBaseService.getAllObjects(MeasurementTemplate.class).stream()
                .filter(x -> x.getHide() == Constant.SHOW).collect(Collectors.toList());
        // 查询用户所在站段下的所有车站
        List<Station> stations = stationService.getStationsBySectionId(sectionId);
        List<String> stationIds = StreamUtil.getList(stations, Station::getId);
        // 查询用户所在站段下的所有设备
        List<Device> devices = deviceBaseService.find("from Device where stationId in :stationIds", Map.of("stationIds", stationIds));

        //获取模板名称, 用于回显
        String measurementTemplateId = measurementTaskDetail.getMeasurementTemplateId();
        MeasurementTemplate measurementTemplate = measurementTemplates.stream().filter(x -> x.getId().equals(measurementTemplateId)).reduce(null, (x, y) -> y);
        if (measurementTemplate != null)
            measurementTaskDetail.setTemplateName(measurementTemplate.getName());

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("measurementTaskDetail", measurementTaskDetail);
        attributes.put("measurementTemplates", measurementTemplates);
        attributes.put("stations", stations);
        attributes.put("devices", devices);
        attributes.put("measurementTaskId", measurementTaskId);
        attributes.put("workAreaId", workAreaId);

        return new ModelAndView("/backstage/measurement-task-detail/measurement-task-detail-info", attributes);
    }

    /**
     * 子任务添加/修改
     */
    @RequestMapping("/saveOrUpdate")
    @ResponseBody
    public Json saveOrUpdate() throws Exception {
        MeasurementTaskDetail measurementTaskDetail;

        String id = pageMeasurementTaskDetail.getId();
        if (StringUtils.isEmpty(id)) {
            String[] deviceIds = pageMeasurementTaskDetail.getDeviceId().split(",");
            for (String deviceId : deviceIds) {
                pageMeasurementTaskDetail.setDeviceId(deviceId);
                measurementTaskDetail = new MeasurementTaskDetail(pageMeasurementTaskDetail);
                measurementTaskDetail.setUpdateTime(current());
                measurementTaskDetailBaseService.saveOrUpdate(measurementTaskDetail);
            }

        } else {
            synchronized (pageMeasurementTaskDetail.getId()) {
                measurementTaskDetail = measurementTaskDetailBaseService.get(MeasurementTaskDetail.class, id);
                measurementTaskDetail.setMeasurementTemplateId(pageMeasurementTaskDetail.getMeasurementTemplateId());
                measurementTaskDetail.setMeasurementTaskId(pageMeasurementTaskDetail.getMeasurementTaskId());
                measurementTaskDetail.setDeviceId(pageMeasurementTaskDetail.getDeviceId());
                measurementTaskDetail.setUpdateTime(current());
                measurementTaskDetailBaseService.saveOrUpdate(measurementTaskDetail);
            }
        }
        return new Json(JsonMessage.SUCCESS);
    }

    /**
     * 添加测量模板
     */
    @RequestMapping("/addTemplate")
    public ModelAndView addTemplate() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        String deviceTypeId = req.getParameter("deviceTypeId");
        String name = req.getParameter("name"); // 模板名称
        String repairClass = req.getParameter("repairClass");//修程

        List<DeviceType> deviceTypes = deviceTypeService.getAllDeviceTypes();
        attributes.put("deviceTypes", deviceTypes);

        Map<String, Object> params = new HashMap<>();
        Map<String, String> conditions = new HashMap<>();

        params.put("hide", Constant.SHOW);
        conditions.put("hide", "=");

        if (StringUtils.isNotEmpty(deviceTypeId)) {
            attributes.put("deviceTypeId", deviceTypeId);
            List<DeviceSubType> deviceSubTypes = deviceSubTypeService.getByDeviceTypeId(deviceTypeId);
            params.put("deviceSubTypeId", StreamUtil.getList(deviceSubTypes, DeviceSubType::getId));
            conditions.put("deviceSubTypeId", "in");
        }
        if (StringUtils.isNotEmpty(name)) {
            attributes.put("name", name);
            params.put("name", "%" + name + "%");
            conditions.put("name", "like");
        }

        if (StringUtils.isNotEmpty(repairClass)) {
            attributes.put("repairClass", repairClass);
            params.put("repairClass", (byte) Integer.valueOf(repairClass).intValue());
            conditions.put("repairClass", "=");
        }
        params.put("hide", Constant.SHOW);
        conditions.put("hide", "=");

        page.setPageSize(10);
        List<MeasurementTemplate> measurementTemplates = measurementTemplateBaseService.getObjcetPagination(MeasurementTemplate.class,
                params, conditions, page.getCurrentPage(), page.getPageSize(), "order by addTime desc");
        page.setDataTotal(measurementTemplateBaseService.getObjectNum(MeasurementTemplate.class, params, conditions));

        // 查询所有的测量项
        params.clear();
        conditions.clear();
        params.put("hide", Constant.SHOW);
        conditions.put("hide", "=");
        List<MeasurementItem> measurementItems = measurementItemBaseService.getObjcetPagination(MeasurementItem.class, params, conditions
                , 1, 99999, "order by addTime desc");
        Map<String, List<MeasurementItem>> measurementItemMap =
                measurementItems.stream().collect(Collectors.groupingBy(MeasurementItem::getMeasurementTemplateId));

        List<MeasurementTemplatePojo> measurementTemplatePojos = new ArrayList<>();
        for (MeasurementTemplate measurementTemplate : measurementTemplates) {
            measurementTemplatePojos.add(new MeasurementTemplatePojo(measurementTemplate));

            List<MeasurementItem> measurementItemList = measurementItemMap.get(measurementTemplate.getId());
            if (measurementItemList == null)
                continue;

            List<MeasurementTemplatePojo> measurementItemPojos =
                    measurementItemList.stream().map(x -> new MeasurementTemplatePojo(x, measurementTemplate)).collect(Collectors.toList());
            measurementTemplatePojos.addAll(measurementItemPojos);
        }

        attributes.put("measurementTemplatePojos", measurementTemplatePojos);
        attributes.put("page", page);
        return new ModelAndView("/backstage/measurementtasksubmit/add_template", attributes);
    }


    /**
     * 添加测量任务
     */
    @RequestMapping("/addDevices")
    public ModelAndView addDevices() throws Exception {
        String workAreaId = req.getParameter("workAreaId");// 用来查询执行人所在工区--车站下的设备

        String deviceName = req.getParameter("deviceName");
        String stationName = req.getParameter("stationName");
        String deviceTypeName = req.getParameter("deviceTypeName");
        String deviceCode = req.getParameter("deviceCode");
        String railwayLineName = req.getParameter("railwayLineName");
        String workshopName = req.getParameter("workshopName");
        Map<String, Object> attributes = new HashMap<>();
        Map<String, Object> params = new HashMap<>();

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT\n");
        sqlBuilder.append("  device.id as id,\n");
        sqlBuilder.append("  device.code as code,\n");
        sqlBuilder.append("  device.name as name,\n");
        sqlBuilder.append("  station.name as stationName,\n");
        sqlBuilder.append("  line.name as railwayLineName,\n");
        sqlBuilder.append("  workshop.name as workshopName,\n");
        sqlBuilder.append("  deviceType.name as deviceTypeName,\n");
        sqlBuilder.append("  deviceType.id as deviceTypeId,\n");
        sqlBuilder.append("  device.add_time as addTime\n");
        sqlBuilder.append("FROM\n");
        sqlBuilder.append("  device\n");
        sqlBuilder.append("  LEFT JOIN railway_line line ON line.id = device.railway_line_id\n");
        sqlBuilder.append("  LEFT JOIN station ON station.id = device.station_id\n");
        sqlBuilder.append("  LEFT JOIN organization workArea ON workArea.id = station.work_area_id\n");
        sqlBuilder.append("  LEFT JOIN organization workshop ON workshop.id = workArea.parent_id\n"); // 设备所在车站的所属工区的上级，为所属车间
        sqlBuilder.append("  LEFT JOIN organization section ON section.id = workshop.parent_id\n"); // 车间上级为站段
        sqlBuilder.append("  LEFT JOIN device_type deviceType ON deviceType.id = device.device_type_id \n");
        sqlBuilder.append("WHERE\n");
        sqlBuilder.append("  1 = 1\n");
        sqlBuilder.append("  AND section.id = :sectionId\n");
        params.put("sectionId", getSectionId());
//        sqlBuilder.append("  AND station.work_area_id = :workAreaId ");
//        params.put("workAreaId", workAreaId);
        if (StringUtils.isNotEmpty(deviceName)) {
            sqlBuilder.append("  AND device.name LIKE :deviceName\n");
            params.put("deviceName", "%" + deviceName + "%");
        }
        if (StringUtils.isNotEmpty(stationName)) {
            sqlBuilder.append("  AND station.name LIKE :stationName\n");
            params.put("stationName", "%" + stationName + "%");
        }
        if (StringUtils.isNotEmpty(workshopName)) {
            sqlBuilder.append("  AND workshop.name LIKE :workshopName\n");
            params.put("workshopName", "%" + workshopName + "%");
        }
        if (StringUtils.isNotEmpty(deviceTypeName)) {
            sqlBuilder.append("  AND deviceType.name LIKE :deviceTypeName\n");
            params.put("deviceTypeName", "%" + deviceTypeName + "%");
        }
        if (StringUtils.isNotEmpty(deviceCode)) {
            sqlBuilder.append("  AND device.code LIKE :deviceCode\n");
            params.put("deviceCode", "%" + deviceCode + "%");
        }
        if (StringUtils.isNotEmpty(railwayLineName)) {
            sqlBuilder.append("  AND line.name LIKE :railwayLineName\n");
            params.put("railwayLineName", "%" + railwayLineName + "%");
        }
        sqlBuilder.append(" order by device.add_time desc ");
        page.setPageSize(10);
        List<Device> devices = deviceBaseService.findSQL(sqlBuilder.toString(), params, Device.class, page.getCurrentPage(), page.getPageSize());
        String countSql = HqlUtils.getCountSql(sqlBuilder.toString());
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        page.setDataTotal(count.isEmpty() ? 0 : count.get(0).longValue());

        attributes.put("deviceName", deviceName);
        attributes.put("stationName", stationName);
        attributes.put("deviceTypeName", deviceTypeName);
        attributes.put("deviceCode", deviceCode);
        attributes.put("railwayLineName", railwayLineName);
        attributes.put("workshopName", workshopName);
        attributes.put("devices", devices);
        attributes.put("workAreaId", workAreaId);

        return new ModelAndView("/backstage/measurementtasksubmit/add_devices", attributes);
    }


    @ModelAttribute
    public void setPageMeasurementTaskDetail(MeasurementTaskDetail pageMeasurementTaskDetail) {
        this.pageMeasurementTaskDetail = pageMeasurementTaskDetail;
    }

}

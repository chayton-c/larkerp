package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.backstage.device.*;
import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTemplate;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.enums.devicemaintenance.DeviceMaintenancePlanFinishStatus;
import com.yingda.lkj.beans.enums.devicemaintenance.DeviceMaintenancePlanStrategy;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.device.DeviceMaintenancePlanPojo;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.device.DeviceMaintenancePlanDeviceService;
import com.yingda.lkj.service.backstage.device.DeviceMaintenancePlanService;
import com.yingda.lkj.service.backstage.device.DeviceTypeService;
import com.yingda.lkj.service.backstage.line.RailwayLineService;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.hql.HqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 设备维护计划页
 *
 * @author hood  2020/3/30
 */
@Controller
@RequestMapping("/backstage/deviceMaintenancePlan")
public class DeviceMaintenancePlanController extends BaseController {

    @Autowired
    private BaseService<DeviceMaintenancePlan> deviceMaintenancePlanBaseService;
    @Autowired
    private BaseService<DeviceMaintenancePlanPojo> deviceMaintenancePlanPojoBaseService;
    @Autowired
    private BaseService<DeviceMaintenancePlanUser> deviceMaintenancePlanUserBaseService;
    @Autowired
    private OrganizationClientService organizationClientService;
    @Autowired
    private BaseService<DeviceType> deviceTypeBaseService;
    @Autowired
    private BaseService<MeasurementTemplate> measurementTemplateBaseService;
    @Autowired
    private BaseService<User> userBaseService;
    @Autowired
    private DeviceMaintenancePlanService deviceMaintenancePlanService;
    @Autowired
    private BaseService<DeviceMaintenancePlanDevice> deviceMaintenancePlanDeviceBaseService;
    @Autowired
    private BaseService<Device> deviceBaseService;
    @Autowired
    private BaseService<BigInteger> bigIntegerBaseService;
    @Autowired
    private DeviceMaintenancePlanDeviceService deviceMaintenancePlanDeviceService;
    @Autowired
    private RailwayLineService railwayLineService;

    private DeviceMaintenancePlan pageDeviceMaintenancePlan;

    @RequestMapping("")
    public ModelAndView getList() throws Exception {
        String workshopName = req.getParameter("workshopName");
        String executeUserNames = req.getParameter("executeUserNames");
        String repairClassStr = req.getParameter("repairClass");
        String executeStatusStr = req.getParameter("executeStatusStr");

        attributes.put("repairClass", repairClassStr);
        attributes.put("executeUserNames", executeUserNames);
        attributes.put("workshopName", workshopName);
        attributes.put("page", page);
        attributes.put("executeStatusStr", executeStatusStr);
        attributes.put("executeStatusList", DeviceMaintenancePlanFinishStatus.values());

        Map<String, Object> params = new HashMap<>();

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder
                .append("SELECT\n")
                .append("  plan.id AS id,\n")
                .append("  submitUser.display_name AS submitUserName,\n")
                .append("  plan.name AS planName,\n")
                .append("  workshop.id AS workshopId,\n")
                .append("  workshop.name AS workshopName,\n")
                .append("  plan.execution_strategy AS executionStrategy,\n")
                .append("  plan.execution_cycle AS executionCycle,\n")
                .append("  plan.execute_status AS executeStatus,\n")
                .append("  plan.execute_time AS executeTime,\n")
                .append("  executors.executorNames AS executeUserNames \n")
                .append("FROM\n")
                .append("  device_maintenance_plan plan\n")
                .append("  LEFT JOIN `user` submitUser ON submitUser.id = plan.submit_user_id\n") // 用户表
                .append("  LEFT JOIN organization workshop ON workshop.id = plan.workshop_id\n") // 组织表(车间)
                .append("  LEFT JOIN ( SELECT \n") // 执行人表(一对多，group_concat执行人姓名)
                .append("                GROUP_CONCAT( execute_user_display_name ) AS executorNames,\n")
                .append("                device_maintenance_plan_id AS planId\n")
                .append("              FROM device_maintenance_plan_user \n")
                .append("              GROUP BY device_maintenance_plan_id ) executors ON executors.planId = plan.id \n")
                .append("WHERE\n")
                .append("  plan.section_id = :sectionId\n")
                .append("AND plan.hide = :hide\n")
                .append("AND submitUser.id = :submitUserId\n");

        params.put("submitUserId", getUser().getId());
        params.put("sectionId", getSectionId());
        params.put("hide", Constant.SHOW);
        if (StringUtils.isNotEmpty(executeStatusStr)) {
            sqlBuilder.append("AND plan.execute_status = :executeStatus\n");
            params.put("executeStatus", Byte.parseByte(executeStatusStr));
        }
        if (StringUtils.isNotEmpty(workshopName)) {
            sqlBuilder.append("AND workshop.name like :workshopName\n");
            params.put("workshopName", "%" + workshopName + "%");
        }
        if (StringUtils.isNotEmpty(executeUserNames)) {
            sqlBuilder.append("AND executors.executorNames like :executeUserNames\n");
            params.put("executeUserNames", "%" + executeUserNames + "%");
        }
        if (StringUtils.isNotEmpty(repairClassStr)) {
            byte repairClass = Byte.parseByte(repairClassStr);
            sqlBuilder.append("AND plan.repair_class = :repairClass\n");
            params.put("repairClass", repairClass);
        }
        sqlBuilder.append(" ORDER BY plan.add_time DESC");
        String sql = sqlBuilder.toString();
        List<DeviceMaintenancePlanPojo> deviceMaintenancePlanPojos = deviceMaintenancePlanPojoBaseService.findSQL(
                sql, params, DeviceMaintenancePlanPojo.class, page.getCurrentPage(), page.getPageSize());

        String countSql = HqlUtils.getCountSql(sql);
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        page.setDataTotal(count.isEmpty() ? 0 : count.get(0).longValue());

        // 查询设备名
        List<String> deviceMaintenancePlanIds = StreamUtil.getList(deviceMaintenancePlanPojos, DeviceMaintenancePlanPojo::getId);

        String deviceSql = new StringBuilder()
                .append("SELECT ")
                .append("  device.name AS deviceName, ")
                .append("  device_maintenance_plan_device.device_maintenance_plan_id AS deviceMaintenancePlanId ")
                .append("FROM device_maintenance_plan_device ")
                .append("LEFT JOIN device ON device.id = device_maintenance_plan_device.device_id ")
                .append("WHERE ")
                .append("  device_maintenance_plan_device.device_maintenance_plan_id IN :deviceMaintenancePlanIds ")
                .append("AND device_maintenance_plan_device.hide = :hide ")
                .append("ORDER BY device_maintenance_plan_device.seq ")
                .toString();
        List<DeviceMaintenancePlanDevice> deviceMaintenancePlanDevices = deviceMaintenancePlanDeviceBaseService.findSQL(
                deviceSql, Map.of("deviceMaintenancePlanIds", deviceMaintenancePlanIds, "hide", Constant.SHOW),
                DeviceMaintenancePlanDevice.class, 1, 9999
        );

        Map<String, List<DeviceMaintenancePlanDevice>> deviceMaintenancePlanUserMap =
                deviceMaintenancePlanDevices.stream().collect(Collectors.groupingBy(DeviceMaintenancePlanDevice::getDeviceMaintenancePlanId));
        // end of  设备名

        for (DeviceMaintenancePlanPojo deviceMaintenancePlanPojo : deviceMaintenancePlanPojos) {
            String deviceMaintenancePlanId = deviceMaintenancePlanPojo.getId();
            List<DeviceMaintenancePlanDevice> deviceMaintenancePlanDeviceList = deviceMaintenancePlanUserMap.get(deviceMaintenancePlanId);

            if (deviceMaintenancePlanDeviceList != null) {
                List<String> deviceNames =
                        deviceMaintenancePlanDeviceList.stream().map(DeviceMaintenancePlanDevice::getDeviceName).collect(Collectors.toList());
                // 设备名
                deviceMaintenancePlanPojo.setDeviceNames(String.join(", ", deviceNames));
            }

            // 所属车间
//            Organization workshop = organizationClientService.getById(deviceMaintenancePlanPojo.getWorkshopId());
//            deviceMaintenancePlanPojo.setWorkshopName(workshop.getName());

            // 策略名称
            byte executionStrategy = deviceMaintenancePlanPojo.getExecutionStrategy();
            String strategyName = DeviceMaintenancePlanStrategy.getName(executionStrategy);
            deviceMaintenancePlanPojo.setExecutionStrategyName(strategyName);

            // 完成状态
            byte planExecuteStatus = deviceMaintenancePlanPojo.getExecuteStatus();
            String executeStatusName = DeviceMaintenancePlanFinishStatus.getName(planExecuteStatus);
            deviceMaintenancePlanPojo.setExecuteStatusName(executeStatusName);
        }

        attributes.put("deviceMaintenancePlanPojos", deviceMaintenancePlanPojos);

        return createModelAndView("/backstage/device-maintenance-plan/device-maintenance-plan-list");
    }

    @RequestMapping("/infoPage")
    public ModelAndView infoPage(String id) throws Exception {
        String sectionId = getSectionId();
        String repairClass = req.getParameter("repairClass");
        Map<String, Object> attribute = new HashMap<>();

        DeviceMaintenancePlan deviceMaintenancePlan;
        if (id != null) {
            deviceMaintenancePlan = deviceMaintenancePlanBaseService.get(DeviceMaintenancePlan.class, id);

            // 执行人
            List<DeviceMaintenancePlanUser> deviceMaintenancePlanUsers = deviceMaintenancePlanUserBaseService.find(
                    "from DeviceMaintenancePlanUser where deviceMaintenancePlanId = :deviceMaintenancePlanId",
                    Map.of("deviceMaintenancePlanId", deviceMaintenancePlan.getId())
            );

            List<String> executeUserNames = StreamUtil.getList(deviceMaintenancePlanUsers, DeviceMaintenancePlanUser::getExecuteUserDisplayName);
            List<String> executeUserIds = StreamUtil.getList(deviceMaintenancePlanUsers, DeviceMaintenancePlanUser::getExecuteUserId);

            attribute.put("executeUserNames", String.join(",", executeUserNames));
            attribute.put("executeUserIds", String.join(",", executeUserIds));
            attribute.put("deviceMaintenancePlan", deviceMaintenancePlan);
        }

        // 查询可选择的车间
        List<Organization> workshops = organizationClientService.getSlave(sectionId);

        // 查询所有设备类型
        List<DeviceType> deviceTypes = deviceTypeBaseService.getAllObjects(DeviceType.class);

        attribute.put("repairClass", repairClass);
        attribute.put("workshops", workshops);
        attribute.put("deviceTypes", deviceTypes);
        // 执行策略
        attribute.put("deviceMaintenancePlanStrategyList", DeviceMaintenancePlanStrategy.values());

        return new ModelAndView("/backstage/device-maintenance-plan/device-maintenance-plan-info", attribute);
    }

    @RequestMapping("/saveOrUpdate")
    @ResponseBody
    public Json saveOrUpdate() throws Exception {
        String executionDateMonth = req.getParameter("executionDateMonth");
        String executionDateYear = req.getParameter("executionDateYear");
        String executionDateHour = req.getParameter("executionDateHour");
        if (pageDeviceMaintenancePlan.getExecutionStrategy() == DeviceMaintenancePlanStrategy.DESIGNATED_DAY_OF_THE_MONTH.getStrategy())
            pageDeviceMaintenancePlan.setExecutionDate(executionDateMonth);
        if (pageDeviceMaintenancePlan.getExecutionStrategy() == DeviceMaintenancePlanStrategy.DESIGNATED_DAY_OF_THE_YEAR.getStrategy())
            pageDeviceMaintenancePlan.setExecutionDate(executionDateYear);
        if (pageDeviceMaintenancePlan.getExecutionStrategy() == DeviceMaintenancePlanStrategy.DESIGNATED_HOUR_OF_THE_DAY.getStrategy())
            pageDeviceMaintenancePlan.setExecutionDate(executionDateHour);

        deviceMaintenancePlanService.saveOrUpdate(pageDeviceMaintenancePlan, getUser());

        return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/close")
    @ResponseBody
    public Json close() {
        String deviceMaintencancePlanId = req.getParameter("deviceMaintencancePlanId");
        deviceMaintenancePlanService.closePlan(deviceMaintencancePlanId);
        return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/start")
    @ResponseBody
    public Json start() {
        String deviceMaintencancePlanId = req.getParameter("deviceMaintencancePlanId");
        deviceMaintenancePlanService.startPlan(deviceMaintencancePlanId);
        return new Json(JsonMessage.SUCCESS);
    }

    /**
     * 执行人选择 —— 用户列表
     */
    @RequestMapping("/userList")
    public ModelAndView userList() throws Exception {
        String sectionId = getSectionId();

        Map<String, Object> attribute = new HashMap<>();
        String workshopId = req.getParameter("workshopId");
        String displayName = req.getParameter("displayName");
        attribute.put("workshopId", workshopId);
        attribute.put("displayName", displayName);

        // 查询使用到的车间
        List<Organization> workshops = organizationClientService.getSlave(sectionId);
        attribute.put("workshops", workshops);

        if (StringUtils.isEmpty(workshopId))
            return new ModelAndView("/backstage/device-maintenance-plan/device-maintenance-plan-info-user-list", attribute);

        Map<String, Object> params = new HashMap<>();
        Map<String, String> conditions = new HashMap<>();

        params.put("workshopId", workshopId);
        conditions.put("workshopId", "=");

        if (StringUtils.isNotEmpty(displayName)) {
            params.put("displayName", "%" + displayName + "%");
            conditions.put("displayName", "like");
        }

        List<User> users = userBaseService.getObjcetPagination(
                User.class, params, conditions, page.getCurrentPage(), page.getPageSize(), "");

        attribute.put("users", users);
        return new ModelAndView("/backstage/device-maintenance-plan/device-maintenance-plan-info-user-list", attribute);
    }

    @RequestMapping("/test")
    @ResponseBody
    public Json test() {
        deviceMaintenancePlanService.timedGenerateMeasurementTask();
        return new Json(JsonMessage.SUCCESS);
    }

    /**
     * 计划设备列表
     */
    @RequestMapping("/deviceList")
    public ModelAndView deviceList() throws Exception {
        String deviceMaintenancePlanId = req.getParameter("deviceMaintenancePlanId");
        String deviceName = req.getParameter("deviceName");
        String stationName = req.getParameter("stationName");
        String workshopId = req.getParameter("workshopId");

        attributes.put("deviceName", deviceName);
        attributes.put("workshopId", workshopId);
        attributes.put("stationName", stationName);
        attributes.put("deviceMaintenancePlanId", deviceMaintenancePlanId);

        DeviceMaintenancePlan deviceMaintenancePlan = deviceMaintenancePlanBaseService.get(DeviceMaintenancePlan.class, deviceMaintenancePlanId);
        attributes.put("deviceMaintenancePlan", deviceMaintenancePlan);

        // 查询线路
        List<RailwayLine> railwayLines = railwayLineService.getRailwayLinesByBureauId(getUser().getBureauId());
        attributes.put("railwayLines", railwayLines);

        Map<String, Object> params = new HashMap<>();
        params.put("deviceMaintenancePlanId", deviceMaintenancePlanId);
        params.put("hide", Constant.SHOW);

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT\n");
        sqlBuilder.append("  planDevice.id AS id,\n");
        sqlBuilder.append("  planDevice.seq AS seq,\n");
        sqlBuilder.append("  planDevice.device_id AS deviceId,\n");
        sqlBuilder.append("  template.name AS measurementTemplateName,\n");
        sqlBuilder.append("  device.name AS deviceName,\n");
        sqlBuilder.append("  device.code AS deviceCode,\n");
        sqlBuilder.append("  device.device_sub_type_id AS deviceSubTypeId,\n");
        sqlBuilder.append("  device_type.name AS deviceTypeName,\n");
        sqlBuilder.append("  device_sub_type.name AS deviceSubTypeName,\n");
        sqlBuilder.append("  station.name AS stationName,\n");
        sqlBuilder.append("  line.name AS railwayLineName \n");
        sqlBuilder.append("FROM\n");
        sqlBuilder.append("  device_maintenance_plan_device planDevice\n");
        sqlBuilder.append("  LEFT JOIN measurement_template template ON template.id = planDevice.measurement_template_id \n");
        sqlBuilder.append("  LEFT JOIN device ON device.id = planDevice.device_id\n");
        sqlBuilder.append("  LEFT JOIN device_type ON device_type.id = device.device_type_id\n");
        sqlBuilder.append("  LEFT JOIN device_sub_type ON device_sub_type.id = device.device_sub_type_id\n");
        sqlBuilder.append("  LEFT JOIN station ON device.station_id = station.id\n");
        sqlBuilder.append("  LEFT JOIN railway_line line ON line.id = device.railway_line_id \n");
        sqlBuilder.append("WHERE\n");
        sqlBuilder.append("  planDevice.device_maintenance_plan_id = :deviceMaintenancePlanId \n");
        sqlBuilder.append("  AND planDevice.hide = :hide \n");

        if (StringUtils.isNotEmpty(deviceName)) {
            params.put("deviceName", "%" + deviceName + "%");
            sqlBuilder.append("AND device.name like :deviceName \n");
        }
        if (StringUtils.isNotEmpty(stationName)) {
            params.put("stationName", "%" + stationName + "%");
            sqlBuilder.append("AND station.name like :stationName \n");
        }
        sqlBuilder.append("ORDER BY\n");
        sqlBuilder.append("  planDevice.seq");

        String sql = sqlBuilder.toString();
        List<DeviceMaintenancePlanDevice> deviceMaintenancePlanDevices = deviceMaintenancePlanDeviceBaseService.findSQL(
                sql, params, DeviceMaintenancePlanDevice.class, page.getCurrentPage(), page.getPageSize()
        );
        String countSql = HqlUtils.getCountSql(sql);
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        page.setDataTotal(count.isEmpty() ? 0 : count.get(0).longValue());

        attributes.put("deviceMaintenancePlanDevices", deviceMaintenancePlanDevices);

        return new ModelAndView("/backstage/device-maintenance-plan/device-maintenance-plan-device-list", attributes);
    }

    /**
     * 计划设备列表
     */
    @RequestMapping("/deviceListSortPage")
    public ModelAndView deviceListSortPage() throws Exception {
        String deviceMaintenancePlanId = req.getParameter("deviceMaintenancePlanId");
        String deviceName = req.getParameter("deviceName");
        String stationName = req.getParameter("stationName");
        String workshopId = req.getParameter("workshopId");

        attributes.put("deviceName", deviceName);
        attributes.put("workshopId", workshopId);
        attributes.put("stationName", stationName);
        attributes.put("deviceMaintenancePlanId", deviceMaintenancePlanId);

        DeviceMaintenancePlan deviceMaintenancePlan = deviceMaintenancePlanBaseService.get(DeviceMaintenancePlan.class, deviceMaintenancePlanId);
        attributes.put("deviceMaintenancePlan", deviceMaintenancePlan);

        // 查询线路
        List<RailwayLine> railwayLines = railwayLineService.getRailwayLinesByBureauId(getUser().getBureauId());
        attributes.put("railwayLines", railwayLines);

        Map<String, Object> params = new HashMap<>();
        params.put("deviceMaintenancePlanId", deviceMaintenancePlanId);
        params.put("hide", Constant.SHOW);

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT\n");
        sqlBuilder.append("  planDevice.id AS id,\n");
        sqlBuilder.append("  planDevice.seq AS seq,\n");
        sqlBuilder.append("  planDevice.device_id AS deviceId,\n");
        sqlBuilder.append("  template.name AS measurementTemplateName,\n");
        sqlBuilder.append("  device.name AS deviceName,\n");
        sqlBuilder.append("  device.code AS deviceCode,\n");
        sqlBuilder.append("  device.device_sub_type_id AS deviceSubTypeId,\n");
        sqlBuilder.append("  device_type.name AS deviceTypeName,\n");
        sqlBuilder.append("  device_sub_type.name AS deviceSubTypeName,\n");
        sqlBuilder.append("  station.name AS stationName,\n");
        sqlBuilder.append("  line.name AS railwayLineName \n");
        sqlBuilder.append("FROM\n");
        sqlBuilder.append("  device_maintenance_plan_device planDevice\n");
        sqlBuilder.append("  LEFT JOIN measurement_template template ON template.id = planDevice.measurement_template_id \n");
        sqlBuilder.append("  LEFT JOIN device ON device.id = planDevice.device_id\n");
        sqlBuilder.append("  LEFT JOIN device_type ON device_type.id = device.device_type_id\n");
        sqlBuilder.append("  LEFT JOIN device_sub_type ON device_sub_type.id = device.device_sub_type_id\n");
        sqlBuilder.append("  LEFT JOIN station ON device.station_id = station.id\n");
        sqlBuilder.append("  LEFT JOIN railway_line line ON line.id = device.railway_line_id \n");
        sqlBuilder.append("WHERE\n");
        sqlBuilder.append("  planDevice.device_maintenance_plan_id = :deviceMaintenancePlanId \n");
        sqlBuilder.append("  AND planDevice.hide = :hide \n");

        if (StringUtils.isNotEmpty(deviceName)) {
            params.put("deviceName", "%" + deviceName + "%");
            sqlBuilder.append("AND device.name like :deviceName \n");
        }
        if (StringUtils.isNotEmpty(stationName)) {
            params.put("stationName", "%" + stationName + "%");
            sqlBuilder.append("AND station.name like :stationName \n");
        }
        sqlBuilder.append("ORDER BY\n");
        sqlBuilder.append("  planDevice.seq");

        String sql = sqlBuilder.toString();
        List<DeviceMaintenancePlanDevice> deviceMaintenancePlanDevices = deviceMaintenancePlanDeviceBaseService.findSQL(
                sql, params, DeviceMaintenancePlanDevice.class, page.getCurrentPage(), page.getPageSize()
        );
        String countSql = HqlUtils.getCountSql(sql);
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        page.setDataTotal(count.isEmpty() ? 0 : count.get(0).longValue());

        attributes.put("deviceMaintenancePlanDevices", deviceMaintenancePlanDevices);

        return new ModelAndView("/backstage/device-maintenance-plan/device-maintenance-plan-device-list-sort-page", attributes);
    }

    @RequestMapping("/updateSeq")
    @ResponseBody
    public Json updateSeq() throws Exception {
        String deviceMaintenancePlanId = req.getParameter("id");
        int seq = Integer.parseInt(req.getParameter("seq"));

        DeviceMaintenancePlanDevice deviceMaintenancePlanDevice =
                deviceMaintenancePlanDeviceBaseService.get(DeviceMaintenancePlanDevice.class, deviceMaintenancePlanId);

        deviceMaintenancePlanDevice.setSeq(seq);
        deviceMaintenancePlanDevice.setUpdateTime(current());
        deviceMaintenancePlanDeviceBaseService.saveOrUpdate(deviceMaintenancePlanDevice);

        return new Json(JsonMessage.SUCCESS);
    }

    @Autowired
    private DeviceTypeService deviceTypeService;

    /**
     * 添加设备列表
     */
    @RequestMapping("/spareDeviceList")
    public ModelAndView spareDeviceList(String deviceMaintenancePlanId) throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("deviceMaintenancePlanId", deviceMaintenancePlanId);

        String stationName = req.getParameter("stationName");
        String deviceName = req.getParameter("deviceName");
        String deviceCode = req.getParameter("deviceCode");
        String deviceTypeId = req.getParameter("deviceTypeId");

        attributes.put("deviceName", deviceName);
        attributes.put("stationName", stationName);
        attributes.put("deviceCode", deviceCode);
        attributes.put("deviceTypeId", deviceTypeId);

        // 设备类型筛选项
        List<DeviceType> deviceTypes = deviceTypeService.getAllDeviceTypes();
        attributes.put("deviceTypes", deviceTypes);

        DeviceMaintenancePlan deviceMaintenancePlan = deviceMaintenancePlanBaseService.get(DeviceMaintenancePlan.class, deviceMaintenancePlanId);
        String workshopId = deviceMaintenancePlan.getWorkshopId();


        Map<String, Object> params = new HashMap<>();
        params.put("hide", Constant.SHOW);
        params.put("workshopId", workshopId);

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT\n");
        sqlBuilder.append("  device.id AS id,\n");
        sqlBuilder.append("  device.NAME AS name,\n");
        sqlBuilder.append("  device.CODE AS code,\n");
        sqlBuilder.append("  line.NAME AS railwayLineName,\n");
        sqlBuilder.append("  station.NAME AS stationName \n");
        sqlBuilder.append("FROM\n");
        sqlBuilder.append("  device\n");
        sqlBuilder.append("  LEFT JOIN railway_line line ON line.id = device.railway_line_id\n");
        sqlBuilder.append("  LEFT JOIN station ON station.id = device.station_id \n");
        sqlBuilder.append("  LEFT JOIN organization workArea ON workArea.id = station.work_area_id \n");
        sqlBuilder.append("WHERE\n");
        sqlBuilder.append("  device.hide = :hide\n");
        sqlBuilder.append("  AND workArea.parent_id = :workshopId\n");
        if (StringUtils.isNotEmpty(deviceName)) {
            sqlBuilder.append(" AND device.name like :deviceName\n");
            params.put("deviceName", "%" + deviceName + "%");
        }
        if (StringUtils.isNotEmpty(stationName)) {
            sqlBuilder.append(" AND station.name like :stationName\n");
            params.put("stationName", "%" + stationName + "%");
        }
        if (StringUtils.isNotEmpty(deviceCode)) {
            sqlBuilder.append(" AND device.code like :deviceCode\n");
            params.put("deviceCode", "%" + deviceCode + "%");
        }
        if (StringUtils.isNotEmpty(deviceTypeId)) {
            sqlBuilder.append(" AND device.device_type_id = :deviceTypeId\n");
            params.put("deviceTypeId", deviceTypeId);
        }

        String sql = sqlBuilder.toString();
        List<Device> devices = deviceBaseService.findSQL(sql, params, Device.class, page.getCurrentPage(), page.getPageSize());

        String countSql = HqlUtils.getCountSql(sql);
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        page.setDataTotal(count.isEmpty() ? 0 : count.get(0).longValue());

        attributes.put("devices", devices);
        attributes.put("page", page);

        return new ModelAndView("/backstage/device-maintenance-plan/device-maintenance-plan-spare-device-list", attributes);
    }

    /**
     * 移除设备
     */
    @RequestMapping("/removeDeviceList")
    @ResponseBody
    public Json removeDeviceList() throws Exception {
        String[] deviceIds = req.getParameterMap().get("deviceIds[]");
        String[] deviceMaintenancePlanIds = req.getParameterMap().get("deviceMaintenancePlanId[]");

        if (StringUtils.isEmpty(deviceIds))
            return new Json(JsonMessage.DATA_NO_COMPLETE, "不能为空");

        List<DeviceMaintenancePlanDevice> deviceMaintenancePlanDevices = deviceMaintenancePlanDeviceBaseService.find(
                "from DeviceMaintenancePlanDevice where deviceId in :deviceIds and deviceMaintenancePlanId = :deviceMaintenancePlanId",
                Map.of("deviceIds", deviceIds, "deviceMaintenancePlanId", deviceMaintenancePlanIds[0])
        );

        for (DeviceMaintenancePlanDevice deviceMaintenancePlanDevice : deviceMaintenancePlanDevices)
            deviceMaintenancePlanDeviceBaseService.delete(deviceMaintenancePlanDevice);

        return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/chooseMeasurementTemplates")
    public ModelAndView chooseMeasurementTemplates() throws Exception {
        // 前端拦截确保deviceIds对应的devices.deviceSubTypeId一致
        String deviceMaintenancePlanDeviceIds = req.getParameter("deviceMaintenancePlanDeviceIds");
        String deviceSubTypeId = req.getParameter("deviceSubTypeId");
        String measurementTemplateName = req.getParameter("measurementTemplateName");
        String repairClass = req.getParameter("repairClass");

        Map<String, Object> params = new HashMap<>();
        Map<String, String> conditions = new HashMap<>();

        params.put("deviceSubTypeId", deviceSubTypeId);
        conditions.put("deviceSubTypeId", "=");
        params.put("repairClass", Byte.parseByte(repairClass));
        conditions.put("repairClass", "=");


        if (StringUtils.isNotEmpty(measurementTemplateName)) {
            params.put("name", "%" + measurementTemplateName + "%");
            conditions.put("name", "like");
        }

        List<MeasurementTemplate> measurementTemplates = measurementTemplateBaseService.getObjcetPagination(
                MeasurementTemplate.class, params, conditions, page.getCurrentPage(), page.getPageSize(), "order by updateTime desc"
        );
        page.setDataTotal(measurementTemplateBaseService.getObjectNum(MeasurementTemplate.class, params, conditions));

        attributes.put("measurementTemplates", measurementTemplates);
        attributes.put("deviceMaintenancePlanDeviceIds", deviceMaintenancePlanDeviceIds);
        attributes.put("page", page);

        return createModelAndView("/backstage/device-maintenance-plan/choose-measurement-templates");
    }

    /**
     * 为计划设备表添加模板
     */
    @RequestMapping("/setMeasurementTemplates")
    @ResponseBody
    public Json setMeasurementTemplates() throws Exception {
        String measurementTemplateId = req.getParameter("measurementTemplateId");
        String deviceMaintenancePlanDeviceIds = req.getParameter("deviceMaintenancePlanDeviceIds");

        List<DeviceMaintenancePlanDevice> deviceMaintenancePlanDevices = deviceMaintenancePlanDeviceBaseService.find(
                "from DeviceMaintenancePlanDevice where id in :deviceMaintenancePlanDeviceIds",
                Map.of("deviceMaintenancePlanDeviceIds", deviceMaintenancePlanDeviceIds.split(","))
        );

        for (DeviceMaintenancePlanDevice deviceMaintenancePlanDevice : deviceMaintenancePlanDevices) {
            deviceMaintenancePlanDevice.setMeasurementTemplateId(measurementTemplateId);
            deviceMaintenancePlanDevice.setUpdateTime(current());
        }

        deviceMaintenancePlanDeviceBaseService.bulkInsert(deviceMaintenancePlanDevices);

        return new Json(JsonMessage.SUCCESS);
    }

    /**
     * 为计划添加设备
     */
    @ResponseBody
    @RequestMapping("/addDevice")
    public Json addDevice() throws Exception {
        String[] deviceIds = req.getParameterMap().get("deviceId[]");
        if (deviceIds == null)
            throw new CustomException(JsonMessage.PARAM_INVALID, "请选择设备");
        String deviceMaintenancePlanId = req.getParameterMap().get("deviceMaintenancePlanId[]")[0];

        List<DeviceMaintenancePlanDevice> deviceMaintenancePlanDevices = deviceMaintenancePlanDeviceBaseService.find(
                "from DeviceMaintenancePlanDevice where deviceMaintenancePlanId = :deviceMaintenancePlanId",
                Map.of("deviceMaintenancePlanId", deviceMaintenancePlanId)
        );

        int currentSeq = deviceMaintenancePlanDeviceService.getCurrentSeq(deviceMaintenancePlanId);
        // 如未添加过该设备，添加
        for (String deviceId : deviceIds) {
            if (CollectionUtils.isEmpty(deviceMaintenancePlanDevices)
                    || !StreamUtil.getList(deviceMaintenancePlanDevices, DeviceMaintenancePlanDevice::getDeviceId).contains(deviceId))
            {

                DeviceMaintenancePlanDevice deviceMaintenancePlanDevice = new DeviceMaintenancePlanDevice(deviceId, deviceMaintenancePlanId, ++currentSeq);
                deviceMaintenancePlanDeviceBaseService.saveOrUpdate(deviceMaintenancePlanDevice);
            }
        }

        return new Json(JsonMessage.SUCCESS);
    }


    @ModelAttribute
    public void setPageDeviceMaintenancePlan(DeviceMaintenancePlan pageDeviceMaintenancePlan) {
        this.pageDeviceMaintenancePlan = pageDeviceMaintenancePlan;
    }
}

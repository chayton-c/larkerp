package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.device.DeviceMeasurementItem;
import com.yingda.lkj.beans.entity.backstage.measurement.DeviceMaintenanceParameter;
import com.yingda.lkj.beans.pojo.measurement.DeviceMaintenanceParameterChatsPojo;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.device.DeviceMeasurementItemService;
import com.yingda.lkj.service.backstage.device.DeviceService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.date.CalendarUtil;
import com.yingda.lkj.utils.date.DateUtil;
import com.yingda.lkj.utils.hql.HqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hood  2020/6/15
 */
@RequestMapping("/backstage/deviceMaintenanceParameter")
@Controller
public class DeviceMaintenanceParameterController extends BaseController {

    @Autowired
    private BaseService<Device> deviceBaseService;
    @Autowired
    private BaseService<BigInteger> bigIntegerBaseService;
    @Autowired
    private BaseService<DeviceMaintenanceParameter> deviceMaintenanceParameterBaseService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DeviceMeasurementItemService deviceMeasurementItemService;

    private Device pageDevice;

    @ModelAttribute
    public void setPageDevice(Device pageDevice) {
        this.pageDevice = pageDevice;
    }

    @RequestMapping("")
    public ModelAndView deviceList() throws Exception {
        Map<String, Object> attributes = new HashMap<>();

        String startTimeStr = req.getParameter("startTime");
        String endTimeStr = req.getParameter("endTime");

        if (StringUtils.isEmpty(startTimeStr)) {
            startTimeStr = DateUtil.format(CalendarUtil.getBeginningOfTheYear(), "yyyy-MM-dd");
        }
        if (StringUtils.isEmpty(endTimeStr)) {
            endTimeStr = DateUtil.format(CalendarUtil.getEndOfTheYear(), "yyyy-MM-dd");
        }

        String deviceName = req.getParameter("deviceName");
        String stationName = req.getParameter("stationName");
        String deviceTypeName = req.getParameter("deviceTypeName");
        String deviceCode = req.getParameter("deviceCode");
        String railwayLineName = req.getParameter("railwayLineName");
        String workshopName = req.getParameter("workshopName");

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
        sqlBuilder.append("order by device.add_time desc");

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
        attributes.put("selectDevice", pageDevice);
        attributes.put("devices", devices);
        attributes.put("startTime", startTimeStr);
        attributes.put("endTime", endTimeStr);
        return new ModelAndView("/backstage/device-maintenance-parameter/device-maintenance-parameter", attributes);
    }

    @RequestMapping("/compareChartsData")
    @ResponseBody
    public Json compareChartsData() throws Exception {
        String[] chooseDeviceIds = req.getParameterMap().get("chooseDeviceIds[]");
        String startTimeStr = req.getParameter("startTimeStr");
        String endTimeStr = req.getParameter("endTimeStr");
        String deviceMeasurementItemId = req.getParameter("deviceMeasurementItemId");

        Timestamp startTime = DateUtil.toTimestamp(startTimeStr, "yyyy-MM-dd");
        Timestamp endTime = DateUtil.toTimestamp(endTimeStr, "yyyy-MM-dd");

        if (chooseDeviceIds == null)
            return new Json(JsonMessage.SUCCESS, new ArrayList<>());

        params.clear();
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        params.put("deviceIds", chooseDeviceIds);

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT\n");
        sqlBuilder.append("  deviceMaintenanceParameter.value as value,\n");
        sqlBuilder.append("  deviceMaintenanceParameter.measurement_time AS measurementTime,\n");
        sqlBuilder.append("  deviceMaintenanceParameter.device_id as deviceId,\n");
        sqlBuilder.append("  deviceMaintenanceParameter.device_measurement_item_id AS deviceMeasurementItemId,\n");
        sqlBuilder.append("  device.NAME AS deviceName,\n");
        sqlBuilder.append("  item.name AS deviceMeasurementItemName \n");
        sqlBuilder.append("FROM\n");
        sqlBuilder.append("  device_maintenance_parameter deviceMaintenanceParameter\n");
        sqlBuilder.append("  LEFT JOIN device ON device.id = deviceMaintenanceParameter.device_id\n");
        sqlBuilder.append("  LEFT JOIN device_measurement_item item ON item.id = deviceMaintenanceParameter.device_measurement_item_id \n");
        sqlBuilder.append("WHERE\n");
        sqlBuilder.append("  deviceMaintenanceParameter.device_id IN :deviceIds \n");
        sqlBuilder.append("  AND deviceMaintenanceParameter.measurement_time > :startTime \n");
        sqlBuilder.append("  AND deviceMaintenanceParameter.measurement_time < :endTime \n");

        if (StringUtils.isNotEmpty(deviceMeasurementItemId)) {
            sqlBuilder.append("  AND deviceMaintenanceParameter.device_measurement_item_id = :deviceMeasurementItemId \n");
            params.put("deviceMeasurementItemId", deviceMeasurementItemId);
        }

        List<DeviceMaintenanceParameter> deviceMaintenanceParameters = deviceMaintenanceParameterBaseService.findSQL(
                sqlBuilder.toString(), params, DeviceMaintenanceParameter.class, 1, 999999
        );

//        DeviceMaintenanceParameterChatsPojo
        Map<String, List<DeviceMaintenanceParameter>> collect =
                deviceMaintenanceParameters.stream().collect(Collectors.groupingBy(x -> x.getDeviceId() + x.getDeviceMeasurementItemId()));

        List<DeviceMaintenanceParameterChatsPojo> deviceMaintenanceParameterChatsPojos =
                collect.values().stream().map(DeviceMaintenanceParameterChatsPojo::new).collect(Collectors.toCollection(LinkedList::new));

        return new Json(JsonMessage.SUCCESS, deviceMaintenanceParameterChatsPojos);
    }

    /**
     * 设备测量数据折线图
     */
    @RequestMapping("/compareCharts")
    public ModelAndView compareCharts() {
        String deviceIds = req.getParameter("deviceIds"); // 显示的设备
        String deviceMeasurementItemId = req.getParameter("deviceMeasurementItemId"); // 显示的字段，如果为空，则显示所有字段
        List<Device> devices = deviceService.getDevicesByIds(Arrays.asList(deviceIds.split(",")));

        attributes.put("devices", devices);
        attributes.put("deviceMeasurementItemId", deviceMeasurementItemId);
        attributes.put("deviceIds", deviceIds);
        attributes.put("startTimeStr", req.getParameter("startTimeStr"));
        attributes.put("endTimeStr", req.getParameter("endTimeStr"));
        return createModelAndView("/backstage/device-maintenance-parameter/compare-device-parameter");
    }

    /**
     * 设备测量记录页
     */
    @RequestMapping("/deviceMaintenanceParameterHistory")
    public ModelAndView deviceMaintenanceParameterHistory() throws Exception {
        String deviceId = req.getParameter("deviceId");
        String startTimeStr = req.getParameter("startTimeStr");
        String endTimeStr = req.getParameter("endTimeStr");
        String deviceMeasurementItemId = req.getParameter("deviceMeasurementItemId");

        if (StringUtils.isEmpty(startTimeStr))
            startTimeStr = DateUtil.format(CalendarUtil.getBeginningOfTheYear(), "yyyy-MM-dd");
        if (StringUtils.isEmpty(endTimeStr))
            endTimeStr = DateUtil.format(CalendarUtil.getEndOfTheYear(), "yyyy-MM-dd");

        attributes.put("deviceId", deviceId);
        attributes.put("startTimeStr", startTimeStr);
        attributes.put("endTimeStr", endTimeStr);
        attributes.put("deviceMeasurementItemId", deviceMeasurementItemId);

        // 筛选用的测量字段信息
        List<DeviceMeasurementItem> deviceMeasurementItems = deviceMeasurementItemService.getByDeviceId(deviceId);
        attributes.put("deviceMeasurementItems", deviceMeasurementItems);

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT\n");
        sqlBuilder.append("  parameter.id AS id,\n");
        sqlBuilder.append("  parameter.source_data_type AS sourceDataType,\n");
        sqlBuilder.append("  parameter.value as value,\n");
        sqlBuilder.append("  parameter.unit_name AS unitName,\n");
        sqlBuilder.append("  parameter.measurement_time AS measurementTime,\n");
        sqlBuilder.append("  parameter.execute_user_names AS executeUserNames,\n");
        sqlBuilder.append("  item.name AS deviceMeasurementItemName \n");
        sqlBuilder.append("FROM\n");
        sqlBuilder.append("  device_maintenance_parameter parameter\n");
        sqlBuilder.append("  LEFT JOIN device_measurement_item item ON item.id = parameter.device_measurement_item_id \n");
        sqlBuilder.append("WHERE\n");
        sqlBuilder.append("  parameter.device_id = :deviceId\n");
        sqlBuilder.append("  AND parameter.measurement_time > :startTime\n");
        sqlBuilder.append("  AND parameter.measurement_time < :endTime\n");

        params.clear();
        params.put("deviceId", deviceId);
        params.put("startTime", DateUtil.toTimestamp(startTimeStr, "yyyy-MM-dd"));
        params.put("endTime", DateUtil.toTimestamp(endTimeStr, "yyyy-MM-dd"));

        if (StringUtils.isNotEmpty(deviceMeasurementItemId)) {
            sqlBuilder.append("  AND parameter.device_measurement_item_id = :deviceMeasurementItemId\n");
            params.put("deviceMeasurementItemId", deviceMeasurementItemId);
        }
        sqlBuilder.append("ORDER BY parameter.measurement_time desc\n");

        String sql = sqlBuilder.toString();
        List<DeviceMaintenanceParameter> deviceMaintenanceParameters = deviceMaintenanceParameterBaseService.findSQL(
                sql, params, DeviceMaintenanceParameter.class, page.getCurrentPage(), page.getPageSize());

        String countSql = HqlUtils.getCountSql(sql);
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        page.setDataTotal(count.isEmpty() ? 0 : count.get(0).longValue());

        attributes.put("deviceMaintenanceParameters", deviceMaintenanceParameters);
        attributes.put("page", page);

        return createModelAndView("/backstage/device-maintenance-parameter/device-maintenance-parameter-history");
    }

}

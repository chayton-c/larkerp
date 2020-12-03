package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.backstage.device.*;
import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.entity.system.Role;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.device.SemaphoreFromExcel;
import com.yingda.lkj.beans.pojo.device.devicetype.DeviceTypeSelectTreeNode;
import com.yingda.lkj.beans.pojo.line.LineSelectTreeNode;
import com.yingda.lkj.beans.pojo.utils.ExcelRowInfo;
import com.yingda.lkj.beans.pojo.utils.ExcelSheetInfo;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.device.*;
import com.yingda.lkj.service.backstage.line.RailwayLineService;
import com.yingda.lkj.service.backstage.line.StationRailwayLineService;
import com.yingda.lkj.service.backstage.line.StationService;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.RequestUtil;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.excel.ExcelUtil;
import com.yingda.lkj.utils.excel.excelClient.LkjDataExcelParser;
import com.yingda.lkj.utils.excel.excelClient.SemaphoreExcelParser;
import com.yingda.lkj.utils.hql.HqlUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 后台设备管理页接口;
 *
 * @author hood  2020/1/2
 */
@RequestMapping("/backstage/device")
@Controller
public class DeviceController extends BaseController {

    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DeviceTypeService deviceTypeService;
    @Autowired
    private DeviceSubTypeService deviceSubTypeService;
    @Autowired
    private BaseService<Device> deviceBaseService;
    @Autowired
    private RailwayLineService railwayLineService;
    @Autowired
    private BaseService<BigInteger> bigIntegerBaseService;
//    @Autowired
//    private OrganizationClientService organizationClientService;
//    @Autowired
//    private DeviceExtendFieldService deviceExtendFieldService;
//    @Autowired
//    private DeviceExtendValuesService deviceExtendValuesService;
//    @Autowired
//    private BaseService<Station> stationBaseService;
//    @Autowired
//    private StationService stationService;
//    @Autowired
//    private StationRailwayLineService stationRailwayLineService;

    private Device pageDevice;

    @ModelAttribute
    public void setPageDevice(Device pageDevice) {
        this.pageDevice = pageDevice;
    }

    @RequestMapping("")
    @ResponseBody
    public Json getList() throws Exception {
        Map<String, Object> attributes = new HashMap<>();

        byte organizationPermission = getOrganizationPermission();
        User user = RequestUtil.getUser(req);

        String railwayLineIdOrStationId = req.getParameter("railwayLineIdOrStationId");
        String deviceTypeIdOrDeviceSubTypeId = req.getParameter("deviceTypeIdOrDeviceSubTypeId");
        String deviceNameOrCode = req.getParameter("deviceNameOrCode");

        Map<String, Object> params = new HashMap<>();

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT\n");
        sqlBuilder.append("  device.id as id,\n");
        sqlBuilder.append("  device.code as code,\n");
        sqlBuilder.append("  device.name as name,\n");
        sqlBuilder.append("  device.hide as hide,\n");
        sqlBuilder.append("  station.name as stationName,\n");
        sqlBuilder.append("  workArea.name as workAreaName,\n");
        sqlBuilder.append("  railwayLines.railwayLineNames as railwayLineNames,\n");
        sqlBuilder.append("  deviceType.name as deviceTypeName,\n");
        sqlBuilder.append("  deviceType.id as deviceTypeId,\n");
        sqlBuilder.append("  deviceSubType.name as deviceSubTypeName,\n");
        sqlBuilder.append("  device.add_time as addTime\n");
        sqlBuilder.append("FROM\n");
        sqlBuilder.append("  device\n");
        sqlBuilder.append("  LEFT JOIN station ON station.id = device.station_id\n");
        // 关联的线路(线路和设备是多对多)
        sqlBuilder.append(
                           """
                             LEFT JOIN (
                                 SELECT
                                     GROUP_CONCAT( railway_line.NAME ) AS railwayLineNames,
                                     station.id AS stationId,
                                     GROUP_CONCAT( railway_line.id ) AS railwayLineIds
                                 FROM
                                     station_railway_line
                                     LEFT JOIN railway_line ON railway_line.id = station_railway_line.railway_line_id
                                     LEFT JOIN station ON station.id = station_railway_line.station_id\s
                                 GROUP BY
                                     station.id
                             ) railwayLines on railwayLines.stationId = station.id
                           """
        );
        sqlBuilder.append("  LEFT JOIN organization workArea ON workArea.id = station.work_area_id\n");
        sqlBuilder.append("  LEFT JOIN organization workshop ON workshop.id = workArea.parent_id\n"); // 设备所在车站的所属工区的上级，为所属车间
        sqlBuilder.append("  LEFT JOIN organization section ON section.id = workshop.parent_id\n"); // 车间上级为站段
        sqlBuilder.append("  LEFT JOIN device_type deviceType ON deviceType.id = device.device_type_id \n");
        sqlBuilder.append("  LEFT JOIN device_sub_type deviceSubType ON deviceSubType.id = device.device_sub_type_id \n");
        sqlBuilder.append("WHERE\n");
        sqlBuilder.append("  1 = 1\n");
        if (Role.SECTION == organizationPermission) {
            sqlBuilder.append("  AND section.id = :sectionId\n");
            params.put("sectionId", getSectionId());
        }
        if (Role.WORKSHOP == organizationPermission) {
            sqlBuilder.append("  AND workshop.id = :adminWorkshopId\n");
            params.put("adminWorkshopId", user.getWorkshopId());
        }
        if (Role.WORK_AREA == organizationPermission) {
            sqlBuilder.append("  AND workArea.id = :adminWorkAreaId\n");
            params.put("adminWorkAreaId", user.getWorkAreaId());
        }
        if (StringUtils.isNotEmpty(railwayLineIdOrStationId)) {
            sqlBuilder.append("  AND (station.id = :stationId OR railwayLines.railwayLineIds like :railwayLineIds)\n");
            params.put("stationId", railwayLineIdOrStationId);
            params.put("railwayLineIds", "%" + railwayLineIdOrStationId + "%");
        }
        if (StringUtils.isNotEmpty(deviceNameOrCode)) {
            sqlBuilder.append("  AND (device.name like :deviceNameOrCode OR device.code like :deviceNameOrCode)\n");
            params.put("deviceNameOrCode", "%" + deviceNameOrCode + "%");
        }
        if (StringUtils.isNotEmpty(deviceTypeIdOrDeviceSubTypeId)) {
            sqlBuilder.append("  AND (deviceType.id = :deviceTypeIdOrDeviceSubTypeId or deviceSubType.id = :deviceTypeIdOrDeviceSubTypeId)\n");
            params.put("deviceTypeIdOrDeviceSubTypeId", deviceTypeIdOrDeviceSubTypeId);
        }
        sqlBuilder.append("order by device.add_time desc");

        List<Device> devices = deviceBaseService.findSQL(sqlBuilder.toString(), params, Device.class, page.getCurrentPage(), page.getPageSize());
        String countSql = HqlUtils.getCountSql(sqlBuilder.toString());
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        page.setDataTotal(count.isEmpty() ? 0 : count.get(0).longValue());

        attributes.put("devices", devices);
        attributes.put("page", page);

        return new Json(JsonMessage.SUCCESS, attributes);
    }

    @RequestMapping("/initSelectTrees")
    @ResponseBody
    public Json initSelectTrees() {
        Map<String, Object> attributes = new HashMap<>();

        List<DeviceTypeSelectTreeNode> deviceTypeSelectTreeNodes = deviceTypeService.initDeviceTypeSelectTree();
        List<LineSelectTreeNode> lineSelectTreeNodes = railwayLineService.initLineSelectTreeNode();
        attributes.put("deviceTypeSelectTreeNodes", deviceTypeSelectTreeNodes);
        attributes.put("lineSelectTreeNodes", lineSelectTreeNodes);

        return new Json(JsonMessage.SUCCESS, attributes);
    }

    @RequestMapping("/info")
    @ResponseBody
    public Json info() {
        Map<String, Object> attributes = new HashMap<>();

        String id = pageDevice.getId();
        Device device = StringUtils.isEmpty(id) ? new Device() : deviceService.getById(id);
        String deviceSubTypeId = device.getDeviceSubTypeId();
        device.setDeviceTypeIdOrDeviceSubTypeId(StringUtils.isNotEmpty(deviceSubTypeId) ? deviceSubTypeId : device.getDeviceTypeId());

        attributes.put("device", device);

        return new Json(JsonMessage.SUCCESS, attributes);
    }

    @RequestMapping("/saveOrUpdate")
    @ResponseBody
    public Json saveOrUpdate() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        String deviceTypeIdOrDeviceSubTypeId = req.getParameter("deviceTypeIdOrDeviceSubTypeId");

        // 处理设备类型id，前端可能回传设备类型id也可能回传设备子类型id
        DeviceType deviceType = deviceTypeService.getDeviceType(deviceTypeIdOrDeviceSubTypeId);
        DeviceSubType deviceSubType = deviceSubTypeService.getById(deviceTypeIdOrDeviceSubTypeId);
        if (deviceSubType != null) {
            pageDevice.setDeviceTypeId(deviceSubType.getDeviceTypeId());
            pageDevice.setDeviceSubTypeId(deviceSubType.getId());
        }
        if (deviceType != null) {
            pageDevice.setDeviceTypeId(deviceType.getId());
        }

        String id = pageDevice.getId();
        Device device = StringUtils.isEmpty(id) ? new Device(pageDevice) : deviceService.getById(id);
        BeanUtils.copyProperties(pageDevice, device, "id", "hasNFC", "errorState", "hide", "addTime", "checkTime");
        device.setUpdateTime(current());
        deviceBaseService.saveOrUpdate(device);

        attributes.put("device", device);

        return new Json(JsonMessage.SUCCESS, attributes);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public Json delete() throws Exception {
        String ids = req.getParameter("ids");
        deviceService.deleteByIds(Arrays.asList(ids.split(",")));

        return new Json(JsonMessage.SUCCESS);
    }

    /**
     * 校验同一车站下code不能重复
     */
    @RequestMapping("/checkCode")
    @ResponseBody
    public Json checkCode() {
        String id = pageDevice.getId();
        String code = pageDevice.getCode();
        String stationId = pageDevice.getStationId();

        Device deviceByStationIdAndName = deviceService.getByStationIdAndCode(stationId, code);
        if (deviceByStationIdAndName == null)
            return new Json(JsonMessage.SUCCESS);

        if (StringUtils.isEmpty(id))
            return new Json(JsonMessage.DUPLICATE_DATA, "同一位置下已存在编码相同的设备");

        Device original = deviceService.getById(id);
        if (!original.getCode().equals(deviceByStationIdAndName.getCode()))
            return new Json(JsonMessage.DUPLICATE_DATA, "同一位置下已存在编码相同的设备");

        return new Json(JsonMessage.SUCCESS);
    }
}

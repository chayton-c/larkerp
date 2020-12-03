package com.yingda.lkj.controller.app.device;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.device.DeviceFailure;
import com.yingda.lkj.beans.entity.backstage.device.DeviceType;
import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.app.AppDevice;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.device.DeviceTypeService;
import com.yingda.lkj.service.backstage.line.StationService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTaskDetailService;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.file.UploadUtil;
import com.yingda.lkj.utils.hql.HqlUtils;
import com.yingda.lkj.utils.math.NumberUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * app设备
 *
 * @author hood  2020/4/14
 */
@Controller
@RequestMapping("/app/device")
public class AppDeviceController extends BaseController {

    @Autowired
    private BaseService<Device> deviceBaseService;
    @Autowired
    private BaseService<AppDevice> appDeviceBaseService;
    @Autowired
    private BaseService<BigInteger> bigIntegerBaseService;
    @Autowired
    private OrganizationClientService organizationClientService;
    @Autowired
    private BaseService<User> userBaseService;
    @Autowired
    private StationService stationService;
    @Autowired
    private DeviceTypeService deviceTypeService;
    @Autowired
    private BaseService<DeviceFailure> deviceFailureBaseService;

    @RequestMapping("")
    @ResponseBody
    public Json getDevices() throws Exception {
        checkParameters("userId", "pageSize", "currentPage");

        String userId = req.getParameter("userId");
        String pageSizeStr = req.getParameter("pageSize");
        String currentPageStr = req.getParameter("currentPage");
        String nameOrCode = req.getParameter("nameOrCode");
        String deviceId = req.getParameter("deviceId");

        int currentPage = Integer.parseInt(currentPageStr);
        int pageSize = Integer.parseInt(pageSizeStr);

        // 查询用户所在车间下的所有车站下的设备
        User user = userBaseService.get(User.class, userId);

        Map<String, Object> params = new HashMap<>();
        params.put("sectionId", user.getSectionId());

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT\n");
        sqlBuilder.append("  device.id AS deviceId,\n");
        sqlBuilder.append("  device.name AS name,\n");
        sqlBuilder.append("  device.code AS deviceCode,\n");
        sqlBuilder.append("  device.device_type_id AS deviceTypeId,\n");
        sqlBuilder.append("  device.railway_line_id AS railwayLineId,\n");
        sqlBuilder.append("  device.station_id AS stationId,\n");
        sqlBuilder.append("  device.position_info AS positionInfo,\n");
        sqlBuilder.append("  device.has_nfc AS hasNFC,\n");
        sqlBuilder.append("  workshop.id AS workshopId,\n");
        sqlBuilder.append("  workshop.NAME AS workshopName,\n");
        sqlBuilder.append("  section.NAME AS sectionName \n");
        sqlBuilder.append("FROM\n");
        sqlBuilder.append("  device\n");
        sqlBuilder.append("  LEFT JOIN station ON station.id = device.station_id\n");
        sqlBuilder.append("  LEFT JOIN organization workArea ON workArea.id = station.work_area_id\n");
        sqlBuilder.append("  LEFT JOIN organization workshop ON workshop.id = workArea.parent_id\n");
        sqlBuilder.append("  LEFT JOIN organization section ON section.id = workshop.parent_id\n");
        sqlBuilder.append("WHERE\n");
        sqlBuilder.append("  1 = 1\n");
        sqlBuilder.append("  AND section.id = :sectionId\n");
        if (StringUtils.isNotEmpty(deviceId)) {
            sqlBuilder.append("  AND device.id = :deviceId\n");
            params.put("deviceId", deviceId);
        }
        if (StringUtils.isNotEmpty(nameOrCode)) {
            sqlBuilder.append("  AND (device.name LIKE :nameOrCode OR device.code LIKE :nameOrCode)\n");
            params.put("nameOrCode", "%" + nameOrCode + "%");
        }
        sqlBuilder.append("ORDER BY device.add_time DESC");

        String sql = sqlBuilder.toString();
        List<AppDevice> appDevices = appDeviceBaseService.findSQL(
                sql, params, AppDevice.class, currentPage, pageSize);
        for (AppDevice appDevice : appDevices)
            if (appDevice.getHasNFC() == null)
                appDevice.setHasNFC((byte) 0);

        List<BigInteger> count = bigIntegerBaseService.findSQL(HqlUtils.getCountSql(sql), params);
        long total = count.isEmpty() ? 0 : count.get(0).longValue();

        return new Json(JsonMessage.SUCCESS, Map.of("appDevices", appDevices, "total", total));
    }

    @RequestMapping("/writeNFC")
    @ResponseBody
    public Json writeNFC() throws Exception {
        checkParameters("deviceId", "userId");
        String deviceId = req.getParameter("deviceId");
        deviceBaseService.executeHql(
                "update Device set hasNFC = :hasNFC where id = :id",
                Map.of("hasNFC", Device.HAS_NFC, "id", deviceId)
        );

        return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/maintenanceRecord")
    @ResponseBody
    public Json maintenanceRecord() throws Exception {
        String deviceId = req.getParameter("device_id");
        String executorTel = req.getParameter("phone");
        String failureMessage = req.getParameter("fault_message"); // 故障信息
        String remark = req.getParameter("remark"); // 备注
        String time = req.getParameter("time");
        Timestamp failureTime = NumberUtil.isInteger(time) ? new Timestamp(Long.parseLong(time)) : current();
        String userId = req.getParameter("userId");
        String images = req.getParameter("pics"); // /data/user/0/tzb.base.xunjian/files/b0e8cb56-893f-471c-bc49-994e12879562.jpg
        images = Arrays.stream(images.split(",")).map(UploadUtil::getAppUploadImageFileName).collect(Collectors.joining(","));

//        "/storage/emulated/0/Pictures/1596786918022.jpg"

        User executor = userBaseService.get(User.class, userId);
        Device device = deviceBaseService.get(Device.class, deviceId);

        DeviceFailure deviceFailure = new DeviceFailure(executor, device, executorTel, failureMessage, remark, images, failureTime);

        deviceFailureBaseService.saveOrUpdate(deviceFailure);

        return new Json(JsonMessage.SUCCESS);
    }

    /**
     * 设备详情
     */
    @RequestMapping("/getById")
    @ResponseBody
    public Json getById() throws Exception {
        String deviceId = req.getParameter("deviceId");
        Device device = deviceBaseService.get(Device.class, deviceId);
        Station station = stationService.getById(device.getStationId());
        Organization workshop = organizationClientService.getById(station.getWorkshopId());
        DeviceType deviceType = deviceTypeService.getDeviceType(device.getDeviceTypeId());
        return new Json(JsonMessage.SUCCESS, new AppDevice(device, deviceType, workshop));
    }

    static class A {
        private String a;

        public A(String a) {
            this.a = a;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            A a1 = (A) o;
            return Objects.equals(a, a1.a);
        }

        @Override
        public int hashCode() {
            return Objects.hash(a);
        }
    }

    public static void main(String[] args) {
        List<A> demo = List.of(new A("A"), new A("A"), new A("A"), new A("B"), new A("B"), new A("C"));
        // 去重
        List<A> distincts = demo.stream().distinct().collect(Collectors.toList());
        List<Integer> result = new ArrayList<>();
        for (A distinct : distincts) {
            result.add(demo.indexOf(distinct));
        }
        System.out.println(result);
    }
}

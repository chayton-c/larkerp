package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.hql.HqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/4/21
 */
@RequestMapping("/backstage/deviceHistory")
@Controller
public class DeviceHistoryController extends BaseController {

    @Autowired
    private BaseService<Device> deviceBaseService;
    @Autowired
    private BaseService<BigInteger> bigIntegerBaseService;

    private Device pageDevice;

    @ModelAttribute
    public void setPageDevice(Device pageDevice) {
        this.pageDevice = pageDevice;
    }

    @RequestMapping("")
    public ModelAndView deviceList() throws Exception {
        Map<String, Object> attributes = new HashMap<>();

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
        return new ModelAndView("/backstage/device-history/device-history-list", attributes);
    }

}

package com.yingda.lkj.controller.backstage.lkjextend;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.entity.backstage.lkj.lkjextends.Lkj15;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.pojo.device.Semaphore;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.device.DeviceExtendValuesService;
import com.yingda.lkj.service.backstage.line.RailwayLineService;
import com.yingda.lkj.service.backstage.line.StationService;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.hql.HqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author hood  2020/4/29
 */
@Controller
@RequestMapping("/backstage/lkj15")
public class Lkj15Controller extends BaseController {
    @Autowired
    private BaseService<Lkj15> lkj15BaseService;
    @Autowired
    private DeviceExtendValuesService deviceExtendValuesService;
    @Autowired
    private BaseService<BigInteger> bigIntegerBaseService;
    @Autowired
    private OrganizationClientService organizationClientService;
    @Autowired
    private StationService stationService;
    @Autowired
    private BaseService<Device> deviceBaseService;
    @Autowired
    private RailwayLineService railwayLineService;

    private Lkj15 pageLkj15;

    @RequestMapping("")
    public ModelAndView getList() throws Exception {
        Map<String, Object> attrubutes = getAttributes();

        String railwayLineName = req.getParameter("railwayLineName");
        String stationName = req.getParameter("stationName");
        String deviceCode = req.getParameter("deviceCode");

        Map<String, Object> params = new HashMap<>();

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT\n");
        sqlBuilder.append("  lkj15.id as id,\n");
        sqlBuilder.append("  bureau.name as bureauName,\n");
        sqlBuilder.append("  bureau.code as bureauCode,\n");
        sqlBuilder.append("  railwayLine.name as railwayLineName,\n");
        sqlBuilder.append("  railwayLine.code as railwayLineCode,\n");
        sqlBuilder.append("  station.name as stationName,\n");
        sqlBuilder.append("  station.code as stationCode,\n");
        sqlBuilder.append("  device.id as deviceId,\n");
        sqlBuilder.append("  device.CODE AS deviceCode,\n");
        sqlBuilder.append("  device.device_type_id AS deviceTypeId,\n");
        sqlBuilder.append("  lkj15.downriver as downriver,\n");
        sqlBuilder.append("  lkj15.special_code_type as specialCodeType,\n");
        sqlBuilder.append("  lkj15.remark as remark,\n");
        sqlBuilder.append("  lkj15.add_time as addTime,\n");
        sqlBuilder.append("  lkj15.update_time as updateTime\n");
        sqlBuilder.append("FROM\n");
        sqlBuilder.append("  lkj_15 lkj15\n");
        sqlBuilder.append("  LEFT JOIN organization bureau ON bureau.id = lkj15.bureau_id\n");
        sqlBuilder.append("  LEFT JOIN railway_line railwayLine ON railwayLine.id = lkj15.railway_line_id\n");
        sqlBuilder.append("  LEFT JOIN station ON station.id = lkj15.station_id\n");
        sqlBuilder.append("  LEFT JOIN device ON device.id = lkj15.device_id \n");
        sqlBuilder.append("WHERE\n");
        sqlBuilder.append("  1 = 1 \n");
        if (StringUtils.isNotEmpty(railwayLineName)) {
            sqlBuilder.append("  AND railwayLine.name like :railwayLineName");
            params.put("railwayLineName", "%" + railwayLineName + "%");
        }
        if (StringUtils.isNotEmpty(stationName)) {
            sqlBuilder.append("  AND station.name like :stationName");
            params.put("stationName", "%" + stationName + "%");
        }
        if (StringUtils.isNotEmpty(deviceCode)) {
            sqlBuilder.append("  AND device.code like :deviceCode");
            params.put("deviceCode", "%" + deviceCode + "%");
        }

        sqlBuilder.append("ORDER BY\n");
        sqlBuilder.append("  lkj15.update_time DESC");

        String sql = sqlBuilder.toString();
        List<Lkj15> lkj15s = lkj15BaseService.findSQL(sql, params, Lkj15.class, page.getCurrentPage(), page.getPageSize());
        attrubutes.put("lkj15s", lkj15s);

        String countSql = HqlUtils.getCountSql(sql);
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        page.setDataTotal(count.isEmpty() ? 0 : count.get(0).longValue());
        attrubutes.put("page", page);

        if (lkj15s.isEmpty())
            return new ModelAndView("/backstage/lkj-extends/lkj15/lkj15", attrubutes);

        List<String> deviceIds = StreamUtil.getList(lkj15s, Lkj15::getDeviceId);
        // key:deviceId, value:{key:扩展字段名(轨道电路制式。。), value:值}
        Map<String, Map<String, String>> extendValueMap =
                deviceExtendValuesService.getExtendValueMap(deviceIds, lkj15s.get(0).getDeviceTypeId());

        for (Lkj15 lkj15 : lkj15s) {
            String deviceId = lkj15.getDeviceId();
            Map<String, String> fieldValueMap = extendValueMap.get(deviceId);

            lkj15.setSemaphoreType(fieldValueMap == null ? "" : fieldValueMap.get(Semaphore.SEMAPHORE_TYPE));
            lkj15.setTrackCircuitStandard(fieldValueMap == null ? "" : fieldValueMap.get(Semaphore.TRACK_SYSTEM));
            lkj15.setKilometerMark(fieldValueMap == null ? "" : fieldValueMap.get(Semaphore.POSITION));
        }

        return new ModelAndView("/backstage/lkj-extends/lkj15/lkj15", attrubutes);
    }

    @RequestMapping("/info")
    public ModelAndView infoPage() throws Exception {
        Map<String, Object> attributes = new HashMap<>();

        String id = req.getParameter("id");
        String bureauId = getUser().getBureauId();

        // 车站筛选
        List<Station> stations = stationService.getStationsBySectionId(getSectionId());
        // 线路筛选
        List<RailwayLine> railwayLines = railwayLineService.getRailwayLinesByBureauId(getUser().getBureauId());

        Lkj15 lkj15 = new Lkj15();
        if (StringUtils.isNotEmpty(id)) {
            lkj15 = lkj15BaseService.get(Lkj15.class, id);
            Organization bureau = organizationClientService.getById(lkj15.getBureauId());
            lkj15.setBureauCode(bureau.getCode());
            lkj15.setBureauName(bureau.getName());

            List<Device> devices = deviceBaseService.find(
                    "from Device where stationId = :stationId",
                    Map.of("stationId", lkj15.getStationId())
            );
            attributes.put("devices", devices);
        }

        if (StringUtils.isEmpty(id)) {
            Organization bureau = organizationClientService.getById(bureauId);
            lkj15.setBureauId(bureau.getId());
            lkj15.setBureauCode(bureau.getCode());
            lkj15.setBureauName(bureau.getName());

        }

        attributes.put("lkj15", lkj15);
        attributes.put("stations", stations);
        attributes.put("railwayLines", railwayLines);

        return new ModelAndView("/backstage/lkj-extends/lkj15/lkj15-info", attributes);
    }

    @RequestMapping("/saveOrUpdate")
    @ResponseBody
    public Json saveOrUpdate() throws Exception {
        String id = pageLkj15.getId();

        Lkj15 lkj15 = new Lkj15();
        if (StringUtils.isNotEmpty(id)) {
            lkj15 = lkj15BaseService.get(Lkj15.class, id);
        } else {
            lkj15.setId(UUID.randomUUID().toString());
            lkj15.setAddTime(current());
        }

        String bureauId = req.getParameter("bureauId");
        String railwayLineId = req.getParameter("railwayLineId");
        String stationId = req.getParameter("stationId");
        String deviceId = req.getParameter("deviceId");
        String downriver = req.getParameter("downriver");
        String specialCodeType = req.getParameter("specialCodeType");
        String remark = req.getParameter("remark");

        lkj15.setBureauId(bureauId);
        lkj15.setRailwayLineId(railwayLineId);
        lkj15.setStationId(stationId);
        lkj15.setDeviceId(deviceId);
        lkj15.setDownriver(downriver);
        lkj15.setSpecialCodeType(specialCodeType);
        lkj15.setRemark(remark);

        lkj15.setUpdateTime(current());

        lkj15BaseService.saveOrUpdate(lkj15);

        return new Json(JsonMessage.SUCCESS);
    }

    @ModelAttribute
    public void setPageLkj15(Lkj15 pageLkj15) {
        this.pageLkj15 = pageLkj15;
    }
}

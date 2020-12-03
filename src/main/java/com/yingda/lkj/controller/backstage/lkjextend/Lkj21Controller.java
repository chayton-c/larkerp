package com.yingda.lkj.controller.backstage.lkjextend;

import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.entity.backstage.lkj.lkjextends.Lkj21;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.enums.dataversion.ApproveDataType;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.line.RailwayLineService;
import com.yingda.lkj.service.backstage.line.StationService;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/backstage/lkj21")
public class Lkj21Controller extends BaseController {
    @Autowired
    private BaseService<Lkj21> lkj21BaseService;
    @Autowired
    private OrganizationClientService organizationClientService;
    @Autowired
    private StationService stationService;
    @Autowired
    private RailwayLineService railwayLineService;
    @Autowired
    private BaseService<BigInteger> bigIntegerBaseService;

    private Lkj21 pagelkj21;

    @RequestMapping("")
    public ModelAndView getList() throws Exception{
        Map<String, Object> attributes = getAttributes();

        Map<String, Object> params = new HashMap<>();

        ApproveDataType.setComponentsAttributes(req, attributes, ApproveDataType.VERSION_PAGE_ROUTING_URL);

        //条件查询
        String railwayLineName = req.getParameter("railwayLineName");
        String stationName = req.getParameter("stationName");

        StringBuilder builder = new StringBuilder();
        builder.append(" SELECT ");
        builder.append(" lkj21.id as id, ");
        builder.append(" bureau.name AS bureauName, ");
        builder.append(" bureau.code AS bureauCode, ");
        builder.append(" railwayLine.name AS railwayLineName, ");
        builder.append(" railwayLine.code AS railwayLineCode, ");
        builder.append(" lkj21.downriver AS downriver, ");
        builder.append(" station.name AS stationName, ");
        builder.append(" lkj21.device_id_for_in_and_out_stations AS deviceIdForInAndOutStations, ");
        builder.append(" lkj21.update_time AS updateTime, ");
        builder.append(" lkj21.remark AS remark ");
        builder.append(" FROM ");
        builder.append(" lkj_21 AS lkj21 ");
        builder.append(" LEFT JOIN organization bureau ON bureau.id = lkj21.bureau_id ");
        builder.append(" LEFT JOIN railway_line railwayLine ON railwayLine.id = lkj21.railway_line_id ");
        builder.append(" LEFT JOIN station ON station.id = lkj21.station_id ");
        builder.append(" WHERE 1=1 ");
        if (StringUtils.isNotEmpty(railwayLineName)) {
            builder.append(" AND railwayLine.name like :railwayLineName ");
            params.put("railwayLineName", "%" + railwayLineName + "%");
        }
        if (StringUtils.isNotEmpty(stationName)){
            builder.append(" AND station.name like :stationName ");
            params.put("stationName", "%"+stationName+"%");
        }
        builder.append(" ORDER BY lkj21.update_time DESC");
        List<Lkj21> lkj21s = lkj21BaseService.findSQL(builder.toString(), params, Lkj21.class, page.getCurrentPage(), page.getPageSize());
        attributes.put("lkj21s", lkj21s);

        String countSql = HqlUtils.getCountSql(builder.toString());
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        page.setDataTotal(count.isEmpty() ? 0 : count.get(0).longValue());
        attributes.put("page", page);
        attributes.put("dataTypeId", req.getParameter("dataTypeId"));

        return new ModelAndView("/backstage/lkj-extends/lkj21/lkj21", attributes);
    }

    @RequestMapping("/info")
    public ModelAndView infoPage() throws Exception {
        Map<String, Object> attributes = new HashMap<>();

        String id = req.getParameter("id");
        String bureauId = getUser().getBureauId();

        ApproveDataType.setComponentsAttributes(req, attributes, ApproveDataType.INFO_PAGE_ROUTING_URL);

        //车站
        List<Station> stations = stationService.getStationsBySectionId(getSectionId());
        //线路
        List<RailwayLine> railwayLines = railwayLineService.getRailwayLinesByBureauId(getUser().getBureauId());

        Lkj21 lkj21 = new Lkj21();
        if (StringUtils.isNotEmpty(id)){
            lkj21 = lkj21BaseService.get(Lkj21.class, id);
            Organization organization = organizationClientService.getById(lkj21.getBureauId());
            lkj21.setBureauCode(organization.getCode());
            lkj21.setBureauName(organization.getName());
        }

        if (StringUtils.isEmpty(id)){
            Organization organization = organizationClientService.getById(bureauId);
            lkj21.setBureauName(organization.getName());
            lkj21.setBureauCode(organization.getCode());
            lkj21.setBureauId(organization.getId());
        }
        attributes.put("lkj21", lkj21);
        attributes.put("stations", stations);
        attributes.put("railwayLines", railwayLines);
        attributes.put("dataTypeId", req.getParameter("dataTypeId"));

        return new ModelAndView("/backstage/lkj-extends/lkj21/lkj21-info", attributes);
    }

    @RequestMapping("/saveOrUpdate")
    @ResponseBody
    public Json saveOrUpdate() throws Exception {
        String id = pagelkj21.getId();

        Lkj21 lkj21 = new Lkj21();
        if (StringUtils.isNotEmpty(id)) {
            lkj21 = lkj21BaseService.get(Lkj21.class, id);
        }
        if (StringUtils.isEmpty(id)) {
            lkj21.setId(UUID.randomUUID().toString());
            lkj21.setAddTime(current());
        }
        lkj21.setBureauId(req.getParameter("bureauId"));
        lkj21.setRailwayLineId(req.getParameter("railwayLineId"));
        lkj21.setStationId(req.getParameter("stationId"));
        lkj21.setDownriver(req.getParameter("downriver"));
        lkj21.setDeviceIdForInAndOutStations(req.getParameter("deviceIdForInAndOutStations"));
        lkj21.setRemark(req.getParameter("remark"));
        lkj21.setUpdateTime(current());

        String uniqueKey = lkj21.getUniqueKey();
        uniqueKey = StringUtils.isEmpty(uniqueKey) ? UUID.randomUUID().toString() : uniqueKey;
        lkj21.setUniqueKey(uniqueKey);

        lkj21BaseService.saveOrUpdate(lkj21);
        return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/approveFlowInfo")
    public ModelAndView approveFlowInfo() throws Exception{
        String id = req.getParameter("id");
        Map<String, Object> params = new HashMap<>();

        StringBuilder builder = new StringBuilder();
        builder.append(" SELECT ");
        builder.append(" lkj21.id as id, ");
        builder.append(" bureau.name AS bureauName, ");
        builder.append(" bureau.code AS bureauCode, ");
        builder.append(" railwayLine.name AS railwayLineName, ");
        builder.append(" railwayLine.code AS railwayLineCode, ");
        builder.append(" lkj21.downriver AS downriver, ");
        builder.append(" station.name AS stationName, ");
        builder.append(" lkj21.device_id_for_in_and_out_stations AS deviceIdForInAndOutStations, ");
        builder.append(" lkj21.update_time AS updateTime, ");
        builder.append(" lkj21.remark AS remark ");
        builder.append(" FROM ");
        builder.append(" lkj_21 AS lkj21 ");
        builder.append(" LEFT JOIN organization bureau ON bureau.id = lkj21.bureau_id ");
        builder.append(" LEFT JOIN railway_line railwayLine ON railwayLine.id = lkj21.railway_line_id ");
        builder.append(" LEFT JOIN station ON station.id = lkj21.station_id ");
        builder.append(" WHERE lkj21.data_approve_flow_id = :id ");
        builder.append(" ORDER BY approveFlow.add_time DESC");
        params.put("id", id);

        List<Lkj21> lkj21s = lkj21BaseService.findSQL(builder.toString(), params, Lkj21.class
                , page.getCurrentPage(), page.getPageSize());

        String countSql = HqlUtils.getCountSql(builder.toString());
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        page.setDataTotal(count.isEmpty() ? 0 : count.get(0).longValue());
        attributes.put("page", page);

        return new ModelAndView("/backstage/lkj-extends/lkj21/lkj21-approve-flow-info"
                , Map.of("lkj21s", lkj21s, "page", page));
    }

    @ModelAttribute
    public void setPagelkj21(Lkj21 lkj21) {
        this.pagelkj21 = lkj21;
    }
}

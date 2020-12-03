package com.yingda.lkj.controller.backstage.lkjextend;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveFlow;
import com.yingda.lkj.beans.entity.backstage.dataversion.DataVersion;
import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.entity.backstage.lkj.lkjextends.Lkj20;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.enums.dataversion.ApproveDataType;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.dataapprove.DataApproveConfigService;
import com.yingda.lkj.service.backstage.dataapprove.DataApproveFlowService;
import com.yingda.lkj.service.backstage.dataversion.DataVersionService;
import com.yingda.lkj.service.backstage.line.RailwayLineService;
import com.yingda.lkj.service.backstage.line.StationService;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.RequestUtil;
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
@RequestMapping("/backstage/lkj20")
public class Lkj20Controller extends BaseController {

    @Autowired
    private BaseService<Lkj20> lkj20BaseService;
    @Autowired
    private OrganizationClientService organizationClientService;
    @Autowired
    private StationService stationService;
    @Autowired
    private BaseService<BigInteger> bigIntegerBaseService;
    @Autowired
    private RailwayLineService railwayLineService;
    @Autowired
    private DataVersionService dataVersionService;
    @Autowired
    private DataApproveConfigService dataApproveConfigService;


    private Lkj20 pageLkj20;

    @RequestMapping("")
    public ModelAndView getList() throws Exception {
        String sectionId = getSectionId();

        Map<String, Object> attributes = getAttributes();

        //条件查询
        String railwayLineName = req.getParameter("railwayLineName");
        String nameForLkj = req.getParameter("nameForLkj");
        String stationName = req.getParameter("stationName");
        String lkjVersionId = req.getParameter("lkjVersionId");
        String compareLkjVersionId = req.getParameter("compareLkjVersionId"); // 给版本比较用，不影响查询

        ApproveDataType.setComponentsAttributes(req, attributes, ApproveDataType.VERSION_PAGE_ROUTING_URL);

        //版本号筛选查询
        List<DataVersion> dataVersions = dataVersionService.getAllVersions(ApproveDataType.LKJ20, sectionId);
        if (dataVersions.isEmpty())
            throw new CustomException(new Json(JsonMessage.DATA_NO_COMPLETE, "尚未生成版本"));

        double dataVersionNumber;
        if (StringUtils.isEmpty(lkjVersionId)) {
            DataVersion dataVersion = dataVersions.get(0);
            lkjVersionId = dataVersion.getId();
            dataVersionNumber = dataVersion.getVersionNumber();
        } else {
            DataVersion dataVersion = dataVersionService.getById(lkjVersionId);
            lkjVersionId = dataVersion.getId();
            dataVersionNumber = dataVersion.getVersionNumber();
        }

        List<String> outdatedDataIds = dataVersionService.getOutdatedDataIds(ApproveDataType.LKJ20, sectionId, dataVersionNumber);

        attributes.put("railwayLineName", railwayLineName);
        attributes.put("nameForLkj", nameForLkj);
        attributes.put("stationName", stationName);
        attributes.put("lkjVersionId", lkjVersionId);
        attributes.put("lkjVersions", dataVersions);
        attributes.put("compareLkjVersionId", compareLkjVersionId);

        Map<String, Object> params = new HashMap<>();

        params.put("approveStatue", LkjDataLine.APPROVED);
        params.put("dataVersionNumber", dataVersionNumber);

        StringBuilder builder = new StringBuilder();
        builder.append(" SELECT ");
        builder.append(" lkj20.id as id, ");
        builder.append(" bureau.name AS bureauName, ");
        builder.append(" bureau.code AS bureauCode, ");
        builder.append(" railwayLine.name AS railwayLineName, ");
        builder.append(" railwayLine.code AS railwayLineCode, ");
        builder.append(" lkj20.downriver AS downriver, ");
        builder.append(" station.name AS stationName, ");
        builder.append(" lkj20.name_for_lkj AS nameForLkj, ");
        builder.append(" lkj20.custom_station_code AS customStationCode, ");
        builder.append(" lkj20.description AS description, ");
        builder.append(" lkj20.update_time AS updateTime, ");
        builder.append(" lkj20.remark AS remark ");
        builder.append(" FROM ");
        builder.append(" lkj_20 AS lkj20 ");
        builder.append(" LEFT JOIN organization bureau ON bureau.id = lkj20.bureau_id ");
        builder.append(" LEFT JOIN railway_line railwayLine ON railwayLine.id = lkj20.railway_line_id ");
        builder.append(" LEFT JOIN station ON station.id = lkj20.station_id ");
        builder.append(" WHERE 1 = 1 ");
        builder.append(" AND lkj20.approve_status = :approveStatue ");
        builder.append(" AND lkj20.data_version_number <= :dataVersionNumber ");
        if (!outdatedDataIds.isEmpty()) {
            builder.append(" AND lkj20.id not in : outdatedDataIds");
            params.put("outdatedDataIds", outdatedDataIds);
        }
        if (StringUtils.isNotEmpty(railwayLineName)) {
            builder.append(" AND railwayLine.name like :railwayLineName ");
            params.put("railwayLineName", "%" + railwayLineName + "%");
        }
        if (StringUtils.isNotEmpty(nameForLkj)) {
            builder.append(" AND lkj20.name_for_lkj like :nameForLkj ");
            params.put("nameForLkj", nameForLkj);
        }
        if (StringUtils.isNotEmpty(stationName)) {
            builder.append(" AND station.name like :stationName ");
            params.put("stationName", "%" + stationName + "%");
        }
        builder.append(" ORDER BY lkj20.update_time DESC");
        List<Lkj20> lkj20s = lkj20BaseService.findSQL(builder.toString(), params, Lkj20.class, page.getCurrentPage(), page.getPageSize());
        attributes.put("lkj20s", lkj20s);

        String countSql = HqlUtils.getCountSql(builder.toString());
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        page.setDataTotal(count.isEmpty() ? 0 : count.get(0).longValue());
        attributes.put("page", page);
        attributes.put("dataTypeId", req.getParameter("dataTypeId"));
        return new ModelAndView("/backstage/lkj-extends/lkj20/lkj20", attributes);
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

        Lkj20 lkj20 = new Lkj20();
        if (StringUtils.isNotEmpty(id)) {
            lkj20 = lkj20BaseService.get(Lkj20.class, id);
            Organization organization = organizationClientService.getById(lkj20.getBureauId());
            lkj20.setBureauCode(organization.getCode());
            lkj20.setBureauName(organization.getName());
        }

        if (StringUtils.isEmpty(id)) {
            Organization organization = organizationClientService.getById(bureauId);
            lkj20.setBureauName(organization.getName());
            lkj20.setBureauCode(organization.getCode());
            lkj20.setBureauId(organization.getId());
        }

        attributes.put("lkj20", lkj20);
        attributes.put("stations", stations);
        attributes.put("railwayLines", railwayLines);
        attributes.put("dataTypeId", req.getParameter("dataTypeId"));
        return new ModelAndView("/backstage/lkj-extends/lkj20/lkj20-info", attributes);
    }

    @Autowired
    private DataApproveFlowService dataApproveFlowService;
    private DataApproveFlow pageDataApproveFlow;

    @RequestMapping("/saveOrUpdate")
    @ResponseBody
    public Json saveOrUpdate() throws Exception {
        User user = RequestUtil.getUser(req);

        String id = pageLkj20.getId();

        //审批人校验
        String approveUserId = req.getParameter("approveUserId");
        List<User> availableApproveUsers = dataApproveConfigService.getAvailableApproveUsers(user);

        if (StringUtils.isEmpty(approveUserId) && !availableApproveUsers.isEmpty())
            return new Json(JsonMessage.PARAM_INVALID, "当前用户无独立审批权限，请选择审核人");

        if (StringUtils.isNotEmpty(approveUserId)
                && availableApproveUsers.stream().noneMatch(x -> x.getId().equals(approveUserId)))
            return new Json(JsonMessage.PARAM_INVALID, "审核人的用户角色发生改变，请重新选择审核人");

        // 提交审批数据
        DataApproveFlow dataApproveFlow = dataApproveFlowService.createDataApproveFlow(
                pageDataApproveFlow, user, approveUserId, List.of(pageLkj20), ApproveDataType.LKJ20);

        if (StringUtils.isEmpty(approveUserId))
            dataApproveFlowService.completeDataApproveFlow(dataApproveFlow.getId(), ApproveDataType.LKJ20);

        Lkj20 lkj20 = new Lkj20();
        if (StringUtils.isNotEmpty(id)) {
            lkj20 = lkj20BaseService.get(Lkj20.class, id);
        }
        if (StringUtils.isEmpty(id)) {
            lkj20.setId(UUID.randomUUID().toString());
            lkj20.setAddTime(current());
        }
        lkj20.setBureauId(req.getParameter("bureauId"));
        lkj20.setRailwayLineId(req.getParameter("railwayLineId"));
        lkj20.setStationId(req.getParameter("stationId"));
        lkj20.setDownriver(req.getParameter("downriver"));
        lkj20.setCustomStationCode(req.getParameter("customStationCode"));
        lkj20.setDescription(req.getParameter("description"));
        lkj20.setRemark(req.getParameter("remark"));
        lkj20.setNameForLkj(req.getParameter("nameForLkj"));
        lkj20.setUpdateTime(current());

        String uniqueKey = lkj20.getUniqueKey();
        uniqueKey = StringUtils.isEmpty(uniqueKey) ? UUID.randomUUID().toString() : uniqueKey;
        lkj20.setUniqueKey(uniqueKey);

        lkj20BaseService.saveOrUpdate(lkj20);
        return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/approveFlowInfo")
    public ModelAndView approveFlowInfo() throws Exception {
        String id = req.getParameter("id");
        Map<String, Object> params = new HashMap<>();

        StringBuilder builder = new StringBuilder();
        builder.append(" SELECT ");
        builder.append(" lkj20.id as id, ");
        builder.append(" bureau.name AS bureauName, ");
        builder.append(" bureau.code AS bureauCode, ");
        builder.append(" railwayLine.name AS railwayLineName, ");
        builder.append(" railwayLine.code AS railwayLineCode, ");
        builder.append(" lkj20.downriver AS downriver, ");
        builder.append(" station.name AS stationName, ");
        builder.append(" lkj20.name_for_lkj AS nameForLkj, ");
        builder.append(" lkj20.custom_station_code AS customStationCode, ");
        builder.append(" lkj20.description AS description, ");
        builder.append(" lkj20.update_time AS updateTime, ");
        builder.append(" lkj20.remark AS remark ");
        builder.append(" FROM ");
        builder.append(" lkj_20 AS lkj20 ");
        builder.append(" LEFT JOIN organization bureau ON bureau.id = lkj20.bureau_id ");
        builder.append(" LEFT JOIN railway_line railwayLine ON railwayLine.id = lkj20.railway_line_id ");
        builder.append(" LEFT JOIN station ON station.id = lkj20.station_id ");
        builder.append(" WHERE lkj20.data_approve_flow_id = :id ");
        builder.append(" ORDER BY approveFlow.add_time DESC");
        params.put("id", id);

        List<Lkj20> lkj20s = lkj20BaseService.findSQL(builder.toString(), params, Lkj20.class
                , page.getCurrentPage(), page.getPageSize());

        String countSql = HqlUtils.getCountSql(builder.toString());
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        page.setDataTotal(count.isEmpty() ? 0 : count.get(0).longValue());
        attributes.put("page", page);

        return new ModelAndView("/backstage/lkj-extends/lkj20/lkj20-approve-flow-info"
                , Map.of("lkj20s", lkj20s, "page", page));
    }

    @ModelAttribute
    public void setPageLkj20(Lkj20 lkj20) {
        this.pageLkj20 = lkj20;
    }

    @ModelAttribute
    public void setPageDataApproveFlow(DataApproveFlow pageDataApproveFlow) {
        this.pageDataApproveFlow = pageDataApproveFlow;
    }
}

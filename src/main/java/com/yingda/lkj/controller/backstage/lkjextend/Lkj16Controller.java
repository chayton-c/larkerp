package com.yingda.lkj.controller.backstage.lkjextend;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveFlow;
import com.yingda.lkj.beans.entity.backstage.dataversion.DataVersion;
import com.yingda.lkj.beans.entity.backstage.dataversion.DataVersionUpdateDetail;
import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.entity.backstage.lkj.lkjextends.Lkj16;
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
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/backstage/lkj16")
public class Lkj16Controller extends BaseController {
    @Autowired
    private BaseService<Lkj16> lkj16BaseService;
    @Autowired
    private OrganizationClientService organizationClientService;
    @Autowired
    private StationService stationService;
    @Autowired
    private RailwayLineService railwayLineService;
    @Autowired
    private BaseService<BigInteger> bigIntegerBaseService;
    @Autowired
    private DataApproveConfigService dataApproveConfigService;
    @Autowired
    private DataApproveFlowService dataApproveFlowService;
    @Autowired
    private DataVersionService dataVersionService;

    private DataApproveFlow pageDataApproveFlow;

    private Lkj16 pagelkj16;

    @RequestMapping("")
    public ModelAndView getList() throws Exception {
        String sectionId = getSectionId();

        Map<String, Object> attributes = getAttributes();

        ApproveDataType.setComponentsAttributes(req, attributes, ApproveDataType.VERSION_PAGE_ROUTING_URL);

        //条件查询
        String railwayLineName = req.getParameter("railwayLineName");
        String stationName = req.getParameter("stationName");
        String lkjVersionId = req.getParameter("lkjVersionId");
        String compareLkjVersionId = req.getParameter("compareLkjVersionId"); // 给版本比较用，不影响查询

        // 版本号筛选查询
        List<DataVersion> dataVersions = dataVersionService.getAllVersions(ApproveDataType.LKJ16, sectionId);
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

        List<String> outdatedDataIds = dataVersionService.getOutdatedDataIds(ApproveDataType.LKJ16, sectionId, dataVersionNumber);

        attributes.put("railwayLineName", railwayLineName);
        attributes.put("stationName", stationName);
        attributes.put("lkjVersionId", lkjVersionId);
        attributes.put("lkjVersions", dataVersions);
        attributes.put("compareLkjVersionId", compareLkjVersionId);

        Map<String, Object> params = new HashMap<>();

        params.put("approveStatue", LkjDataLine.APPROVED);
        params.put("dataVersionNumber", dataVersionNumber);

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT \n");
        sqlBuilder.append("  lkj16.id as id, \n");
        sqlBuilder.append("  lkj16.downriver AS downriver, \n");
        sqlBuilder.append("  lkj16.track_circuit_standard AS trackCircuitStandard, \n");
        sqlBuilder.append("  lkj16.staion_track_number AS staionTrackNumber, \n");
        sqlBuilder.append("  lkj16.update_time AS updateTime, \n");
        sqlBuilder.append("  lkj16.remark AS remark, \n");
        sqlBuilder.append("  bureau.name AS bureauName, \n");
        sqlBuilder.append("  bureau.code AS bureauCode, \n");
        sqlBuilder.append("  railwayLine.name AS railwayLineName, \n");
        sqlBuilder.append("  railwayLine.code AS railwayLineCode, \n");
        sqlBuilder.append("  station.name AS stationName \n");
        sqlBuilder.append("FROM \n");
        sqlBuilder.append("  lkj_16 AS lkj16 \n");
        sqlBuilder.append("  LEFT JOIN organization bureau ON bureau.id = lkj16.bureau_id \n");
        sqlBuilder.append("  LEFT JOIN railway_line railwayLine ON railwayLine.id = lkj16.railway_line_id \n");
        sqlBuilder.append("  LEFT JOIN station ON station.id = lkj16.station_id \n");
        sqlBuilder.append("WHERE \n");
        sqlBuilder.append("  lkj16.approve_status = :approveStatue \n");
        sqlBuilder.append("  AND lkj16.data_version_number <= :dataVersionNumber \n");
        if (!outdatedDataIds.isEmpty()) {
            sqlBuilder.append("  AND lkj16.id not in :outdatedDataIds \n");
            params.put("outdatedDataIds", outdatedDataIds);
        }
        if (StringUtils.isNotEmpty(railwayLineName)) {
            sqlBuilder.append("  AND railwayLine.name like :railwayLineName \n");
            params.put("railwayLineName", "%" + railwayLineName + "%");
        }
        if (StringUtils.isNotEmpty(stationName)) {
            sqlBuilder.append("  AND station.name like :stationName \n");
            params.put("stationName", "%" + stationName + "%");
        }
        sqlBuilder.append("ORDER BY lkj16.update_time DESC \n");
        List<Lkj16> lkj16s = lkj16BaseService.findSQL(sqlBuilder.toString(), params, Lkj16.class, page.getCurrentPage(), page.getPageSize());
        attributes.put("lkj16s", lkj16s);

        String countSql = HqlUtils.getCountSql(sqlBuilder.toString());
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        page.setDataTotal(count.isEmpty() ? 0 : count.get(0).longValue());
        attributes.put("page", page);
        attributes.put("dataTypeId", req.getParameter("dataTypeId"));

        return new ModelAndView("/backstage/lkj-extends/lkj16/lkj16", attributes);
    }


    @RequestMapping("/compare")
    public ModelAndView compare() throws Exception {
        String baseVersionId = req.getParameter("baseVersionId");
        String compareLkjVersionId = req.getParameter("compareLkjVersionId");
        String operation = req.getParameter("operation"); //对照版本 检索的 操作字段值

        Map<String, Object> attributes = new HashMap<>();
        List<DataVersionUpdateDetail> updateDetails =
                dataVersionService.compare(ApproveDataType.LKJ16, getSectionId(), baseVersionId, compareLkjVersionId);

        List<String> modifiedLkjDataLineIds = new ArrayList<>();
        modifiedLkjDataLineIds.addAll(StreamUtil.getList(updateDetails, DataVersionUpdateDetail::getPreviousDataId));
        modifiedLkjDataLineIds.addAll(StreamUtil.getList(updateDetails, DataVersionUpdateDetail::getCurrentDataId));
        modifiedLkjDataLineIds = modifiedLkjDataLineIds.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList()); // idList中去掉null

        Map<String, Object> params = new HashMap<>();
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT \n");
        sqlBuilder.append("  lkj16.id as id, \n");
        sqlBuilder.append("  lkj16.downriver AS downriver, \n");
        sqlBuilder.append("  lkj16.track_circuit_standard AS trackCircuitStandard, \n");
        sqlBuilder.append("  lkj16.staion_track_number AS staionTrackNumber, \n");
        sqlBuilder.append("  lkj16.update_time AS updateTime, \n");
        sqlBuilder.append("  lkj16.remark AS remark, \n");
        sqlBuilder.append("  bureau.name AS bureauName, \n");
        sqlBuilder.append("  bureau.code AS bureauCode, \n");
        sqlBuilder.append("  railwayLine.name AS railwayLineName, \n");
        sqlBuilder.append("  railwayLine.code AS railwayLineCode, \n");
        sqlBuilder.append("  station.name AS stationName \n");
        sqlBuilder.append("FROM \n");
        sqlBuilder.append("  lkj_16 AS lkj16 \n");
        sqlBuilder.append("  LEFT JOIN organization bureau ON bureau.id = lkj16.bureau_id \n");
        sqlBuilder.append("  LEFT JOIN railway_line railwayLine ON railwayLine.id = lkj16.railway_line_id \n");
        sqlBuilder.append("  LEFT JOIN station ON station.id = lkj16.station_id \n");
        sqlBuilder.append("WHERE \n");
        sqlBuilder.append("  lkj16.id in :modifiedLkjDataLineIds \n");
        params.put("modifiedLkjDataLineIds", modifiedLkjDataLineIds);
        sqlBuilder.append("ORDER BY lkj16.update_time DESC \n");
        List<Lkj16> lkj16s = lkj16BaseService.findSQL(sqlBuilder.toString(), params, Lkj16.class, page.getCurrentPage(), page.getPageSize());
        attributes.put("lkj16s", lkj16s);

        String countSql = HqlUtils.getCountSql(sqlBuilder.toString());
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        page.setDataTotal(count.isEmpty() ? 0 : count.get(0).longValue());
        attributes.put("page", page);

        Map<String, Lkj16> lkjDataLineMap = StreamUtil.getMap(lkj16s, Lkj16::getId, x -> x);

        List<Lkj16> modifiedLkjDataLines = new ArrayList<>();
        for (DataVersionUpdateDetail updateDetail : updateDetails) {
            String previousDataId = updateDetail.getPreviousDataId();
            String currentDataId = updateDetail.getCurrentDataId();

            Lkj16 previousData = previousDataId == null ? null : lkjDataLineMap.get(previousDataId);
            Lkj16 currentData = currentDataId == null ? null : lkjDataLineMap.get(currentDataId);

            if (DataVersionUpdateDetail.UPDATE == updateDetail.getOperationType()) {
                currentData.setOperation("重新测量");
                modifiedLkjDataLines.add(currentData);
            }

            if (DataVersionUpdateDetail.ADD == updateDetail.getOperationType()) {
                currentData.setOperation("新增");
                modifiedLkjDataLines.add(currentData);
            }

            if (DataVersionUpdateDetail.DELETE == updateDetail.getOperationType()) {
                previousData.setOperation("删除");
                modifiedLkjDataLines.add(previousData);
            }
        }

//        if (StringUtils.isNotEmpty(operation))
//            lkjDataLines = lkjDataLines.stream().filter(x -> operation.equals(x.getOperation())).collect(Collectors.toList());

        attributes.put("lkj16s", modifiedLkjDataLines);
        attributes.put("baseVersionId", baseVersionId);
        attributes.put("compareLkjVersionId", compareLkjVersionId);
        attributes.put("operation", operation);

        return new ModelAndView("/backstage/lkj-extends/lkj16/lkj16-compare", attributes);
    }

    @RequestMapping("/info")
    public ModelAndView infoPage() throws Exception {
        Map<String, Object> attributes = new HashMap<>();

        ApproveDataType.setComponentsAttributes(req, attributes, ApproveDataType.INFO_PAGE_ROUTING_URL);

        User user = getUser();
        String id = req.getParameter("id");
        String bureauId = getUser().getBureauId();

        //车站
        List<Station> stations = stationService.getStationsBySectionId(getSectionId());
        //线路
        List<RailwayLine> railwayLines = railwayLineService.getRailwayLinesByBureauId(getUser().getBureauId());

        Lkj16 lkj16 = new Lkj16();
        if (StringUtils.isNotEmpty(id)) {
            lkj16 = lkj16BaseService.get(Lkj16.class, id);
            Organization organization = organizationClientService.getById(lkj16.getBureauId());
            lkj16.setBureauCode(organization.getCode());
            lkj16.setBureauName(organization.getName());
        }

        if (StringUtils.isEmpty(id)) {
            Organization organization = organizationClientService.getById(bureauId);
            lkj16.setBureauName(organization.getName());
            lkj16.setBureauCode(organization.getCode());
            lkj16.setBureauId(organization.getId());
        }
        List<User> availableApproveUsers = dataApproveConfigService.getAvailableApproveUsers(user);

        attributes.put("stations", stations);
        attributes.put("availableApproveUsers", availableApproveUsers);
        attributes.put("lkj16", lkj16);
        attributes.put("railwayLines", railwayLines);

        return new ModelAndView("/backstage/lkj-extends/lkj16/lkj16-info", attributes);
    }

    @RequestMapping("/submit")
    @ResponseBody
    public Json submit() throws Exception {
        // 审批人校验
        String approveUserId = req.getParameter("approveUserId");

        User user = RequestUtil.getUser(req);

        List<User> availableApproveUsers = dataApproveConfigService.getAvailableApproveUsers(user);

        if (StringUtils.isEmpty(approveUserId) && !availableApproveUsers.isEmpty())
            return new Json(JsonMessage.PARAM_INVALID, "当前用户无独立审批权限，请选择审核人");

        if (StringUtils.isNotEmpty(approveUserId)
                && availableApproveUsers.stream().noneMatch(x -> x.getId().equals(approveUserId)))
            return new Json(JsonMessage.PARAM_INVALID, "审核人的用户角色发生改变，请重新选择审核人");

        // 提交审批数据
        DataApproveFlow dataApproveFlow = dataApproveFlowService.createDataApproveFlow(
                pageDataApproveFlow, user, approveUserId, List.of(pagelkj16), ApproveDataType.LKJ16);

        if (StringUtils.isEmpty(approveUserId))
            dataApproveFlowService.completeDataApproveFlow(dataApproveFlow.getId(), ApproveDataType.LKJ16);
        return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/approveFlowInfo")
    public ModelAndView approveFlowInfo() throws Exception{
        String id = req.getParameter("id");
        Map<String, Object> params = new HashMap<>();

        StringBuilder builder = new StringBuilder();
        builder.append(" SELECT ");
        builder.append(" lkj16.id AS id, ");
        builder.append(" lkj16.downriver AS downriver, ");
        builder.append(" lkj16.track_circuit_standard AS trackCircuitStandard, ");
        builder.append(" lkj16.staion_track_number AS staionTrackNumber, ");
        builder.append(" lkj16.update_time AS updateTime, ");
        builder.append(" lkj16.remark AS remark, ");
        builder.append(" bureau.name AS bureauName, ");
        builder.append(" bureau.code AS bureauCode, ");
        builder.append(" railwayLine.name AS railwayLineName, ");
        builder.append(" railwayLine.code AS railwayLineCode, ");
        builder.append(" station.name AS stationName ");
        builder.append(" FROM lkj_16 lkj16");
        builder.append(" LEFT JOIN organization bureau ON bureau.id = lkj16.bureau_id ");
        builder.append(" LEFT JOIN railway_line railwayLine ON railwayLine.id = lkj16.railway_line_id ");
        builder.append(" LEFT JOIN station ON station.id = lkj16.station_id ");
        builder.append(" LEFT JOIN data_approve_flow AS approveFlow ON approveFlow.id = lkj16.data_approve_flow_id ");
        builder.append(" WHERE lkj16.data_approve_flow_id = :id ");
        builder.append(" ORDER BY approveFlow.add_time DESC");
        params.put("id", id);

        List<Lkj16> lkj16s = lkj16BaseService.findSQL(builder.toString(), params, Lkj16.class
                , page.getCurrentPage(), page.getPageSize());

        String countSql = HqlUtils.getCountSql(builder.toString());
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        page.setDataTotal(count.isEmpty() ? 0 : count.get(0).longValue());
        attributes.put("page", page);

        return new ModelAndView("/backstage/lkj-extends/lkj16/lkj16-approve-flow-info"
                , Map.of("lkj16s", lkj16s, "page", page));
    }


    @ModelAttribute
    public void setPagelkj16(Lkj16 lkj16) {
        this.pagelkj16 = lkj16;
    }

    @ModelAttribute
    public void setPageDataApproveFlow(DataApproveFlow pageDataApproveFlow) {
        this.pageDataApproveFlow = pageDataApproveFlow;
    }
}

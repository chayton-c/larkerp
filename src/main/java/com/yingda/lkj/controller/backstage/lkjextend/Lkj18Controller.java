package com.yingda.lkj.controller.backstage.lkjextend;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveFlow;
import com.yingda.lkj.beans.entity.backstage.dataversion.DataVersion;
import com.yingda.lkj.beans.entity.backstage.dataversion.DataVersionUpdateDetail;
import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.entity.backstage.lkj.lkjextends.Lkj18;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.enums.dataversion.ApproveDataType;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.device.Semaphore;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.dataapprove.DataApproveConfigService;
import com.yingda.lkj.service.backstage.dataapprove.DataApproveFlowService;
import com.yingda.lkj.service.backstage.dataversion.DataVersionService;
import com.yingda.lkj.service.backstage.device.DeviceExtendValuesService;
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
@RequestMapping("/backstage/lkj18")
public class Lkj18Controller extends BaseController{
    @Autowired
    private OrganizationClientService organizationClientService;
    @Autowired
    private RailwayLineService railwayLineService;
    @Autowired
    private BaseService<Lkj18> lkj18BaseService;
    @Autowired
    private BaseService<BigInteger> bigIntegerBaseService;
    @Autowired
    private DeviceExtendValuesService deviceExtendValuesService;
    @Autowired
    private DataApproveConfigService dataApproveConfigService;
    @Autowired
    private StationService stationService;
    @Autowired
    private BaseService<Device> deviceBaseService;
    @Autowired
    private DataVersionService dataVersionService;

    private Lkj18 pageLkj18;

    @RequestMapping("")
    public ModelAndView getList() throws Exception {
        String sectionId = getSectionId();
        Map<String, Object> attributes = getAttributes();

        String railwayLineName = req.getParameter("railwayLineName");
        String stationName = req.getParameter("stationName");
        String deviceCode = req.getParameter("deviceCode");
        String lkjVersionId = req.getParameter("lkjVersionId");
        String compareLkjVersionId = req.getParameter("compareLkjVersionId"); // 给版本比较用，不影响查询

        ApproveDataType.setComponentsAttributes(req, attributes, ApproveDataType.VERSION_PAGE_ROUTING_URL);

        // 版本号筛选查询
        List<DataVersion> dataVersions = dataVersionService.getAllVersions(ApproveDataType.LKJ18, sectionId);
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
        sqlBuilder.append("SELECT ");
        sqlBuilder.append("  lkj18.id as id,\n");
        sqlBuilder.append("  bureau.name as bureauName,\n");
        sqlBuilder.append("  bureau.code as bureauCode,\n");
        sqlBuilder.append("  railwayLine.name as railwayLineName,\n");
        sqlBuilder.append("  railwayLine.code as railwayLineCode,\n");
        sqlBuilder.append("  station.name as stationName,\n");
        sqlBuilder.append("  station.code as stationCode,\n");
        sqlBuilder.append("  device.id as deviceId,\n");
        sqlBuilder.append("  device.CODE AS deviceCode,\n");
        sqlBuilder.append("  device.device_type_id AS deviceTypeId,\n");
        sqlBuilder.append("  lkj18.downriver as downriver,\n");
        sqlBuilder.append("  lkj18.turnout_frog_number as turnoutFrogNumber,\n");
        sqlBuilder.append("  lkj18.remark as remark,\n");
        sqlBuilder.append("  lkj18.add_time as addTime,\n");
        sqlBuilder.append("  lkj18.update_time as updateTime\n");
        sqlBuilder.append("FROM ");
        sqlBuilder.append("  lkj_18 AS lkj18\n");
        sqlBuilder.append("  LEFT JOIN organization bureau ON bureau.id = lkj18.bureau_id\n");
        sqlBuilder.append("  LEFT JOIN railway_line railwayLine ON railwayLine.id = lkj18.railway_line_id\n");
        sqlBuilder.append("  LEFT JOIN station ON station.id = lkj18.station_id\n");
        sqlBuilder.append("  LEFT JOIN device ON device.id = lkj18.device_id\n");
        sqlBuilder.append("WHERE ");
        sqlBuilder.append("  lkj18.approve_status = :approveStatue \n");
        sqlBuilder.append("  AND lkj18.data_version_number <= :dataVersionNumber \n");
        if (!outdatedDataIds.isEmpty()) {
            sqlBuilder.append("  AND lkj18.id not in :outdatedDataIds \n");
            params.put("outdatedDataIds", outdatedDataIds);
        }
        if (StringUtils.isNotEmpty(railwayLineName)) {
            sqlBuilder.append(" AND railwayLine.name like :railwayLineName ");
            params.put("railwayLineName", "%" + railwayLineName + "%");
        }
        if (StringUtils.isNotEmpty(stationName)) {
            sqlBuilder.append(" AND station.name like :stationName ");
            params.put("stationName", "%" + stationName + "%");
        }
        if (StringUtils.isNotEmpty(deviceCode)) {
            sqlBuilder.append(" AND device.code like :deviceCode ");
            params.put("deviceCode", "%" + deviceCode + "%");
        }

        sqlBuilder.append(" ORDER BY ");
        sqlBuilder.append(" lkj18.update_time DESC ");

        String sql = sqlBuilder.toString();
        List<Lkj18> lkj18s = lkj18BaseService.findSQL(sql, params, Lkj18.class, page.getCurrentPage(), page.getPageSize());
        attributes.put("lkj18s", lkj18s);

        String countSql = HqlUtils.getCountSql(sqlBuilder.toString());
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        page.setDataTotal(count.isEmpty() ? 0 : count.get(0).longValue());

        if (lkj18s.isEmpty())
            return new ModelAndView("/backstage/lkj-extends/lkj18/lkj18", attributes);

        List<String> deviceIds = StreamUtil.getList(lkj18s, Lkj18::getDeviceId);
        // key:deviceId, value:{key:扩展字段名(轨道电路制式。。), value:值}
        Map<String, Map<String, String>> extendValueMap =
                deviceExtendValuesService.getExtendValueMap(deviceIds, lkj18s.get(0).getDeviceTypeId());

        for (Lkj18 lkj18 : lkj18s) {
            String deviceId = lkj18.getDeviceId();
            Map<String, String> fieldValueMap = extendValueMap.get(deviceId);

            lkj18.setSemaphoreType(fieldValueMap == null ? "" : fieldValueMap.get(Semaphore.SEMAPHORE_TYPE));
            lkj18.setTrackCircuitStandard(fieldValueMap == null ? "" : fieldValueMap.get(Semaphore.TRACK_SYSTEM));
            lkj18.setKilometerMark(fieldValueMap == null ? "" : fieldValueMap.get(Semaphore.POSITION));
        }
        attributes.put("dataTypeId", req.getParameter("dataTypeId"));
        return new ModelAndView("/backstage/lkj-extends/lkj18/lkj18", attributes);
    }

    @RequestMapping("/info")
    public ModelAndView infoPage() throws Exception {
        User user = getUser();
        String bureauId = user.getBureauId();
        Map<String, Object> attributes = new HashMap<>();

        String id = req.getParameter("id");
        ApproveDataType.setComponentsAttributes(req, attributes, ApproveDataType.INFO_PAGE_ROUTING_URL);

        // 车站筛选
        List<Station> stations = stationService.getStationsBySectionId(getSectionId());
        // 线路筛选
        List<RailwayLine> railwayLines = railwayLineService.getRailwayLinesByBureauId(bureauId);
        Lkj18 lkj18 = new Lkj18();
        if (StringUtils.isNotEmpty(id)) {
            lkj18 = lkj18BaseService.get(Lkj18.class, id);
            Organization bureau = organizationClientService.getById(lkj18.getBureauId());
            lkj18.setBureauCode(bureau.getCode());
            lkj18.setBureauName(bureau.getName());

            List<Device> devices = deviceBaseService.find(
                    "from Device where stationId = :stationId",
                    Map.of("stationId", lkj18.getStationId())
            );
            attributes.put("devices", devices);
        }
        if (StringUtils.isEmpty(id)) {
            Organization bureau = organizationClientService.getById(bureauId);
            lkj18.setBureauId(bureau.getId());
            lkj18.setBureauCode(bureau.getCode());
            lkj18.setBureauName(bureau.getName());
        }
        List<User> availableApproveUsers = dataApproveConfigService.getAvailableApproveUsers(user);

        attributes.put("lkj18", lkj18);
        attributes.put("stations", stations);
        attributes.put("availableApproveUsers", availableApproveUsers);
        attributes.put("railwayLines", railwayLines);

        return new ModelAndView("/backstage/lkj-extends/lkj18/lkj18-info", attributes);
    }

    @Autowired
    private DataApproveFlowService dataApproveFlowService;
    private DataApproveFlow pageDataApproveFlow;

    @RequestMapping("/saveOrUpdate")
    @ResponseBody
    public Json saveOrUpdate() throws Exception {
        User user = RequestUtil.getUser(req);

        String id = pageLkj18.getId();

        // 审批人校验
        String approveUserId = req.getParameter("approveUserId");
        List<User> availableApproveUsers = dataApproveConfigService.getAvailableApproveUsers(user);

        if (StringUtils.isEmpty(approveUserId) && !availableApproveUsers.isEmpty())
            return new Json(JsonMessage.PARAM_INVALID, "当前用户无独立审批权限，请选择审核人");

        if (StringUtils.isNotEmpty(approveUserId)
                && availableApproveUsers.stream().noneMatch(x -> x.getId().equals(approveUserId)))
            return new Json(JsonMessage.PARAM_INVALID, "审核人的用户角色发生改变，请重新选择审核人");

        // 提交审批数据
        DataApproveFlow dataApproveFlow = dataApproveFlowService.createDataApproveFlow(
                pageDataApproveFlow, user, approveUserId, List.of(pageLkj18), ApproveDataType.LKJ18);

        if (StringUtils.isEmpty(approveUserId))
            dataApproveFlowService.completeDataApproveFlow(dataApproveFlow.getId(), ApproveDataType.LKJ18);


        return new Json(JsonMessage.SUCCESS);
    }


    @RequestMapping("/compare")
    public ModelAndView compare() throws Exception {
        String baseVersionId = req.getParameter("baseVersionId");
        String compareLkjVersionId = req.getParameter("compareLkjVersionId");
        String operation = req.getParameter("operation"); //对照版本 检索的 操作字段值

        Map<String, Object> attributes = new HashMap<>();
        List<DataVersionUpdateDetail> updateDetails =
                dataVersionService.compare(ApproveDataType.LKJ18, getSectionId(), baseVersionId, compareLkjVersionId);

        if (baseVersionId.equals(compareLkjVersionId)) {
            attributes.put("baseVersionId", baseVersionId);
            attributes.put("compareLkjVersionId", compareLkjVersionId);
            attributes.put("operation", operation);

            return new ModelAndView("/backstage/lkj-extends/lkj18/lkj18-compare", attributes);
        }

        List<String> modifiedLkjDataLineIds = new ArrayList<>();
        modifiedLkjDataLineIds.addAll(StreamUtil.getList(updateDetails, DataVersionUpdateDetail::getPreviousDataId));
        modifiedLkjDataLineIds.addAll(StreamUtil.getList(updateDetails, DataVersionUpdateDetail::getCurrentDataId));
        modifiedLkjDataLineIds = modifiedLkjDataLineIds.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList()); // idList中去掉null

        Map<String, Object> params = new HashMap<>();
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(" SELECT ");
        sqlBuilder.append("  lkj18.id as id,\n");
        sqlBuilder.append("  bureau.name as bureauName,\n");
        sqlBuilder.append("  bureau.code as bureauCode,\n");
        sqlBuilder.append("  railwayLine.name as railwayLineName,\n");
        sqlBuilder.append("  railwayLine.code as railwayLineCode,\n");
        sqlBuilder.append("  station.name as stationName,\n");
        sqlBuilder.append("  station.code as stationCode,\n");
        sqlBuilder.append("  device.id as deviceId,\n");
        sqlBuilder.append("  device.CODE AS deviceCode,\n");
        sqlBuilder.append("  device.device_type_id AS deviceTypeId,\n");
        sqlBuilder.append("  lkj18.downriver as downriver,\n");
        sqlBuilder.append("  lkj18.turnout_frog_number as turnoutFrogNumber,\n");
        sqlBuilder.append("  lkj18.remark as remark,\n");
        sqlBuilder.append("  lkj18.add_time as addTime,\n");
        sqlBuilder.append("  lkj18.update_time as updateTime\n");
        sqlBuilder.append("FROM ");
        sqlBuilder.append("  lkj_18 AS lkj18");
        sqlBuilder.append("  LEFT JOIN organization bureau ON bureau.id = lkj18.bureau_id\n");
        sqlBuilder.append("  LEFT JOIN railway_line railwayLine ON railwayLine.id = lkj18.railway_line_id\n");
        sqlBuilder.append("  LEFT JOIN station ON station.id = lkj18.station_id\n");
        sqlBuilder.append("  LEFT JOIN device ON device.id = lkj18.device_id\n");
        sqlBuilder.append("WHERE ");
        sqlBuilder.append("  lkj18.id in :modifiedLkjDataLineIds \n");
        params.put("modifiedLkjDataLineIds", modifiedLkjDataLineIds);
        sqlBuilder.append("ORDER BY lkj18.update_time DESC \n");
        List<Lkj18> lkj18s = lkj18BaseService.findSQL(sqlBuilder.toString(), params, Lkj18.class, page.getCurrentPage(), page.getPageSize());
        attributes.put("lkj18s", lkj18s);

        List<String> deviceIds = StreamUtil.getList(lkj18s, Lkj18::getDeviceId);
        // key:deviceId, value:{key:扩展字段名(轨道电路制式。。), value:值}
        Map<String, Map<String, String>> extendValueMap =
                deviceExtendValuesService.getExtendValueMap(deviceIds, lkj18s.get(0).getDeviceTypeId());
        for (Lkj18 lkj18 : lkj18s) {
            String deviceId = lkj18.getDeviceId();
            Map<String, String> fieldValueMap = extendValueMap.get(deviceId);

            lkj18.setSemaphoreType(fieldValueMap == null ? "" : fieldValueMap.get(Semaphore.SEMAPHORE_TYPE));
            lkj18.setTrackCircuitStandard(fieldValueMap == null ? "" : fieldValueMap.get(Semaphore.TRACK_SYSTEM));
            lkj18.setKilometerMark(fieldValueMap == null ? "" : fieldValueMap.get(Semaphore.POSITION));
        }

        String countSql = HqlUtils.getCountSql(sqlBuilder.toString());
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        page.setDataTotal(count.isEmpty() ? 0 : count.get(0).longValue());

        attributes.put("page", page);
        attributes.put("dataTypeId", req.getParameter("dataTypeId"));
        Map<String, Lkj18> lkjDataLineMap = StreamUtil.getMap(lkj18s, Lkj18::getId, x -> x);

        List<Lkj18> modifiedLkjDataLines = new ArrayList<>();
        for (DataVersionUpdateDetail updateDetail : updateDetails) {
            String previousDataId = updateDetail.getPreviousDataId();
            String currentDataId = updateDetail.getCurrentDataId();

            Lkj18 previousData = previousDataId == null ? null : lkjDataLineMap.get(previousDataId);
            Lkj18 currentData = currentDataId == null ? null : lkjDataLineMap.get(currentDataId);

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

        attributes.put("lkj18s", modifiedLkjDataLines);
        attributes.put("baseVersionId", baseVersionId);
        attributes.put("compareLkjVersionId", compareLkjVersionId);
        attributes.put("operation", operation);

        return new ModelAndView("/backstage/lkj-extends/lkj18/lkj18-compare", attributes);
    }

    @RequestMapping("/approveFlowInfo")
    public ModelAndView approveFlowInfo() throws Exception{
        String id = req.getParameter("id");
        Map<String, Object> params = new HashMap<>();

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(" SELECT ");
        sqlBuilder.append("  lkj18.id as id,\n");
        sqlBuilder.append("  bureau.name as bureauName,\n");
        sqlBuilder.append("  bureau.code as bureauCode,\n");
        sqlBuilder.append("  railwayLine.name as railwayLineName,\n");
        sqlBuilder.append("  railwayLine.code as railwayLineCode,\n");
        sqlBuilder.append("  station.name as stationName,\n");
        sqlBuilder.append("  station.code as stationCode,\n");
        sqlBuilder.append("  device.id as deviceId,\n");
        sqlBuilder.append("  device.CODE AS deviceCode,\n");
        sqlBuilder.append("  device.device_type_id AS deviceTypeId,\n");
        sqlBuilder.append("  lkj18.downriver as downriver,\n");
        sqlBuilder.append("  lkj18.turnout_frog_number as turnoutFrogNumber,\n");
        sqlBuilder.append("  lkj18.remark as remark,\n");
        sqlBuilder.append("  lkj18.add_time as addTime,\n");
        sqlBuilder.append("  lkj18.update_time as updateTime\n");
        sqlBuilder.append(" FROM ");
        sqlBuilder.append(" lkj_18 AS lkj18");
        sqlBuilder.append(" LEFT JOIN organization bureau ON bureau.id = lkj18.bureau_id");
        sqlBuilder.append(" LEFT JOIN railway_line railwayLine ON railwayLine.id = lkj18.railway_line_id");
        sqlBuilder.append(" LEFT JOIN station ON station.id = lkj18.station_id");
        sqlBuilder.append(" LEFT JOIN device ON device.id = lkj18.device_id");
        sqlBuilder.append(" WHERE lkj18.data_approve_flow_id = :id ");
        sqlBuilder.append(" ORDER BY approveFlow.add_time DESC");
        params.put("id", id);

        List<Lkj18> lkj18s = lkj18BaseService.findSQL(sqlBuilder.toString(), params, Lkj18.class
                , page.getCurrentPage(), page.getPageSize());

        String countSql = HqlUtils.getCountSql(sqlBuilder.toString());
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        page.setDataTotal(count.isEmpty() ? 0 : count.get(0).longValue());
        attributes.put("page", page);

        return new ModelAndView("/backstage/lkj-extends/lkj18/lkj18-approve-flow-info"
                , Map.of("lkj18s", lkj18s, "page", page));
    }


    @ModelAttribute
    public void setLkj18(Lkj18 lkj18){
        this.pageLkj18 = lkj18;
    }

    @ModelAttribute
    public void setPageDataApproveFlow(DataApproveFlow pageDataApproveFlow) {
        this.pageDataApproveFlow = pageDataApproveFlow;
    }
}

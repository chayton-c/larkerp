package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveFlow;
import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveNode;
import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.device.DeviceType;
import com.yingda.lkj.beans.entity.backstage.line.Fragment;
import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLineLocation;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.enums.dataversion.ApproveDataType;
import com.yingda.lkj.beans.pojo.device.Semaphore;
import com.yingda.lkj.beans.pojo.lkj.LkjHistory;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.dataapprove.DataApproveConfigService;
import com.yingda.lkj.service.backstage.dataapprove.DataApproveFlowService;
import com.yingda.lkj.service.backstage.dataapprove.DataApproveNodeService;
import com.yingda.lkj.service.backstage.line.StationService;
import com.yingda.lkj.service.backstage.lkjdataline.LkjDataLineService;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.RequestUtil;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hood  2020/1/14
 */
@RequestMapping("/backstage/lkjApproveUpdate")
@Controller
public class LkjApproveFlowUpdateController extends BaseController {

    @Autowired
    private BaseService<DataApproveFlow> dataApproveFlowBaseService;
    @Autowired
    private OrganizationClientService organizationClientService;
    @Autowired
    private BaseService<Fragment> fragmentBaseService;
    @Autowired
    private BaseService<Device> deviceBaseService;
    @Autowired
    private StationService stationService;
    @Autowired
    private DataApproveFlowService dataApproveFlowService;
    @Autowired
    private LkjDataLineService lkjDataLineService;
    @Autowired
    private BaseService<LkjDataLine> lkjDataLineBaseService;
    @Autowired
    private BaseService<LkjHistory> lkjHistoryBaseService;
    @Autowired
    private BaseService<User> userBaseService;
    @Autowired
    private BaseService<LkjDataLineLocation> lkjDataLineLocationBaseService;
    @Autowired
    private DataApproveConfigService dataApproveConfigService;
    @Autowired
    private DataApproveNodeService dataApproveNodeService;


    private DataApproveFlow pageDataApproveFlow;
    private DataApproveNode pageDataApproveNode;

    @RequestMapping("")
    public ModelAndView lkjApproveFlow() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        User user = getUser();
        String userId = user.getId();

        String submitUserName = req.getParameter("submitUserName");
        String name = req.getParameter("name");
        String workAreaId = req.getParameter("workAreaId");
        String approveStatus = req.getParameter("approveStatusStr");

        attributes.put("page", page);
        attributes.put("submitUserName", submitUserName);
        attributes.put("workAreaId", workAreaId);
        attributes.put("approveStatus", approveStatus);
        attributes.put("name", name);

        // 查询时使用的工区
        List<Organization> workAreas = organizationClientService.getWorkAreasBySectionId(getSectionId());
        attributes.put("workAreas", workAreas);

        Map<String, Object> params = new HashMap<>();
        Map<String, String> conditions = new HashMap<>();

        List<DataApproveNode> dataApproveNodes = dataApproveNodeService.getDataApproveNodes(userId);
        if (dataApproveNodes.isEmpty())
            return new ModelAndView("/backstage/lkjapproveupdate/lkj-approve-list", attributes);

        params.put("id", StreamUtil.getList(dataApproveNodes, DataApproveNode::getDataApproveFlowId));
        conditions.put("id", "in");

        if (StringUtils.isNotEmpty(submitUserName)) {
            List<User> users = userBaseService.find("from User where displayName like :submitUserName", Map.of("submitUserName", "%" + submitUserName + "%"));
            params.put("submitUserId#1", users.stream().map(User::getId).collect(Collectors.toList()));
            conditions.put("submitUserId#1", "in");
        }
        if (StringUtils.isNotEmpty(approveStatus)) {
            params.put("approveStatus", Byte.valueOf(approveStatus));
            conditions.put("approveStatus", "=");
        }
        if (StringUtils.isNotEmpty(name)) {
            params.put("name", "%" + name + "%");
            conditions.put("name", "like");
        }
        if (StringUtils.isNotEmpty(workAreaId)) {
            List<User> users = userBaseService.find("from User where workAreaId = :workAreaId", Map.of("workAreaId", workAreaId));
            params.put("submitUserId#2", users.stream().map(User::getId).collect(Collectors.toList()));
            conditions.put("submitUserId#2", "in");
        }
        List<DataApproveFlow> dataApproveFlows = dataApproveFlowBaseService.getObjcetPagination(
                DataApproveFlow.class, params, conditions, page.getCurrentPage(), page.getPageSize(), "order by addTime desc"
        );
        Long count = dataApproveFlowBaseService.getObjectNum(DataApproveFlow.class, params, conditions);
        page.setDataTotal(count);

        // 提报人所在工区
        for (DataApproveFlow dataApproveFlow : dataApproveFlows) {
            String dataApproveFlowId = dataApproveFlow.getId();
            String submitUserId = dataApproveFlow.getSubmitUserId();
            User submitter = userBaseService.get(User.class, submitUserId);
            Organization workArea = organizationClientService.getById(submitter.getWorkAreaId());

            dataApproveFlow.setSubmitUserName(submitter.getDisplayName());
            dataApproveFlow.setSubmitUserWorkAreaName(workArea.getName());

            DataApproveNode dataApproveNode = dataApproveNodeService.getDataApproveNode(userId, dataApproveFlowId);
            dataApproveFlow.setDataApproveNode(dataApproveNode);
        }

        attributes.put("dataApproveFlows", dataApproveFlows);

        return new ModelAndView("/backstage/lkjapproveupdate/lkj-approve-list", attributes);
    }


    @RequestMapping("/lkjApproveFlowDetail")
    public ModelAndView lkjApproveFlowInfo() throws Exception {
        String id = req.getParameter("id");

        List<LkjDataLine> lkjDataLines = lkjDataLineBaseService.find(
                "from LkjDataLine where dataApproveFlowId = :dataApproveFlowId order by uniqueCode, addTime desc",
                Map.of("dataApproveFlowId", id)
        );
        List<Semaphore> semaphores = lkjDataLineService.expandLkjDataLine(lkjDataLines);

        return new ModelAndView("/backstage/lkjapproveupdate/lkj-approve-flow-detail", Map.of("semaphores", semaphores));
    }


    /**
     * 通过审批页
     *
     * @param id 待通过的lkjApproveFlowId
     */
    @RequestMapping("/agreeApprovePage")
    public ModelAndView agreeApprovePage(String id) throws Exception {
        User user = RequestUtil.getUser(req);

        // 查询用户所在站段下的所有区间
        List<Organization> workshops = organizationClientService.getSlave(user.getSectionId());
        List<Organization> workAreas = new ArrayList<>();
        workshops.forEach(x -> workAreas.addAll(organizationClientService.getSlave(x.getId())));
        List<String> workAreaIds = workAreas.stream().map(Organization::getId).collect(Collectors.toList());
        List<Fragment> fragments = fragmentBaseService.find("from Fragment where workAreaId in :workAreaIds", Map.of("workAreaIds", workAreaIds));

        // 查询区间下所有信号机
        List<String> stationIds = stationService.getStationsByWorkAreaIds(workAreaIds).stream().map(Station::getId).collect(Collectors.toList());
        List<Device> devices = deviceBaseService.find("from Device where stationId in :stationIds and deviceTypeId = :deviceTypeId", Map.of("stationIds",
                stationIds, "deviceTypeId", DeviceType.SEMAPHORE_ID));

        List<User> availableApproveUsers = dataApproveConfigService.getAvailableApproveUsers(user);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("fragments", fragments);
        attributes.put("devices", devices);
        attributes.put("availableApproveUsers", availableApproveUsers);
        attributes.put("lkjApproveFlowId", id);

        return new ModelAndView("/backstage/lkjapproveupdate/lkj-approve-agree-info", attributes);
    }

    /**
     * 通过审批方法
     *
     * @param approveUserId 下一节点审批人id
     */
    @ResponseBody
    @RequestMapping("/agreeApprove")
    public Json agreeApprove(String approveUserId) throws Exception {
        User user = RequestUtil.getUser(req);

        List<User> availableApproveUsers = dataApproveConfigService.getAvailableApproveUsers(user);

        if (StringUtils.isEmpty(approveUserId) && !availableApproveUsers.isEmpty())
            return new Json(JsonMessage.PARAM_INVALID, "当前用户无独立审批权限，请选择审核人");

        if (StringUtils.isNotEmpty(approveUserId)
                && availableApproveUsers.stream().noneMatch(x -> x.getId().equals(approveUserId)))
            return new Json(JsonMessage.PARAM_INVALID, "审核人的用户角色发生改变，请重新选择审核人");

        dataApproveFlowService.approveDataApproveNode(user, approveUserId, pageDataApproveFlow, pageDataApproveNode);

        if (StringUtils.isEmpty(approveUserId))
            dataApproveFlowService.completeDataApproveFlow(pageDataApproveFlow.getId(), ApproveDataType.LKJ14);

        return new Json(JsonMessage.SUCCESS);
    }


    /**
     * 拒绝审批页
     *
     * @param id 待通过的lkjApproveFlowId
     */
    @RequestMapping("/refuseApprovePage")
    public ModelAndView refuseApprovePage(String id) throws Exception {
        User user = RequestUtil.getUser(req);

        // 查询用户所在站段下的所有区间
        List<Organization> workshops = organizationClientService.getSlave(user.getSectionId());
        List<Organization> workAreas = new ArrayList<>();
        workshops.forEach(x -> workAreas.addAll(organizationClientService.getSlave(x.getId())));
        List<String> workAreaIds = workAreas.stream().map(Organization::getId).collect(Collectors.toList());
        List<Fragment> fragments = fragmentBaseService.find("from Fragment where workAreaId in :workAreaIds", Map.of("workAreaIds", workAreaIds));

        // 查询区间下所有信号机
        List<String> stationIds = stationService.getStationsByWorkAreaIds(workAreaIds).stream().map(Station::getId).collect(Collectors.toList());
        List<Device> devices = deviceBaseService.find("from Device where stationId in :stationIds and deviceTypeId = :deviceTypeId", Map.of("stationIds",
                stationIds, "deviceTypeId", DeviceType.SEMAPHORE_ID));

        List<User> availableApproveUsers = dataApproveConfigService.getAvailableApproveUsers(user);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("fragments", fragments);
        attributes.put("devices", devices);
        attributes.put("availableApproveUsers", availableApproveUsers);
        attributes.put("lkjApproveFlowId", id);

            return new ModelAndView("/backstage/lkjapproveupdate/lkj-approve-refuse-info", attributes);
    }



    /**
     * 拒绝 审批方法
     *
     * @param approveUserId 下一节点审批人id
     */
    @RequestMapping("/refuseApprove")
    @ResponseBody
    public Json refuseApprove(String approveUserId) throws Exception {
        User user = RequestUtil.getUser(req);
        List<User> availableApproveUsers = dataApproveConfigService.getAvailableApproveUsers(user);

        if (StringUtils.isEmpty(approveUserId) && !availableApproveUsers.isEmpty())
            return new Json(JsonMessage.PARAM_INVALID, "当前用户无独立审批权限，请选择审核人");

        if (StringUtils.isNotEmpty(approveUserId)
                && availableApproveUsers.stream().noneMatch(x -> x.getId().equals(approveUserId)))
            return new Json(JsonMessage.PARAM_INVALID, "审核人的用户角色发生改变，请重新选择审核人");

        dataApproveFlowService.refuseDataApproveNode(user, approveUserId, pageDataApproveFlow, pageDataApproveNode);
        if (StringUtils.isEmpty(approveUserId)) {
            dataApproveFlowService.refuseDataApproveFlow(pageDataApproveFlow, ApproveDataType.LKJ14);
        }
        return new Json(JsonMessage.SUCCESS);
    }


    @RequestMapping("/history/{lkjDataLineId}")
    @ResponseBody
    public Json getHistory(@PathVariable String lkjDataLineId) throws Exception {
        LkjDataLine lkjDataLine = lkjDataLineBaseService.get(LkjDataLine.class, lkjDataLineId);
        String leftDeviceId = lkjDataLine.getLeftDeviceId();
        String rightDeviceId = lkjDataLine.getRightDeviceId();
        String uniqueCode = lkjDataLine.getUniqueCode();
        byte retrograde = lkjDataLine.getRetrograde();
        byte downriver = lkjDataLine.getDownriver();

        String sql = new StringBuilder()
                .append("SELECT\n")
                .append("  lkjData.id as lkjDataLineId,\n")
                .append("  lkjData.distance as distance,\n")
                .append("  date_format(lkjData.add_time, '%Y-%m-%d %H:%i:%s') as addTime,\n")
                .append("  user.display_name as userName\n")
                .append("FROM\n")
                .append("  lkj_data_line lkjData\n")
                .append("  LEFT JOIN data_approve_flow flow ON lkjData.data_approve_flow_id = flow.id\n")
                .append("  LEFT JOIN USER ON flow.submit_user_id = USER.id \n")
                .append("WHERE\n")
                .append("  lkjData.left_device_id = :leftDeviceId \n")
                .append("  AND lkjData.right_device_id = :rightDeviceId \n")
                .append("  AND lkjData.unique_code = :uniqueCode \n")
                .append("  AND lkjData.retrograde = :retrograde \n")
                .append("  AND lkjData.approve_status = :approveStatus \n")
                .append("  AND lkjData.downriver = :downriver \n")
                .append("ORDER BY lkjData.add_time desc")
                .toString();
        Map<String, Object> params = Map.of(
                "leftDeviceId", leftDeviceId,
                "rightDeviceId", rightDeviceId,
                "uniqueCode", uniqueCode,
                "downriver", downriver,
                "approveStatus", LkjDataLine.APPROVED,
                "retrograde", retrograde
        );
        List<LkjHistory> lkjHistories = lkjHistoryBaseService.findSQL(sql, params, LkjHistory.class, 1, 99999);

        return new Json(JsonMessage.SUCCESS, lkjHistories);
    }


    @RequestMapping("/measuringPath")
    public ModelAndView measuringPath() {
        String lkjDateLineId = req.getParameter("lkjDateLineId");
        return new ModelAndView(
                "/backstage/lkj/lkj-notfree-measuring-path"
                , Map.of("lkjDateLineId", lkjDateLineId)
        );
    }

    @RequestMapping("/getLocations")
    @ResponseBody
    public Json getLocations() throws Exception {
        String lkjDateLineId = req.getParameter("lkjDateLineId");
        List<LkjDataLineLocation> lkjDataLineLocations = lkjDataLineLocationBaseService.find(
                "from LkjDataLineLocation where lkjDataLineId = :lkjDataLineId order by executeTime desc",
                Map.of("lkjDataLineId", lkjDateLineId)
        );
//
//        for (LkjDataLineLocation lkjDataLineLocation : lkjDataLineLocations) {
//            Double latitude = lkjDataLineLocation.getLatitude();
//
//            Double longitude = lkjDataLineLocation.getLongitude();
//            double[] doubles1 = CoordinateTransform.gpsToBaidu(longitude, latitude);
//
//            lkjDataLineLocation.setLongitude(doubles1[0]);
//            lkjDataLineLocation.setLatitude(doubles1[1]);
//        }

        return new Json(JsonMessage.SUCCESS, lkjDataLineLocations);
    }

    @ModelAttribute
    public void setPageDataApproveFlow(DataApproveFlow pageDataApproveFlow) {
        this.pageDataApproveFlow = pageDataApproveFlow;
    }

    @ModelAttribute
    public void setPageDataApproveNode(DataApproveNode pageDataApproveNode) {
        this.pageDataApproveNode = pageDataApproveNode;
    }
}

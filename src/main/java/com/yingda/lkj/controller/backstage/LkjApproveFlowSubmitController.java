package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveFlow;
import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.device.DeviceType;
import com.yingda.lkj.beans.entity.backstage.line.Fragment;
import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.enums.dataversion.ApproveDataType;
import com.yingda.lkj.beans.pojo.lkj.LkjDataLineFromExcel;
import com.yingda.lkj.beans.pojo.utils.ExcelSheetInfo;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.dataapprove.DataApproveConfigService;
import com.yingda.lkj.service.backstage.dataapprove.DataApproveFlowService;
import com.yingda.lkj.service.backstage.line.RailwayLineService;
import com.yingda.lkj.service.backstage.line.StationService;
import com.yingda.lkj.service.backstage.lkjdataline.LkjDataLineService;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.RequestUtil;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.date.DateUtil;
import com.yingda.lkj.utils.excel.ExcelUtil;
import com.yingda.lkj.utils.excel.excelClient.LkjDataExcelParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 提交审批页方法
 *
 * @author hood  2020/1/8
 */
@RequestMapping("/backstage/lkjApproveSubmit")
@Controller
public class LkjApproveFlowSubmitController extends BaseController {

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
    private RailwayLineService railwayLineService;
    @Autowired
    private DataApproveConfigService dataApproveConfigService;


    private LkjDataLine pageLkjDataLine;
    private DataApproveFlow pageDataApproveFlow;

    @RequestMapping("")
    public ModelAndView lkjApproveFlow() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        User user = RequestUtil.getUser(req);

        String approveStatus = req.getParameter("approveStatusStr");
        String startTimeStr = req.getParameter("startTime");
        String endTimeStr = req.getParameter("endTime");
        String dataTypeId = req.getParameter("dataTypeId");

        // 数据类型选择项
        ApproveDataType.setComponentsAttributes(req, attributes, ApproveDataType.VERSION_PAGE_ROUTING_URL);

        attributes.put("startTime", startTimeStr);
        attributes.put("endTime", endTimeStr);
        attributes.put("approveStatus", approveStatus);

        Map<String, String> conditions = new HashMap<>();

        Map<String, Object> params = new HashMap<>();
        params.put("submitUserId", user.getId());
        conditions.put("submitUserId", "=");

        if (StringUtils.isNotEmpty(startTimeStr)) {
            params.put("addTime#1", DateUtil.toTimestamp(startTimeStr, "yyyy-MM-dd"));
            conditions.put("addTime#1", ">");
        }
        if (StringUtils.isNotEmpty(dataTypeId)) {
            params.put("dataTypeId", dataTypeId);
            conditions.put("dataTypeId", "=");
        }
        if (StringUtils.isNotEmpty(endTimeStr)) {
            params.put("addTime#2", DateUtil.toTimestamp(endTimeStr, "yyyy-MM-dd"));
            conditions.put("addTime#2", "<");
        }
        if (StringUtils.isNotEmpty(approveStatus)) {
            params.put("approveStatus", Byte.valueOf(approveStatus));
            conditions.put("approveStatus", "=");
        }
        List<DataApproveFlow> dataApproveFlows = dataApproveFlowBaseService.getObjcetPagination(
                DataApproveFlow.class, params, conditions, page.getCurrentPage(), page.getPageSize(), "order by addTime desc");

        Long count = dataApproveFlowBaseService.getObjectNum(DataApproveFlow.class, params, conditions);
        page.setDataTotal(count);

        attributes.put("dataApproveFlows", dataApproveFlows);
        attributes.put("page", page);

        return new ModelAndView("/backstage/lkjapprovesubmit/lkj-approve-submit-list", attributes);
    }

    @RequestMapping("/addPage")
    public ModelAndView lkjApproveFlowAdd() throws Exception {
        User user = RequestUtil.getUser(req);

        List<RailwayLine> railwayLines = railwayLineService.getRailwayLinesByBureauId(getUser().getBureauId());

        // 查询用户所在站段下的所有区间
        List<Organization> workshops = organizationClientService.getSlave(user.getSectionId());
        List<Organization> workAreas = new ArrayList<>();
        workshops.forEach(x -> workAreas.addAll(organizationClientService.getSlave(x.getId())));
        List<String> workAreaIds = workAreas.stream().map(Organization::getId).collect(Collectors.toList());
        List<Fragment> fragments = fragmentBaseService.find("from Fragment where workAreaId in :workAreaIds", Map.of("workAreaIds", workAreaIds));
//        List<Fragment> fragments = fragmentBaseService.find("from Fragment");

        // 查询区间下所有信号机
        List<String> stationIds = stationService.getStationsByWorkAreaIds(workAreaIds).stream().map(Station::getId).collect(Collectors.toList());
        List<Device> devices = deviceBaseService.find(
                "from Device where stationId in :stationIds and deviceTypeId = :deviceTypeId",
                Map.of("stationIds", stationIds, "deviceTypeId", DeviceType.SEMAPHORE_ID)
        );

        List<User> availableApproveUsers = dataApproveConfigService.getAvailableApproveUsers(user);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("fragments", fragments);
        attributes.put("devices", devices);
        attributes.put("availableApproveUsers", availableApproveUsers);
        attributes.put("railwayLines", railwayLines);
        return new ModelAndView("/backstage/lkjapprovesubmit/lkj-approve-submit-page", attributes);
    }

    @RequestMapping("/importPage")
    public ModelAndView lkjApproveFlowImport() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        User user = RequestUtil.getUser(req);

        ApproveDataType.setComponentsAttributes(req, attributes, ApproveDataType.INFO_PAGE_ROUTING_URL);

        List<RailwayLine> railwayLines = railwayLineService.getRailwayLinesByBureauId(getUser().getBureauId());

        // 查询用户所在站段下的所有区间
        List<Organization> workshops = organizationClientService.getSlave(user.getSectionId());
        List<Organization> workAreas = new ArrayList<>();
        workshops.forEach(x -> workAreas.addAll(organizationClientService.getSlave(x.getId())));
        List<String> workAreaIds = workAreas.stream().map(Organization::getId).collect(Collectors.toList());
        List<Fragment> fragments = fragmentBaseService.find("from Fragment where workAreaId in :workAreaIds", Map.of("workAreaIds", workAreaIds));

        List<User> availableApproveUsers = dataApproveConfigService.getAvailableApproveUsers(user);

        attributes.put("availableApproveUsers", availableApproveUsers);
        attributes.put("fragments", fragments);
        attributes.put("railwayLines", railwayLines);
        return new ModelAndView("/backstage/lkjapprovesubmit/lkj-approve-flow-import", attributes);
    }

    @ResponseBody
    @RequestMapping("/submit")
    public Json sumbit(String approveUserId) throws Exception {
        User user = RequestUtil.getUser(req);

        List<User> availableApproveUsers = dataApproveConfigService.getAvailableApproveUsers(user);

        if (StringUtils.isEmpty(approveUserId) && !availableApproveUsers.isEmpty())
            return new Json(JsonMessage.PARAM_INVALID, "当前用户无独立审批权限，请选择审核人");

        if (StringUtils.isNotEmpty(approveUserId)
                && availableApproveUsers.stream().noneMatch(x -> x.getId().equals(approveUserId)))
            return new Json(JsonMessage.PARAM_INVALID, "审核人的用户角色发生改变，请重新选择审核人");

        // 放一个临时的唯一码，生成lkj数据接口需要使用唯一码来区分是否为同一组数据(lkjDataLineService.createLkjDataLine()方法)
        pageLkjDataLine.setUniqueCode(UUID.randomUUID().toString());
        DataApproveFlow dataApproveFlow = dataApproveFlowService.createDataApproveFlow(
                pageDataApproveFlow, user, approveUserId, new ArrayList<>(Collections.singletonList(pageLkjDataLine)), ApproveDataType.LKJ14);

        if (StringUtils.isEmpty(approveUserId))
            dataApproveFlowService.completeDataApproveFlow(dataApproveFlow.getId(), ApproveDataType.LKJ14);

        return new Json(JsonMessage.SUCCESS);
    }

    @ResponseBody
    @RequestMapping("/import")
    public Json importLkj(MultipartFile file) throws Exception {
        String approveUserId = req.getParameter("approveUserId");
        String fragmentId = req.getParameter("fragmentId");

        User user = getUser();
        Organization bureau = organizationClientService.getById(user.getBureauId());

        List<User> availableApproveUsers = dataApproveConfigService.getAvailableApproveUsers(user);

        if (StringUtils.isEmpty(approveUserId) && !availableApproveUsers.isEmpty())
            return new Json(JsonMessage.PARAM_INVALID, "当前用户无独立审批权限，请选择审核人");

        if (StringUtils.isNotEmpty(approveUserId)
                && availableApproveUsers.stream().noneMatch(x -> x.getId().equals(approveUserId)))
            return new Json(JsonMessage.PARAM_INVALID, "审核人的用户角色发生改变，请重新选择审核人");

        List<ExcelSheetInfo> excelSheetInfos = ExcelUtil.readExcelFile(file);
        List<LkjDataLineFromExcel> lkjDataLineFromExcel = new LkjDataExcelParser(bureau.getCode()).getLkjDataLineFromExcel(excelSheetInfos);
        List<LkjDataLine> rawLkjDataLines = lkjDataLineService.wrapLkjDataLine(lkjDataLineFromExcel, fragmentId);

        DataApproveFlow dataApproveFlow = dataApproveFlowService.createDataApproveFlow(
                pageDataApproveFlow, user, approveUserId, rawLkjDataLines, ApproveDataType.LKJ14);

        if (StringUtils.isEmpty(approveUserId))
            dataApproveFlowService.completeDataApproveFlow(dataApproveFlow.getId(), ApproveDataType.LKJ14);

        return new Json(JsonMessage.SUCCESS);
    }

    @ModelAttribute
    public void setPageLkjDataLine(LkjDataLine pageLkjDataLine) {
        this.pageLkjDataLine = pageLkjDataLine;
    }

    @ModelAttribute
    public void setPageDataApproveFlow(DataApproveFlow pageDataApproveFlow) {
        this.pageDataApproveFlow = pageDataApproveFlow;
    }


}

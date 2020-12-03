package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.entity.backstage.line.Fragment;
import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjTask;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.lkj.LkjDataLineFromExcel;
import com.yingda.lkj.beans.pojo.device.Semaphore;
import com.yingda.lkj.beans.pojo.lkj.lkjtask.LkjTaskStatistics;
import com.yingda.lkj.beans.pojo.utils.ExcelSheetInfo;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.line.RailwayLineService;
import com.yingda.lkj.service.backstage.lkjdataline.LkjDataLineService;
import com.yingda.lkj.service.backstage.lkjtask.LkjTaskService;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.RequestUtil;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.date.CalendarUtil;
import com.yingda.lkj.utils.date.DateUtil;
import com.yingda.lkj.utils.excel.ExcelUtil;
import com.yingda.lkj.utils.excel.excelClient.LkjDataExcelParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.stream.Collectors;

/**
 * lkj测量任务提交接口(给段长看的)
 *
 * @author hood  2020/2/19
 */
@Controller
@RequestMapping("/backstage/lkjTaskSubmit")
public class LkjTaskSubmitController extends BaseController {

    @Autowired
    private BaseService<LkjTask> lkjTaskBaseService;
    @Autowired
    private OrganizationClientService organizationClientService;
    @Autowired
    private LkjDataLineService lkjDataLineService;
    @Autowired
    private LkjTaskService lkjTaskService;
    @Autowired
    private BaseService<User> userBaseService;
    @Autowired
    private BaseService<Fragment> fragmentBaseService;
    @Autowired
    private BaseService<LkjDataLine> lkjDataLineBaseService;
    @Autowired
    private RailwayLineService railwayLineService;

    private LkjTask pageLkjTask;

    /**
     * 任务列表页
     */
    @RequestMapping("")
    public ModelAndView getList() throws Exception {
        User user = RequestUtil.getUser(req);

        String executeUserName = req.getParameter("executeUserName");
        String executeUserWorkAreaName = req.getParameter("executeUserWorkAreaName");
        String workAreaId = req.getParameter("workAreaId");
        String taskName = req.getParameter("taskName");
        String finishedStatusStr = req.getParameter("finishedStatusStr");
        Map<String, Object> attribute = new HashMap<>();

        Map<String, Object> params = new HashMap<>();
        Map<String, String> conditions = new HashMap<>();
        conditions.put("submitUserId", "=");
        params.put("submitUserId", user.getId());

        if (StringUtils.isNotEmpty(executeUserName)) {
            List<User> executeUsers = userBaseService.find("from User where displayName like :executeUserName", Map.of("executeUserName",
                    "%" + executeUserName + "%"));

            params.put("executeUserId#1", executeUsers.stream().map(User::getId).collect(Collectors.toList()));
            conditions.put("executeUserId#1", "in");
        }

        if (StringUtils.isNotEmpty(workAreaId)) {

            List<User> executeUsers = userBaseService.find("from User where workAreaId = :workAreaId", Map.of("workAreaId", workAreaId));

            params.put("executeUserId#2", executeUsers.stream().map(User::getId).collect(Collectors.toList()));
            conditions.put("executeUserId#2", "in");
            attribute.put("workAreaId", workAreaId);
        }
        if (StringUtils.isNotEmpty(taskName)) {
            params.put("name", "%" + taskName + "5");
            conditions.put("name", "like");
            attribute.put("name", taskName);
        }
        if (StringUtils.isNotEmpty(finishedStatusStr)) {
            params.put("finishedStatus", Byte.valueOf(finishedStatusStr));
            conditions.put("finishedStatus", "=");
            attribute.put("finishedStatusStr", finishedStatusStr);
        }

        List<LkjTask> lkjTasks = lkjTaskBaseService.getObjcetPagination(
                LkjTask.class, params, conditions, page.getCurrentPage(), page.getPageSize(), "order by addTime desc");

        Long count = lkjTaskBaseService.getObjectNum(LkjTask.class, params, conditions);
        page.setDataTotal(count);

        for (LkjTask lkjTask : lkjTasks) {
            User executeUser = userBaseService.get(User.class, lkjTask.getExecuteUserId());
            lkjTask.setExecuteUserName(executeUser.getDisplayName());

            Organization workArea = organizationClientService.getById(executeUser.getWorkAreaId());
            lkjTask.setExecuteUserWorkAreaName(workArea.getName());
        }

        List<Organization> workAreas = organizationClientService.getWorkAreasBySectionId(getUser().getSectionId());

        attribute.put("lkjTasks", lkjTasks);
        attribute.put("page", page);
        attribute.put("executeUserName", executeUserName);
        attribute.put("executeUserWorkAreaName", executeUserWorkAreaName);
        attribute.put("workAreas", workAreas);
        return new ModelAndView("/backstage/lkjtasksubmit/lkj-task-submit-list", attribute);
    }

    /**
     * 任务统计页
     */
    @RequestMapping("/taskStatistics")
    public ModelAndView taskStatistics() throws Exception {
        String executeUserName = req.getParameter("executeUserName");
        String executeUserWorkAreaName = req.getParameter("executeUserWorkAreaName");
        String workAreaId = req.getParameter("workAreaId");
        String taskName = req.getParameter("taskName");
        String finishedStatusStr = req.getParameter("finishedStatusStr");
        String startTimeStr = req.getParameter("startTimeStr");
        String endTimeStr = req.getParameter("endTimeStr");

        if (StringUtils.isEmpty(startTimeStr))
            startTimeStr = DateUtil.format(CalendarUtil.getBeginningOfTheYear(), "yyyy-MM-dd");
        if (StringUtils.isEmpty(endTimeStr))
            endTimeStr = DateUtil.format(CalendarUtil.getEndOfTheYear(), "yyyy-MM-dd");

        attributes.clear();
        attributes.put("startTimeStr", startTimeStr);
        attributes.put("endTimeStr", endTimeStr);
        attributes.put("taskName", taskName);

        Map<String, Object> params = new HashMap<>();
        Map<String, String> conditions = new HashMap<>();
        params.put("sectionId", getSectionId());
        conditions.put("sectionId", "=");
        params.put("addTime#1", DateUtil.toTimestamp(startTimeStr, "yyyy-MM-dd"));
        conditions.put("addTime#1", ">");
        params.put("addTime#2", DateUtil.toTimestamp(endTimeStr, "yyyy-MM-dd"));
        conditions.put("addTime#2", "<");

        if (StringUtils.isNotEmpty(executeUserName)) {
            List<User> executeUsers = userBaseService.find("from User where displayName like :executeUserName", Map.of("executeUserName",
                    "%" + executeUserName + "%"));

            params.put("executeUserId#1", executeUsers.stream().map(User::getId).collect(Collectors.toList()));
            conditions.put("executeUserId#1", "in");
        }

        if (StringUtils.isNotEmpty(workAreaId)) {
            List<User> executeUsers = userBaseService.find("from User where workAreaId = :workAreaId", Map.of("workAreaId", workAreaId));

            params.put("executeUserId#2", executeUsers.stream().map(User::getId).collect(Collectors.toList()));
            conditions.put("executeUserId#2", "in");
            attributes.put("workAreaId", workAreaId);
        }
        if (StringUtils.isNotEmpty(taskName)) {
            params.put("name", "%" + taskName + "%");
            conditions.put("name", "like");
            attributes.put("name", taskName);
        }
        if (StringUtils.isNotEmpty(finishedStatusStr)) {
            params.put("finishedStatus", Byte.valueOf(finishedStatusStr));
            conditions.put("finishedStatus", "=");
            attributes.put("finishedStatusStr", finishedStatusStr);
        }

        List<LkjTask> lkjTasks = lkjTaskBaseService.getObjcetPagination(
                LkjTask.class, params, conditions, 1, 9999999, "order by addTime desc");

        LkjTaskStatistics lkjTaskStatistics = new LkjTaskStatistics();
        long lkjUpdateCount = lkjTaskService.lkjUpdateCount(StreamUtil.getList(lkjTasks, LkjTask::getId));
        lkjTaskStatistics.setLkjUpdateCount((int) lkjUpdateCount);
        for (LkjTask lkjTask : lkjTasks) {
            byte finishedStatus = lkjTask.getFinishedStatus();
            lkjTaskStatistics.setTaskCount(lkjTaskStatistics.getTaskCount() + 1);
            if (finishedStatus == LkjTask.PENDING_HANDLE)
                lkjTaskStatistics.setPendHandleCount(lkjTaskStatistics.getPendHandleCount() + 1);
            if (finishedStatus == LkjTask.PENDING_APPROVAL)
                lkjTaskStatistics.setPendingApprovalCount(lkjTaskStatistics.getPendingApprovalCount() + 1);
            if (finishedStatus == LkjTask.COMPLETED)
                lkjTaskStatistics.setCompleteCount(lkjTaskStatistics.getCompleteCount() + 1);
            if (finishedStatus == LkjTask.REFUSED)
                lkjTaskStatistics.setRefusedCount(lkjTaskStatistics.getRefusedCount() + 1);
            if (finishedStatus == LkjTask.CLOSED)
                lkjTaskStatistics.setClosedCount(lkjTaskStatistics.getClosedCount() + 1);
        }
        attributes.put("lkjTaskStatistics", lkjTaskStatistics);

        page.setDataTotal((long) lkjTasks.size());
        Integer pageSize = page.getPageSize();
        Integer currentPage = page.getCurrentPage();
        int toIndex = pageSize * currentPage;
        toIndex = Math.min(toIndex, lkjTasks.size());
        lkjTasks = lkjTasks.subList(pageSize * (currentPage - 1), toIndex);

        for (LkjTask lkjTask : lkjTasks) {
            User executeUser = userBaseService.get(User.class, lkjTask.getExecuteUserId());
            lkjTask.setExecuteUserName(executeUser.getDisplayName());

            Organization workArea = organizationClientService.getById(executeUser.getWorkAreaId());
            lkjTask.setExecuteUserWorkAreaName(workArea.getName());
        }

        List<Organization> workAreas = organizationClientService.getWorkAreasBySectionId(getSectionId());

        attributes.put("lkjTasks", lkjTasks);
        attributes.put("page", page);
        attributes.put("executeUserName", executeUserName);
        attributes.put("executeUserWorkAreaName", executeUserWorkAreaName);
        attributes.put("workAreas", workAreas);
        return createModelAndView("/backstage/lkjtasksubmit/lkj-task-submit-list");
    }

    /**
     * 添加任务页
     */
    @RequestMapping("/insertPage")
    public ModelAndView insertPage() throws Exception {
        User user = RequestUtil.getUser(req);

        List<RailwayLine> railwayLines = railwayLineService.getRailwayLinesByBureauId(getUser().getBureauId());

        // 查询用户所在站段下的所有区间
        List<Organization> workshops = organizationClientService.getSlave(user.getSectionId());
        List<Organization> workAreas = new ArrayList<>();
        workshops.forEach(x -> workAreas.addAll(organizationClientService.getSlave(x.getId())));
        List<String> workAreaIds = workAreas.stream().map(Organization::getId).collect(Collectors.toList());
        List<Fragment> fragments = fragmentBaseService.find("from Fragment where workAreaId in :workAreaIds", Map.of("workAreaIds", workAreaIds));

        // 同一站段下的用户可执行
        List<User> availableExecuters = userBaseService.find("from User where sectionId = :sectionId", Map.of("sectionId", user.getSectionId()));

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("availableExecuters", availableExecuters);
        attributes.put("fragments", fragments);
        attributes.put("railwayLines", railwayLines);
        return new ModelAndView("/backstage/lkjtasksubmit/lkj-task-submit-import", attributes);
    }

    @ResponseBody
    @RequestMapping("/insert")
    public Json insertTasks() throws Exception {
        String executeUserId = req.getParameter("executeUserId");
        if (StringUtils.isEmpty(executeUserId))
            throw new CustomException(new Json(JsonMessage.PARAM_INVALID, "请选择执行人"));

        User user = RequestUtil.getUser(req);
        User executer = userBaseService.get(User.class, executeUserId);

        lkjTaskService.createLkjTask(pageLkjTask, user, executer);

        return new Json(JsonMessage.SUCCESS);
    }

    /**
     * 导入任务接口
     */
    @ResponseBody
    @RequestMapping("/import")
    public Json importTasks(MultipartFile file) throws Exception {
        String executeUserId = req.getParameter("executeUserId");
        String fragmentId = req.getParameter("fragmentId");

        User user = RequestUtil.getUser(req);
        Organization bureau = organizationClientService.getById(user.getBureauId());

        User executer = userBaseService.get(User.class, executeUserId);

        List<ExcelSheetInfo> excelSheetInfos = ExcelUtil.readExcelFile(file);
        List<LkjDataLineFromExcel> lkjDataLineFromExcel = new LkjDataExcelParser(bureau.getCode()).getLkjDataLineFromExcel(excelSheetInfos);
        List<LkjDataLine> rawLkjDataLines = lkjDataLineService.wrapLkjDataLine(lkjDataLineFromExcel, fragmentId);

        lkjTaskService.createLkjTask(pageLkjTask, user, executer, rawLkjDataLines);

        return new Json(JsonMessage.SUCCESS);
    }

    /**
     * 任务详情
     */
    @RequestMapping("/{id}")
    public ModelAndView lkjApproveFlowInfo(@PathVariable String id) throws Exception {
        List<LkjDataLine> lkjDataLines = lkjDataLineBaseService.find(
                "from LkjDataLine where lkjTaskId = :lkjTaskId order by seq, uniqueCode, addTime desc",
                Map.of("lkjTaskId", id)
        );
        List<Semaphore> semaphores = lkjDataLineService.expandLkjDataLine(lkjDataLines);

        return new ModelAndView("/backstage/lkjtasksubmit/lkj-task-submit-detail", Map.of("semaphores", semaphores));
    }

    /**
     * 关闭任务
     */
    @RequestMapping("/closeTasks")
    @ResponseBody
    public Json closeTasks() throws Exception {
        String taskIds = req.getParameter("taskIds");
        List<LkjTask> lkjTasks = lkjTaskBaseService.find(
                "from LkjTask where id in :taskIds",
                Map.of("taskIds", taskIds.split(","))
        );

        lkjTasks.forEach(x -> {
            x.setFinishedStatus(LkjTask.CLOSED);
            x.setUpdateTime(current());
        });

        lkjTaskBaseService.bulkInsert(lkjTasks);

        return new Json(JsonMessage.SUCCESS);
    }

    @ModelAttribute
    public void setPageLkjTask(LkjTask pageLkjTask) {
        this.pageLkjTask = pageLkjTask;
    }
}

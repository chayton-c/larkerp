package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.line.Fragment;
import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjTask;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.line.RailwayLineService;
import com.yingda.lkj.service.backstage.line.StationService;
import com.yingda.lkj.service.backstage.lkjdataline.LkjDataLineService;
import com.yingda.lkj.service.backstage.lkjdataline.LkjTaskCustomService;
import com.yingda.lkj.service.backstage.lkjtask.LkjTaskService;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.RequestUtil;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.hql.HqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * lkj 自定义任务接口，对应lkj测量任务 -> 任务管理中的生成任务页
 *
 * @author hood  2020/3/25
 */
@RequestMapping("/backstage/lkjTaskCustom")
@Controller
public class LkjTaskCustomController extends BaseController {

    @Autowired
    private OrganizationClientService organizationClientService;
    @Autowired
    private LkjDataLineService lkjDataLineService;
    @Autowired
    private RailwayLineService railwayLineService;
    @Autowired
    private BaseService<Fragment> fragmentBaseService;
    @Autowired
    private BaseService<LkjDataLine> lkjDataLineBaseService;
    @Autowired
    private BaseService<BigInteger> bigIntegerBaseService;
    @Autowired
    private LkjTaskCustomService lkjTaskCustomService;
    @Autowired
    private BaseService<Device> deviceBaseService;
    @Autowired
    private StationService stationService;
    @Autowired
    private LkjTaskService lkjTaskService;

    /**
     * 任务选择页
     */
    @RequestMapping("/lkjDataLines")
    public ModelAndView lkjDataLines() throws Exception {
        User user = RequestUtil.getUser(req);

        String lkjTaskId = req.getParameter("lkjTaskId");
        if (StringUtils.isEmpty(lkjTaskId)) { // 如果没有任务，自动生成一个
            LkjTask lkjTask = lkjTaskService.createLkjTask(new LkjTask(), user, user);
            lkjTaskId = lkjTask.getId();
        }

        String fragmentId = req.getParameter("fragmentId");
        String railwayLineId = req.getParameter("railwayLineId");
        String downriver = req.getParameter("downriver");
        String retrograde = req.getParameter("retrograde");

        List<RailwayLine> railwayLines = railwayLineService.getRailwayLinesByBureauId(getUser().getBureauId());

        // 查询用户所在站段下的所有区间
        List<Organization> workshops = organizationClientService.getSlave(user.getSectionId());
        List<Organization> workAreas = new ArrayList<>();
        workshops.forEach(x -> workAreas.addAll(organizationClientService.getSlave(x.getId())));
        List<String> workAreaIds = workAreas.stream().map(Organization::getId).collect(Collectors.toList());
        List<Fragment> fragments = fragmentBaseService.find("from Fragment where workAreaId in :workAreaIds", Map.of("workAreaIds", workAreaIds));

        // 先查询unique_code(一组lkj数有相同的先查询unique_code)，再用unique_code换lkj
        Map<String, Object> params = new HashMap<>();
        params.put("outdated", LkjDataLine.USING);
        params.put("approveStatue", LkjDataLine.APPROVED);
        params.put("fragmentIds", fragments.stream().map(Fragment::getId).collect(Collectors.toList()));

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT\n");
        sqlBuilder.append("  unique_code as uniqueCode\n");
        sqlBuilder.append("FROM\n");
        sqlBuilder.append("  lkj_data_line \n");
        sqlBuilder.append("WHERE\n");
        sqlBuilder.append("  outdated = :outdated \n");
        sqlBuilder.append("  AND approve_status = :approveStatue \n");
        sqlBuilder.append("  AND fragment_id in :fragmentIds \n");
        if (StringUtils.isNotEmpty(fragmentId)) {
            sqlBuilder.append("AND fragment_id = :fragmentId ");
            params.put("fragmentId", fragmentId);
        }
        // 查询线路下的所有区间，再用这些区间查询
        if (StringUtils.isNotEmpty(railwayLineId)) {
            List<Fragment> checkFragments = fragmentBaseService.find("from Fragment where railwayLineId = :railwayLineId",
                    Map.of("railwayLineId", railwayLineId));
            List<String> checkFragmentIds = checkFragments.stream().map(Fragment::getId).collect(Collectors.toList());
            sqlBuilder.append("AND fragment_id in :fragmentIds");
            params.put("fragmentIds", checkFragmentIds);
        }
        if (StringUtils.isNotEmpty(downriver)) {
            sqlBuilder.append("AND downriver = :downriver ");
            params.put("downriver", downriver);
        }
        if (StringUtils.isNotEmpty(retrograde)) {
            sqlBuilder.append("AND retrograde = :retrograde ");
            params.put("retrograde", retrograde);
        }
        sqlBuilder.append("GROUP BY\n");
        sqlBuilder.append("  unique_code \n");
        sqlBuilder.append("ORDER BY\n");
        sqlBuilder.append("  seq");
        List<LkjDataLine> uniqueCodeItems = lkjDataLineBaseService.findSQL(sqlBuilder.toString(), params, LkjDataLine.class, page.getCurrentPage(),
                page.getPageSize());
        List<String> uniqueCodes = uniqueCodeItems.stream().map(LkjDataLine::getUniqueCode).collect(Collectors.toList());
        String countSql = HqlUtils.getCountSql(sqlBuilder.toString());
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        page.setDataTotal((long) count.size());

        List<LkjDataLine> lkjDataLines = lkjDataLineBaseService.find(
                "from LkjDataLine where uniqueCode in :uniqueCodes and outdated = :outdated and approveStatus = :approveStatus order by seq",
                Map.of("uniqueCodes", uniqueCodes, "outdated", LkjDataLine.USING, "approveStatus", LkjDataLine.APPROVED)
        );

        // 查询任务提交的lkjDataLine，并替换
        List<LkjDataLine> taskLkjDataLines = lkjDataLineBaseService.find(
                "from LkjDataLine where lkjTaskId = :lkjTaskId order by seq",
                Map.of("lkjTaskId", lkjTaskId)
        );
        List<String> groupIds = StreamUtil.getList(taskLkjDataLines, LkjDataLine::getLkjGroupId);
        lkjDataLines = lkjDataLines.stream().filter(x -> !groupIds.contains(x.getLkjGroupId())).collect(Collectors.toList());
        if (StringUtils.isNotEmpty(fragmentId))
            taskLkjDataLines = taskLkjDataLines.stream().filter(x -> fragmentId.equals(x.getFragmentId())).collect(Collectors.toList());

        lkjDataLines.addAll(0, taskLkjDataLines); // 新添加的置顶

        lkjDataLines = lkjDataLineService.fillLkjDataLineDevice(lkjDataLines);
        lkjDataLines = linkage(lkjDataLines);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("fragments", fragments);
        attributes.put("railwayLines", railwayLines);
        attributes.put("lkjDataLines", lkjDataLines);

        attributes.put("lkjTaskId", lkjTaskId);
        attributes.put("railwayLineId", railwayLineId);
        attributes.put("fragmentId", fragmentId);
        attributes.put("downriver", downriver);
        attributes.put("retrograde", retrograde);

        return new ModelAndView("/backstage/lkj-task-custom/lkj-task-submit-lkj-data-lines", attributes);
    }

    @RequestMapping("/insertPage")
    public ModelAndView insertPage() throws Exception {
        User user = RequestUtil.getUser(req);

        String baseLkjDataLineId = req.getParameter("id");
        String previousLkjDataLineId = req.getParameter("previousLkjDataLineId");
        String nextLkjDataLineId = req.getParameter("nextLkjDataLineId");
        String lkjTaskId = req.getParameter("lkjTaskId");

        LkjDataLine lkjDataLine = lkjDataLineBaseService.get(LkjDataLine.class, baseLkjDataLineId);
        Fragment fragment = fragmentBaseService.get(Fragment.class, lkjDataLine.getFragmentId());
        RailwayLine railwayLine = railwayLineService.getById(fragment.getRailwayLineId());

        List<Station> stations = stationService.getStationsBySectionId(user.getSectionId());

        Map<String, Object> attribute = new HashMap<>();
        attribute.put("railwayLine", railwayLine);
        attribute.put("stations", stations);
        attribute.put("nextLkjDataLineId", nextLkjDataLineId);
        attribute.put("previousLkjDataLineId", previousLkjDataLineId);
        attribute.put("lkjTaskId", lkjTaskId);
        attribute.put("baseLkjDataLineId", baseLkjDataLineId);

        return new ModelAndView("/backstage/lkj-task-custom/lkj-task-custom-insert", attribute);
    }

    @RequestMapping("/insertLkjDataLine")
    @ResponseBody
    public Json insertLkjDataLineTask() {
        String baseLkjDataLineId = req.getParameter("baseLkjDataLineId");
        String nextLkjDataLineId = req.getParameter("nextLkjDataLineId");
        String previousLkjDataLineId = req.getParameter("previousLkjDataLineId");
        String lkjTaskId = req.getParameter("lkjTaskId");
        String deviceId = req.getParameter("deviceId");

        String strategy = req.getParameter("strategy");
        if (StringUtils.isEmpty(strategy))
            return new Json(JsonMessage.DATA_NO_COMPLETE, "请选择插入策略");

        switch (strategy) {
            case "append":
                lkjTaskCustomService.appendDevice(baseLkjDataLineId, nextLkjDataLineId, lkjTaskId, deviceId);
                break;
            case "prepend":
                lkjTaskCustomService.prependDevice(baseLkjDataLineId, previousLkjDataLineId, lkjTaskId, deviceId);
                break;
            case "insert":
                lkjTaskCustomService.insertDevice(baseLkjDataLineId, lkjTaskId, deviceId);
                break;
        }

        return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/udpateLkjDataLineTask")
    @ResponseBody
    public Json udpateLkjDataLineTask() {
        return null;
    }


    @RequestMapping("/deletePage")
    public ModelAndView deletePage() throws Exception {
        String baseLkjDataLineId = req.getParameter("id");
        String previousLkjDataLineId = req.getParameter("previousLkjDataLineId");
        String nextLkjDataLineId = req.getParameter("nextLkjDataLineId");
        String lkjTaskId = req.getParameter("lkjTaskId");

        LkjDataLine lkjDataLine = lkjDataLineBaseService.get(LkjDataLine.class, baseLkjDataLineId);
        String leftDeviceId = lkjDataLine.getLeftDeviceId();
        String rightDeviceId = lkjDataLine.getRightDeviceId();

        Device leftDevice = deviceBaseService.get(Device.class, leftDeviceId);
        Device rightDevice = deviceBaseService.get(Device.class, rightDeviceId);

        Map<String, Object> attribute = new HashMap<>();
        attribute.put("nextLkjDataLineId", nextLkjDataLineId);
        attribute.put("previousLkjDataLineId", previousLkjDataLineId);
        attribute.put("lkjTaskId", lkjTaskId);
        attribute.put("baseLkjDataLineId", baseLkjDataLineId);

        if (StringUtils.isNotEmpty(previousLkjDataLineId)) // 没有上一节点，不能删除左节点
            attribute.put("leftDevice", leftDevice);
        if (StringUtils.isNotEmpty(nextLkjDataLineId)) // 没有下一节点，不能删除右节点
            attribute.put("rightDevice", rightDevice);

        return new ModelAndView("/backstage/lkj-task-custom/lkj-task-custom-delete", attribute);
    }

    @RequestMapping("/updateLkjDataLineTask")
    @ResponseBody
    public Json updateLkjDataLineTask() {
        String baseLkjDataLineId = req.getParameter("baseLkjDataLineId");
        String lkjTaskId = req.getParameter("lkjTaskId");
        String distanceStr = req.getParameter("distanceStr");

        double distance = StringUtils.isNotEmpty(distanceStr) ? Double.parseDouble(distanceStr) : 0;

        lkjTaskCustomService.updateLkj(baseLkjDataLineId, lkjTaskId, distance);
        return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/deleteLkjDataLineTask")
    @ResponseBody
    public Json deleteLkjDataLineTask() throws CustomException {
        String baseLkjDataLineId = req.getParameter("baseLkjDataLineId");
        String previousLkjDataLineId = req.getParameter("previousLkjDataLineId");
        String nextLkjDataLineId = req.getParameter("nextLkjDataLineId");
        String lkjTaskId = req.getParameter("lkjTaskId");
        String deviceId = req.getParameter("deviceId");

        lkjTaskCustomService.deleteLkj(baseLkjDataLineId, previousLkjDataLineId, nextLkjDataLineId, lkjTaskId, deviceId);
        return new Json(JsonMessage.SUCCESS);
    }

    /**
     * <p>遍历在同一组(lkjGroupId相同的lkjDataLine)的lkj，</p>
     * <p>按顺序(seq)填写一组lkj中的前一个lkjId和后一个lkjId</p>
     * <p>用于生成任务</p>
     */
    private List<LkjDataLine> linkage(List<LkjDataLine> lkjDataLines) {
        Map<String, List<LkjDataLine>> lkjDataLineMap = lkjDataLines.stream()
                .sorted(Comparator.comparingInt(LkjDataLine::getSeq))
                .collect(Collectors.groupingBy(LkjDataLine::getLkjGroupId));

        List<LkjDataLine> returnList = new ArrayList<>();
        for (List<LkjDataLine> group : lkjDataLineMap.values()) {
            for (int i = 0; i < group.size(); i++) {
                LkjDataLine current = group.get(i);

                if (i != 0)
                    current.setPreviousLkjDataLineId(group.get(i - 1).getId());

                if (i != (group.size() - 1))
                    current.setNextLkjDataLineId(group.get(i + 1).getId());

                returnList.add(current);
            }
        }
        return returnList;
    }
}

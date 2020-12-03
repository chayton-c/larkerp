package com.yingda.lkj.controller.app;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjTask;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.service.system.UserService;
import com.yingda.lkj.utils.JsonUtils;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author hood  2020/2/17
 */
@Controller
@RequestMapping("/app/user")
public class AppUserController extends BaseController {

    private UserService userService;

    @Autowired
    private BaseService<LkjTask> lkjTaskBaseService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/login")
    @ResponseBody
    public Json login(String username, String password) {
        User user = userService.getUserByUserName(username);
        if (user == null)
            return new Json(JsonMessage.AUTH_ERROR, "找不到该用户");

        String realPassword = user.getPassword();

        if (!realPassword.equals(password))
            return new Json(JsonMessage.AUTH_ERROR, "密码错误");

        return new Json(JsonMessage.SUCCESS, Map.of("userName", user.getUserName(), "displayName", user.getDisplayName(), "nfcAdministrator", 1), user.getId());
    }

    @Autowired
    private BaseService<LkjDataLine> lkjDataLineBaseService;
    @Autowired
    private BaseService<TaskDevice> taskDeviceBaseService;

    @RequestMapping(value = "/missions")
    @ResponseBody
    public Json getMissions() throws Exception {
        String userId = "";

        if (StringUtils.isNotEmpty(req.getParameter("userId"))) {
            userId = req.getParameter("userId");
        } else {
            String paramStr = new String(req.getInputStream().readAllBytes());
            Map<String, Object> parse = JsonUtils.parse(paramStr, Map.class);
            System.out.println(parse);
            userId = (String) parse.get("userId");
        }

        if (StringUtils.isEmpty(userId))
            throw new CustomException(new Json(JsonMessage.PARAM_INVALID));

        // 查询所有lkj测量任务
        List<LkjTask> lkjTasks = lkjTaskBaseService.find(
                "from LkjTask where executeUserId = :executeUserId and finishedStatus = :finishedStatus",
                Map.of("executeUserId", userId, "finishedStatus", LkjTask.PENDING_HANDLE)
        );

        List<String> lkjTaskIds = StreamUtil.getList(lkjTasks, LkjTask::getId);
        // 查询lkjTask相关的lkjDataLine
        List<LkjDataLine> lkjDataLines = lkjDataLineBaseService.find(
                "from LkjDataLine where lkjTaskId in :lkjTaskIds",
                Map.of("lkjTaskIds", lkjTaskIds)
        );
        Map<String, List<LkjDataLine>> lkjDataLineMap = lkjDataLines.stream()
                .filter(x -> x.getReadonly() == LkjDataLine.EDITABLE)
                .collect(Collectors.groupingBy(LkjDataLine::getLkjTaskId));

        List<String> leftDeviceIds = StreamUtil.getList(lkjDataLines, LkjDataLine::getLeftDeviceId);
        List<String> rightDeviceIds = StreamUtil.getList(lkjDataLines, LkjDataLine::getRightDeviceId);
        List<String> deviceIds = new ArrayList<>();
        deviceIds.addAll(leftDeviceIds);
        deviceIds.addAll(rightDeviceIds);

        // 查询lkjDataLines相关的所有设备
        String sql = new StringBuilder()
                    .append("SELECT\n")
                    .append("  device.id AS id,\n")
                    .append("  device.Name AS name,\n")
                    .append("  device.CODE AS code,\n")
                    .append("  railwayLine.NAME AS lineName,\n")
                    .append("  railwayLine.CODE AS lineCode,\n")
                    .append("  station.id AS stationId,\n")
                    .append("  station.NAME AS stationName \n")
                    .append("FROM\n")
                    .append("  device\n")
                    .append("  LEFT JOIN railway_line railwayLine ON railwayLine.id = device.railway_line_id\n")
                    .append("  LEFT JOIN station ON station.id = device.station_id \n")
                    .append("WHERE\n")
                    .append("  device.id IN :deviceIds")
                    .toString();
        List<TaskDevice> taskDevices = taskDeviceBaseService.findSQL(sql, Map.of("deviceIds", deviceIds), TaskDevice.class, 1, 99999);
        Map<String, TaskDevice> taskDeviceMap = taskDevices.stream().collect(Collectors.toMap(TaskDevice::getId, x -> x));

        List<Task> tasks = new ArrayList<>();
        for (LkjTask lkjTask : lkjTasks) {
            Task task = new Task(lkjTask);

            // 该任务下的lkj测量
            List<LkjDataLine> lkjDataLineList = lkjDataLineMap.get(lkjTask.getId());
            if (lkjDataLineList == null)
                continue;
            tasks.add(task);
            List<TaskLkjDataLine> taskLkjDataLines = lkjDataLineList.stream().map(TaskLkjDataLine::new).collect(Collectors.toList());
            task.setLkjDataLines(taskLkjDataLines);

            for (TaskLkjDataLine taskLkjDataLine : taskLkjDataLines) {
                LkjDataLine lkjDataLine = taskLkjDataLine.getLkjDataLine();
                String leftDeviceId = lkjDataLine.getLeftDeviceId();
                String rightDeviceId = lkjDataLine.getRightDeviceId();
                TaskDevice taskLeftDevice = taskDeviceMap.get(leftDeviceId);
                TaskDevice taskRightDevice = taskDeviceMap.get(rightDeviceId);

                taskLkjDataLine.setLeftDeviceId(leftDeviceId);
                taskLkjDataLine.setRightDeviceId(rightDeviceId);
                taskLkjDataLine.setDeviceList(List.of(taskLeftDevice, taskRightDevice));

                taskLkjDataLine.setLkjDataLine(null);
            }
        }

        return new Json(JsonMessage.SUCCESS, tasks);
    }

}

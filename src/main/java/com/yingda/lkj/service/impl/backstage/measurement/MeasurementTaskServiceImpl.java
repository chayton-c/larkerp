package com.yingda.lkj.service.impl.backstage.measurement;

import com.yingda.lkj.beans.entity.backstage.device.DeviceMaintenancePlan;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjTask;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTask;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTaskDetail;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTaskExecuteUser;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTemplate;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.enums.devicemaintenance.DeviceMaintenancePlanStrategy;
import com.yingda.lkj.beans.pojo.lkj.lkjtask.UserLkjTask;
import com.yingda.lkj.beans.pojo.measurement.UserMeasurementTaskDetail;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.device.DeviceMaintenancePlanService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTaskDetailService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTaskService;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hood  2020/3/18
 */
@Service("measurementTaskService")
public class MeasurementTaskServiceImpl implements MeasurementTaskService {
    @Autowired
    private BaseDao<MeasurementTask> measurementTaskBaseDao;
    @Autowired
    private BaseDao<MeasurementTaskDetail> measurementTaskDetailBaseDao;
    @Autowired
    private BaseDao<MeasurementTaskExecuteUser> measurementTaskExecuteUserBaseDao;
    @Autowired
    private DeviceMaintenancePlanService deviceMaintenancePlanService;
    @Autowired
    private BaseDao<MeasurementTemplate> measurementTemplateBaseDao;
    @Autowired
    private MeasurementTaskDetailService measurementTaskDetailService;

    @Override
    public MeasurementTask getById(String id) {
        return measurementTaskBaseDao.get(MeasurementTask.class, id);
    }

    @Override
    public MeasurementTask saveOrUpdate(MeasurementTask pageMeasurementTask, User submitUserId, String[] executeUserIds) {
        Timestamp current = new Timestamp(System.currentTimeMillis());

        String measurementTaskId = pageMeasurementTask.getId();

        MeasurementTask measurementTask;
        // 如pageMeasurementTask无id新建任务
        if (StringUtils.isEmpty(measurementTaskId)) {
            measurementTask = new MeasurementTask(pageMeasurementTask, submitUserId);
        } else {
            // 如pageMeasurementTask有id修改任务
            measurementTask = measurementTaskBaseDao.get(MeasurementTask.class, measurementTaskId);
            measurementTask.setWorkAreaId(pageMeasurementTask.getWorkAreaId());
            measurementTask.setName(pageMeasurementTask.getName());
            measurementTask.setRemark(pageMeasurementTask.getRemark());
            measurementTask.setUpdateTime(current);
            measurementTask.setStartTime(pageMeasurementTask.getStartTime());
            measurementTask.setEndTime(pageMeasurementTask.getEndTime());
        }
        measurementTaskId = measurementTask.getId();

        measurementTaskBaseDao.saveOrUpdate(measurementTask);
        // 覆盖执行人关联表
        measurementTaskExecuteUserBaseDao.executeHql(
                "delete from MeasurementTaskExecuteUser where measurementTaskId = :measurementTaskId",
                Map.of("measurementTaskId", measurementTaskId)
        );

        // 保存执行人
        List<MeasurementTaskExecuteUser> measurementTaskExecuteUsers =
                Arrays.stream(executeUserIds)
                        .filter(StringUtils::isNotEmpty)
                        .distinct()
                        .map(x -> new MeasurementTaskExecuteUser(measurementTask.getId(), x))
                        .collect(Collectors.toList());

        measurementTaskExecuteUserBaseDao.bulkInsert(measurementTaskExecuteUsers);

        return measurementTask;
    }

    @Override
    public void closeTask(String measurementTaskId) {
        measurementTaskBaseDao.executeHql(
                "update MeasurementTask set finishedStatus = :finishedStatus where id = :id",
                Map.of("finishedStatus", MeasurementTaskDetail.CLOSED, "id", measurementTaskId)
        );
        measurementTaskDetailBaseDao.executeHql(
                "update MeasurementTaskDetail set finishedStatus = :finishedStatus where measurementTaskId = :measurementTaskId",
                Map.of("finishedStatus", MeasurementTaskDetail.CLOSED, "measurementTaskId", measurementTaskId)
        );
    }

    @Override
    public void submitTask(String measurementTaskId) {
        measurementTaskBaseDao.executeHql(
                "update MeasurementTask set finishedStatus = :finishedStatus where id = :id",
                Map.of("finishedStatus", MeasurementTaskDetail.PENDING_HANDLE, "id", measurementTaskId)
        );
        measurementTaskDetailBaseDao.executeHql(
                "update MeasurementTaskDetail set finishedStatus = :finishedStatus where measurementTaskId = :measurementTaskId",
                Map.of("finishedStatus", MeasurementTaskDetail.PENDING_HANDLE, "measurementTaskId", measurementTaskId)
        );
    }

    @Override
    public void executeTask(String measurementTaskId) {
        // 注意跟上两个比，多了个executeTime = now()
        measurementTaskBaseDao.executeHql(
                "update MeasurementTask set finishedStatus = :finishedStatus, executeTime = now() where id = :id",
                Map.of("finishedStatus", MeasurementTaskDetail.COMPLETED, "id", measurementTaskId)
        );
        measurementTaskDetailBaseDao.executeHql(
                "update MeasurementTaskDetail set finishedStatus = :finishedStatus, executeTime = now() where measurementTaskId = :measurementTaskId",
                Map.of("finishedStatus", MeasurementTaskDetail.COMPLETED, "measurementTaskId", measurementTaskId)
        );
    }

    @Override
    public List<MeasurementTask> getByIds(List<String> ids) {
        return measurementTaskBaseDao.find(
                "from MeasurementTask where id in :ids",
                Map.of("ids", ids)
        );
    }

    @Override
    public List<MeasurementTask> getByDeviceMaintenancePlan(List<DeviceMaintenancePlan> deviceMaintenancePlans, Timestamp startTime, Timestamp endTime) {
        List<String> deviceMaintenancePlanIds = StreamUtil.getList(deviceMaintenancePlans, DeviceMaintenancePlan::getId);
        return measurementTaskBaseDao.find(
                "from MeasurementTask where deviceMaintenancePlanId in :deviceMaintenancePlanIds and startTime > :startTime and endTime < :endTime",
                Map.of("deviceMaintenancePlanIds", deviceMaintenancePlanIds,
                        "startTime", startTime,
                        "endTime", endTime)
        );
    }

    @Override
    public List<MeasurementTask> getTasksGantt(List<DeviceMaintenancePlan> deviceMaintenancePlans, Timestamp startTime, Timestamp endTime) {
        List<MeasurementTask> returnList = new ArrayList<>();

        // 查询符合条件的已生成的
        List<MeasurementTask> measurementTasks = getByDeviceMaintenancePlan(deviceMaintenancePlans, startTime, endTime);
        if (!measurementTasks.isEmpty())
            returnList.addAll(measurementTasks);

        // 如查询3个月后的，按照条件，把三个月后的生成出来
        for (DeviceMaintenancePlan deviceMaintenancePlan : deviceMaintenancePlans) {
            Timestamp previousCreateTaskDate = deviceMaintenancePlan.getPreviousCreateTaskDate(); // 上次任务生成日期
            previousCreateTaskDate = previousCreateTaskDate == null ? new Timestamp(System.currentTimeMillis()) : previousCreateTaskDate;

            if (previousCreateTaskDate.getTime() >= endTime.getTime()) // 已经生成任务到截止日期了，不再生成
                continue;

            int count = 0;
            while (true) {
                if (count++ > 1000000)
                    break;

                DeviceMaintenancePlanStrategy strategy = DeviceMaintenancePlanStrategy.getStrategy(deviceMaintenancePlan);
                List<MeasurementTask> nextTask = strategy.createNextTask(deviceMaintenancePlan, strategy.getNextBaseTime(deviceMaintenancePlan, previousCreateTaskDate));
                if (nextTask.isEmpty())
                    break;
                nextTask.forEach(x -> x.setVirtual(true));
                returnList.addAll(nextTask);
                previousCreateTaskDate = nextTask.get(0).getStartTime();
                if (previousCreateTaskDate.getTime() > endTime.getTime())
                    break;
            }
        }

        // 查询执行人名称
        Map<String, List<String>> executeUserNameMap = deviceMaintenancePlanService.getExecuteUserNames(deviceMaintenancePlans);
        for (MeasurementTask measurementTask : returnList) {
            String deviceMaintenancePlanId = measurementTask.getDeviceMaintenancePlanId();
            List<String> executeUserNames = executeUserNameMap.get(deviceMaintenancePlanId);
            measurementTask.setExecuteUserNames(executeUserNames);
        }

        Map<String, List<String>> taskDevices = deviceMaintenancePlanService.getTaskDevices(deviceMaintenancePlans);
        for (MeasurementTask measurementTask : returnList) {
            String deviceMaintenancePlanId = measurementTask.getDeviceMaintenancePlanId();
            List<String> deviceNames = taskDevices.get(deviceMaintenancePlanId);
            measurementTask.setDeviceNames(deviceNames);
        }

        return returnList;
    }

    @Override
    public UserMeasurementTaskDetail getUserMeasurementTaskDetail(String userId) {
        UserMeasurementTaskDetail userMeasurementTaskDetail = new UserMeasurementTaskDetail();

        // 查询用户执行的etms任务
        List<MeasurementTaskExecuteUser> measurementTaskExecuteUsers = measurementTaskExecuteUserBaseDao.find(
                "from MeasurementTaskExecuteUser where executeUserId = :executeUserId",
                Map.of("executeUserId", userId)
        );
        List<String> measurementTaskIds = StreamUtil.getList(measurementTaskExecuteUsers, MeasurementTaskExecuteUser::getMeasurementTaskId);

        List<MeasurementTask> measurementTasks = getByIds(measurementTaskIds);
        measurementTasks = measurementTasks.stream(). // 不统计未提交的任务
                filter(x -> x.getFinishedStatus() != MeasurementTask.PENDING_SUBMIT).collect(Collectors.toList());

        List<MeasurementTask> pendingHandleTasks = new ArrayList<>();
        List<MeasurementTask> completeTasks = new ArrayList<>();
        List<MeasurementTask> refusedTasks = new ArrayList<>();
        List<MeasurementTask> closedTasks = new ArrayList<>();
        for (MeasurementTask measurementTask : measurementTasks) {
            // 任务完成状态
            byte finishedStatus = measurementTask.getFinishedStatus();

            switch (finishedStatus) {
                case MeasurementTask.PENDING_HANDLE:
                    pendingHandleTasks.add(measurementTask);
                    break;
                case MeasurementTask.COMPLETED:
                    completeTasks.add(measurementTask);
                    break;
                case MeasurementTask.CLOSED:
                    closedTasks.add(measurementTask);
                    break;
            }
        }

        userMeasurementTaskDetail.setTotalTasks(measurementTasks);
        userMeasurementTaskDetail.setPendingHandleTasks(pendingHandleTasks);
        userMeasurementTaskDetail.setCompleteTasks(completeTasks);
        userMeasurementTaskDetail.setRefusedTasks(refusedTasks);
        userMeasurementTaskDetail.setClosedTasks(closedTasks);

        return userMeasurementTaskDetail;
    }

    @Override
    public void checkMissed() {
        // 查询待处理超时的
        Timestamp current = new Timestamp(System.currentTimeMillis());
        List<MeasurementTask> measurementTasks = measurementTaskBaseDao.find(
                "from MeasurementTask where endTime <= :current and finishedStatus = :finishedStatus",
                Map.of("current", current, "finishedStatus", MeasurementTask.PENDING_HANDLE)
        );

        // 我知道不用循环提交，一个sql就能改完，甚至不需要传measurementTask，但是这样更爽
        for (MeasurementTask measurementTask : measurementTasks) {
            measurementTask.setUpdateTime(current);
            measurementTask.setFinishedStatus(MeasurementTask.MISSED);
            measurementTaskDetailService.setMissed(measurementTask);
        }

        measurementTaskBaseDao.bulkInsert(measurementTasks);

    }

}

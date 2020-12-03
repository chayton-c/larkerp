package com.yingda.lkj.service.impl.backstage.measurement;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.measurement.*;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.measurement.MeasurementTaskDetailService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTaskService;
import com.yingda.lkj.service.system.UserService;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.date.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hood  2020/4/13
 */
@Service("measurementTaskDetailService")
public class MeasurementTaskDetailServiceImpl implements MeasurementTaskDetailService {

    @Autowired
    private BaseDao<MeasurementTaskExecuteUser> measurementTaskExecuteUserBaseDao;
    @Autowired
    private MeasurementTaskService measurementTaskService;
    @Autowired
    private BaseDao<MeasurementTaskDetail> measurementTaskDetailBaseDao;
    @Autowired
    private UserService userService;
    @Autowired
    private BaseDao<MeasurementTask> measurementTaskBaseDao;
    @Autowired
    private MeasurementTaskDetailService measurementTaskDetailService;

    @Override
    public MeasurementTaskDetail getById(String id) {
        return measurementTaskDetailBaseDao.get(MeasurementTaskDetail.class, id);
    }

    @Override
    public Map<String, String> getExecuteUserNames(List<MeasurementTaskDetail> measurementTaskDetails) {
        return getExecuteUserNamesByTaskIds(StreamUtil.getList(measurementTaskDetails, MeasurementTaskDetail::getMeasurementTaskId));
    }

    @Override
    public Map<String, String> getExecuteUserNamesByTaskIds(List<String> measurementTaskIds) {
        List<MeasurementTaskExecuteUser> measurementTaskExecuteUsers = measurementTaskExecuteUserBaseDao.find(
                "from MeasurementTaskExecuteUser where measurementTaskId in :measurementTaskIds",
                Map.of("measurementTaskIds", measurementTaskIds)
        );
        Map<String, String> userNameMap = userService.getByIds(
                StreamUtil.getList(measurementTaskExecuteUsers, MeasurementTaskExecuteUser::getExecuteUserId))
                .stream()
                .collect(Collectors.toMap(User::getId, User::getDisplayName));

        for (MeasurementTaskExecuteUser measurementTaskExecuteUser : measurementTaskExecuteUsers) {
            String userName = userNameMap.get(measurementTaskExecuteUser.getExecuteUserId()); // 不可能会有null
            measurementTaskExecuteUser.setExecuteUserName(userName);
        }

        Map<String, List<MeasurementTaskExecuteUser>> taskExecuteUserNameMap =
                measurementTaskExecuteUsers.stream().collect(Collectors.groupingBy(MeasurementTaskExecuteUser::getMeasurementTaskId));

        Map<String, String> executeUserNameMap = new HashMap<>();
        for (Map.Entry<String, List<MeasurementTaskExecuteUser>> entry : taskExecuteUserNameMap.entrySet()) {
            String taskId = entry.getKey();
            List<MeasurementTaskExecuteUser> users = entry.getValue();
            String userNames = users.stream().map(MeasurementTaskExecuteUser::getExecuteUserName).collect(Collectors.joining(","));

            executeUserNameMap.put(taskId, userNames);
        }

        return executeUserNameMap;
    }

    @Override
    public Map<String, String> getExecuteUserIds(List<MeasurementTaskDetail> measurementTaskDetails) {
        List<MeasurementTaskExecuteUser> measurementTaskExecuteUsers = measurementTaskExecuteUserBaseDao.find(
                "from MeasurementTaskExecuteUser where measurementTaskId in :measurementTaskIds",
                Map.of("measurementTaskIds", StreamUtil.getList(measurementTaskDetails, MeasurementTaskDetail::getMeasurementTaskId))
        );

        Map<String, List<MeasurementTaskExecuteUser>> taskExecuteUserNameMap =
                measurementTaskExecuteUsers.stream().collect(Collectors.groupingBy(MeasurementTaskExecuteUser::getMeasurementTaskId));

        Map<String, String> executeUserIdMap = new HashMap<>();
        for (Map.Entry<String, List<MeasurementTaskExecuteUser>> entry : taskExecuteUserNameMap.entrySet()) {
            String taskId = entry.getKey();
            List<MeasurementTaskExecuteUser> users = entry.getValue();
            String userIds = users.stream().map(MeasurementTaskExecuteUser::getExecuteUserId).collect(Collectors.joining(","));

            executeUserIdMap.put(taskId, userIds);
        }

        return executeUserIdMap;
    }

    @Override
    public Map<String, MeasurementTask> getMeasurementTaskMap(List<MeasurementTaskDetail> measurementTaskDetails) {
        List<MeasurementTask> measurementTasks = measurementTaskService.getByIds(StreamUtil.getList(measurementTaskDetails,
                MeasurementTaskDetail::getMeasurementTaskId));
        Map<String, MeasurementTask> rawMeasurementTaskMap = measurementTasks.stream().collect(Collectors.toMap(MeasurementTask::getId, x -> x));

        Map<String, MeasurementTask> measurementTaskMap = new HashMap<>();
        for (MeasurementTaskDetail measurementTaskDetail : measurementTaskDetails) {
            String taskId = measurementTaskDetail.getMeasurementTaskId();
            MeasurementTask measurementTask = rawMeasurementTaskMap.get(taskId);
            if (measurementTask == null)
                continue;

            measurementTaskMap.put(measurementTaskDetail.getId(), measurementTask);
        }

        return measurementTaskMap;
    }

    @Override
    public Map<String, Boolean> hasTask(List<Device> devices) {
        List<String> deviceIds = StreamUtil.getList(devices, Device::getId);
        List<MeasurementTaskDetail> measurementTaskDetails = measurementTaskDetailBaseDao.findSQL(
                "select device_id as deviceId from measurement_task_detail where finished_status = :finishedStatus and device_id in :deviceIds",
                Map.of("finishedStatus", MeasurementTaskDetail.PENDING_HANDLE, "deviceIds", deviceIds),
                MeasurementTaskDetail.class,
                1, 9999999
        );

        // key:deviceId value:true
        Map<String, Boolean> deviceIdMap = measurementTaskDetails.stream()
                .map(MeasurementTaskDetail::getDeviceId)
                .distinct()
                .collect(Collectors.toMap(x -> x, x -> Boolean.TRUE));

        return devices.stream()
                .collect(
                        Collectors.toMap(
                                Device::getId, x -> deviceIdMap.get(x.getId()) != null
                        )
                );
    }

    @Override
    public Map<MeasurementTask, List<MeasurementTaskDetail>> getAbnormalTaskAndTaskDetails(MeasurementTaskDetail pageMeasurementTaskDetail
            , List<String> measurementTaskIds) throws ParseException {
        Map<String, Object> params = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        builder.append(" SELECT ");
        builder.append(" detailTask.id AS id, ");
        builder.append(" detailTask.measurement_task_id AS measurementTaskId, ");
        builder.append(" detailTask.add_time AS addTime, ");
        builder.append(" template.name AS templateName, ");
        builder.append(" device.name AS deviceName, ");
        builder.append(" device.id AS deviceId, ");
        builder.append(" line.name AS railwayLineName, ");
        builder.append(" detailTask.measurement_template_id AS measurementTemplateId, ");
        builder.append(" station.name AS stationName ");
        builder.append(" FROM ");
        builder.append(" measurement_task_detail detailTask ");
        builder.append(" LEFT JOIN measurement_template template ON template.id = detailTask.measurement_template_id ");
        builder.append(" LEFT JOIN device ON device.id = detailTask.device_id ");
        builder.append(" LEFT JOIN station ON station.id = device.station_id ");
        builder.append(" LEFT JOIN railway_line line ON line.id = device.railway_line_id ");
        builder.append(" WHERE detailTask.measurement_task_id in :measurementTaskIds ");
        if (StringUtils.isNotEmpty(pageMeasurementTaskDetail.getStationName())) {
            builder.append(" AND station.name like :stationName ");
            params.put("stationName", "%" + pageMeasurementTaskDetail.getStationName() + "%");
        }
        if (StringUtils.isNotEmpty(pageMeasurementTaskDetail.getDeviceId())) {
            builder.append(" AND device.id = :deviceId ");
            params.put("deviceId", pageMeasurementTaskDetail.getDeviceId());
        }
        if (StringUtils.isNotEmpty(pageMeasurementTaskDetail.getStartTime()) && StringUtils.isNotEmpty(pageMeasurementTaskDetail.getEndTime())) {
            builder.append(" AND detailTask.execute_time BETWEEN :startTime AND :endTime");
            params.put("startTime", DateUtil.toDate(pageMeasurementTaskDetail.getStartTime(), "yyyy-MM-dd"));
            params.put("endTime", DateUtil.toDate(pageMeasurementTaskDetail.getEndTime(), "yyyy-MM-dd"));
        }
        builder.append(" ORDER BY detailTask.add_time DESC ");
        params.put("measurementTaskIds", measurementTaskIds);
        List<MeasurementTaskDetail> abnormalMeasurementTaskDetails = measurementTaskDetailBaseDao.findSQL(builder.toString(), params, MeasurementTaskDetail.class);

        //筛选后 异常任务id
        List<String> abnormalMeasurementTaskId = StreamUtil.getList(abnormalMeasurementTaskDetails, MeasurementTaskDetail::getMeasurementTaskId);
        //获取异常任务
        String hql = "from MeasurementTask where id in :abnormalMeasurementTaskId";
        List<MeasurementTask> abnormalMeasurementTasks = measurementTaskBaseDao.find(hql, Map.of("abnormalMeasurementTaskId", abnormalMeasurementTaskId));
        Map<MeasurementTask, List<MeasurementTaskDetail>> tasksMap = new HashMap<>();

        //获取执行人名key:measurementTask.id(任务id) value:执行人姓名
        Map<String, String> executeUserNameMap = measurementTaskDetailService.getExecuteUserNames(abnormalMeasurementTaskDetails);
        for (MeasurementTask measurementTask : abnormalMeasurementTasks) {
            String taskId = measurementTask.getId();
            String executeUserNames = executeUserNameMap.get(taskId);
            measurementTask.setExecuteUserName(executeUserNames);
        }

        //组装 key：任务 value：对应子任务
        for (MeasurementTask task : abnormalMeasurementTasks) {
            List<MeasurementTaskDetail> measurementTaskDetails = abnormalMeasurementTaskDetails.stream()
                    .filter(x -> x.getMeasurementTaskId().equals(task.getId())).collect(Collectors.toList());
            tasksMap.put(task, measurementTaskDetails);
        }
        return tasksMap;
    }

    @Override
    public List<MeasurementTaskDetail> getMeasurementTaskDetailsByDeviceId(String deviceId) {
        return measurementTaskDetailBaseDao.find(
                "from MeasurementTaskDetail where deviceId = :deviceId",
                Map.of("deviceId", deviceId)
        );
    }

    @Override
    public void executeTask(String measurementTaskDetailId) {
        Timestamp current = new Timestamp(System.currentTimeMillis());
        // 完成当前子任务
        MeasurementTaskDetail measurementTaskDetail = getById(measurementTaskDetailId);
        measurementTaskDetail.setFinishedStatus(MeasurementTaskDetail.COMPLETED);
        measurementTaskDetail.setExecuteTime(current);
        measurementTaskDetail.setUpdateTime(current);
        measurementTaskDetailBaseDao.saveOrUpdate(measurementTaskDetail);

        // 如果所有子任务都已完成，提交主任务
        String measurementTaskId = measurementTaskDetail.getMeasurementTaskId();
        List<MeasurementTaskDetail> measurementTaskDetails = getByMeasurementTaskId(measurementTaskId);
        if (measurementTaskDetails.stream().allMatch(x -> MeasurementTaskDetail.COMPLETED == x.getFinishedStatus()))
            measurementTaskService.executeTask(measurementTaskId);
    }

    @Override
    public void setMissed(MeasurementTask measurementTask) {
        measurementTaskDetailBaseDao.executeHql(
                "update MeasurementTaskDetail set finishedStatus = :finishedStatus, updateTime = now() where measurementTaskId = :measurementTaskId",
                Map.of("finishedStatus", MeasurementTaskDetail.MISSED, "measurementTaskId", measurementTask.getId())
        );
    }

    private List<MeasurementTaskDetail> getByMeasurementTaskId(String measurementTaskId) {
        return measurementTaskDetailBaseDao.find(
                "from MeasurementTaskDetail where measurementTaskId = :measurementTaskId",
                Map.of("measurementTaskId", measurementTaskId)
        );
    }
}

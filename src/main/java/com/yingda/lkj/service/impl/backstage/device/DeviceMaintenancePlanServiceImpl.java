package com.yingda.lkj.service.impl.backstage.device;

import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.backstage.device.DeviceMaintenancePlan;
import com.yingda.lkj.beans.entity.backstage.device.DeviceMaintenancePlanDevice;
import com.yingda.lkj.beans.entity.backstage.device.DeviceMaintenancePlanUser;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTask;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTaskDetail;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTemplate;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.enums.devicemaintenance.DeviceMaintenancePlanFinishStatus;
import com.yingda.lkj.beans.enums.devicemaintenance.DeviceMaintenancePlanStrategy;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.device.DeviceMaintenancePlanDeviceService;
import com.yingda.lkj.service.backstage.device.DeviceMaintenancePlanService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTaskService;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.date.CalendarUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hood  2020/4/3
 */
@Service("deviceMaintenancePlanService")
public class DeviceMaintenancePlanServiceImpl implements DeviceMaintenancePlanService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceMaintenancePlanServiceImpl.class);

    @Autowired
    private BaseDao<DeviceMaintenancePlan> deviceMaintenancePlanBaseDao;
    @Autowired
    private BaseDao<DeviceMaintenancePlanUser> deviceMaintenancePlanUserBaseDao;
    @Autowired
    private BaseDao<DeviceMaintenancePlanDevice> deviceMaintenancePlanDeviceBaseDao;
    @Autowired
    private BaseDao<User> userBaseDao;
    @Autowired
    private MeasurementTaskService measurementTaskService;
    @Autowired
    private OrganizationClientService organizationClientService;
    @Autowired
    private BaseDao<MeasurementTaskDetail> measurementTaskDetailBaseDao;
    @Autowired
    private BaseDao<MeasurementTemplate> measurementTemplateBaseDao;

    @Override
    public void saveOrUpdate(DeviceMaintenancePlan pageDeviceMaintenancePlan, User submitUser) {
        String deviceMaintenancePlanId = pageDeviceMaintenancePlan.getId();
        DeviceMaintenancePlan deviceMaintenancePlan;

        if (StringUtils.isNotEmpty(deviceMaintenancePlanId)) {
            deviceMaintenancePlan = deviceMaintenancePlanBaseDao.get(DeviceMaintenancePlan.class, deviceMaintenancePlanId);

            String workshopId = pageDeviceMaintenancePlan.getWorkshopId();
            String originalWorkshopId = deviceMaintenancePlan.getWorkshopId();
            // 变更车间，要移除旧的设备扩展表和人员表
            if (!workshopId.equals(originalWorkshopId)) {
                deviceMaintenancePlanDeviceBaseDao.executeHql(
                        "delete from DeviceMaintenancePlanDevice where deviceMaintenancePlanId = :deviceMaintenancePlanId",
                        Map.of("deviceMaintenancePlanId", deviceMaintenancePlanId)
                );
            }

            deviceMaintenancePlan.setWorkshopId(workshopId);
            deviceMaintenancePlan.setName(pageDeviceMaintenancePlan.getName());
            deviceMaintenancePlan.setExecutionStrategy(pageDeviceMaintenancePlan.getExecutionStrategy());
            deviceMaintenancePlan.setExecutionCycle(pageDeviceMaintenancePlan.getExecutionCycle());
            deviceMaintenancePlan.setExecutionDate(pageDeviceMaintenancePlan.getExecutionDate());
            deviceMaintenancePlan.setExecutionDuration(pageDeviceMaintenancePlan.getExecutionDuration());
            deviceMaintenancePlan.setExecuteTime(pageDeviceMaintenancePlan.getExecuteTime());

            Timestamp previousCreateTaskDate = DeviceMaintenancePlanStrategy.getStrategy(deviceMaintenancePlan).getPreviousCreateTaskDate(deviceMaintenancePlan);
            deviceMaintenancePlan.setPreviousCreateTaskDate(previousCreateTaskDate);
            deviceMaintenancePlan.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        } else {
            deviceMaintenancePlan = new DeviceMaintenancePlan(submitUser, pageDeviceMaintenancePlan);
        }

        deviceMaintenancePlanBaseDao.saveOrUpdate(deviceMaintenancePlan);

        // 更新执行人
        deviceMaintenancePlanUserBaseDao.executeHql(
                "delete from DeviceMaintenancePlanUser where deviceMaintenancePlanId = :deviceMaintenancePlanId",
                Map.of("deviceMaintenancePlanId", deviceMaintenancePlan.getId())
        );
        String executeUserIds = pageDeviceMaintenancePlan.getExecuteUserIds();
        String[] executeUserIdArr = executeUserIds.split(",");
        for (String executeUserId : executeUserIdArr) {
            User executeUser = userBaseDao.get(User.class, executeUserId);
            DeviceMaintenancePlanUser deviceMaintenancePlanUser = new DeviceMaintenancePlanUser(executeUser, deviceMaintenancePlan);
            deviceMaintenancePlanUserBaseDao.saveOrUpdate(deviceMaintenancePlanUser);
        }
    }

    @Autowired
    private DeviceMaintenancePlanDeviceService deviceMaintenancePlanDeviceService;

    @Override
    public void timedGenerateMeasurementTask() {
        // 查询所有的周期计划
        List<DeviceMaintenancePlan> deviceMaintenancePlans = deviceMaintenancePlanBaseDao.find(
                "from DeviceMaintenancePlan where hide = :hide and executeStatus = :executeStatus",
                Map.of("hide", Constant.SHOW, "executeStatus", DeviceMaintenancePlanFinishStatus.PROCESSING.getExecuteStatus())
        );
        Map<String, List<DeviceMaintenancePlanDevice>> taskDevicesMap = deviceMaintenancePlanDeviceService.getByDeviceMaintenanPlans(deviceMaintenancePlans);

        for (DeviceMaintenancePlan deviceMaintenancePlan : deviceMaintenancePlans) {
            List<DeviceMaintenancePlanDevice> devices = taskDevicesMap.get(deviceMaintenancePlan.getId());
            if (devices != null)
                generateMeasurementTask(deviceMaintenancePlan);
        }
    }

    @Override
    public List<DeviceMaintenancePlan> getByName(String name, String sectionId) {
        List<Organization> workshops = organizationClientService.getWorkshops();
        if (StringUtils.isNotEmpty(sectionId))
            workshops = workshops.stream().filter(x -> sectionId.equals(x.getParentId())).collect(Collectors.toList());

        List<String> workshopIds = workshops.stream().filter(x -> x.getName().contains(name)).map(Organization::getId).collect(Collectors.toList());

        return deviceMaintenancePlanBaseDao.find(
                "from DeviceMaintenancePlan where workshopId in :workshopIds",
                Map.of("workshopIds", workshopIds)
        );
    }

    @Override
    public Map<String, List<String>> getExecuteUserNames(List<DeviceMaintenancePlan> deviceMaintenancePlans) {
        List<String> deviceMaintenancePlanIds = StreamUtil.getList(deviceMaintenancePlans, DeviceMaintenancePlan::getId);
        List<DeviceMaintenancePlanUser> deviceMaintenancePlanUsers = deviceMaintenancePlanUserBaseDao.find(
                "from DeviceMaintenancePlanUser where deviceMaintenancePlanId in :deviceMaintenancePlanIds",
                Map.of("deviceMaintenancePlanIds", deviceMaintenancePlanIds)
        );

        // key:计划id value:执行人list
        Map<String, List<DeviceMaintenancePlanUser>> collect =
                deviceMaintenancePlanUsers.stream().collect(Collectors.groupingBy(DeviceMaintenancePlanUser::getDeviceMaintenancePlanId));

        Map<String, List<String>> returnMap = new HashMap<>();
        for (Map.Entry<String, List<DeviceMaintenancePlanUser>> entry : collect.entrySet()) {
            List<DeviceMaintenancePlanUser> executeUsers = entry.getValue();
            String planId = entry.getKey();

            List<String> executeUserName =
                    executeUsers.stream().map(DeviceMaintenancePlanUser::getExecuteUserDisplayName).collect(Collectors.toList());
            returnMap.put(planId, executeUserName);
        }

        return returnMap;
    }

    @Override
    public Map<String, List<String>> getTaskDevices(List<DeviceMaintenancePlan> deviceMaintenancePlans) {
        List<String> deviceMaintenancePlanIds = StreamUtil.getList(deviceMaintenancePlans, DeviceMaintenancePlan::getId);

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT\n");
        sqlBuilder.append("  planDevice.device_maintenance_plan_id AS deviceMaintenancePlanId,\n");
        sqlBuilder.append("  device.NAME AS deviceName \n");
        sqlBuilder.append("FROM\n");
        sqlBuilder.append("  device_maintenance_plan_device planDevice\n");
        sqlBuilder.append("  LEFT JOIN device ON device.id = planDevice.device_id\n");
        sqlBuilder.append("WHERE\n");
        sqlBuilder.append("  planDevice.device_maintenance_plan_id IN :deviceMaintenancePlanIds");

        List<DeviceMaintenancePlanDevice> deviceMaintenancePlanDevices = deviceMaintenancePlanDeviceBaseDao.findSQL(
                sqlBuilder.toString(), Map.of("deviceMaintenancePlanIds", deviceMaintenancePlanIds), DeviceMaintenancePlanDevice.class
        );

        Map<String, List<DeviceMaintenancePlanDevice>> deviceMaintenancePlanDeviceMap =
                deviceMaintenancePlanDevices.stream().collect(Collectors.groupingBy(DeviceMaintenancePlanDevice::getDeviceMaintenancePlanId));

        Map<String, List<String>> returnMap = new HashMap<>();
        for (Map.Entry<String, List<DeviceMaintenancePlanDevice>> entry : deviceMaintenancePlanDeviceMap.entrySet()) {
            List<DeviceMaintenancePlanDevice> devices = entry.getValue();
            String planId = entry.getKey();

            List<String> deviceNames =
                    devices.stream().map(DeviceMaintenancePlanDevice::getDeviceName).collect(Collectors.toList());
            returnMap.put(planId, deviceNames);
        }
        return returnMap;
    }

    @Override
    public void closePlan(String deviceMaintenancePlanId) {
        deviceMaintenancePlanBaseDao.executeHql(
                "update DeviceMaintenancePlan set executeStatus = :executeStatus where id = :deviceMaintenancePlanId",
                Map.of("executeStatus", DeviceMaintenancePlanFinishStatus.CLOSED.getExecuteStatus(), "deviceMaintenancePlanId", deviceMaintenancePlanId)
        );
    }

    @Override
    public void startPlan(String deviceMaintenancePlanId) {
        deviceMaintenancePlanBaseDao.executeHql(
                "update DeviceMaintenancePlan set executeStatus = :executeStatus where id = :deviceMaintenancePlanId",
                Map.of("executeStatus", DeviceMaintenancePlanFinishStatus.PROCESSING.getExecuteStatus(), "deviceMaintenancePlanId", deviceMaintenancePlanId)
        );
    }

    /**
     * 周期计划生成测量计划
     */
    private void generateMeasurementTask(DeviceMaintenancePlan deviceMaintenancePlan) {
        String deviceMaintenancePlanId = deviceMaintenancePlan.getId();

        String workshopId = deviceMaintenancePlan.getWorkshopId();
        List<Organization> workAreas = organizationClientService.getSlave(workshopId);

        if (CollectionUtils.isEmpty(workAreas)) {
            String errorMsg = String.format("找不到 id为'%s'的车间下的工区，无法生成Id为'%s'的计划对应的任务", workshopId, deviceMaintenancePlanId);
            LOGGER.error(errorMsg);
            return;
        }
        // 生成测量任务需要的是工区，而测量计划选择的是车间，这里取车间下的任意一个工区作为测量任务的执行工区
        Organization workArea = workAreas.get(0);

        // 周期计划执行人副表
        List<DeviceMaintenancePlanUser> deviceMaintenancePlanUsers = deviceMaintenancePlanUserBaseDao.find(
                "from DeviceMaintenancePlanUser where deviceMaintenancePlanId = :deviceMaintenancePlanId and hide = :hide",
                Map.of("deviceMaintenancePlanId", deviceMaintenancePlanId, "hide", Constant.SHOW)
        );
        String[] executeUserIds = deviceMaintenancePlanUsers.stream().
                map(DeviceMaintenancePlanUser::getExecuteUserId)
                .collect(Collectors.toList())
                .toArray(new String[deviceMaintenancePlanUsers.size()]); // 执行人userIds

        // 周期计划设备副表
        List<DeviceMaintenancePlanDevice> deviceMaintenancePlanDevices = deviceMaintenancePlanDeviceBaseDao.find(
                "from DeviceMaintenancePlanDevice where deviceMaintenancePlanId = :deviceMaintenancePlanId and hide = :hide",
                Map.of("deviceMaintenancePlanId", deviceMaintenancePlanId, "hide", Constant.SHOW)
        );
        List<MeasurementTask> rawMeasurementTasks =
                DeviceMaintenancePlanStrategy.getStrategy(deviceMaintenancePlan).createNextTask(deviceMaintenancePlan);

        for (MeasurementTask rawMeasurementTask : rawMeasurementTasks) {
            rawMeasurementTask.setWorkAreaId(workArea.getId());

            // 生成测量任务
            String submitUserId = deviceMaintenancePlan.getSubmitUserId();
            User submiteUser = userBaseDao.get(User.class, submitUserId);
            MeasurementTask measurementTask = measurementTaskService.saveOrUpdate(rawMeasurementTask, submiteUser, executeUserIds);

            String measurementTaskId = measurementTask.getId();

            // 每个设备生成一条测量任务副表
            for (DeviceMaintenancePlanDevice deviceMaintenancePlanDevice : deviceMaintenancePlanDevices) {
                String measurementTemplateId = deviceMaintenancePlanDevice.getMeasurementTemplateId();
                if (StringUtils.isEmpty(measurementTemplateId)) // 尚未配置模板的设备，不会生成子任务
                    continue;

                String deviceId = deviceMaintenancePlanDevice.getDeviceId();
                MeasurementTaskDetail rawMeasurementTaskDetail = new MeasurementTaskDetail();

                rawMeasurementTaskDetail.setMeasurementTemplateId(measurementTemplateId);
                rawMeasurementTaskDetail.setMeasurementTaskId(measurementTaskId);
                rawMeasurementTaskDetail.setDeviceId(deviceId);

                MeasurementTaskDetail measurementTaskDetail = new MeasurementTaskDetail(rawMeasurementTaskDetail);
                measurementTaskDetailBaseDao.saveOrUpdate(measurementTaskDetail);
            }
        }

        if (!rawMeasurementTasks.isEmpty()) {
            // 生成成功后，修改deviceMaintenancePlan的生成时间
            deviceMaintenancePlan.setPreviousCreateTaskDate(new Timestamp(System.currentTimeMillis()));
            deviceMaintenancePlanBaseDao.saveOrUpdate(deviceMaintenancePlan);
        }
    }


}

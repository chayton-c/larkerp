package com.yingda.lkj.service.backstage.device;

import com.yingda.lkj.beans.entity.backstage.device.DeviceMaintenancePlan;
import com.yingda.lkj.beans.entity.system.User;

import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/4/3
 */
public interface DeviceMaintenancePlanService {
    void saveOrUpdate(DeviceMaintenancePlan pageDeviceMaintenancePlan, User submitUser) throws Exception;

    /**
     * 根据deviceMeasurementPlan(设备维护计划)定时生成测量计划
     */
    void timedGenerateMeasurementTask();

    List<DeviceMaintenancePlan> getByName(String name, String sectionId);

    /**
     * 获取计划下的执行人姓名
     * @return key:计划id(DeviceMaintenancePlan.id) value:计划下所有执行人姓名
     */
    Map<String, List<String>> getExecuteUserNames(List<DeviceMaintenancePlan> deviceMaintenancePlans);

    /**
     * 获取计划下的设备名
     * @return key:计划id(DeviceMaintenancePlan.id) value:计划下所有设备名
     */
    Map<String, List<String>> getTaskDevices(List<DeviceMaintenancePlan> deviceMaintenancePlans);

    void closePlan(String deviceMaintenancePlanId);

    void startPlan(String deviceMaintenancePlanId);
}

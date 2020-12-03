package com.yingda.lkj.service.backstage.device;

import com.yingda.lkj.beans.entity.backstage.device.DeviceMaintenancePlan;
import com.yingda.lkj.beans.entity.backstage.device.DeviceMaintenancePlanDevice;

import java.util.List;
import java.util.Map;

/**
 * 设备养护计划设备副表
 *
 * @author hood  2020/6/13
 */
public interface DeviceMaintenancePlanDeviceService {

    /**
     * 获取deviceMaintenancePlanId对应计划的当前最大排序
     */
    int getCurrentSeq(String deviceMaintenancePlanId);

    /**
     * 获取deviceMaintenancePlanId对应计划的当前最大排序
     */
    Map<String, List<DeviceMaintenancePlanDevice>> getByDeviceMaintenanPlans(List<DeviceMaintenancePlan> deviceMaintenancePlans);
}

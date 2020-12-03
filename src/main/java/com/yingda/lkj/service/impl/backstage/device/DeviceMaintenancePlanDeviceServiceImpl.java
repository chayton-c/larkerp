package com.yingda.lkj.service.impl.backstage.device;

import com.yingda.lkj.beans.entity.backstage.device.DeviceMaintenancePlan;
import com.yingda.lkj.beans.entity.backstage.device.DeviceMaintenancePlanDevice;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.device.DeviceMaintenancePlanDeviceService;
import com.yingda.lkj.utils.StreamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/6/13
 */
@Service("deviceMaintenancePlanDeviceService")
public class DeviceMaintenancePlanDeviceServiceImpl implements DeviceMaintenancePlanDeviceService {

    @Autowired
    private BaseDao<DeviceMaintenancePlanDevice> deviceMaintenancePlanDeviceBaseDao;

    @Override
    public int getCurrentSeq(String deviceMaintenancePlanId) {

        List<DeviceMaintenancePlanDevice> deviceMaintenancePlanDevices = deviceMaintenancePlanDeviceBaseDao.find(
                "from DeviceMaintenancePlanDevice where deviceMaintenancePlanId = :deviceMaintenancePlanId order by seq desc",
                Map.of("deviceMaintenancePlanId", deviceMaintenancePlanId), 1, 1
        );

        if (deviceMaintenancePlanDevices.isEmpty())
            return 0;

        return deviceMaintenancePlanDevices.get(0).getSeq();
    }

    @Override
    public Map<String, List<DeviceMaintenancePlanDevice>> getByDeviceMaintenanPlans(List<DeviceMaintenancePlan> deviceMaintenancePlans) {
        List<String> deviceMaintenancePlanIds = StreamUtil.getList(deviceMaintenancePlans, DeviceMaintenancePlan::getId);

        List<DeviceMaintenancePlanDevice> deviceMaintenancePlanDevices = deviceMaintenancePlanDeviceBaseDao.find(
                "from DeviceMaintenancePlanDevice where deviceMaintenancePlanId in :deviceMaintenancePlanIds",
                Map.of("deviceMaintenancePlanIds", deviceMaintenancePlanIds)
        );

        return StreamUtil.groupList(deviceMaintenancePlanDevices, DeviceMaintenancePlanDevice::getDeviceMaintenancePlanId);
    }
}

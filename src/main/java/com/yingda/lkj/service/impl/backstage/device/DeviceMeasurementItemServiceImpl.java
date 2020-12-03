package com.yingda.lkj.service.impl.backstage.device;

import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.device.DeviceExtendField;
import com.yingda.lkj.beans.entity.backstage.device.DeviceMeasurementItem;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.device.DeviceMeasurementItemService;
import com.yingda.lkj.service.backstage.device.DeviceService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.StreamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("deviceMeasurementItemService")
public class DeviceMeasurementItemServiceImpl implements DeviceMeasurementItemService {
    @Autowired
    private BaseDao<DeviceMeasurementItem> deviceMeasurementItemBaseDao;
    @Autowired
    private DeviceService deviceService;

    @Override
    public List<DeviceMeasurementItem> getByDeviceId(String deviceId) {
        return getByDeviceTypeId(deviceService.getById(deviceId).getDeviceTypeId());
    }

    @Override
    public List<DeviceMeasurementItem> getByDeviceTypeId(String deviceTypeId) {
        return deviceMeasurementItemBaseDao.find(
                "from DeviceMeasurementItem where deviceTypeId = :deviceTypeId and hide = :hide",
                Map.of("deviceTypeId", deviceTypeId, "hide", Constant.SHOW)
        );
    }

    @Override
    public DeviceMeasurementItem getById(String deviceMeasurementItemId) {
        if (deviceMeasurementItemId == null)
            return null;

        return deviceMeasurementItemBaseDao.get(DeviceMeasurementItem.class, deviceMeasurementItemId);
    }
}

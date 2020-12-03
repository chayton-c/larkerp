package com.yingda.lkj.service.backstage.device;

import com.yingda.lkj.beans.entity.backstage.device.DeviceMeasurementItem;

import java.util.ArrayList;
import java.util.List;

public interface DeviceMeasurementItemService {

    List<DeviceMeasurementItem> getByDeviceId(String deviceId);

    List<DeviceMeasurementItem> getByDeviceTypeId(String deviceTypeId);

    DeviceMeasurementItem getById(String deviceMeasurementItemId);

}

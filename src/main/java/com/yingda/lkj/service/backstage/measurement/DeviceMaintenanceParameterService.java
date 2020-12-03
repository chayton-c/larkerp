package com.yingda.lkj.service.backstage.measurement;

import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItemFieldValue;

/**
 * @author hood  2020/6/10
 */
public interface DeviceMaintenanceParameterService {
    void saveOrUpdateDeviceMaintenanceParameter(MeasurementItemFieldValue measurementItemFieldValue);
}

package com.yingda.lkj.service.impl.backstage.measurement;

import com.yingda.lkj.beans.entity.backstage.measurement.DeviceMaintenanceParameter;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItemField;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItemFieldValue;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTaskDetail;
import com.yingda.lkj.beans.pojo.utils.UnitPojo;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.measurement.DeviceMaintenanceParameterService;
import com.yingda.lkj.service.backstage.measurement.MeasurementItemFieldService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTaskDetailService;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.UnitUtil;
import com.yingda.lkj.utils.math.NumberUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/6/10
 */
@Service("deviceMaintenanceParameterService")
public class DeviceMaintenanceParameterServiceImpl implements DeviceMaintenanceParameterService {

    @Autowired
    private BaseDao<DeviceMaintenanceParameter> deviceMaintenanceParameterBaseDao;
    @Autowired
    private MeasurementItemFieldService measurementItemFieldService;
    @Autowired
    private MeasurementTaskDetailService measurementTaskDetailService;

    @Override
    public void saveOrUpdateDeviceMaintenanceParameter(MeasurementItemFieldValue measurementItemFieldValue) {
        String valueStr = measurementItemFieldValue.getValue();
        // 转不成数字，就不生成设备维护参数
        if (!NumberUtil.isDouble(valueStr))
            return;
        // 如果没有对应的设备测量字段，不添加新的设备维护参数记录
        if (StringUtils.isEmpty(measurementItemFieldValue.getDeviceMeasurementItemId()))
            return;

        String measurementItemFieldValueId = measurementItemFieldValue.getId();
        String measurementItemFieldId = measurementItemFieldValue.getMeasurementItemFieldId();
        MeasurementItemField measurementItemField = measurementItemFieldService.getById(measurementItemFieldId);

        DeviceMaintenanceParameter deviceMaintenanceParameter = deviceMaintenanceParameterBaseDao.get(
                "from DeviceMaintenanceParameter where sourceDataId = :measurementItemFieldValueId",
                Map.of("measurementItemFieldValueId", measurementItemFieldValueId)
        );

        String unitName = measurementItemField.getUnitName();
        double value = Double.parseDouble(valueStr);
        UnitPojo unitPojo = UnitUtil.convertToSmallestUnit(unitName, value);
        String measurementUnitId = measurementItemField.getMeasurementUnitId();

        String measurementTaskDetailId = measurementItemFieldValue.getMeasurementTaskDetailId();
        MeasurementTaskDetail measurementTaskDetail = measurementTaskDetailService.getById(measurementTaskDetailId);
        Map<String, String> executeUserNameMap = measurementTaskDetailService.getExecuteUserNames(List.of(measurementTaskDetail));
        String executeUserNames = executeUserNameMap.get(measurementTaskDetail.getMeasurementTaskId());

        if (deviceMaintenanceParameter == null)
            deviceMaintenanceParameter = new DeviceMaintenanceParameter(measurementItemFieldValue, unitPojo, measurementUnitId, executeUserNames);

        deviceMaintenanceParameter.setValue(unitPojo.getValue());
        deviceMaintenanceParameter.setUnitName(unitPojo.getUnitName());
        deviceMaintenanceParameter.setExecuteUserNames(executeUserNames);

        deviceMaintenanceParameterBaseDao.saveOrUpdate(deviceMaintenanceParameter);
    }
}

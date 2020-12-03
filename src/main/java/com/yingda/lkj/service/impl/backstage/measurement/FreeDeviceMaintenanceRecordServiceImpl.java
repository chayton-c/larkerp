package com.yingda.lkj.service.impl.backstage.measurement;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.measurement.DeviceMaintenanceParameter;
import com.yingda.lkj.beans.entity.backstage.measurement.FreeDeviceMaintenanceRecord;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementUnit;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.app.etms.freeetms.AppFreeDeviceMaintenanceRecord;
import com.yingda.lkj.beans.pojo.app.etms.freeetms.AppFreeDeviceMaintenanceRecordFieldValue;
import com.yingda.lkj.beans.pojo.utils.UnitPojo;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.device.DeviceService;
import com.yingda.lkj.service.backstage.measurement.FreeDeviceMaintenanceRecordService;
import com.yingda.lkj.service.backstage.measurement.MeasurementUnitService;
import com.yingda.lkj.service.system.UserService;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.UnitUtil;
import com.yingda.lkj.utils.date.DateUtil;
import com.yingda.lkj.utils.math.NumberUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hood  2020/6/10
 */
@Service("freeDeviceMaintenanceRecordService")
public class FreeDeviceMaintenanceRecordServiceImpl implements FreeDeviceMaintenanceRecordService {

    @Autowired
    private BaseDao<FreeDeviceMaintenanceRecord> freeDeviceMaintenanceRecordBaseDao;
    @Autowired
    private BaseDao<DeviceMaintenanceParameter> deviceMaintenanceParameterBaseDao;
    @Autowired
    private MeasurementUnitService measurementUnitService;
    @Autowired
    private UserService userService;
    @Autowired
    private DeviceService deviceService;

    @Override
    public void addFromApp(AppFreeDeviceMaintenanceRecord appFreeDeviceMaintenanceRecord) {
        String userId = appFreeDeviceMaintenanceRecord.getUserId();
        User user = userService.getById(userId);
        Device device = deviceService.getById(appFreeDeviceMaintenanceRecord.getDeviceId());
        FreeDeviceMaintenanceRecord freeDeviceMaintenanceRecord = new FreeDeviceMaintenanceRecord(appFreeDeviceMaintenanceRecord, user, device);
        freeDeviceMaintenanceRecordBaseDao.saveOrUpdate(freeDeviceMaintenanceRecord);

        List<DeviceMaintenanceParameter> deviceMaintenanceParameters = new ArrayList<>();
        // app中的测量值
        List<AppFreeDeviceMaintenanceRecordFieldValue> appFreeDeviceMaintenanceRecordValues = appFreeDeviceMaintenanceRecord.getValue();
        for (AppFreeDeviceMaintenanceRecordFieldValue appFreeDeviceMaintenanceRecordValue : appFreeDeviceMaintenanceRecordValues) {
            String value = appFreeDeviceMaintenanceRecordValue.getValue();
            if (!NumberUtil.isDouble(value)) // 转不成数字的不参与比较
                continue;

            String mainFunctionCode = appFreeDeviceMaintenanceRecordValue.getMainFunctionCode(); // 主功能码
            String subFunctionCode = appFreeDeviceMaintenanceRecordValue.getSubFunctionCode(); // 子功能码
            String unitName = appFreeDeviceMaintenanceRecordValue.getUnit();
            // 转换单位为最小单位 如3A => 3000mA，便于比较
            UnitPojo unitPojo = UnitUtil.convertToSmallestUnit(unitName, Double.parseDouble(value));

            MeasurementUnit measurementUnit = measurementUnitService.getByFunctionCode(mainFunctionCode, subFunctionCode); // 测量单位表中的数据

            String deviceMeasurementItemId = appFreeDeviceMaintenanceRecordValue.getDeviceMeasurementItemId();
            if (StringUtils.isEmpty(deviceMeasurementItemId))
                continue;
            DeviceMaintenanceParameter deviceMaintenanceParameter = new DeviceMaintenanceParameter(
                    freeDeviceMaintenanceRecord, measurementUnit.getId(), unitPojo, user.getDisplayName(), deviceMeasurementItemId);
            deviceMaintenanceParameters.add(deviceMaintenanceParameter);
        }

        deviceMaintenanceParameterBaseDao.bulkInsert(deviceMaintenanceParameters);
    }
}

package com.yingda.lkj.service.impl.backstage.device;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.device.DeviceExtendField;
import com.yingda.lkj.beans.entity.backstage.device.DeviceType;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.device.DeviceExtendFieldService;
import com.yingda.lkj.utils.StreamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hood  2019/12/31
 */
@Service("deviceExtendFieldService")
public class DeviceExtendFieldServiceImpl implements DeviceExtendFieldService {

    @Autowired
    private BaseDao<Device> deviceBaseDao;
    @Autowired
    private BaseDao<DeviceExtendField> deviceExtendFieldBaseDao;


    @Override
    public void saveDeviceExtendField(List<DeviceExtendField> deviceExtendFields) {
        String deviceTypeId = deviceExtendFields.stream().map(DeviceExtendField::getDeviceTypeId).distinct().reduce(null, (x, y) -> y);

        // 原有的扩展字段
        List<DeviceExtendField> originalFields = deviceExtendFieldBaseDao.find("from DeviceExtendField where deviceTypeId = :deviceTypeId", Map.of(
                "deviceTypeId", deviceTypeId));
        List<String> originalFieldNames = StreamUtil.getList(originalFields, DeviceExtendField::getName);

        // 已添加的字段不会插入
        deviceExtendFields = deviceExtendFields.stream().filter(x -> !originalFieldNames.contains(x.getName())).collect(Collectors.toList());

        deviceExtendFieldBaseDao.bulkInsert(deviceExtendFields);
    }

    @Override
    public List<DeviceExtendField> getFieldsByDeviceId(String deviceId) {
        Device device = deviceBaseDao.get(Device.class, deviceId);
        return this.getFieldsByDeviceTypeId(device.getDeviceTypeId());
    }

    @Override
    public List<DeviceExtendField> getFieldsByDeviceTypeId(String deviceTypeId) {
        return deviceExtendFieldBaseDao.find("from DeviceExtendField where deviceTypeId = :deviceTypeId  order by seq", Map.of("deviceTypeId",
                deviceTypeId));
    }

    @Override
    public List<DeviceExtendField> getFieldsByNames(List<String> names) throws CustomException {
        List<DeviceExtendField> deviceExtendFields = deviceExtendFieldBaseDao.find("from DeviceExtendField where name in :names", Map.of("names",
                names));
        if (names.size() != deviceExtendFields.size()) {
            List<String> acquiredNameList = deviceExtendFields.stream().map(DeviceExtendField::getName).collect(Collectors.toList());
            List<String> notIncluded = names.stream().filter(x -> !acquiredNameList.contains(x)).collect(Collectors.toList());
            throw new CustomException(new Json(JsonMessage.SYS_ERROR, "找不到扩展字段(device_extend_field)记录，name in " + notIncluded));
        }

        return deviceExtendFields;
    }

}

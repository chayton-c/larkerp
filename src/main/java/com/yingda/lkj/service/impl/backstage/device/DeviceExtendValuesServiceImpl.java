package com.yingda.lkj.service.impl.backstage.device;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.device.DeviceExtendField;
import com.yingda.lkj.beans.entity.backstage.device.DeviceExtendValues;
import com.yingda.lkj.service.backstage.device.DeviceExtendFieldService;
import com.yingda.lkj.service.backstage.device.DeviceExtendValuesService;
import com.yingda.lkj.service.base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hood  2019/12/31
 */
@Service("deviceExtendValuesService")
public class DeviceExtendValuesServiceImpl implements DeviceExtendValuesService {

    @Autowired
    private BaseService<DeviceExtendValues> deviceExtendValuesBaseService;
    @Autowired
    private DeviceExtendFieldService deviceExtendFieldService;

    @Override
    public Map<String, DeviceExtendValues> getExtendValueMap(Device device) throws Exception {
        String deviceTypeId = device.getDeviceTypeId();

        List<DeviceExtendValues> deviceExtendValues = deviceExtendValuesBaseService.find(
                "from DeviceExtendValues where deviceTypeId = :deviceTypeId and deviceId = :deviceId",
                Map.of("deviceTypeId", deviceTypeId,
                        "deviceId", device.getId())
        );

        return deviceExtendValues.stream().collect(Collectors.toMap(
                DeviceExtendValues::getDeviceFieldId,
                x -> x
        ));
    }

    @Override
    public void saveExtendValues(Device device, Map<String, String> parameterMap) throws Exception {
        List<DeviceExtendField> deviceExtendFields = deviceExtendFieldService.getFieldsByDeviceTypeId(device.getDeviceTypeId());

        // Map<String, String> parameterMap 包装为 Map<String, DeviceExtendValues> extendValues
        List<DeviceExtendValues> extendValues = new ArrayList<>();
        for (DeviceExtendField deviceExtendField : deviceExtendFields) {
            String deviceExtendFieldId = deviceExtendField.getId();
            String value = parameterMap.get(deviceExtendFieldId);
//            if (value == null)
//                throw new CustomException(new Json(JsonMessage.PARAM_INVALID, deviceExtendField.getName() + " 不能为空"));

            DeviceExtendValues deviceExtendValues = new DeviceExtendValues(device, deviceExtendFieldId, value);
            extendValues.add(deviceExtendValues);
        }

        deviceExtendValuesBaseService.executeHql("delete from DeviceExtendValues where deviceId = :deviceId", Map.of("deviceId", device.getId()));
        deviceExtendValuesBaseService.bulkInsert(extendValues);
    }

    @Override
    public List<Device> getExtendValues(List<Device> devices) throws Exception {
        // key: 设备id, value：设备
        Map<String, Device> devicesMap = devices.stream().collect(Collectors.toMap(Device::getId, x -> x));

        List<String> deviceIds = devices.stream().map(Device::getId).collect(Collectors.toList());
        List<DeviceExtendValues> deviceExtendValues = deviceExtendValuesBaseService.find("from DeviceExtendValues where deviceId in :deviceIds",
                Map.of(
                "deviceIds", deviceIds));

        for (DeviceExtendValues deviceExtendValue : deviceExtendValues) {
            String deviceId = deviceExtendValue.getDeviceId();
            Device device = devicesMap.get(deviceId);

            List<DeviceExtendValues> extendValues = device.getExtendValues();
            if (extendValues == null)
                extendValues = new ArrayList<>();

            extendValues.add(deviceExtendValue);
            device.setExtendValues(extendValues);
        }
        return devices;
    }

    @Override
    public Map<String, Map<String, String>> getExtendValueMap(List<String> deviceIds, String deviceTypeId) throws Exception {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT\n");
        sqlBuilder.append("	extendValues.device_id as deviceId,\n");
        sqlBuilder.append("	extendValues.field_value as fieldValue,\n");
        sqlBuilder.append("	extendFields.name as fieldName\n");
        sqlBuilder.append("FROM\n");
        sqlBuilder.append("	device_extend_values extendValues\n");
        sqlBuilder.append("	LEFT JOIN device_extend_field extendFields ON extendFields.id = extendValues.device_field_id\n");
        sqlBuilder.append("WHERE extendValues.device_id in :deviceIds");
        sqlBuilder.append("AND extendValues.device_type_id = :deviceTypeId");

        Map<String, Object> params = Map.of("deviceIds", deviceIds, "deviceTypeId", deviceTypeId);

        List<DeviceExtendValues> deviceExtendValues = deviceExtendValuesBaseService.findSQL(
                sqlBuilder.toString(), params, DeviceExtendValues.class, 1, 9999999);

        // key:deviceId, value:{key:扩展字段名(轨道电路制式。。), value:值}
        Map<String, Map<String, String>> result = new HashMap<>();
        for (DeviceExtendValues deviceExtendValue : deviceExtendValues) {
            String deviceId = deviceExtendValue.getDeviceId();

            // key:扩展字段名(轨道电路制式。。), value:值
            Map<String, String> fieldValueMap = Optional.ofNullable(result.get(deviceId)).orElse(new HashMap<>());
            result.put(deviceId, fieldValueMap);

            String fieldName = deviceExtendValue.getFieldName();
            String fieldValue = deviceExtendValue.getFieldValue();
            fieldValueMap.put(fieldName, fieldValue);
        }

        return result;
    }

    @Override
    public void saveExtendValues(List<Device> devices) throws Exception {
        List<DeviceExtendValues> deviceExtendValues = new ArrayList<>();
        devices.forEach(x -> deviceExtendValues.addAll(x.getExtendValues()));

        deviceExtendValuesBaseService.executeHql("delete from DeviceExtendValues where deviceId in :deviceIds", Map.of("deviceIds",
                devices.stream().map(Device::getId).collect(Collectors.toList())));
        deviceExtendValuesBaseService.bulkInsert(deviceExtendValues);
    }

}

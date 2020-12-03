package com.yingda.lkj.service.backstage.device;

import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.backstage.device.DeviceExtendField;
import com.yingda.lkj.beans.exception.CustomException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hood  2019/12/31
 */
public interface DeviceExtendFieldService {
    void saveDeviceExtendField(List<DeviceExtendField> deviceExtendFields);

    List<DeviceExtendField> getFieldsByDeviceId(String deviceId);
    List<DeviceExtendField> getFieldsByDeviceTypeId(String deviceTypeId);
    List<DeviceExtendField> getFieldsByNames(List<String> names) throws CustomException;

    /*
        电流,1; 电压,0; 功率,1
            ==>
        DeviceExtendFields
     */
    default List<DeviceExtendField> parse(String parse, String deviceTypeId) {
        int seq = 0;

        List<DeviceExtendField> deviceExtendFields = new ArrayList<>();

        for (String name : parse.split(",")) {
            seq++;
            byte appendOnForm = DeviceExtendField.NOT_APPEND_ON_FORM;

            deviceExtendFields.add(new DeviceExtendField(name, deviceTypeId, appendOnForm, seq));
        }

        return deviceExtendFields;
    }

    default String format(List<DeviceExtendField> deviceExtendFields) {
        StringBuilder stringBuilder = new StringBuilder();

        for (DeviceExtendField deviceExtendField : deviceExtendFields)
            stringBuilder.append(deviceExtendField.getName() + ",");

        return stringBuilder.toString();
    }

}

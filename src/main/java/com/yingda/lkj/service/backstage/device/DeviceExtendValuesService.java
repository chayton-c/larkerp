package com.yingda.lkj.service.backstage.device;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.device.DeviceExtendField;
import com.yingda.lkj.beans.entity.backstage.device.DeviceExtendValues;

import java.util.List;
import java.util.Map;

/**
 * @author hood  2019/12/31
 */
public interface DeviceExtendValuesService {
    /**
     * 获取一个设备的扩展字段值
     * @return key:对应扩展字段id(DeviceExtendField.id), value: 对应的字段的值
     */
    Map<String, DeviceExtendValues> getExtendValueMap(Device device) throws Exception;

    /**
     * 保存扩展字段数据
     * @param device 对应设备
     * @param parameterMap 扩展字段Map key:对应扩展字段id(DeviceExtendField.id), value: 对应的字段的值
     */
    void saveExtendValues(Device device, Map<String, String> parameterMap) throws Exception;

    /**
     * 保存多个扩展字段数据
     * @param devices 设备list(扩展字段保存在 device.getExtendValus中)
     */
    void saveExtendValues(List<Device> devices) throws Exception;

    /**
     * 获取多个设备的扩展字段值
     * @param devices 需要获取扩展值的设备
     * @return List<Device> param中的devices.extendValues中包含了他们的扩展字段值
     */
    List<Device> getExtendValues(List<Device> devices) throws Exception;

    /**
     * 获取多个设备的扩展字段值
     * @param deviceIds 需要获取扩展值的设备id
     * @param deviceTypeId 这些设备的设备类型
     * @return key:deviceId, value:{key:扩展字段名(轨道电路制式。。), value:值}
     */
    Map<String, Map<String, String>> getExtendValueMap(List<String> deviceIds, String deviceTypeId) throws Exception;

}

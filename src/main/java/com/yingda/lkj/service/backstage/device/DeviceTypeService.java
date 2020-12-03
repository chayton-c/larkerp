package com.yingda.lkj.service.backstage.device;

import com.yingda.lkj.beans.entity.backstage.device.DeviceExtendField;
import com.yingda.lkj.beans.entity.backstage.device.DeviceMeasurementItem;
import com.yingda.lkj.beans.entity.backstage.device.DeviceType;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.device.devicetype.DeviceTypeNodePojo;
import com.yingda.lkj.beans.pojo.device.devicetype.DeviceTypeSelectTreeNode;

import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/1/2
 */
public interface DeviceTypeService {
    void saveOrUpdate(DeviceType deviceType);

    List<DeviceType> getAllDeviceTypes();

    DeviceType getDeviceType(String deviceTypeId);

    DeviceType getByDeviceSubTypeId(String deviceSubTypeId);

    Map<String, DeviceType> getByNames(List<String> names);

    List<DeviceTypeSelectTreeNode> initDeviceTypeSelectTree();

    void delete(List<String> deviceTypeIds) throws CustomException;
}

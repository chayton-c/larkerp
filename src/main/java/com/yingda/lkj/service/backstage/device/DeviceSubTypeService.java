package com.yingda.lkj.service.backstage.device;

import com.yingda.lkj.beans.entity.backstage.device.DeviceSubType;
import com.yingda.lkj.beans.entity.backstage.device.DeviceType;
import com.yingda.lkj.beans.exception.CustomException;

import java.util.List;
import java.util.Map;

/**
 * 设备子类型
 *
 * @author hood  2020/3/12
 */
public interface DeviceSubTypeService {

    List<DeviceSubType> getByDeviceTypeId(String deviceTypeId);

    void saveOrUpdate(List<String> subTypeNames, String deviceTypeId);

    DeviceSubType getById(String id);

    Map<String, DeviceSubType> getByNames(List<String> names);

    void delete(List<String> ids) throws CustomException;
}

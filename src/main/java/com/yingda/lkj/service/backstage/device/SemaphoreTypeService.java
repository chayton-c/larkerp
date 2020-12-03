package com.yingda.lkj.service.backstage.device;

import com.yingda.lkj.beans.entity.backstage.device.DeviceExtendField;

import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/1/7
 */
public interface SemaphoreTypeService {
    Map<String, DeviceExtendField> getSemaphoreTypes();
    Map<String, String> getSemaphoreTypeNames();
}

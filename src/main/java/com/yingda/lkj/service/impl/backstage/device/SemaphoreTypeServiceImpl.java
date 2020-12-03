package com.yingda.lkj.service.impl.backstage.device;

import com.yingda.lkj.annotation.CacheMethod;
import com.yingda.lkj.beans.entity.backstage.device.DeviceExtendField;
import com.yingda.lkj.beans.entity.backstage.device.DeviceType;
import com.yingda.lkj.beans.system.cache.CacheMap;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.device.SemaphoreTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hood  2020/1/7
 */
@Service("semaphoreTypeService")
@CacheMethod
public class SemaphoreTypeServiceImpl implements SemaphoreTypeService {

    @Autowired
    private BaseDao<DeviceExtendField> deviceExtendFieldBaseDao;

    private static final Map<String, DeviceExtendField> DEVICE_EXTEND_FIELDS_MAP = new CacheMap<>();

    @Override
    public Map<String, DeviceExtendField> getSemaphoreTypes() {
        init();
        return new HashMap<>(DEVICE_EXTEND_FIELDS_MAP);
    }

    @Override
    public Map<String, String> getSemaphoreTypeNames() {
        init();
        return DEVICE_EXTEND_FIELDS_MAP.values().stream().collect(Collectors.toMap(DeviceExtendField::getId, DeviceExtendField::getName));
    }

    private void init() {
        if (!DEVICE_EXTEND_FIELDS_MAP.isEmpty())
            return;

        List<DeviceExtendField> deviceExtendFields = deviceExtendFieldBaseDao.find("from DeviceExtendField where deviceTypeId = :deviceTypeId", Map.of(
                "deviceTypeId", DeviceType.SEMAPHORE_ID));

        DEVICE_EXTEND_FIELDS_MAP.putAll(deviceExtendFields.stream().collect(Collectors.toMap(DeviceExtendField::getId, x -> x)));
    }


}

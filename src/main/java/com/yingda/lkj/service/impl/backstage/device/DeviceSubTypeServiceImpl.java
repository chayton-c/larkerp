package com.yingda.lkj.service.impl.backstage.device;

import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.device.DeviceSubType;
import com.yingda.lkj.beans.entity.backstage.device.DeviceType;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTemplate;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.device.DeviceService;
import com.yingda.lkj.service.backstage.device.DeviceSubTypeService;
import com.yingda.lkj.service.backstage.device.DeviceTypeService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTemplateService;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hood  2020/3/16
 */
@Service("deviceSubTypeService")
public class DeviceSubTypeServiceImpl implements DeviceSubTypeService {

    @Autowired
    private DeviceTypeService deviceTypeService;
    @Autowired
    private BaseDao<DeviceSubType> deviceSubTypeBaseDao;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private MeasurementTemplateService measurementTemplateService;

    @Override
    public List<DeviceSubType> getByDeviceTypeId(String deviceTypeId) {
        return deviceSubTypeBaseDao.find("from DeviceSubType where deviceTypeId = :deviceTypeId", Map.of("deviceTypeId", deviceTypeId));
    }

    @Override
    public void saveOrUpdate(List<String> subTypeNames, String deviceTypeId) {
        List<DeviceSubType> original = deviceSubTypeBaseDao.find("from DeviceSubType where deviceTypeId = :deviceTypeId", Map.of(
                "deviceTypeId", deviceTypeId));
        List<String> originalNames = StreamUtil.getList(original, DeviceSubType::getName);

        // 如果没有输入设备类型，填写一个默认的
        if (subTypeNames.stream().noneMatch(StringUtils::isNotEmpty)
                && originalNames.stream().noneMatch(StringUtils::isNotEmpty)) {
            DeviceType deviceType = deviceTypeService.getDeviceType(deviceTypeId);
            subTypeNames = List.of(String.format("%s默认子类型", deviceType.getName()));
        }

        List<String> addTypeNames = subTypeNames.stream().filter(x -> !originalNames.contains(x)).collect(Collectors.toList());

        List<DeviceSubType> deviceSubTypes = addTypeNames.stream().map(x -> new DeviceSubType(deviceTypeId, x)).collect(Collectors.toList());
        deviceSubTypes.forEach(x -> deviceSubTypeBaseDao.saveOrUpdate(x));

//        for (DeviceSubType deviceSubType : original)
//            if (!subTypeNames.contains(deviceSubType.getName()))
//                hide(deviceSubType);
    }

    @Override
    public DeviceSubType getById(String id) {
        return deviceSubTypeBaseDao.get(DeviceSubType.class, id);
    }

    @Override
    public Map<String, DeviceSubType> getByNames(List<String> names) {
        names = names.stream().distinct().collect(Collectors.toList());
        List<DeviceSubType> deviceTypes = deviceSubTypeBaseDao.find(
                "from DeviceSubType where name in :names",
                Map.of("names", names)
        );

        return StreamUtil.getMap(deviceTypes, DeviceSubType::getName, x -> x);
    }

    @Override
    public void delete(List<String> ids) throws CustomException {
        for (String id : ids) {
            List<Device> devices = deviceService.getByDeviceSubType(id);
            if (!devices.isEmpty())
                throw new CustomException(JsonMessage.CONTAINING_ASSOCIATED_DATA, "指定设备类型下仍包含未删除的设备，请在修改后再进行删除");

            List<MeasurementTemplate> measurementTemplates = measurementTemplateService.getByDeviceSubTypeId(id);
            if (!measurementTemplates.isEmpty())
                throw new CustomException(JsonMessage.CONTAINING_ASSOCIATED_DATA, "指定设备类型下仍包含未删除的巡检标准，请在修改后再进行删除");
        }

        deviceSubTypeBaseDao.executeHql(
                "delete from DeviceSubType where id in :ids",
                Map.of("ids", ids)
        );
    }

    /**
     * 假删
     */
    private void hide(DeviceSubType deviceSubType) {
        deviceSubType.setHide(Constant.HIDE);
        deviceSubTypeBaseDao.saveOrUpdate(deviceSubType);
    }
}

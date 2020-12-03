package com.yingda.lkj.service.impl.backstage.device;

import com.yingda.lkj.beans.entity.backstage.device.*;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTemplate;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.device.devicetype.DeviceTypeSelectTreeNode;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.device.DeviceExtendFieldService;
import com.yingda.lkj.service.backstage.device.DeviceService;
import com.yingda.lkj.service.backstage.device.DeviceSubTypeService;
import com.yingda.lkj.service.backstage.device.DeviceTypeService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTemplateService;
import com.yingda.lkj.utils.StreamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hood  2020/1/2
 */
@Service("deviceTypeService")
public class DeviceTypeServiceImpl implements DeviceTypeService {

    @Autowired
    private DeviceExtendFieldService deviceExtendFieldService;
    @Autowired
    private DeviceSubTypeService deviceSubTypeService;
    @Autowired
    private BaseDao<DeviceSubType> deviceSubTypeBaseDao;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private MeasurementTemplateService measurementTemplateService;

    @Autowired
    private BaseDao<DeviceType> deviceTypeBaseDao;


    @Override
    public void saveOrUpdate(DeviceType deviceType) {
        deviceTypeBaseDao.saveOrUpdate(deviceType);
    }

    @Override
    public List<DeviceType> getAllDeviceTypes() {
        return deviceTypeBaseDao.find("from DeviceType");
    }

    @Override
    public DeviceType getDeviceType(String deviceTypeId) {
        return deviceTypeBaseDao.get(DeviceType.class, deviceTypeId);
    }

    @Override
    public DeviceType getByDeviceSubTypeId(String deviceSubTypeId) {
        DeviceSubType deviceSubType = deviceSubTypeService.getById(deviceSubTypeId);
        return getDeviceType(deviceSubType.getDeviceTypeId());
    }

    @Override
    public Map<String, DeviceType> getByNames(List<String> names) {
        names = names.stream().distinct().collect(Collectors.toList());
        List<DeviceType> deviceTypes = deviceTypeBaseDao.find(
                "from DeviceType where name in :names",
                Map.of("names", names)
        );

        return StreamUtil.getMap(deviceTypes, DeviceType::getName, x -> x);
    }

    @Override
    public List<DeviceTypeSelectTreeNode> initDeviceTypeSelectTree() {
        List<DeviceTypeSelectTreeNode> result = new ArrayList<>();

        String sql = """
                SELECT
                	deviceSubType.id AS id,
                	deviceSubType.name AS name,
                	deviceType.id AS deviceTypeId,
                	deviceType.name AS deviceTypeName
                FROM
                	device_sub_type AS deviceSubType
                	INNER JOIN device_type AS deviceType ON deviceType.id = deviceSubType.device_type_id
                """;
        List<DeviceSubType> deviceSubTypes = deviceSubTypeBaseDao.findSQL(sql, null, DeviceSubType.class);
        Map<String, List<DeviceSubType>> deviceSubTypeMap = deviceSubTypes.stream().collect(Collectors.groupingBy(DeviceSubType::getDeviceTypeId));

        for (List<DeviceSubType> deviceSubTypeList : deviceSubTypeMap.values()) {
            String deviceTypeId = deviceSubTypeList.get(0).getDeviceTypeId();
            String deviceTypeName = deviceSubTypeList.get(0).getDeviceTypeName();

            result.add(new DeviceTypeSelectTreeNode(deviceTypeId, deviceTypeName, deviceSubTypeList));
        }

        return result;
    }

    @Override
    public void delete(List<String> deviceTypeIds) throws CustomException {
        for (String deviceTypeId : deviceTypeIds) {
            List<DeviceSubType> deviceSubTypes = deviceSubTypeService.getByDeviceTypeId(deviceTypeId);
            if (!deviceSubTypes.isEmpty())
                throw new CustomException(JsonMessage.CONTAINING_ASSOCIATED_DATA, "指定设备类型下仍包含未删除的子类型，请在修改后再进行删除");

            List<Device> devices = deviceService.getByDeviceType(deviceTypeId);
            if (!devices.isEmpty())
                throw new CustomException(JsonMessage.CONTAINING_ASSOCIATED_DATA, "指定设备类型下仍包含设备，请在修改后再进行删除");

            List<MeasurementTemplate> measurementTemplates = measurementTemplateService.getByDeviceTypeId(deviceTypeId);
            if (!measurementTemplates.isEmpty())
                throw new CustomException(JsonMessage.CONTAINING_ASSOCIATED_DATA, "指定设备类型下仍包含巡检标准，请在修改后再进行删除");
        }


        deviceTypeBaseDao.executeHql(
                "delete from DeviceType where id in :ids",
                Map.of("ids", deviceTypeIds)
        );
    }

}

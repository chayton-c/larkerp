package com.yingda.lkj.service.impl.backstage.measurement;

import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.device.DeviceSubType;
import com.yingda.lkj.beans.entity.backstage.device.DeviceType;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItem;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItemField;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTemplate;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementUnit;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.device.DeviceSubTypeService;
import com.yingda.lkj.service.backstage.device.DeviceTypeService;
import com.yingda.lkj.service.backstage.measurement.MeasurementItemFieldService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTemplateService;
import com.yingda.lkj.service.backstage.measurement.MeasurementUnitService;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hood  2020/3/18
 */
@Service("measurementTemplateService")
public class MeasurementTemplateServiceImpl implements MeasurementTemplateService {

    @Autowired
    private BaseDao<MeasurementTemplate> measurementTemplateBaseDao; // 模板(信号机模板)
    @Autowired
    private BaseDao<MeasurementItem> measurementItemBaseDao; // 模板下的测量项(信号机u灯，l灯)
    @Autowired
    private BaseDao<MeasurementItemField> measurementItemFieldBaseDao; // 模板测量项的字段(电压，电流etc)
    @Autowired
    private MeasurementUnitService measurementUnitService;
    @Autowired
    private BaseDao<Device> deviceBaseDao;
    @Autowired
    private DeviceSubTypeService deviceSubTypeService;
    @Autowired
    private MeasurementItemFieldService measurementItemFieldService;

    @Override
    public MeasurementTemplate getById(String id) {
        return measurementTemplateBaseDao.get(MeasurementTemplate.class, id);
    }

    @Override
    public void delete(String id) {
        measurementTemplateBaseDao.executeHql(
                "delete from MeasurementTemplate where id = :id",
                Map.of("id", id)
        );
        measurementItemFieldService.deleteByMeasurementTemplateId(id);
    }

    @Override
    public List<MeasurementItem> getItemsAndItemFieldsByTemplateId(String measurementTemplateId) {
        List<MeasurementItem> measurementItems = measurementItemBaseDao.find(
                "from MeasurementItem where measurementTemplateId = :measurementTemplateId and hide = :hide",
                Map.of("measurementTemplateId", measurementTemplateId, "hide", Constant.SHOW)
        );

        List<String> measurementItemIds = StreamUtil.getList(measurementItems, MeasurementItem::getId);

        List<MeasurementItemField> measurementItemFields = measurementItemFieldBaseDao.find(
                "from MeasurementItemField where measurementItemId in :measurementItemIdsand hide = :hide",
                Map.of("measurementItemIds", measurementItemIds, "hide", Constant.SHOW)
        );
        Map<String, List<MeasurementItemField>> measurementItemFieldMap =
                measurementItemFields.stream().collect(Collectors.groupingBy(MeasurementItemField::getMeasurementItemId));

        List<MeasurementItem> returnList = new ArrayList<>();
        for (MeasurementItem measurementItem : measurementItems) {
            String measurementItemId = measurementItem.getId();
            String measurementItemName = measurementItem.getName();
            List<MeasurementItemField> measurementItemFieldList = measurementItemFieldMap.get(measurementItemId);

            if (measurementItemFieldList == null)
                continue;

            // 填写fullName给前端使用
            measurementItemFieldList.forEach(x -> x.setFullName(String.format("%s——%s", measurementItemName, x.getName())));

            measurementItem.setMeasurementItemFields(measurementItemFieldList);
            measurementItem.setMeasurementItemFieldCount(measurementItemFieldList.size());
            returnList.add(measurementItem);
        }

        return returnList;
    }

    @Override
    public List<MeasurementTemplate> getTemplatesByMeasurementItems(List<MeasurementItem> measurementItems) {

        List<String> measurementTemplateIds = StreamUtil.getList(measurementItems, MeasurementItem::getMeasurementTemplateId);

        return measurementTemplateBaseDao.find(
                "from MeasurementTemplate where id in :measurementItems",
                Map.of("measurementItems", measurementTemplateIds)
        );
    }

    @Override
    public List<MeasurementTemplate> getVisiableTemplates() {
        return measurementTemplateBaseDao.find(
                "from MeasurementTemplate where hide = :hide",
                Map.of("hide", Constant.SHOW)
        );
    }

    @Override
    public List<MeasurementTemplate> getVisiableTemplates(String deviceId) throws CustomException {
        Device device = deviceBaseDao.get(Device.class, deviceId);
        String deviceSubTypeId = device.getDeviceSubTypeId();
        if (StringUtils.isEmpty(deviceSubTypeId))
            throw new CustomException(JsonMessage.DATA_NO_COMPLETE, "该设备尚未配置子类型");

        List<DeviceSubType> deviceSubTypes = List.of(deviceSubTypeService.getById(deviceSubTypeId));
        List<String> deviceSubTypeIds = StreamUtil.getList(deviceSubTypes, DeviceSubType::getId);

        return measurementTemplateBaseDao.find(
                "from MeasurementTemplate where hide = :hide and deviceSubTypeId in :deviceSubTypeIds",
                Map.of("hide", Constant.SHOW, "deviceSubTypeIds", deviceSubTypeIds)
        );
    }

    @Override
    public List<MeasurementTemplate> getByDeviceTypeId(String deviceTypeId) {
        return measurementTemplateBaseDao.find(
                "from MeasurementTemplate where deviceTypeId = :deviceTypeId",
                Map.of("deviceTypeId", deviceTypeId)
        );
    }

    @Override
    public List<MeasurementTemplate> getByDeviceSubTypeId(String deviceSubTypeId) {
        return measurementTemplateBaseDao.find(
                "from MeasurementTemplate where deviceSubTypeId = :deviceSubTypeId",
                Map.of("deviceSubTypeId", deviceSubTypeId)
        );
    }
}

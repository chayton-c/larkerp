package com.yingda.lkj.service.impl.backstage.measurement;

import com.yingda.lkj.beans.entity.backstage.measurement.*;
import com.yingda.lkj.beans.entity.system.UploadImage;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.measurement.DeviceMaintenanceParameterService;
import com.yingda.lkj.service.backstage.measurement.MeasurementItemFieldValueService;
import com.yingda.lkj.service.backstage.measurement.MeasurementUnitService;
import com.yingda.lkj.service.system.UploadImageService;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.file.UploadUtil;
import com.yingda.lkj.utils.math.NumberUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hood  2020/3/19
 */
@Service("measurementItemFieldValueService")
public class MeasurementItemFieldValueServiceImpl implements MeasurementItemFieldValueService {

    @Autowired
    private BaseDao<MeasurementItemFieldValue> measurementItemFieldValueBaseDao;
    @Autowired
    private BaseDao<MeasurementItemField> measurementItemFieldBaseDao;
    @Autowired
    private BaseDao<MeasurementTaskDetail> measurementTaskDetailBaseDao;
    @Autowired
    private DeviceMaintenanceParameterService deviceMaintenanceParameterService;
    @Autowired
    private MeasurementUnitService measurementUnitService;
    @Autowired
    private UploadImageService uploadImageService;

    @Override
    public MeasurementItemFieldValue saveOrUpdateFieldValue(String measurementItemFieldId, String measurementTaskDetailId, String value) {
        // 测量字段
        MeasurementItemField measurementItemField = measurementItemFieldBaseDao.get(MeasurementItemField.class, measurementItemFieldId);
        // 测量子任务
        MeasurementTaskDetail measurementTaskDetail = measurementTaskDetailBaseDao.get(MeasurementTaskDetail.class,
                measurementTaskDetailId);
        // 测量字段使用的测量单位
        MeasurementUnit measurementUnit = measurementUnitService.getById(measurementItemField.getMeasurementUnitId());
        // 根据测量单位的数据类型，也是测量值的数据类型
        byte valueType = measurementUnit.getValueType();
        // 如果是图片类型的数据，需要获取app上传的文件名(UUID)作为value
        if (MeasurementUnit.IMAGE == valueType)
            value = UploadUtil.getAppUploadImageFileName(value);

        MeasurementItemFieldValue measurementItemFieldValue = measurementItemFieldValueBaseDao.get(
                "from MeasurementItemFieldValue where measurementTaskDetailId = :measurementTaskDetailId and measurementItemFieldId = " +
                        ":measurementItemFieldId ",
                Map.of("measurementItemFieldId", measurementItemFieldId, "measurementTaskDetailId", measurementTaskDetailId)
        );

        if (measurementItemFieldValue == null)
            measurementItemFieldValue = new MeasurementItemFieldValue(measurementItemField, measurementTaskDetail, value, measurementUnit);

        measurementItemFieldValue.setValue(value);

        // 判断数据是否异常
        boolean abnormal = true;

        // 根据测量字段对应的单位的数据类型(measurementUnit.getValueType())判断数据是否异常
        if (MeasurementUnit.NUMBER == valueType) {
            Double currentMaxValue = measurementItemFieldValue.getCurrentMaxValue();
            Double currentMinValue = measurementItemFieldValue.getCurrentMinValue();

            double valueDouble = 0.0;
            boolean valueIsDouble = NumberUtil.isDouble(value);
            abnormal = false;
            if (valueIsDouble)
                valueDouble = Double.parseDouble(value);
            if (valueIsDouble && currentMaxValue != null && (valueDouble > currentMaxValue))
                abnormal = true;
            if (valueIsDouble && currentMinValue != null && (valueDouble < currentMinValue))
                abnormal = true;
        }
        if (MeasurementUnit.STRING == valueType) {
            String correctValue = measurementItemField.getCorrectValue();
            abnormal = StringUtils.isEmpty(correctValue) || correctValue.equals(value);
        }

        measurementItemFieldValue.setAbnormal(abnormal ? MeasurementItemFieldValue.ABNORMAL : MeasurementItemFieldValue.NORMAL);

        measurementItemFieldValueBaseDao.saveOrUpdate(measurementItemFieldValue);

        // 如果字段的值为异常，那么对应的任务也为异常
        if (abnormal)
            measurementTaskDetail.setAbnormal(MeasurementItemFieldValue.ABNORMAL);

        // 生成设备维护参数
        deviceMaintenanceParameterService.saveOrUpdateDeviceMaintenanceParameter(measurementItemFieldValue);

        return measurementItemFieldValue;
    }

    @Override
    public Map<String, List<MeasurementItemFieldValue>> getMeasurementItemFieldValues(String templateId,
                                                                                      List<String> measurementTaskDetailIds) {
        List<MeasurementItemFieldValue> measurementItemFieldValues = measurementItemFieldValueBaseDao.find(
                "from MeasurementItemFieldValue where measurementTaskDetailId in " +
                        ":measurementTaskDetailIds",
                Map.of("measurementTaskDetailIds", measurementTaskDetailIds)
        );
        if (StringUtils.isNotEmpty(templateId)) {
            measurementItemFieldValues = measurementItemFieldValues
                    .stream()
                    .filter(x -> templateId.equals(x.getMeasurementTemplateId())).collect(Collectors.toList());
        }

        List<String> uploadImageIds = measurementItemFieldValues.stream()
                .filter(x -> MeasurementUnit.IMAGE == x.getValueType())
                .map(MeasurementItemFieldValue::getValue)
                .collect(Collectors.toList());

        Map<String, UploadImage> uploadImageMap = uploadImageService.getByIds(uploadImageIds);
        for (MeasurementItemFieldValue measurementItemFieldValue : measurementItemFieldValues) {
            String uploadImageId = measurementItemFieldValue.getValue();
            UploadImage uploadImage = uploadImageMap.get(uploadImageId);
            if (MeasurementUnit.IMAGE != measurementItemFieldValue.getValueType())
                continue;
            if (uploadImage != null)
                measurementItemFieldValue.setUrl(uploadImage.getUrl());
        }

        return measurementItemFieldValues.stream().collect(Collectors.groupingBy(MeasurementItemFieldValue::getMeasurementTaskDetailId));
    }

    @Override
    public Map<String, List<MeasurementItemFieldValue>> getMeasurementItemFieldValues(List<String> measurementTaskDetailIds) {
        return getMeasurementItemFieldValues(null, measurementTaskDetailIds);
    }

    @Override
    public List<MeasurementItemFieldValue> getByMeasurementTaskDetailId(String measurementTaskDetailId) {
        List<MeasurementItemFieldValue> measurementItemFieldValues = measurementItemFieldValueBaseDao.find(
                "from MeasurementItemFieldValue where measurementTaskDetailId = :measurementTaskDetailId",
                Map.of("measurementTaskDetailId", measurementTaskDetailId)
        );
        for (MeasurementItemFieldValue measurementItemFieldValue : measurementItemFieldValues) {
            if (MeasurementItemFieldValue.IMAGE == measurementItemFieldValue.getValueType()) {
                UploadImage uploadImage = uploadImageService.getById(measurementItemFieldValue.getValue());
                if (uploadImage != null)
                    measurementItemFieldValue.setUrl(uploadImage.getUrl());
            }
        }
        return measurementItemFieldValues;
    }
}

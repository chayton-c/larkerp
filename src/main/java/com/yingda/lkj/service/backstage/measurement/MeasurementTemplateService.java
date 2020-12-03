package com.yingda.lkj.service.backstage.measurement;

import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItem;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItemField;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTemplate;
import com.yingda.lkj.beans.exception.CustomException;

import java.util.List;

/**
 * 测量模板接口
 * @author hood  2020/3/18
 */
public interface MeasurementTemplateService {

    MeasurementTemplate getById(String id);

    void delete(String id);

    List<MeasurementItem> getItemsAndItemFieldsByTemplateId(String measurementTemplateId);

    List<MeasurementTemplate> getTemplatesByMeasurementItems(List<MeasurementItem> measurementItems);

    /**
     * 获取可见的模板
     */
    List<MeasurementTemplate> getVisiableTemplates();

    /**
     * 获取设备可用的模板
     */
    List<MeasurementTemplate> getVisiableTemplates(String deviceId) throws CustomException;

    List<MeasurementTemplate> getByDeviceTypeId(String deviceTypeId);

    List<MeasurementTemplate> getByDeviceSubTypeId(String deviceSubTypeId);
}

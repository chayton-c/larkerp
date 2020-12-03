package com.yingda.lkj.service.backstage.measurement;

import com.yingda.lkj.beans.entity.backstage.device.DeviceType;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItem;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItemField;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTaskDetail;
import com.yingda.lkj.beans.exception.CustomException;

import java.util.List;
import java.util.Map;

/**
 * 测量子任务接口
 */
public interface MeasurementItemFieldService {
    DeviceType getDeviceType(MeasurementItemField measurementItemField);

    void saveOrUpdate(MeasurementItemField pageMeasurementItemField);

    /**
     * 新增或保存模板测量字段：如果对应模板下有多个测量项，那么这个模板下的所有测量项都要对应的新增/修改
     */
    void saveOrUpdateWithMeasurementItem(MeasurementItemField pageMeasurementItemField, String measurementItemId);

    void deleteByMeasurementTemplateId(String measurementTemplateId);

    /**
     * 删除模板测量字段：如果对应模板下有多个测量项，那么这个模板下的所有测量项都要对应的删除
     */
    void delete(String id);

    /**
     * 关联模板测量字段：如果对应模板下有多个测量项，那么这个模板下的所有测量项都要对应的关联
     */
    void groupFields(List<String> ids) throws CustomException;

    /**
     * 获取对应测量模板下的所有字段
     */
    List<MeasurementItemField> getFieldsByTemplateId(String measurementTemplateId);

    /**
     * 获取对应测量模板下的所有字段
     * @return key:模板id，value:模板下的字段
     */
    Map<String, List<MeasurementItemField>> getFieldsByTemplateIds(List<String> measurementTemplateIds);

    /**
     * 查询子任务下包含的字段
     */
    List<MeasurementItemField> getFieldsByDetailId(String measurementTaskDetailId);

    /**
     * 获取测量子模板下的所有测量项
     * @return key:子模板id, value:子模板下的所有未隐藏字段
     */
    Map<String, List<MeasurementItemField>> getFieldsByMeasurmenetItems(List<MeasurementItem> measurementItems) throws Exception;

    List<MeasurementItemField> getFieldsByMeasurementItem(String measurementItemId);

    MeasurementItemField getById(String id);
}

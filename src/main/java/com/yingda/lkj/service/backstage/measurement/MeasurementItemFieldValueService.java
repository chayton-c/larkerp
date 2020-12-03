package com.yingda.lkj.service.backstage.measurement;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItemField;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItemFieldValue;

import java.util.*;

/**
 * @author hood  2020/3/19
 */
public interface MeasurementItemFieldValueService {

    /**
     * 添加/修改测量值
     */
    MeasurementItemFieldValue saveOrUpdateFieldValue(String measurementItemFieldId, String measurementTaskDetailId, String value);

    /**
     * 获取测量子任务的测量值
     * @param templateId 使用的模板
     * @param measurementTaskDetailIds 子任务ids
     * @return key:测量子任务id，value:子任务id对应的测量值
     */
    Map<String, List<MeasurementItemFieldValue>> getMeasurementItemFieldValues(String templateId, List<String> measurementTaskDetailIds);
    /**
     * 获取测量子任务的测量值
     * @param measurementTaskDetailIds 子任务ids
     * @return key:测量子任务id，value:子任务id对应的测量值
     */
    Map<String, List<MeasurementItemFieldValue>> getMeasurementItemFieldValues(List<String> measurementTaskDetailIds);

    List<MeasurementItemFieldValue> getByMeasurementTaskDetailId(String measurementTaskDetailId);

}

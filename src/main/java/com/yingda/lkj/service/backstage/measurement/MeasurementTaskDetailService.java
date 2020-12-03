package com.yingda.lkj.service.backstage.measurement;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTask;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTaskDetail;
import com.yingda.lkj.utils.StringUtils;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/4/13
 */
public interface MeasurementTaskDetailService {

    MeasurementTaskDetail getById(String id);

    /**
     * 获取执行人名
     * @return key:measuermeentTask.id(任务id) value:执行人姓名
     */
    Map<String, String> getExecuteUserNames(List<MeasurementTaskDetail> measurementTaskDetails);

    /**
     * 获取执行人名
     * @return key:measuermeentTask.id(任务id) value:执行人姓名
     */
    Map<String, String> getExecuteUserNamesByTaskIds(List<String> measurementTaskIds);

    /**
     * 获取执行人id
     * @return key:measuermeentTask.id(任务id) value:执行人id
     */
    Map<String, String> getExecuteUserIds(List<MeasurementTaskDetail> measurementTaskDetails);

    /**
     * 获取子任务关联的上级任务
     * @return key:measurementTaskDetail.id(子任务id) value:对应的主任务
     */
    Map<String, MeasurementTask> getMeasurementTaskMap(List<MeasurementTaskDetail> measurementTaskDetails);

    /**
     * 查询devices是否包含需要测量的任务
     * @return key:deviceId value:false表示没有需要测量的任务
     */
    Map<String, Boolean> hasTask(List<Device> devices);

    /**
     * 获取筛选后的异常任务  key: 异常任务 value:对应异常子任务
     * @param pageMeasurementTaskDetail 页面传来的子任务信息
     * @param measurementTaskIds 异常任务id
     */
    Map<MeasurementTask, List<MeasurementTaskDetail>> getAbnormalTaskAndTaskDetails(MeasurementTaskDetail pageMeasurementTaskDetail, List<String> measurementTaskIds) throws ParseException;

    List<MeasurementTaskDetail> getMeasurementTaskDetailsByDeviceId(String deviceId);

    void executeTask(String measurementTaskDetailId);

    void setMissed(MeasurementTask measurementTask);
}

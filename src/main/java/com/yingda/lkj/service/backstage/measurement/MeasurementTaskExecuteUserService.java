package com.yingda.lkj.service.backstage.measurement;

import java.util.List;

/**
 * @author hood  2020/4/15
 */
public interface MeasurementTaskExecuteUserService {

    /**
     * 查询用户关联的测量任务
     * @param userId 执行人id
     * @return 关联的任务ids(measurementTaskIds)
     */
    List<String> getMeasurementTaskIdsByUserId(String userId);

    String getUserNamesByMeasurementTaskId(String measurementTaskId);
}

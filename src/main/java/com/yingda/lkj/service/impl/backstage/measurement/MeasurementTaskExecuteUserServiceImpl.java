package com.yingda.lkj.service.impl.backstage.measurement;

import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTaskExecuteUser;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.measurement.MeasurementTaskExecuteUserService;
import com.yingda.lkj.service.system.UserService;
import com.yingda.lkj.utils.StreamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hood  2020/4/15
 */
@Service("measurementTaskExecuteUserServiceImpl")
public class MeasurementTaskExecuteUserServiceImpl implements MeasurementTaskExecuteUserService {

    @Autowired
    private BaseDao<MeasurementTaskExecuteUser> measurementTaskExecuteUserBaseDao;
    @Autowired
    private UserService userService;

    @Override
    public List<String> getMeasurementTaskIdsByUserId(String userId) {
        List<MeasurementTaskExecuteUser> measurementTaskExecuteUsers = measurementTaskExecuteUserBaseDao.find(
                "from MeasurementTaskExecuteUser where executeUserId = :executeUserId",
                Map.of("executeUserId", userId)
        );

        return StreamUtil.getList(measurementTaskExecuteUsers, MeasurementTaskExecuteUser::getMeasurementTaskId);
    }

    @Override
    public String getUserNamesByMeasurementTaskId(String measurementTaskId) {
        List<MeasurementTaskExecuteUser> measurementTaskExecuteUsers = measurementTaskExecuteUserBaseDao.find(
                "from MeasurementTaskExecuteUser where measurementTaskId = :measurementTaskId",
                Map.of("measurementTaskId", measurementTaskId)
        );
        List<String> executeUserIds = StreamUtil.getList(measurementTaskExecuteUsers, MeasurementTaskExecuteUser::getExecuteUserId);
        List<User> users = userService.getByIds(executeUserIds);
        return users.stream().map(User::getDisplayName).collect(Collectors.joining(", "));
    }
}

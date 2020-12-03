package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTask;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTaskDetail;
import com.yingda.lkj.beans.pojo.measurement.MeasurementTaskPojo;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.measurement.MeasurementTaskDetailService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.hql.HqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务异常统计
 *
 * @author eggk  2020/4/15
 */
@Controller
@RequestMapping("/backstage/abnormal")
public class AbnormalStatisticsController extends BaseController {
    @Autowired
    private MeasurementTaskDetailService measurementTaskDetailService;
    @Autowired
    private BaseService<MeasurementTask> measurementTaskBaseService;
    @Autowired
    private BaseService<Device> deviceBaseService;
    @Autowired
    private BaseService<BigInteger> bigIntegerBaseService;

    private MeasurementTaskDetail pageMeasurementTaskDetail;


    @RequestMapping("/abnormalStatistics")
    public ModelAndView measurementTaskAbnormalStatisticsController() throws Exception {
        //获取异常任务
        Map<String, Object> params = new HashMap<>();
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(" SELECT ");
        sqlBuilder.append(" id AS id, ");
        sqlBuilder.append(" name AS name, ");
        sqlBuilder.append(" add_time AS addTime, ");
        sqlBuilder.append(" execute_time AS executeTime ");
        sqlBuilder.append(" FROM measurement_task task");
        sqlBuilder.append(" WHERE ");
        sqlBuilder.append(" EXISTS" +
                "(SELECT measurement_task_id " +
                "FROM measurement_task_detail detail " +
                "WHERE abnormal = :abnormal " +
                "AND task.id = detail.measurement_task_id AND finished_status = :finishedStatus) ");
        params.put("abnormal", MeasurementTaskDetail.ABNORMAL);
        params.put("finishedStatus", MeasurementTaskDetail.COMPLETED);
        String sql = sqlBuilder.toString();
        List<MeasurementTask> abnormalMeasurementTasks = measurementTaskBaseService
                .findSQL(sql, params, MeasurementTask.class, page.getCurrentPage(), page.getPageSize());

        String countSql = HqlUtils.getCountSql(sql);
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        page.setDataTotal(count.isEmpty() ? 0 : count.get(0).longValue());

        List<String> measurementTaskIds = StreamUtil.getList(abnormalMeasurementTasks, MeasurementTask::getId);
        //获取筛选后的异常任务  key: 异常任务 value:对应异常子任务  （任务下包含多个子任务，通过任务去分页和查找 可以提高效率）
        Map<MeasurementTask, List<MeasurementTaskDetail>> abnormalTaskAndTaskDetails = measurementTaskDetailService
                .getAbnormalTaskAndTaskDetails(pageMeasurementTaskDetail, measurementTaskIds);


        //向 MeasurementTaskPojo 填充数据 用于页面的2级展示
        List<MeasurementTaskPojo> measurementTaskPojos = new ArrayList<>();
        for (Map.Entry<MeasurementTask, List<MeasurementTaskDetail>> entry : abnormalTaskAndTaskDetails.entrySet()) {
            measurementTaskPojos.add(new MeasurementTaskPojo(entry.getKey()));

            List<MeasurementTaskPojo> measurementTaskDetailPojos = new ArrayList<>();
            for (MeasurementTaskDetail measurementTaskDetail : entry.getValue()) {
                measurementTaskDetailPojos.add(
                        new MeasurementTaskPojo(measurementTaskDetail, entry.getKey(), measurementTaskDetail.getDeviceName()));
            }
            measurementTaskPojos.addAll(measurementTaskDetailPojos);
        }
        //获取设备
        String hql = "from Device";
        List<Device> devices = deviceBaseService.find(hql);

        Map<String, Object> viewMap = new HashMap<>();
        viewMap.put("measurementTaskPojos", measurementTaskPojos);
        viewMap.put("devices", devices);
        viewMap.put("pageMeasurementTaskDetail", pageMeasurementTaskDetail);
        return new ModelAndView("/backstage/measurementtasksubmit/measurement-task-abnormal-statistics-list", viewMap);
    }

    @ModelAttribute
    public void setPageMeasurementTaskDetail(MeasurementTaskDetail measurementTaskDetail) {
        this.pageMeasurementTaskDetail = measurementTaskDetail;
    }
}

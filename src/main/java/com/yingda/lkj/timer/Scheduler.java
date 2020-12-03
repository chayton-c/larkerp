package com.yingda.lkj.timer;

import com.sun.istack.Nullable;
import com.yingda.lkj.beans.entity.backstage.device.DeviceMaintenancePlan;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTask;
import com.yingda.lkj.service.backstage.device.DeviceMaintenancePlanService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTaskService;
import com.yingda.lkj.utils.SpringContextUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author hood  2020/4/7
 */
@Component
@EnableScheduling
public class Scheduler {

    /**
     * Scheduling跟socket冲突了，这么写解决
     * https://blog.csdn.net/kzcming/article/details/102390593
     */
    @Bean
    @Nullable
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler threadPoolScheduler = new ThreadPoolTaskScheduler();
        threadPoolScheduler.setThreadNamePrefix("SockJS-");
        threadPoolScheduler.setPoolSize(Runtime.getRuntime().availableProcessors());
        threadPoolScheduler.setRemoveOnCancelPolicy(true);
        return threadPoolScheduler;
    }

    /**
     * <p>根据deviceMeasurementPlan(设备维护计划)定时生成测量计划</p>
     * <p>每小时执行</p>
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void createMeasurementTask() {
        DeviceMaintenancePlanService deviceMaintenancePlanService = (DeviceMaintenancePlanService) SpringContextUtil.getBean("deviceMaintenancePlanService");
        deviceMaintenancePlanService.timedGenerateMeasurementTask();
    }

    /**
     * <p>检查超时的任务，设置为漏检</p>
     * <p>每小时执行</p>
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkMissedMeasurementTask() {
        MeasurementTaskService measurementTaskService = (MeasurementTaskService) SpringContextUtil.getBean("measurementTaskService");
        measurementTaskService.checkMissed();
    }

}

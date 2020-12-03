package com.yingda.lkj.controller.app;

import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItemField;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTaskDetail;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementUnit;
import com.yingda.lkj.beans.pojo.app.etms.*;
import com.yingda.lkj.beans.pojo.app.etms.freeetms.AppFreeDeviceMaintenanceRecord;
import com.yingda.lkj.beans.pojo.app.etms.freeetms.AppFreeDeviceMaintenanceRecordFieldValue;
import com.yingda.lkj.beans.pojo.app.etms.freeetms.ReceiveDeviceFreeMaintenanceRecordAppJson;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.measurement.FreeDeviceMaintenanceRecordService;
import com.yingda.lkj.service.backstage.measurement.MeasurementItemFieldValueService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTaskDetailService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.JsonUtils;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 从app获取etms测量数据
 * @author hood  2020/4/26
 */
@Controller
@RequestMapping("/app/appDeviceMaintenance")
public class AppDeviceMaintenanceController extends BaseController {

    @Autowired
    private MeasurementItemFieldValueService measurementItemFieldValueService;
    @Autowired
    private FreeDeviceMaintenanceRecordService freeDeviceMaintenanceRecordService;
    @Autowired
    private MeasurementTaskDetailService measurementTaskDetailService;


    @RequestMapping("/receiveDeviceMaintenanceRecord")
    @ResponseBody
    public Json receiveDeviceMaintenanceRecord() throws IOException {
        System.out.println("receiveDeviceMaintenanceRecord");
        String akagi = req.getParameter("json");
        System.out.println(akagi);

        List<AppDeviceMaintenanceRecord> appDeviceMaintenanceRecords = JsonUtils.parseList(akagi, AppDeviceMaintenanceRecord.class);

        // 提交测量值
        for (AppDeviceMaintenanceRecord appDeviceMaintenanceRecord : appDeviceMaintenanceRecords) {
            String measurementTaskDetailId = appDeviceMaintenanceRecord.getMeasurementTaskDetailId();

            String value = appDeviceMaintenanceRecord.getValue();
            value = StringUtils.isEmpty(value) ? "0" : value;

            measurementItemFieldValueService.saveOrUpdateFieldValue(
                    appDeviceMaintenanceRecord.getId(), measurementTaskDetailId, value);
        }

        // 提交任务
        for (AppDeviceMaintenanceRecord appDeviceMaintenanceRecord : appDeviceMaintenanceRecords)
            measurementTaskDetailService.executeTask(appDeviceMaintenanceRecord.getMeasurementTaskDetailId());

        return new Json(JsonMessage.SUCCESS, akagi);
    }

    @RequestMapping("/receiveDeviceFreeMaintenanceRecord")
    @ResponseBody
    public Json receiveDeviceFreeMaintenanceRecord() throws IOException {
        System.out.println("receiveDeviceFreeMaintenanceRecord");
        String akagi = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println(req.getParameterNames());
        System.out.println(akagi);

        ReceiveDeviceFreeMaintenanceRecordAppJson receives = JsonUtils.parse(akagi, ReceiveDeviceFreeMaintenanceRecordAppJson.class);
        List<AppFreeDeviceMaintenanceRecord> appFreeDeviceMaintenanceRecords = receives.getJson();
        for (AppFreeDeviceMaintenanceRecord appFreeDeviceMaintenanceRecord : appFreeDeviceMaintenanceRecords)
            freeDeviceMaintenanceRecordService.addFromApp(appFreeDeviceMaintenanceRecord);

        return new Json(JsonMessage.SUCCESS, akagi);
    }

    private String jsonStr = "";

}

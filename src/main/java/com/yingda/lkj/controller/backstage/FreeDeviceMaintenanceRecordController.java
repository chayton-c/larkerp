package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.entity.backstage.measurement.FreeDeviceMaintenanceRecord;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hood  2020/6/10
 */
@Controller
@RequestMapping("/backstage/freeDeviceMaintenanceRecord")
public class FreeDeviceMaintenanceRecordController extends BaseController {

    @Autowired
    private BaseService<FreeDeviceMaintenanceRecord> freeDeviceMaintenanceRecordBaseService;

    @RequestMapping("/")
    public void getList() {
    }
}

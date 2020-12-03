package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.entity.backstage.device.DeviceFailure;
import com.yingda.lkj.beans.entity.system.UploadImage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.device.DeviceFailureService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.hql.HqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

/**
 * @author hood  2020/8/5
 */
@RequestMapping("/backstage/deviceFailure")
@Controller
public class DeviceFailureController extends BaseController {

    @Autowired
    private BaseService<DeviceFailure> deviceFailureBaseService;
    @Autowired
    private BaseService<Long> longBaseServicel;
    @Autowired
    private DeviceFailureService deviceFailureService;

    @RequestMapping("")
    public ModelAndView getList() throws Exception {
        Map<String, Object> attributes = new HashMap<>();

        String deviceName = req.getParameter("deviceName");
        String deviceCode = req.getParameter("deviceCode");
        String executeUserName = req.getParameter("executeUserName");

        attributes.put("deviceName", deviceName);
        attributes.put("deviceCode", deviceCode);
        attributes.put("executeUserName", executeUserName);

        Map<String, Object> params = new HashMap<>();
        StringBuilder hqlBuilder = new StringBuilder();

        hqlBuilder.append("FROM\n");
        hqlBuilder.append("  DeviceFailure deviceFailure\n");
        hqlBuilder.append("WHERE\n");
        hqlBuilder.append("  1 = 1\n");
        if (StringUtils.isNotEmpty(executeUserName)) {
            hqlBuilder.append("AND deviceFailure.user.displayName like :executeUserName\n");
            params.put("executeUserName", "%" + executeUserName + "%");
        }
        if (StringUtils.isNotEmpty(deviceName)) {
            hqlBuilder.append("AND deviceFailure.device.name like :deviceName\n");
            params.put("deviceName", "%" + deviceName + "%");
        }
        if (StringUtils.isNotEmpty(deviceCode)) {
            hqlBuilder.append("AND deviceFailure.device.code like :deviceCode\n");
            params.put("deviceCode", "%" + deviceCode + "%");
        }
        hqlBuilder.append("ORDER BY addTime desc");

        String hql = hqlBuilder.toString();
        List<DeviceFailure> deviceFailures = deviceFailureBaseService.find(
                hql, params, page.getCurrentPage(), page.getPageSize()
        );

        String countHql = HqlUtils.getCountSql(hql);
        List<Long> count = longBaseServicel.find(countHql, params);
        page.setDataTotal(count.isEmpty() ? 0L : count.get(0));

        attributes.put("page", page);
        attributes.put("deviceFailures", deviceFailures);

        return new ModelAndView("/backstage/device/device-failure/device-failure-list", attributes);
    }

    @RequestMapping("/getImages")
    @ResponseBody
    public ModelAndView getImages() {
        Map<String, Object> attributes = new HashMap<>();

        String id = req.getParameter("id");

        List<UploadImage> deviceFailureImages = deviceFailureService.getDeviceFailureImages(id);
        attributes.put("deviceFailureImages", deviceFailureImages);
        return new ModelAndView("/backstage/device/device-failure/device-failure-images", attributes);
    }


}

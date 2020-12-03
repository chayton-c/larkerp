package com.yingda.lkj.controller.system;

import com.yingda.lkj.beans.entity.backstage.device.DeviceFailure;
import com.yingda.lkj.beans.entity.backstage.device.DeviceSubType;
import com.yingda.lkj.beans.entity.backstage.device.DeviceType;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItem;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItemField;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTemplate;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.enums.repairclass.RepairClass;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.device.DeviceSubTypeService;
import com.yingda.lkj.service.backstage.device.DeviceTypeService;
import com.yingda.lkj.service.backstage.measurement.MeasurementItemFieldService;
import com.yingda.lkj.service.backstage.measurement.MeasurementItemService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTemplateService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author hood  2020/5/18
 */
@Controller
@RequestMapping("/test")
public class TestController extends BaseController {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private BaseService<MeasurementItemField> measurementItemFieldBaseService;
    @Autowired
    private BaseService<MeasurementItem> measurementItemBaseService;

    @Autowired
    private BaseService<MeasurementTemplate> measurementTemplateBaseService;
    @Autowired
    private BaseService<DeviceSubType> deviceSubTypeBaseService;
    @Autowired
    private BaseService<User> userBaseService;
    @Autowired
    private BaseService<DeviceFailure> deviceFailureBaseService;
    @Autowired
    private DeviceTypeService deviceTypeService;
    @Autowired
    private DeviceSubTypeService deviceSubTypeService;
    @Autowired
    private MeasurementItemService measurementItemService;
    @Autowired
    private MeasurementItemFieldService measurementItemFieldService;
    @Autowired
    private MeasurementTemplateService measurementTemplateService;

    @RequestMapping("/nagato")
    @ResponseBody
    public Json nagato() throws Exception {
        List<Map<String, Object>> showdown = RepairClass.showdown();
        return new Json(JsonMessage.SUCCESS, Map.of("showdown", showdown));
    }

    @RequestMapping("/set")
    @ResponseBody
    public String HelloSpring (String key,String value){
        redisTemplate.opsForValue().set(key,value);
        return String.format("redis set成功！key=%s,value=%s",key,value);
    }

    @RequestMapping("/get")
    @ResponseBody
    public String HelloSpring (String key){
        String value = (String) redisTemplate.opsForValue().get(key);
        return "redis get结果 value=" + value;
    }

    @RequestMapping("/akagi")
    @ResponseBody
    public Json akagi() {
        String name = req.getParameter("name");
        String age = req.getParameter("age");
        System.out.println(name);
        System.out.println(age);
        return new Json(JsonMessage.SUCCESS);
    }

    public static void main(String[] args) {
        String akagi = "akagi";
        System.out.println(Arrays.toString(akagi.split("")));
    }
}

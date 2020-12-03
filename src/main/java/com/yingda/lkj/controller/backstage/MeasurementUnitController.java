package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementUnit;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

/**
 * @author hood  2020/3/12
 */
@Controller
@RequestMapping("/backstage/measurementUnit")
public class MeasurementUnitController extends BaseController {
    @Autowired
    private BaseService<MeasurementUnit> measurementUnitBaseService;

    private MeasurementUnit pageMeasurementUnit;

    @RequestMapping("")
    public ModelAndView getList() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        String groupName = req.getParameter("groupName");
        String name = req.getParameter("name");

        Map<String, Object> params = new HashMap<>();
        Map<String, String> conditions = new HashMap<>();

        if (StringUtils.isNotEmpty(name)) {
            attributes.put("name", name);
            params.put("name", "%" + name + "%");
            conditions.put("name", "like");
        }
        if (StringUtils.isNotEmpty(groupName)) {
            attributes.put("groupName", groupName);
            params.put("groupName", "%" + groupName + "%");
            conditions.put("namegroupName", "like");
        }
        List<MeasurementUnit> measurementUnits = measurementUnitBaseService.getObjcetPagination(MeasurementUnit.class, params, conditions,
                page.getCurrentPage(), page.getPageSize(), "order by groupName ,addTime desc");
        page.setDataTotal(measurementUnitBaseService.getObjectNum(MeasurementUnit.class, params, conditions));

        attributes.put("page", page);
        attributes.put("measurementUnits", measurementUnits);
        return new ModelAndView("/backstage/measurementunit/measurement-unit-list", attributes);
    }

    @RequestMapping("/info")
    public ModelAndView info(String id) throws Exception {
        MeasurementUnit measurementUnit = new MeasurementUnit();
        if (StringUtils.isNotEmpty(id))
            measurementUnit = measurementUnitBaseService.get(MeasurementUnit.class, id);

        return new ModelAndView("/backstage/measurementunit/measurement-unit-info", Map.of("measurementUnit", measurementUnit));
    }

    @RequestMapping("/saveOrUpdate")
    @ResponseBody
    public Json saveOrUpdate() throws Exception {
        String id = pageMeasurementUnit.getId();
        if (StringUtils.isEmpty(id))
            measurementUnitBaseService.saveOrUpdate(new MeasurementUnit(pageMeasurementUnit));
        else {
            MeasurementUnit measurementUnit = measurementUnitBaseService.get(MeasurementUnit.class, id);
            measurementUnit.setGroupName(pageMeasurementUnit.getGroupName());
            measurementUnit.setName(pageMeasurementUnit.getName());
            measurementUnit.setUnitName(pageMeasurementUnit.getUnitName());
            measurementUnit.setValueType(pageMeasurementUnit.getValueType());
            measurementUnit.setDescription(pageMeasurementUnit.getDescription());
            measurementUnit.setMainFunctionCode(pageMeasurementUnit.getMainFunctionCode());
            measurementUnit.setSubFunctionCode(pageMeasurementUnit.getSubFunctionCode());
            measurementUnit.setHide(pageMeasurementUnit.getHide());
            measurementUnit.setRemark(pageMeasurementUnit.getRemark());
            measurementUnitBaseService.saveOrUpdate(measurementUnit);
        }

        return new Json(JsonMessage.SUCCESS);
    }

    /**
     * 假删
     */
    @RequestMapping("/delete/{id}")
    @ResponseBody
    public Json delete(@PathVariable String id) throws Exception {
        MeasurementUnit measurementUnit = measurementUnitBaseService.get(MeasurementUnit.class, id);
        measurementUnit.setHide(Constant.HIDE);
        measurementUnitBaseService.saveOrUpdate(measurementUnit);
        return new Json(JsonMessage.SUCCESS);
    }

    @ModelAttribute
    public void setPageMeasurementUnit(MeasurementUnit pageMeasurementUnit) {
        this.pageMeasurementUnit = pageMeasurementUnit;
    }
}

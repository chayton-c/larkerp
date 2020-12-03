package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.entity.backstage.line.Fragment;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjFreeMeasurement;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjFreeMeasurementLocation;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.line.RailwayLineService;
import com.yingda.lkj.service.backstage.lkjdataline.LkjDataLineService;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.hql.HqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * lkj自由测量页
 *
 * @author hood  2020/4/26
 */
@Controller
@RequestMapping("/backstage/lkjFreeMeasurement")
public class LkjFreeMeasurementController extends BaseController {
    @Autowired
    private BaseService<LkjFreeMeasurement> lkjFreeMeasurementBaseService;
    @Autowired
    private BaseService<BigInteger> bigIntegerBaseService;
    @Autowired
    private BaseService<LkjFreeMeasurementLocation> lkjFreeMeasurementLocationBaseService;
    @Autowired
    private OrganizationClientService organizationClientService;
    @Autowired
    private LkjDataLineService lkjDataLineService;
    @Autowired
    private RailwayLineService railwayLineService;
    @Autowired
    private BaseService<Fragment> fragmentBaseService;
    @Autowired
    private BaseService<LkjDataLine> lkjDataLineBaseService;


    @RequestMapping("")
    public ModelAndView getList() throws Exception {
        Map<String, Object> attributes = new HashMap<>();

        String executeUserName = req.getParameter("executeUserName");
        String pointNames = req.getParameter("pointNames");

        attributes.put("executeUserName", executeUserName);
        attributes.put("pointNames", pointNames);

        Map<String, Object> params = new HashMap<>();

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT\n");
        sqlBuilder.append("  measurement.id AS id,\n");
        sqlBuilder.append("  measurement.execute_user_id AS executeUserId,\n");
        sqlBuilder.append("  measurement.distance AS distance,\n");
        sqlBuilder.append("  measurement.point_names AS pointNames,\n");
        sqlBuilder.append("  measurement.measure_time AS measureTime,\n");
        sqlBuilder.append("  measurement.add_time AS addTime,\n");
        sqlBuilder.append("  measurement.update_time AS updateTime,\n");
        sqlBuilder.append("  USER.display_name AS executeUserName,\n");
        sqlBuilder.append("  workshop.name AS workshopName \n");
        sqlBuilder.append("FROM\n");
        sqlBuilder.append("  lkj_free_measurement measurement\n");
        sqlBuilder.append("  LEFT JOIN USER ON USER.id = measurement.execute_user_id \n");
        sqlBuilder.append("  LEFT JOIN organization workshop ON USER.workshop_id = workshop.id \n");
        sqlBuilder.append("WHERE\n");
        sqlBuilder.append("  1 = 1 \n");
        if (StringUtils.isNotEmpty(executeUserName)) {
            sqlBuilder.append("  AND USER.display_name LIKE :executeUserName \n");
            params.put("executeUserName", "%" + executeUserName + "%");
        }
        if (StringUtils.isNotEmpty(pointNames)) {
            sqlBuilder.append("  AND measurement.point_names LIKE :pointNames \n");
            params.put("pointNames", "%" + pointNames + "%");
        }

        sqlBuilder.append("ORDER BY\n");
        sqlBuilder.append("  measurement.add_time DESC");

        String sql = sqlBuilder.toString();
        List<LkjFreeMeasurement> lkjFreeMeasurements = lkjFreeMeasurementBaseService.findSQL(
                sql, params, LkjFreeMeasurement.class, page.getCurrentPage(), page.getPageSize());
        String countSql = HqlUtils.getCountSql(sql);
        List<BigInteger> counts = bigIntegerBaseService.findSQL(countSql, params);
        page.setDataTotal(counts.isEmpty() ? 0 : counts.get(0).longValue());

        attributes.put("lkjFreeMeasurements", lkjFreeMeasurements);
        attributes.put("page", page);

        return new ModelAndView("/backstage/lkj-free-measurement/lkj-free-measurement", attributes);
    }

    @RequestMapping("/measuringPath")
    public ModelAndView measuringPath() {
        String lkjFreeMeasurementId = req.getParameter("lkjFreeMeasurementId");
        return new ModelAndView("/backstage/lkj-free-measurement/measuring-path", Map.of("lkjFreeMeasurementId", lkjFreeMeasurementId));
    }

    @RequestMapping("/getLocations")
    @ResponseBody
    public Json getLocations() throws Exception {
        String lkjFreeMeasurementId = req.getParameter("lkjFreeMeasurementId");
        List<LkjFreeMeasurementLocation> lkjFreeMeasurementLocations = lkjFreeMeasurementLocationBaseService.find(
                "from LkjFreeMeasurementLocation where lkjFreeMeasurementId = :lkjFreeMeasurementId order by executeTime desc",
                Map.of("lkjFreeMeasurementId", lkjFreeMeasurementId)
        );

        return new Json(JsonMessage.SUCCESS, lkjFreeMeasurementLocations);
    }


    /**
     * 任务选择页
     */
}

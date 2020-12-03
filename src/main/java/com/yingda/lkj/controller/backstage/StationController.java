package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.entity.system.Role;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.utils.ExcelRowInfo;
import com.yingda.lkj.beans.pojo.utils.ExcelSheetInfo;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.line.RailwayLineService;
import com.yingda.lkj.service.backstage.line.StationRailwayLineService;
import com.yingda.lkj.service.backstage.line.StationService;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.RequestUtil;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.date.DateUtil;
import com.yingda.lkj.utils.excel.ExcelUtil;
import com.yingda.lkj.utils.hql.HqlUtils;
import com.yingda.lkj.utils.pojo.PojoUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hood  2020/1/2
 */
@RequestMapping("/backstage/station")
@Controller
public class StationController extends BaseController {

    @Autowired
    private StationRailwayLineService stationRailwayLineService;
    @Autowired
    private BaseService<Station> stationBaseService;
    @Autowired
    private OrganizationClientService organizationClientService;
    @Autowired
    private RailwayLineService railwayLineService;
    @Autowired
    private StationService stationService;
    private Station pageStation;

    @ModelAttribute
    public void setPageStation(Station pageStation) {
        this.pageStation = pageStation;
    }

    @RequestMapping("/info")
    @ResponseBody
    public Json info() throws Exception {
        String stationId = pageStation.getId();
        Station station = StringUtils.isNotEmpty(stationId) ? stationBaseService.get(Station.class, stationId) : new Station();
        if (StringUtils.isNotEmpty(stationId)) {
           String workAreaId = station.getWorkAreaId();
            Organization workshop = organizationClientService.getParent(workAreaId);
            Organization section = organizationClientService.getParent(workshop.getId());
            Organization bureau = organizationClientService.getParent(section.getId());
            station.setWorkshopId(workshop.getId());
            station.setSectionId(section.getId());
            station.setBureauId(bureau.getId());

            List<String> railwayLineIds = stationRailwayLineService.getRailwayLineIdsByStationIds(stationId);
            if (!railwayLineIds.isEmpty())
                station.setRailwayLineId(railwayLineIds.get(0));
        }

        if (StringUtils.isEmpty(station.getRailwayLineId()))
            station.setRailwayLineId(pageStation.getRailwayLineId());

        // 备选线路
        List<RailwayLine> railwayLines = railwayLineService.getAll();
        // 备选局
        List<Organization> bereaus = organizationClientService.getBureaus();
        // 备选站段
        List<Organization> sections = organizationClientService.getSections();
        // 备选车间
        List<Organization> workshops = organizationClientService.getWorkshops();
        // 备选工区
        List<Organization> workAreas = organizationClientService.getWorkAreas();

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("station", station);
        attributes.put("railwayLines", railwayLines);
        attributes.put("bureaus", bereaus);
        attributes.put("sections", sections);
        attributes.put("workshops", workshops);
        attributes.put("workAreas", workAreas);

        return new Json(JsonMessage.SUCCESS, attributes);
    }

    @RequestMapping("/getStationsByRailwayLineId")
    @ResponseBody
    public Json getStationsByRailwayLineId() throws Exception {
        String railwayLineId = req.getParameter("railwayLineId");
        String name = req.getParameter("name");

        List<String> stationIds = stationRailwayLineService.getStationIdsByRailwayLineId(railwayLineId);
        Map<String, Object> attributes = new HashMap<>();

        Map<String, Object> params = new HashMap<>();
        String sql = """
                SELECT
                	station.id,
                	station.name AS name,
                	station.code AS code,
                	workArea.name AS workAreaName,
                	workshop.name AS workshopName
                FROM
                	station
                	INNER JOIN organization AS workArea ON station.work_area_id = workArea.id
                	INNER JOIN organization AS workshop ON workArea.parent_id = workshop.id
                WHERE
                    station.id in :stationIds
                """;
        params.put("stationIds", stationIds);
        if (StringUtils.isNotEmpty(name)) {
            sql += "AND station.name like :name";
            params.put("name", "%" + name + "%");
        }

        List<Station> stations = stationBaseService.findSQL(sql, params, Station.class, page.getCurrentPage(), page.getPageSize());
        List<BigInteger> count = bigIntegerBaseService.findSQL(HqlUtils.getCountSql(sql), params);
        page.setDataTotal(count.isEmpty() ? 0 : count.get(0).longValue());

        attributes.put("stations", stations);
        attributes.put("page", page);

        return new Json(JsonMessage.SUCCESS, attributes);
    }


    @RequestMapping("/saveOrUpdate")
    @ResponseBody
    public Json saveOrUpdate() throws Exception {
        String stationId = pageStation.getId();
        Station station = StringUtils.isNotEmpty(stationId) ? stationService.getById(stationId) : new Station(pageStation);
        BeanUtils.copyProperties(pageStation, station, "id", "addTime", "hide");
        station.setUpdateTime(current());
        stationBaseService.saveOrUpdate(station);

        String railwayLineId = req.getParameter("railwayLineId");
        stationRailwayLineService.saveOrUpdate(station.getId(), railwayLineId);

        return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public Json delete(String ids) throws Exception {
        stationService.deleteByIds(Arrays.asList(ids.split(",")));
        return new Json(JsonMessage.SUCCESS);
    }

    /**
     * 导入车站
     */
    @RequestMapping("/importStations")
    @ResponseBody
    public Json importStations(MultipartFile file) throws Exception {
        List<ExcelSheetInfo> excelSheetInfos = ExcelUtil.readExcelFile(file);
        return stationService.importStations(excelSheetInfos);
    }

    /**
     * 导出车站
     */
    @RequestMapping("/exportStations")
    @ResponseBody
    public void exportStations() throws Exception {
        User user = RequestUtil.getUser(req);

        Map<String, String> conditions = new HashMap<>();
        Map<String, Object> params = new HashMap<>();

        String code = pageStation.getCode();
        String name = pageStation.getName();
        String workshopId = pageStation.getWorkshopId();

        if (StringUtils.isNotEmpty(name)) {
            conditions.put("name", "like");
            params.put("name", "%" + name + "%");
        }
        if (StringUtils.isNotEmpty(code)) {
            conditions.put("code", "like");
            params.put("code", "%" + code + "%");
        }

        String sectionId = user.getSectionId();
        List<Organization> workAreas;
        if (StringUtils.isNotEmpty(sectionId))
            workAreas = organizationClientService.getWorkAreasBySectionId(sectionId); // 用户所在站段下的所有区间
        else
            workAreas = organizationClientService.getAllWorkAreas();
        if (StringUtils.isNotEmpty(workshopId)) {
            workAreas = organizationClientService.getSlave(workshopId);
            List<String> workAreaIds = StreamUtil.getList(workAreas, Organization::getId);
            conditions.put("workAreaId", "in");
            params.put("workAreaId", workAreaIds);
        } else {
            conditions.put("workAreaId", "in");
            params.put("workAreaId", workAreas.stream().map(Organization::getId).collect(Collectors.toList()));
        }

        conditions.put("hide", "=");
        params.put("hide", Constant.SHOW);
        List<Station> stationList = stationBaseService.getObjects(
                Station.class, params, conditions, "order by updateTime desc");

        for (Station station : stationList) {
            String workAreaId = station.getWorkAreaId();
            Organization workshop = organizationClientService.getParent(workAreaId);
            Organization section = organizationClientService.getParent(workshop.getId());
            station.setWorkshopName(workshop.getName());
            station.setSectionName(section.getName());
        }

        int rows = 0;
        Map<Integer, ExcelRowInfo> excelRowInfoMap = new HashMap<>();
        //列明
        excelRowInfoMap.put(rows++, new ExcelRowInfo(rows++, "编码", "名称", "电务段", "所属车间", "加入时间"));

        for (Station station : stationList) {
            //每行信息
            excelRowInfoMap.put(rows++, new ExcelRowInfo(++rows, station.getCode(), station.getName()
                    , station.getSectionName(), station.getWorkshopName()
                    , DateUtil.format(station.getAddTime(), "yyyy-mm-dd")));
        }

        ExcelSheetInfo excelSheetInfo = new ExcelSheetInfo("车站信息", excelRowInfoMap);

        Workbook workbook = ExcelUtil.createExcelFile(List.of(excelSheetInfo));
        MultipartFile workbookFile = ExcelUtil.workbook2File(workbook, "车站信息");
        export(workbookFile);
    }

    @RequestMapping("/linkageStationAndRailwayLine")
    @ResponseBody
    public Json linkageStationAndRailwayLine() {
        String railwayLineIds = req.getParameter("railwayLineIds");
        String stationId = req.getParameter("stationId");

        stationRailwayLineService.saveOrUpdate(stationId, Arrays.asList(railwayLineIds.split(",")));

        return new Json(JsonMessage.SUCCESS);
    }

    /**
     * 通过车间 获取 车站
     * 联系  车间--工区--车站
     */
    @RequestMapping("/getStations")
    @ResponseBody
    public Json getStations(String workShopId) {
        return new Json(JsonMessage.SUCCESS, stationService.getStationsByWorkshopId(workShopId));
    }
}

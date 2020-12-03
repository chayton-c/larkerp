package com.yingda.lkj.service.impl.backstage.line;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.line.Fragment;
import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.utils.ExcelRowInfo;
import com.yingda.lkj.beans.pojo.utils.ExcelSheetInfo;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.device.DeviceService;
import com.yingda.lkj.service.backstage.line.RailwayLineService;
import com.yingda.lkj.service.backstage.line.StationRailwayLineService;
import com.yingda.lkj.service.backstage.line.StationService;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.utils.StreamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hood  2020/1/7
 */
@Service("StationService")
public class StationServiceImpl implements StationService {

    @Autowired
    private BaseDao<Station> stationBaseDao;
    @Autowired
    private BaseDao<Fragment> fragmentBaseDao;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private RailwayLineService railwayLineService;
    @Autowired
    private StationRailwayLineService stationRailwayLineService;
    @Autowired
    private OrganizationClientService organizationClientService;

    @Override
    public List<Station> getStationsByNames(List<String> names) throws CustomException {
        names = names.stream().distinct().collect(Collectors.toList());
        List<Station> stations = stationBaseDao.find("from Station where name in :stationNames", Map.of("stationNames", names));

        if (stations.size() != names.size()) {
            List<String> acquiredNameList = stations.stream().map(Station::getName).collect(Collectors.toList());
            List<String> notIncluded = names.stream().filter(x -> !acquiredNameList.contains(x)).collect(Collectors.toList());
            System.out.println(notIncluded);
            throw new CustomException(new Json(JsonMessage.SYS_ERROR, "找不到name = " + notIncluded + "的站记录，请前往基础数据 -> 车站管理中添加对应名称的车站"));
        }

        return stations;
    }

    @Override
    public Station getStationByName(String name) {
        return stationBaseDao.get("from Station where name = :name", Map.of("name", name));
    }

    @Override
    public List<Station> getStationsByWorkAreaIds(List<String> workAreaIds) {
        return stationBaseDao.find("from Station where workAreaId in (:workAreaIds)", Map.of("workAreaIds", workAreaIds));
    }

    @Override
    public List<Station> getStationsByWorkshopId(String workshopId) {
        List<Organization> workAreas = organizationClientService.getSlave(workshopId);
        if (workAreas.isEmpty())
            return new ArrayList<>();

        return getStationsByWorkAreaIds(StreamUtil.getList(workAreas, Organization::getId));
    }

    @Override
    public List<Station> getStationsBySectionId(String sectionId) {
        List<Organization> workAreas = organizationClientService.getWorkAreasBySectionId(sectionId);
        if (workAreas.isEmpty())
            return new ArrayList<>();

        return getStationsByWorkAreaIds(StreamUtil.getList(workAreas, Organization::getId));
    }

    @Override
    public void deleteByIds(List<String> ids) throws CustomException {
        for (String id : ids) {
            List<Device> devices = deviceService.getByStationId(id);
            if (!devices.isEmpty())
                throw new CustomException(JsonMessage.CONTAINING_ASSOCIATED_DATA, String.format("名为%s的车站下仍有使用中的设备，请在修改对应设备后再删除", getById(id).getName()));
        }

        stationBaseDao.executeHql(
                "delete from Station where id in :ids",
                Map.of("ids", ids)
        );

        stationBaseDao.executeHql(
                "delete from StationRailwayLine where stationId in :ids",
                Map.of("ids", ids)
        );
    }

    @Override
    public List<Station> getByIds(List<String> ids) {
        return stationBaseDao.find("from Station where id in :ids", Map.of("ids", ids));
    }

    @Override
    public Map<String, Station> getByNames(List<String> names) {
        names = names.stream().distinct().collect(Collectors.toList());
        List<Station> stations = stationBaseDao.find("from Station where name in :names", Map.of("names", names));
        return StreamUtil.getMap(stations, Station::getName, x -> x);
    }

    @Override
    public Station getById(String id) {
        return stationBaseDao.get(Station.class, id);
    }

    @Override
    public int getCurrentSeq() {
        List<Station> stations = stationBaseDao.find("from Station order by seq desc", 1, 1);

        if (stations.isEmpty())
            return 0;
        return stations.get(0).getSeq();
    }

    @Override
    public Json importStations(List<ExcelSheetInfo> excelSheetInfos) throws CustomException {
        Timestamp current = new Timestamp(System.currentTimeMillis());

        int currentSeq = getCurrentSeq();

        if (excelSheetInfos.size() > 1)
            throw new CustomException(new Json(JsonMessage.PARAM_INVALID, "车站excel导入时只能包含一页，具体格式要求查看模板"));

        Map<Integer, ExcelRowInfo> rowInfoMap = excelSheetInfos.get(0).getRowInfoMap();

        List<Organization> workshops = organizationClientService.getWorkshops();
        // key:站段名 + 车间名(加站段名避免不同站段下车间名重复), value: 车间
        Map<String, Organization> workshopsMap = workshops.stream()
                .collect(Collectors.toMap(x -> organizationClientService.getById(x.getParentId()).getName() + x.getName(), x -> x));

        List<Station> stations = new ArrayList<>();
        // 数据从第三行开始
        for (int i = 2; i < rowInfoMap.size(); i++) {
            ExcelRowInfo excelRowInfo = rowInfoMap.get(i);
            List<String> cells = excelRowInfo.getCells();

            String lineName = cells.get(0);
            String stationName = cells.get(1);
            String stationCode = cells.get(2);
            String sectionName = cells.get(3);
            String workshopName = cells.get(4);

            Organization workshop = workshopsMap.get(sectionName + workshopName);
            if (workshop == null)
                throw new CustomException(new Json(JsonMessage.PARAM_INVALID,
                        String.format("第%d行出现错误，找不到'%s'下名为'%s'的车间", i + 1, sectionName, workshopName)
                        ));

            List<Organization> workAreas = organizationClientService.getSlave(workshop.getId());

            if (workAreas == null || workAreas.isEmpty())
                throw new CustomException(new Json(JsonMessage.PARAM_INVALID,
                        String.format("第%d行出现错误，名为'%s'的车间下未设置工区", i + 1, workshopName)
                ));

            String workAreaId = workAreas.get(0).getId();

            Station station = Optional.ofNullable(getStationByName(stationName)).orElse(new Station(workAreaId, stationCode, stationName, -1));

            station.setWorkAreaId(workAreaId);
            station.setCode(stationCode);
            station.setName(stationName);
            station.setUpdateTime(current);
            station.setLineName(lineName);
            station.setSeq(currentSeq++);

            RailwayLine railwayLine = railwayLineService.getRailwayLineByName(lineName);
            if (railwayLine == null)
                throw new CustomException(JsonMessage.PARAM_INVALID, String.format("找不到名为'%s'的线路", lineName));
            stationRailwayLineService.saveOrUpdate(station.getId(), Collections.singletonList(railwayLine.getId()));

            stations.add(station);
        }

        stations = stations.stream().filter(StreamUtil.distinct(Station::getName)).collect(Collectors.toList());
        stationBaseDao.bulkInsert(stations);

        return new Json(JsonMessage.SUCCESS);
    }
}

package com.yingda.lkj.service.impl.backstage.line;

import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.backstage.line.Fragment;
import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.entity.backstage.line.StationRailwayLine;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.line.LineSelectTreeNode;
import com.yingda.lkj.beans.pojo.utils.ExcelRowInfo;
import com.yingda.lkj.beans.pojo.utils.ExcelSheetInfo;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.dao.BaseDao;
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
 * @author hood  2020/1/2
 */
@Service("railwayLineService")
public class RailwayLineServiceImpl implements RailwayLineService {

    @Autowired
    private BaseDao<RailwayLine> railwayLineBaseDao;
    @Autowired
    private BaseDao<Fragment> fragmentBaseDao;
    @Autowired
    private OrganizationClientService organizationClineService;
    @Autowired
    private StationRailwayLineService stationRailwayLineService;
    @Autowired
    private StationService stationService;

    @Override
    public List<RailwayLine> getAll() {
        return railwayLineBaseDao.find("from RailwayLine");
    }

    @Override
    public void deleteRailwayLines(List<String> ids) {
        fragmentBaseDao.executeHql("delete from Fragment where railwayLineId in (:railwayLineId)", Map.of("railwayLineId", ids));
        railwayLineBaseDao.executeHql("delete from RailwayLine where id in (:ids)", Map.of("ids", ids));
    }

    @Override
    public RailwayLine getRailwayLinesByCode(String code) {
        return railwayLineBaseDao.get("from RailwayLine where code = :code", Map.of("code", code));
    }

    @Override
    public List<RailwayLine> getRailwayLinesByCodes(List<String> codes) throws CustomException {
        codes = codes.stream().distinct().collect(Collectors.toList());
        List<RailwayLine> railwayLines = railwayLineBaseDao.find("from RailwayLine where code in :railwayLineCodes", Map.of("railwayLineCodes", codes));

        if (railwayLines.size() != codes.size()) {
            List<String> acquiredNameList = railwayLines.stream().map(RailwayLine::getCode).collect(Collectors.toList());
            List<String> notIncluded = codes.stream().filter(x -> !acquiredNameList.contains(x)).collect(Collectors.toList());
            throw new CustomException(new Json(JsonMessage.SYS_ERROR, "找不到code = " + notIncluded + "的线路(railway_line表)记录"));
        }

        return railwayLines;
    }

    @Override
    public RailwayLine getRailwayLineByName(String name) {
        return railwayLineBaseDao.get("from RailwayLine where name = :name", Map.of("name", name));
    }

    @Override
    public Json importRailwayLine(List<ExcelSheetInfo> excelSheetInfos) throws CustomException {
        Timestamp current = new Timestamp(System.currentTimeMillis());

        if (excelSheetInfos.size() > 1)
            throw new CustomException(new Json(JsonMessage.PARAM_INVALID, "线路excel导入时只能包含一页，具体格式要求查看模板"));

        Map<Integer, ExcelRowInfo> rowInfoMap = excelSheetInfos.get(0).getRowInfoMap();

        List<RailwayLine> railwayLines = new ArrayList<>();
        // 数据从第二行开始
        for (int i = 1; i < rowInfoMap.size(); i++) {
            ExcelRowInfo excelRowInfo = rowInfoMap.get(i);
            List<String> cells = excelRowInfo.getCells();

            String bureauCode = cells.get(0);
            String lineName = cells.get(1);
            String lineCode = cells.get(2);

            // 内存方法，可以循环
            Organization bureau = organizationClineService.getBureauByCode(bureauCode);

            RailwayLine railwayLine = new RailwayLine();

            if (bureau == null)
                throw new CustomException(new Json(JsonMessage.PARAM_INVALID, "第" + (i + 1) + "行出现错误，找不到code = " + bureauCode + " 的局"));

            railwayLine.setId(UUID.randomUUID().toString());
            railwayLine.setBureauId(bureau.getId());
            railwayLine.setName(lineName);
            railwayLine.setHide(Constant.SHOW);
            railwayLine.setAddTime(current);
            railwayLine.setUpdateTime(current);
            railwayLine.setCode(lineCode);
            railwayLine.setLineNumber(i);

            railwayLines.add(railwayLine);
        }

        // 查询code重复的，这些不做导入，但是要提示
        List<String> codes = railwayLines.stream().map(RailwayLine::getCode).collect(Collectors.toList());
        String hql = "from RailwayLine where code in :codes";
        Map<String, Object> params = new HashMap<>();
        params.put("codes", codes);

        List<RailwayLine> duplicates = railwayLineBaseDao.find(hql, params);
        List<String> duplicateCodes = duplicates.stream().map(RailwayLine::getCode).collect(Collectors.toList());

        // code重复的不做插入
        railwayLines = railwayLines.stream().filter(x -> !duplicateCodes.contains(x.getCode())).collect(Collectors.toList());
        railwayLineBaseDao.bulkInsert(railwayLines);

        return new Json(JsonMessage.SUCCESS, "查询到成功导入" + railwayLines.size() + "条，排除code重复的数据" + duplicates.size() + "条");
    }

    @Override
    public List<RailwayLine> getRailwayLinesByBureauId(String bureauId) {
        return railwayLineBaseDao.find("from RailwayLine where bureauId = :bureauId", Map.of("bureauId", bureauId));
    }

    @Override
    public RailwayLine getById(String id) {
        return railwayLineBaseDao.get(RailwayLine.class, id);
    }

    @Override
    public List<RailwayLine> getByIds(List<String> ids) {
        return railwayLineBaseDao.find(
                "from RailwayLine where id in :ids",
                Map.of("ids", ids)
        );
    }

    @Override
    public Map<String, RailwayLine>  getByNames(List<String> names) {
        names = names.stream().distinct().collect(Collectors.toList());
        List<RailwayLine> railwayLines = railwayLineBaseDao.find(
                "from RailwayLine where name in :names",
                Map.of("names", names)
        );
        return StreamUtil.getMap(railwayLines, RailwayLine::getName, x -> x);
    }

    @Override
    public List<LineSelectTreeNode> initLineSelectTreeNode() {
        List<LineSelectTreeNode> result = new ArrayList<>();
        List<RailwayLine> railwayLines = railwayLineBaseDao.find("from RailwayLine");

        for (RailwayLine railwayLine : railwayLines) {
            List<String> stationIds = stationRailwayLineService.getStationIdsByRailwayLineId(railwayLine.getId());
            List<Station> stations = stationService.getByIds(stationIds);

            result.add(new LineSelectTreeNode(railwayLine, stations));
        }

        return result;
    }
}

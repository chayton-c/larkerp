package com.yingda.lkj.service.impl.backstage.line;

import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.entity.backstage.line.StationRailwayLine;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.line.RailwayLineService;
import com.yingda.lkj.service.backstage.line.StationRailwayLineService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.StreamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hood  2020/5/30
 */
@Service("stationRailwayLineService")
public class StationRailwayLineServiceImpl implements StationRailwayLineService {

    @Autowired
    private RailwayLineService railwayLineService;
    @Autowired
    private BaseDao<StationRailwayLine> stationRailwayLineBaseDao;

    @Override
    public void saveOrUpdate(String stationId, String railwayLineId) {
        stationRailwayLineBaseDao.executeHql("delete from StationRailwayLine where stationId = :stationId", Map.of("stationId", stationId));

        StationRailwayLine stationRailwayLine = getByStationIdAndRailwayLineId(stationId, railwayLineId);
        if (stationRailwayLine == null)
            stationRailwayLineBaseDao.saveOrUpdate(new StationRailwayLine(stationId, railwayLineId));
    }

    @Override
    public void saveOrUpdate(String stationId, List<String> railwayLineIds) {
        List<StationRailwayLine> stationRailwayLines = new ArrayList<>();

        for (String railwayLineId : railwayLineIds) {
            StationRailwayLine item = getByStationIdAndRailwayLineId(stationId, railwayLineId);
            if (item == null)
                stationRailwayLines.add(new StationRailwayLine(stationId, railwayLineId));
        }

        stationRailwayLineBaseDao.bulkInsert(stationRailwayLines);
    }

    @Override
    public String getRailwayLineNames(String stationId) {
        List<String> railwayLineIds = getRailwayLineIdsByStationIds(stationId);
        if (railwayLineIds.isEmpty())
            return "";

        List<RailwayLine> railwayLines = railwayLineService.getByIds(railwayLineIds);

        if (railwayLines.isEmpty())
            return "";

        return railwayLines.stream().map(RailwayLine::getName).distinct().collect(Collectors.joining(", "));
    }

    @Override
    public List<String> getRailwayLineIdsByStationIds(String stationId) {
        List<StationRailwayLine> stationRailwayLines = stationRailwayLineBaseDao.find(
                "from StationRailwayLine where stationId = :stationId",
                Map.of("stationId", stationId)
        );

        return StreamUtil.getList(stationRailwayLines, StationRailwayLine::getRailwayLineId);
    }

    @Override
    public List<String> getStationIdsByRailwayLineId(String railwayLineId) {
        List<StationRailwayLine> stationRailwayLines = stationRailwayLineBaseDao.find(
                "from StationRailwayLine where railwayLineId = :railwayLineId",
                Map.of("railwayLineId", railwayLineId)
        );

        return StreamUtil.getList(stationRailwayLines, StationRailwayLine::getStationId);
    }

    @Override
    public List<StationRailwayLine> getStationRailwayLinesByRailwayLines(List<RailwayLine> railwayLines) {
        List<String> railwayLineIds = StreamUtil.getList(railwayLines, RailwayLine::getId);
        return stationRailwayLineBaseDao.find(
                "from StationRailwayLine where railwayLineId = :railwayLineIds",
                Map.of("railwayLineIds", railwayLineIds)
        );
    }

    private StationRailwayLine getByStationIdAndRailwayLineId(String stationId, String railwayLineId) {
        return stationRailwayLineBaseDao.get(
                "from StationRailwayLine where railwayLineId = :railwayLineId and stationId = :stationId",
                Map.of("railwayLineId", railwayLineId, "stationId", stationId)
        );
    }
}

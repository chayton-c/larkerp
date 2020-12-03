package com.yingda.lkj.service.backstage.line;

import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.entity.backstage.line.StationRailwayLine;

import java.util.List;

/**
 * @author hood  2020/5/30
 */
public interface StationRailwayLineService {

    void saveOrUpdate(String stationId, String railwayLineId);

    /**
     * 添加线路车站关联
     */
    void saveOrUpdate(String stationId, List<String> railwayLineIds);

    String getRailwayLineNames(String stationId);

    List<String> getRailwayLineIdsByStationIds(String stationId);

    List<String> getStationIdsByRailwayLineId(String railwayLineId);

    List<StationRailwayLine> getStationRailwayLinesByRailwayLines(List<RailwayLine> railwayLines);
}

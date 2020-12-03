package com.yingda.lkj.service.backstage.line;

import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.line.LineSelectTreeNode;
import com.yingda.lkj.beans.pojo.utils.ExcelSheetInfo;
import com.yingda.lkj.beans.system.Json;

import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/1/2
 */
public interface RailwayLineService {

    List<RailwayLine> getAll();

    /**
     * 删除线路，同时删除线路下的区间
     * @param ids 线路ids
     */
    void deleteRailwayLines(List<String> ids);

    RailwayLine getRailwayLinesByCode(String code);

    List<RailwayLine> getRailwayLinesByCodes(List<String> codes) throws CustomException;

    RailwayLine getRailwayLineByName(String name) throws CustomException;

    Json importRailwayLine(List<ExcelSheetInfo> excelSheetInfos) throws CustomException;

    List<RailwayLine> getRailwayLinesByBureauId(String bureauId);

    RailwayLine getById(String id);

    List<RailwayLine> getByIds(List<String> ids);

    Map<String, RailwayLine> getByNames(List<String> names) throws CustomException;

    List<LineSelectTreeNode> initLineSelectTreeNode();
}

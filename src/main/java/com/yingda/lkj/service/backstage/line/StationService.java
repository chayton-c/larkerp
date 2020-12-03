package com.yingda.lkj.service.backstage.line;

import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.utils.ExcelSheetInfo;
import com.yingda.lkj.beans.system.Json;

import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/1/7
 */
public interface StationService {
    List<Station> getStationsByNames(List<String> names) throws CustomException;
    Station getStationByName(String name) throws CustomException;
    List<Station> getStationsByWorkAreaIds(List<String> workAreaIds);
    List<Station> getStationsByWorkshopId(String workshopId);
    List<Station> getStationsBySectionId(String sectionId);
    void deleteByIds(List<String> ids) throws CustomException;

    List<Station> getByIds(List<String> ids);

    Map<String, Station> getByNames(List<String> names);

    Station getById(String id);

    /**
     * 获取当前最大排序
     */
    int getCurrentSeq();

    Json importStations(List<ExcelSheetInfo> excelSheetInfos) throws CustomException;
}

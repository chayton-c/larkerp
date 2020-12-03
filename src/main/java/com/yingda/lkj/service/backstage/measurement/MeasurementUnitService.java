package com.yingda.lkj.service.backstage.measurement;

import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementUnit;
import com.yingda.lkj.beans.system.Pair;

import java.util.List;

/**
 * @author hood  2020/6/10
 */
public interface MeasurementUnitService {
    List<MeasurementUnit> showdown();

    MeasurementUnit getById(String id);

    /**
     * 主从功能码获取测量单位
     * @param mainFunctionCode 主功能码
     * @param subFunctionCode 子功能码
     */
    MeasurementUnit getByFunctionCode(String mainFunctionCode, String subFunctionCode);
}

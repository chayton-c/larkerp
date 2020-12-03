package com.yingda.lkj.service.impl.backstage.measurement;

import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementUnit;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.measurement.MeasurementUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/6/10
 */
@Service("measurementUnitService")
public class MeasurementUnitServiceImpl implements MeasurementUnitService {

    @Autowired
    private BaseDao<MeasurementUnit> measurementUnitBaseDao;

    @Override
    public List<MeasurementUnit> showdown() {
        return measurementUnitBaseDao.find(
                "from MeasurementUnit"
        );
    }

    @Override
    public MeasurementUnit getById(String id) {
        return measurementUnitBaseDao.get(MeasurementUnit.class, id);
    }

    @Override
    public MeasurementUnit getByFunctionCode(String mainFunctionCode, String subFunctionCode) {
        return measurementUnitBaseDao.get(
                "from MeasurementUnit where mainFunctionCode = :mainFunctionCode and subFunctionCode = :subFunctionCode",
                Map.of("mainFunctionCode", mainFunctionCode, "subFunctionCode", subFunctionCode)
        );
    }
}

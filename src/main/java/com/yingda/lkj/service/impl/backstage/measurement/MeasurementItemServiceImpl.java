package com.yingda.lkj.service.impl.backstage.measurement;

import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementItem;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.measurement.MeasurementItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/6/17
 */
@Service("measurementItemService")
public class MeasurementItemServiceImpl implements MeasurementItemService {

    @Autowired
    private BaseDao<MeasurementItem> measurementItemBaseDao;

    @Override
    public List<MeasurementItem> getMeasurementItemsByMeasurementTemplateId(String measurementTemplateId) {
        return measurementItemBaseDao.find(
                "from MeasurementItem where measurementTemplateId = :measurementTemplateId",
                Map.of("measurementTemplateId", measurementTemplateId)
        );
    }

}

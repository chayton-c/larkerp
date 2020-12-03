package com.yingda.lkj.service.impl.backstage.measurement;

import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.backstage.device.DeviceType;
import com.yingda.lkj.beans.entity.backstage.measurement.*;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.device.DeviceTypeService;
import com.yingda.lkj.service.backstage.measurement.MeasurementItemFieldService;
import com.yingda.lkj.service.backstage.measurement.MeasurementItemService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTemplateService;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hood  2020/3/18
 */
@Service("measurementItemFieldService")
public class MeasurementItemFieldServiceImpl implements MeasurementItemFieldService {

    @Autowired
    private MeasurementTemplateService measurementTemplateService;
    @Autowired
    private BaseDao<MeasurementTaskDetail> measurementTaskDetailBaseDao;
    @Autowired
    private BaseDao<MeasurementItemField> measurementItemFieldBaseDao;
    @Autowired
    private DeviceTypeService deviceSubTypeService;
    @Autowired
    private BaseDao<MeasurementItem> measurementItemBaseDao;
    @Autowired
    private BaseDao<MeasurementUnit> measurementUnitBaseDao;
    @Autowired
    private MeasurementItemService measurementItemService;

    @Override
    public DeviceType getDeviceType(MeasurementItemField measurementItemField) {
        String measurementItemId = measurementItemField.getMeasurementItemId();
        MeasurementItem measurementItem = measurementItemBaseDao.get(MeasurementItem.class, measurementItemId);
        String measurementTemplateId = measurementItem.getMeasurementTemplateId();
        MeasurementTemplate measurementTemplate = measurementTemplateService.getById(measurementTemplateId);
        return deviceSubTypeService.getByDeviceSubTypeId(measurementTemplate.getDeviceSubTypeId());
    }

    @Override
    public void saveOrUpdate(MeasurementItemField pageMeasurementItemField) {
        MeasurementItemField measurementItemField = new MeasurementItemField();
        BeanUtils.copyProperties(pageMeasurementItemField, measurementItemField, "addTime");
        if (measurementItemField.getAddTime() == null)
            measurementItemField.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        measurementItemField.setUpdateTime(new Timestamp(System.currentTimeMillis()));

        measurementItemFieldBaseDao.saveOrUpdate(measurementItemField);
    }

    @Override
    public void saveOrUpdateWithMeasurementItem(MeasurementItemField pageMeasurementItemField, String measurementItemId) {
        Timestamp current = new Timestamp(System.currentTimeMillis());

        if (StringUtils.isEmpty(pageMeasurementItemField.getGroupId()))
            pageMeasurementItemField.setGroupName("");

        // 新增时，每个测量项下都添加一个
        String measurementItemFieldId = pageMeasurementItemField.getId();
        if (StringUtils.isEmpty(measurementItemFieldId)) {
            MeasurementItem measurementItem = measurementItemBaseDao.get(MeasurementItem.class, measurementItemId);
            List<MeasurementItem> measurementItems =
                    measurementItemService.getMeasurementItemsByMeasurementTemplateId(measurementItem.getMeasurementTemplateId());

            String associationCpde = UUID.randomUUID().toString();
            for (MeasurementItem item : measurementItems) {
                MeasurementItemField measurementItemField = new MeasurementItemField();
                BeanUtils.copyProperties(pageMeasurementItemField, measurementItemField);

                measurementItemField.setId(UUID.randomUUID().toString());
                measurementItemField.setMeasurementItemId(item.getId());
                measurementItemField.setMeasurementTemplateId(item.getMeasurementTemplateId());
                measurementItemField.setAssociationCode(associationCpde);
                measurementItemField.setHide(Constant.SHOW);
                measurementItemField.setAddTime(current);
                measurementItemField.setUpdateTime(current);
                measurementItemFieldBaseDao.saveOrUpdate(measurementItemField);
            }
        }
        // 修改时，相同associationCode的字段都要修改
        else {
            String associationCode = pageMeasurementItemField.getAssociationCode();
            List<MeasurementItemField> measurementItemFields = getByAssociationCode(associationCode);
            for (MeasurementItemField measurementItemField : measurementItemFields) {
                BeanUtils.copyProperties(pageMeasurementItemField, measurementItemField, "id", "addTime", "measurementItemId");
                measurementItemField.setUpdateTime(current);
                measurementItemFieldBaseDao.saveOrUpdate(measurementItemField);
            }
        }
    }

    @Override
    public void deleteByMeasurementTemplateId(String measurementTemplateId) {
        measurementItemFieldBaseDao.executeHql(
                "delete from MeasurementItemField where measurementTemplateId = :measurementTemplateId",
                Map.of("measurementTemplateId", measurementTemplateId)
        );
    }

    @Override
    public void delete(String id) {
        measurementItemFieldBaseDao.executeHql(
                "delete from MeasurementItemField where id = :id",
                Map.of("id", id)
        );
    }

    @Override
    public void groupFields(List<String> ids) throws CustomException {
        List<MeasurementItemField> measurementItemFields = measurementItemFieldBaseDao.find(
                "from MeasurementItemField where id in :ids",
                Map.of("ids", ids)
        );
        List<String> associationCodes = StreamUtil.getList(measurementItemFields, MeasurementItemField::getAssociationCode);
        // associationCode一致时认为是同一模板字段
        measurementItemFields = measurementItemFieldBaseDao.find(
                "from MeasurementItemField where associationCode in :associationCodes",
                Map.of("associationCodes", associationCodes)
        );
        String groupId = UUID.randomUUID().toString();

        MeasurementUnit groupMeasurementUnit = measurementUnitBaseDao.get(
                MeasurementUnit.class, measurementItemFields.get(0).getMeasurementUnitId());

        List<MeasurementUnit> measurementUnits = new ArrayList<>();
        for (MeasurementItemField measurementItemField : measurementItemFields) {
            // 分组id一致为同一分组，在app
            measurementItemField.setGroupId(groupId);
            MeasurementUnit measurementUnit = measurementUnitBaseDao.get(MeasurementUnit.class, measurementItemField.getMeasurementUnitId());
            measurementUnits.add(measurementUnit);
            measurementItemField.setGroupName(groupMeasurementUnit.getGroupName());
        }

        if (StreamUtil.getList(measurementUnits, MeasurementUnit::getMainFunctionCode).size() > 1)
            throw new CustomException(new Json(JsonMessage.PARAM_INVALID, "所选分组的主功能码不一致"));

        measurementItemFieldBaseDao.bulkInsert(measurementItemFields);
    }

    @Override
    public List<MeasurementItemField> getFieldsByTemplateId(String measurementTemplateId) {
        return measurementItemFieldBaseDao.find(
                "from MeasurementItemField where measurementTemplateId = :measurementTemplateId order by measurementItemId",
                Map.of("measurementTemplateId", measurementTemplateId)
        );
    }

    @Override
    public Map<String, List<MeasurementItemField>> getFieldsByTemplateIds(List<String> measurementTemplateIds) {
        List<MeasurementItemField> measurementItemFields = measurementItemFieldBaseDao.find(
                "from MeasurementItemField where measurementTemplateId in :measurementTemplateIds order by measurementItemId",
                Map.of("measurementTemplateIds", measurementTemplateIds)
        );

        return StreamUtil.groupList(measurementItemFields, MeasurementItemField::getMeasurementTemplateId);
    }

    @Override
    public List<MeasurementItemField> getFieldsByDetailId(String measurementTaskDetailId) {
        MeasurementTaskDetail measurementTaskDetail = measurementTaskDetailBaseDao.get(MeasurementTaskDetail.class, measurementTaskDetailId);
        String measurementTemplateId = measurementTaskDetail.getMeasurementTemplateId();
        return getFieldsByTemplateId(measurementTemplateId);
    }

    @Override
    public Map<String, List<MeasurementItemField>> getFieldsByMeasurmenetItems(List<MeasurementItem> measurementItems) throws Exception {
        List<String> measurementItemIds = StreamUtil.getList(measurementItems, MeasurementItem::getId);

        List<MeasurementItemField> measurementItemFields = measurementItemFieldBaseDao.find(
                "from MeasurementItemField where measurementItemId in :measurementItemIds",
                Map.of("measurementItemIds", measurementItemIds)
        );

        return measurementItemFields.stream()
                .filter(x -> x.getHide() == Constant.SHOW)
                .collect(Collectors.groupingBy(MeasurementItemField::getMeasurementItemId));
    }

    @Override
    public List<MeasurementItemField> getFieldsByMeasurementItem(String measurementItemId) {
        return measurementItemFieldBaseDao.find(
                "from MeasurementItemField where measurementItemId = :measurementItemId",
                Map.of("measurementItemId", measurementItemId)
        );
    }

    private List<MeasurementItemField> getByAssociationCode(String associationCode) {
        return measurementItemFieldBaseDao.find(
                "from MeasurementItemField where associationCode = :associationCode",
                Map.of("associationCode", associationCode)
        );
    }

    @Override
    public MeasurementItemField getById(String id) {
        return measurementItemFieldBaseDao.get(MeasurementItemField.class, id);
    }
}

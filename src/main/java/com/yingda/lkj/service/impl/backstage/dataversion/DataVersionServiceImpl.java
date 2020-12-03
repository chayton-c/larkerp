package com.yingda.lkj.service.impl.backstage.dataversion;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataVersion;
import com.yingda.lkj.beans.entity.backstage.dataversion.DataVersionUpdateDetail;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.enums.dataversion.ApproveDataType;
import com.yingda.lkj.beans.pojo.approvedata.VersionData;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.dataversion.DataVersionService;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.math.Arithmetic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hood  2020/5/28
 */
@Service("dataVersionService")
public class DataVersionServiceImpl implements DataVersionService {

    @Autowired
    private BaseDao<DataVersion> dataVersionBaseDao;
    @Autowired
    private BaseDao<DataVersionUpdateDetail> dataVersionUpdateDetailBaseDao;

    @Override
    public DataVersion getById(String id) {
        return dataVersionBaseDao.get(DataVersion.class, id);
    }

    @Override
    public void createStableDataVersion(String dataVersionId, String versionName) {
        DataVersion dataVersion = getById(dataVersionId);
        dataVersion.setName(versionName);
        dataVersion.setType(DataVersion.STABLE_VERSION);
        dataVersion.setUpdateTime(new Timestamp(System.currentTimeMillis()));

        dataVersionBaseDao.saveOrUpdate(dataVersion);
    }

    @Override
    public void createInterimDataVersion(
            String sectionId, ApproveDataType approveDataType, List<? extends VersionData> currentDataList, List<? extends VersionData> previousDataList) {
        double previousVerionNumber = getPreviousVerionNumber(approveDataType, sectionId); // 上一个版本号
        double currentVersionNumber = Arithmetic.add(previousVerionNumber, 1.0); // + 1生成新版本号

        // 生成版本实体
        DataVersion dataVersion = new DataVersion(sectionId, approveDataType.getDataTypeId(), currentVersionNumber);
        dataVersionBaseDao.saveOrUpdate(dataVersion);

        // key: uniqueCode value:更新后数据
        Map<String, VersionData> currentDataMap = StreamUtil.getMap(currentDataList, VersionData::getUniqueKey, x -> x);

        // key: uniqueCode value:更新前数据
        Map<String, VersionData> previousDataMap = StreamUtil.getMap(previousDataList, VersionData::getUniqueKey, x -> x);

        // 提交操作记录
        List<DataVersionUpdateDetail> dataVersionUpdateDetails = new ArrayList<>();
        for (Map.Entry<String, VersionData> currentVersionDataEntry : currentDataMap.entrySet()) {
            String uniqueKey = currentVersionDataEntry.getKey();

            VersionData currentVersionData = currentVersionDataEntry.getValue();
            VersionData previousVersionData = previousDataMap.get(uniqueKey);

            DataVersionUpdateDetail dataVersionUpdateDetail = new DataVersionUpdateDetail(dataVersion, currentVersionData, previousVersionData);
            dataVersionUpdateDetails.add(dataVersionUpdateDetail);

            previousDataMap.remove(uniqueKey);
        }

        for (Map.Entry<String, VersionData> previousVersionDataEntry : previousDataMap.entrySet()) {
            String uniqueKey = previousVersionDataEntry.getKey();

            VersionData previousVersionData = previousVersionDataEntry.getValue();
            VersionData currentVersionData = previousDataMap.get(uniqueKey);

            DataVersionUpdateDetail dataVersionUpdateDetail = new DataVersionUpdateDetail(dataVersion, currentVersionData, previousVersionData);
            dataVersionUpdateDetails.add(dataVersionUpdateDetail);
        }

        dataVersionUpdateDetailBaseDao.bulkInsert(dataVersionUpdateDetails);

        // 更新数据的版本信息
        double versionNumber = dataVersion.getVersionNumber();
        String versionId = dataVersion.getId();
        List<VersionData> versionData = new ArrayList<>();
        for (VersionData currentData : currentDataList) {
            currentData.setDataVersionNumber(versionNumber);
            currentData.setDataVersionId(versionId);
            versionData.add(currentData);
        }
        versionDataBaseDao.bulkInsert(versionData);
    }

    @Autowired
    private BaseDao<VersionData> versionDataBaseDao;

    @Override
    public List<DataVersion> getAllVersions(ApproveDataType approveDataType, String sectionId) {
        String dataTypeId = approveDataType.getDataTypeId();
        return dataVersionBaseDao.find(
                "from DataVersion where sectionId = :sectionId and dataTypeId = :dataTypeId order by updateTime desc",
                Map.of("sectionId", sectionId, "dataTypeId", dataTypeId)
        );
    }

    @Override
    public List<DataVersionUpdateDetail> compare(ApproveDataType approveDataType, String sectionId, String currentVersionId, String previousVersionId) {
        DataVersion currentVersion = getById(currentVersionId);
        DataVersion previousVersion = getById(previousVersionId);

        return dataVersionUpdateDetailBaseDao.find(
                "from DataVersionUpdateDetail " +
                        "where dataVersionNumber <= :currentVersionNumber " +
                        "and dataVersionNumber > :previousVersionNumber " +
                        "and sectionId = :sectionId " +
                        "and dataTypeId = :dataTypeId",
                Map.of("currentVersionNumber", currentVersion.getVersionNumber(),
                        "previousVersionNumber", previousVersion.getVersionNumber(),
                        "sectionId", sectionId,
                        "dataTypeId", approveDataType.getDataTypeId())
        );
    }

    @Override
    public List<String> getOutdatedDataIds(ApproveDataType approveDataType, String sectionId, double versionNumber) {
        List<DataVersionUpdateDetail> dataVersionUpdateDetails = dataVersionUpdateDetailBaseDao.find(
                "from DataVersionUpdateDetail where " +
                        "dataTypeId = :dataTypeId " +
                        "and dataVersionNumber <= :dataVersionNumber " +
                        "and previousDataId is not null " +
                        "and sectionId = :sectionId",
                Map.of("dataTypeId", approveDataType.getDataTypeId(), "dataVersionNumber", versionNumber, "sectionId", sectionId)
        );

        return StreamUtil.getList(dataVersionUpdateDetails, DataVersionUpdateDetail::getPreviousDataId);
    }

    private double getPreviousVerionNumber(ApproveDataType approveDataType, String sectionId) {
        String dataTypeId = approveDataType.getDataTypeId();
        List<DataVersion> dataVersions = dataVersionBaseDao.find(
                "from DataVersion where sectionId = :sectionId and dataTypeId = :dataTypeId order by versionNumber desc",
                Map.of("sectionId", sectionId, "dataTypeId", dataTypeId),
                1, 1
        );

        if (dataVersions.isEmpty())
            return 0;

        return dataVersions.get(0).getVersionNumber();
    }
}

package com.yingda.lkj.service.backstage.dataversion;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataVersion;
import com.yingda.lkj.beans.entity.backstage.dataversion.DataVersionUpdateDetail;
import com.yingda.lkj.beans.enums.dataversion.ApproveDataType;
import com.yingda.lkj.beans.pojo.approvedata.VersionData;

import java.util.List;

/**
 * @author hood  2020/5/28
 */
public interface DataVersionService {

    DataVersion getById(String id);

    void createStableDataVersion(String dataVersionId, String versionName);

    /**
     * 生成临时数据版本
     */
    void createInterimDataVersion(
            String sectionId, ApproveDataType approveDataType, List<? extends VersionData> currentDataList, List<? extends VersionData> previousDataList);

    List<DataVersion> getAllVersions(ApproveDataType approveDataType, String sectionId);

    /**
     * 获取版本更新数据
     */
    List<DataVersionUpdateDetail> compare(ApproveDataType approveDataType, String sectionId, String currentVersionId, String previousVersionId);

    /**
     * 查询过时的数据，用于筛选掉旧数据
     * @param approveDataType 数据类型
     * @param sectionId 数据所在站段id
     * @param versionNumber 查询这个版本号之前的旧数据
     */
    List<String> getOutdatedDataIds(ApproveDataType approveDataType, String sectionId, double versionNumber);
}

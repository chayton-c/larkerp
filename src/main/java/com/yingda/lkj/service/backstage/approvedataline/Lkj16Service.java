package com.yingda.lkj.service.backstage.approvedataline;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveFlow;
import com.yingda.lkj.beans.entity.backstage.dataversion.DataVersion;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.entity.backstage.lkj.lkjextends.Lkj16;

import java.util.List;

/**
 * @author hood  2020/5/29
 */
public interface Lkj16Service {

    /**
     * 生成待审批的lkjDataLine
     */
    List<Lkj16> createLkjDataLine(DataApproveFlow dataApproveFlow, List<Lkj16> rawLkjDataLines);

    /**
     * 修改dataApproveFlow下的数据为未通过
     */
    void refuseLkjDataLines(DataApproveFlow dataApproveFlow);

    /**
     * 提交审批流下的数据为已完成
     */
    void completeLkjDataLine(DataApproveFlow dataApproveFlow);

    /**
     * 更新版本后，对数据打上版本信息
     */
    void setVersionData(DataVersion dataVersion, List<Lkj16> lkjDataLines);
}

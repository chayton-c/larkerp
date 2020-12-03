package com.yingda.lkj.service.impl.backstage.approvedataline;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveFlow;
import com.yingda.lkj.beans.entity.backstage.dataversion.DataVersion;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.entity.backstage.lkj.lkjextends.Lkj16;
import com.yingda.lkj.beans.enums.dataversion.ApproveDataType;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.approvedataline.Lkj16Service;
import com.yingda.lkj.service.backstage.dataversion.DataVersionService;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author hood  2020/5/29
 */
@Service("lkj16Service")
public class Lkj16ServiceImpl implements Lkj16Service {
    @Autowired
    private BaseDao<Lkj16> lkj16BaseDao;
    @Autowired
    private DataVersionService dataVersionService;

    @Override
    public List<Lkj16> createLkjDataLine(DataApproveFlow dataApproveFlow, List<Lkj16> rawLkjDataLines) {
        // 目前没有导入，rawLkjDataLines.size()只可能为1
        List<Lkj16> lkj16s = new ArrayList<>();
        for (Lkj16 rawLkj16 : rawLkjDataLines)
            lkj16s.add(new Lkj16(rawLkj16, dataApproveFlow));

        lkj16BaseDao.bulkInsert(lkj16s);
        return lkj16s;
    }

    @Override
    public void refuseLkjDataLines(DataApproveFlow dataApproveFlow) {

    }

    @Override
    public void completeLkjDataLine(DataApproveFlow dataApproveFlow) {
        Timestamp current = new Timestamp(System.currentTimeMillis());
        String rawDataApproveFlowId = dataApproveFlow.getId();

        // 这个审批下的数据
        List<Lkj16> lkj16s = lkj16BaseDao.find(
                "from Lkj16 where dataApproveFlowId = :dataApproveFlowId",
                Map.of("dataApproveFlowId", rawDataApproveFlowId)
        );

        // 找到唯一键
        List<String> uniqueKeys = lkj16s.stream().map(Lkj16::getUniqueKey).distinct().collect(Collectors.toList());

        // 根据唯一键查询之前的数据
        List<Lkj16> theOutdated = lkj16BaseDao.find(
                "from Lkj16 where uniqueKey in :uniqueKeys and outdated = :outdated and approveStatus = :approveStatus",
                Map.of("uniqueKeys", uniqueKeys, "outdated", LkjDataLine.USING, "approveStatus", LkjDataLine.APPROVED)
        );

        // 旧数据修改已过时
        for (Lkj16 lkj16 : theOutdated) {
            lkj16.setUpdateTime(current);
            lkj16.setOutdated(Lkj16.OUTDATED);
        }

        // 新数据修改为通过，正在使用
        for (Lkj16 lkj16 : lkj16s) {
            lkj16.setUpdateTime(current);
            lkj16.setApproveStatus(Lkj16.APPROVED);
            lkj16.setOutdated(Lkj16.USING);
        }
        lkj16BaseDao.bulkInsert(lkj16s);

        // 生成临时版本(dataVersion)
        dataVersionService.createInterimDataVersion(
                dataApproveFlow.getSectionId(), ApproveDataType.LKJ16, lkj16s, theOutdated);
    }

    @Override
    public void setVersionData(DataVersion dataVersion, List<Lkj16> lkjDataLines) {
    }
}

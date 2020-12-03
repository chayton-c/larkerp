package com.yingda.lkj.service.impl.backstage.approvedataline;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveFlow;
import com.yingda.lkj.beans.entity.backstage.dataversion.DataVersion;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.entity.backstage.lkj.lkjextends.Lkj20;
import com.yingda.lkj.beans.enums.dataversion.ApproveDataType;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.approvedataline.Lkj20Service;
import com.yingda.lkj.service.backstage.dataversion.DataVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hood  2020/6/15
 */
@Service("lkj20Service")
public class Lkj20ServiceImpl implements Lkj20Service {
    @Autowired
    private BaseDao<Lkj20> lkj20BaseDao;
    @Autowired
    private DataVersionService dataVersionService;

    @Override
    public List<Lkj20> createLkjDataLine(DataApproveFlow dataApproveFlow, List<Lkj20> rawLkjDataLines) {
        // 目前没有导入，rawLkjDataLines.size()只可能为1
        List<Lkj20> lkj20s = new ArrayList<>();
        for (Lkj20 rawLkj20 : rawLkjDataLines)
            lkj20s.add(new Lkj20(rawLkj20, dataApproveFlow));

        lkj20BaseDao.bulkInsert(lkj20s);
        return lkj20s;
    }

    @Override
    public void refuseLkjDataLines(DataApproveFlow dataApproveFlow) {

    }

    @Override
    public void completeLkjDataLine(DataApproveFlow dataApproveFlow) {
        Timestamp current = new Timestamp(System.currentTimeMillis());
        String rawDataApproveFlowId = dataApproveFlow.getId();

        // 这个审批下的数据
        List<Lkj20> lkj20s = lkj20BaseDao.find(
                "from Lkj20 where dataApproveFlowId = :dataApproveFlowId",
                Map.of("dataApproveFlowId", rawDataApproveFlowId)
        );

        // 找到唯一键
        List<String> uniqueKeys = lkj20s.stream().map(Lkj20::getUniqueKey).distinct().collect(Collectors.toList());

        // 根据唯一键查询之前的数据
        List<Lkj20> theOutdated = lkj20BaseDao.find(
                "from Lkj20 where uniqueKey in :uniqueKeys and outdated = :outdated and approveStatus = :approveStatus",
                Map.of("uniqueKeys", uniqueKeys, "outdated", LkjDataLine.USING, "approveStatus", LkjDataLine.APPROVED)
        );

        // 旧数据修改已过时
        for (Lkj20 lkj20 : theOutdated) {
            lkj20.setUpdateTime(current);
            lkj20.setOutdated(Lkj20.OUTDATED);
        }

        // 新数据修改为通过，正在使用
        for (Lkj20 lkj20 : lkj20s) {
            lkj20.setUpdateTime(current);
            lkj20.setApproveStatus(Lkj20.APPROVED);
            lkj20.setOutdated(Lkj20.USING);
        }
        lkj20BaseDao.bulkInsert(lkj20s);

        // 生成临时版本(dataVersion)
        dataVersionService.createInterimDataVersion(
                dataApproveFlow.getSectionId(), ApproveDataType.LKJ20, lkj20s, theOutdated);
    }

    @Override
    public void setVersionData(DataVersion dataVersion, List<Lkj20> lkjDataLines) {

    }
}

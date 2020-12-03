package com.yingda.lkj.service.impl.backstage.approvedataline;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveFlow;
import com.yingda.lkj.beans.entity.backstage.dataversion.DataVersion;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.entity.backstage.lkj.lkjextends.Lkj16;
import com.yingda.lkj.beans.entity.backstage.lkj.lkjextends.Lkj18;
import com.yingda.lkj.beans.enums.dataversion.ApproveDataType;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.approvedataline.Lkj18Service;
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
@Service("lkj18Service")
public class Lkj18ServiceImpl implements Lkj18Service {
    @Autowired
    private BaseDao<Lkj18> lkj18BaseDao;
    @Autowired
    private DataVersionService dataVersionService;

    @Override
    public List<Lkj18> createLkjDataLine(DataApproveFlow dataApproveFlow, List<Lkj18> rawLkjDataLines) {
        // 目前没有导入，rawLkjDataLines.size()只可能为1
        List<Lkj18> lkj18s = new ArrayList<>();
        for (Lkj18 rawLkj18 : rawLkjDataLines)
            lkj18s.add(new Lkj18(rawLkj18, dataApproveFlow));

        lkj18BaseDao.bulkInsert(lkj18s);
        return lkj18s;
    }

    @Override
    public void refuseLkjDataLines(DataApproveFlow dataApproveFlow) {

    }

    @Override
    public void completeLkjDataLine(DataApproveFlow dataApproveFlow) {
        Timestamp current = new Timestamp(System.currentTimeMillis());
        String rawDataApproveFlowId = dataApproveFlow.getId();

        // 这个审批下的数据
        List<Lkj18> lkj18s = lkj18BaseDao.find(
                "from Lkj18 where dataApproveFlowId = :dataApproveFlowId",
                Map.of("dataApproveFlowId", rawDataApproveFlowId)
        );

        // 找到唯一键
        List<String> uniqueKeys = lkj18s.stream().map(Lkj18::getUniqueKey).distinct().collect(Collectors.toList());

        // 根据唯一键查询之前的数据
        List<Lkj18> theOutdated = lkj18BaseDao.find(
                "from Lkj18 where uniqueKey in :uniqueKeys and outdated = :outdated and approveStatus = :approveStatus",
                Map.of("uniqueKeys", uniqueKeys, "outdated", LkjDataLine.USING, "approveStatus", LkjDataLine.APPROVED)
        );

        // 旧数据修改已过时
        for (Lkj18 lkj18 : theOutdated) {
            lkj18.setUpdateTime(current);
            lkj18.setOutdated(Lkj16.OUTDATED);
        }

        // 新数据修改为通过，正在使用
        for (Lkj18 lkj18 : lkj18s) {
            lkj18.setUpdateTime(current);
            lkj18.setApproveStatus(Lkj16.APPROVED);
            lkj18.setOutdated(Lkj16.USING);
        }
        lkj18BaseDao.bulkInsert(lkj18s);

        // 生成临时版本(dataVersion)
        dataVersionService.createInterimDataVersion(
                dataApproveFlow.getSectionId(), ApproveDataType.LKJ18, lkj18s, theOutdated);
    }

    @Override
    public void setVersionData(DataVersion dataVersion, List<Lkj18> lkjDataLines) {

    }
}

package com.yingda.lkj.service.impl.backstage.lkjapprove;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveFlow;
import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveNode;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.enums.dataversion.ApproveDataType;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.lkjapprove.LkjApproveFlowService;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/1/9
 */
@Service("lkjApproveFlowService")
public class LkjApproveFlowServiceImpl implements LkjApproveFlowService {

    @Autowired
    private BaseDao<DataApproveFlow> dataApproveFlowBaseDao;
    @Autowired
    private BaseDao<DataApproveNode> dataApproveNodeBaseDao;
    @Autowired
    private BaseDao<LkjDataLine> lkjDataLineBaseDao;

    @Override
    public DataApproveFlow createDataApproveFlowByLkjTask(
            String lkjTaskId, DataApproveFlow rawDataApproveFlow, User submitter, String approveUserId) {
        String submitterId = submitter.getId();
        if (StringUtils.isEmpty(approveUserId)) // 如果没有上级(段长提交的？)，那么审批人为自己
            approveUserId = submitterId;

        // 1.生成审批流数据(data_approve_flow)
        DataApproveFlow dataApproveFlow = new DataApproveFlow(rawDataApproveFlow, submitter, approveUserId, ApproveDataType.LKJ14);
        dataApproveFlowBaseDao.saveOrUpdate(dataApproveFlow);

        // 2.生成两个审批节点(data_approve_node)：提交人的审批节点和下一级审批人的审批节点
        DataApproveNode submitNode = new DataApproveNode(dataApproveFlow, submitterId);
        submitNode.setUserApproveStatus(DataApproveNode.APPROVED);
        dataApproveNodeBaseDao.saveOrUpdate(submitNode);

        // 生成下一级审批节点，如果下级节点对应用户与提交人相同，不会再生成一级
        if (!approveUserId.equals(submitterId)) {
            DataApproveNode nextNode = new DataApproveNode(dataApproveFlow, approveUserId);
            nextNode.setPreviousNodeId(submitNode.getId());
            nextNode.setUserApproveStatus(DataApproveNode.PENDING_REVIEW);
            dataApproveNodeBaseDao.saveOrUpdate(nextNode);
        }

        List<LkjDataLine> lkjDataLines = lkjDataLineBaseDao.find(
                "from LkjDataLine where lkjTaskId = :lkjTaskId",
                Map.of("lkjTaskId", lkjTaskId)
        );
        lkjDataLines.forEach(x -> x.setDataApproveFlowId(dataApproveFlow.getId()));
        lkjDataLineBaseDao.bulkInsert(lkjDataLines);

        return dataApproveFlow;
    }
}

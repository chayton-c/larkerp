package com.yingda.lkj.service.impl.backstage.dataapprove;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveFlow;
import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveNode;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.enums.dataversion.ApproveDataType;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.dataapprove.DataApproveFlowService;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/5/26
 */
@Service("dataApproveFlowService")
public class DataApproveFlowServiceImpl implements DataApproveFlowService {

    @Autowired
    private BaseDao<DataApproveFlow> dataApproveFlowBaseDao;
    @Autowired
    private BaseDao<DataApproveNode> dataApproveNodeBaseDao;

    @Override
    public DataApproveFlow createDataApproveFlow(
            DataApproveFlow rawDataApproveFlow, User submitter, String approveUserId, List<?> rawDataLines, ApproveDataType approveDataType)
    {
        String submitterId = submitter.getId();
        if (StringUtils.isEmpty(approveUserId)) // 如果没有上级(段长提交的？)，那么审批人为自己
            approveUserId = submitterId;

        // 1.生成审批流数据(data_approve_flow)
        DataApproveFlow dataApproveFlow = new DataApproveFlow(rawDataApproveFlow, submitter, approveUserId, approveDataType);
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

        // 3.生成审批数据
        approveDataType.createDataLines(dataApproveFlow, rawDataLines);

        return dataApproveFlow;
    }

    @Override
    public void approveDataApproveNode(
            User approveUser, String nextApproveUserId, DataApproveFlow pageDataApproveFlow, DataApproveNode pageDataApproveNode) {
        Timestamp current = new Timestamp(System.currentTimeMillis());
        String rawDataApproveFlowId = pageDataApproveFlow.getId();
        DataApproveFlow dataApproveFlow = dataApproveFlowBaseDao.get(DataApproveFlow.class, rawDataApproveFlowId);
        String approveUserId = approveUser.getId();
        if (StringUtils.isEmpty(nextApproveUserId)) // 如果没有上级(段长提交的？)，那么下级审批人为自己
            nextApproveUserId = approveUserId;

        // 1.修改审批流数据(data_approve_flow)的当前审批人(currentReviewerId)
        dataApproveFlow.setCurrentReviewerId(nextApproveUserId);
        dataApproveFlow.setUpdateTime(current);
        dataApproveFlow.setRemark(pageDataApproveFlow.getRemark());
        dataApproveFlowBaseDao.saveOrUpdate(dataApproveFlow);

        // 2.通过用户所在审批节点(data_approve_flow)
        DataApproveNode currentNode = dataApproveNodeBaseDao.get(
                "from DataApproveNode where approveUserId = :approveUserId and dataApproveFlowId = :dataApproveFlowId",
                Map.of("approveUserId", approveUserId, "dataApproveFlowId", rawDataApproveFlowId)
        );
        currentNode.setUserApproveStatus(DataApproveNode.APPROVED);
        currentNode.setUpdateTime(current);
        currentNode.setApproveOpinion(pageDataApproveNode.getApproveOpinion());
        dataApproveNodeBaseDao.saveOrUpdate(currentNode);

        // 生成下一级审批节点，如果下级节点对应用户与提交人相同，不会再生成一级
        if (!nextApproveUserId.equals(approveUserId)) {
            DataApproveNode nextNode = new DataApproveNode(dataApproveFlow, nextApproveUserId);
            nextNode.setUserApproveStatus(DataApproveNode.PENDING_REVIEW);
            nextNode.setPreviousNodeId(currentNode.getId());
            dataApproveNodeBaseDao.saveOrUpdate(nextNode);
        }
    }

    @Override
    public void completeDataApproveFlow(String rawDataApproveFlowId, ApproveDataType approveDataType) {
        DataApproveFlow dataApproveFlow = dataApproveFlowBaseDao.get(DataApproveFlow.class, rawDataApproveFlowId);

        // 修改审批流数据(lkj_approve_flow)为已完成
        dataApproveFlow.setApproveStatus(DataApproveFlow.APPROVED);
        dataApproveFlowBaseDao.saveOrUpdate(dataApproveFlow);

        // 提交审批流下的数据为已完成
        approveDataType.completeDataLines(dataApproveFlow);
    }

    @Override
    public void refuseDataApproveNode(
            User approveUser, String nextApproveUserId, DataApproveFlow pageDataApproveFlow, DataApproveNode pageDataApproveNode) {
        Timestamp current = new Timestamp(System.currentTimeMillis());
        String rawDataApproveFlowId = pageDataApproveFlow.getId();
        DataApproveFlow dataApproveFlow = dataApproveFlowBaseDao.get(DataApproveFlow.class, rawDataApproveFlowId);

        String approveUserId = approveUser.getId();
        if (StringUtils.isEmpty(nextApproveUserId)) // 如果没有上级(段长提交的？)，那么下级审批人为自己
            nextApproveUserId = approveUserId;

        // 1.修改审批流数据(data_approve_flow)的当前审批人(currentReviewer()
        dataApproveFlow.setCurrentReviewerId(nextApproveUserId);
        dataApproveFlow.setUpdateTime(current);
        dataApproveFlow.setRemark(pageDataApproveFlow.getRemark());
        dataApproveFlowBaseDao.saveOrUpdate(dataApproveFlow);

        // 2.拒绝通过用户所在审批节点(data_approve_flow)
        DataApproveNode currentNode = dataApproveNodeBaseDao.get(
                "from DataApproveNode where approveUserId = :approveUserId and dataApproveFlowId = :dataApproveFlowId",
                Map.of("approveUserId", approveUserId, "dataApproveFlowId", rawDataApproveFlowId)
        );
        currentNode.setUserApproveStatus(DataApproveNode.FAILED);
        currentNode.setUpdateTime(current);
        currentNode.setApproveOpinion(pageDataApproveNode.getApproveOpinion());
        dataApproveNodeBaseDao.saveOrUpdate(currentNode);

        // 生成下一级审批节点，如果下级节点对应用户与提交人相同，不会再生成一级
        if (!nextApproveUserId.equals(approveUserId)) {
            DataApproveNode nextNode = new DataApproveNode(dataApproveFlow, nextApproveUserId);
            nextNode.setUserApproveStatus(DataApproveNode.PENDING_REVIEW);
            nextNode.setPreviousNodeId(currentNode.getId());
            dataApproveNodeBaseDao.saveOrUpdate(nextNode);
        }
    }

    @Override
    public void refuseDataApproveFlow(DataApproveFlow rawDataApproveFlow, ApproveDataType approveDataType) {
        String rawDataApproveFlowId = rawDataApproveFlow.getId();
        DataApproveFlow dataApproveFlow = dataApproveFlowBaseDao.get(DataApproveFlow.class, rawDataApproveFlowId);

        // 修改审批流数据(data_approve_flow)为未通过
        dataApproveFlow.setApproveStatus(DataApproveFlow.FAILED);
        dataApproveFlowBaseDao.saveOrUpdate(dataApproveFlow);

        // 数据修改为未通过
        approveDataType.refuseDataLines(dataApproveFlow);
    }
}

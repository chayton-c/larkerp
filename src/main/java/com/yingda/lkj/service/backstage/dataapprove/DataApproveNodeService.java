package com.yingda.lkj.service.backstage.dataapprove;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveNode;

import java.util.List;

/**
 * @author hood  2020/6/3
 */
public interface DataApproveNodeService {
    /**
     * 获取当前用户的审批过/待审批的审批节点
     */
    List<DataApproveNode> getDataApproveNodes(String userId);

    DataApproveNode getDataApproveNode(String userId, String dataApproveFlowId);
}

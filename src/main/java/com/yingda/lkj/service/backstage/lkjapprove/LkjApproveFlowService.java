package com.yingda.lkj.service.backstage.lkjapprove;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveFlow;
import com.yingda.lkj.beans.entity.system.User;

/**
 * @author hood  2020/1/9
 */
public interface LkjApproveFlowService {

    /**
     *
     * <p>通过任务，发起审核接口</p>
     * <p>注意：不会生成新的lkj数据，因为生成任务时已经有了</p>
     * <p>1.生成审批流数据(data_approve_flow)</p>
     * <p>2.生成两个审批节点(data_approve_node)：提交人的审批节点和下一级审批人的审批节点</p>
     * <p>2.生成两个审批节点(data_approve_node)：提交人的审批节点和下一级审批人的审批节点</p>
     * <p>3.修改任务对应的lkj数据(lkj_data_line)：setDataApproveFlowId为新生村的审批流数据</p>
     *
     * @param rawLkjApproveFlow 未处理的LkjApproveFlow，参数要求看 LkjApproveFlow的有参构造
     * @param submitter 提交人
     * @param approveUserId 下级审核人id
     * @return 持久化后的审批流主表
     */
    DataApproveFlow createDataApproveFlowByLkjTask(String lkjTaskId, DataApproveFlow rawLkjApproveFlow, User submitter, String approveUserId);

}

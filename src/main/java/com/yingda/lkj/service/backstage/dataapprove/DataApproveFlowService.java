package com.yingda.lkj.service.backstage.dataapprove;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveFlow;
import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveNode;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.enums.dataversion.ApproveDataType;
import com.yingda.lkj.beans.exception.CustomException;

import java.util.List;

/**
 * @author hood  2020/5/26
 */
public interface DataApproveFlowService {

    /**
     *
     * <p>发起审核接口</p>
     * <p>1.生成审批流数据(data_approve_flow)</p>
     * <p>2.生成两个审批节点(data_approve_node)：提交人的审批节点和下一级审批人的审批节点</p>
     * <p>如果approveUserId为空，那么只生成一级</p>
     * <p>3.生成dataclass对应的数据(lkj_line_data)</p>
     *
     * @param rawDataApproveFlow 未处理的LkjApproveFlow，参数要求看 LkjApproveFlow的有参构造
     * @param submitter 提交人
     * @param approveUserId 下级审核人id
     * @param rawDataLines 未处理的LkjDataLine，参数要求看 LkjDataLine的有参构造
     * @return 持久化后的审批流主表
     */
    DataApproveFlow createDataApproveFlow(
            DataApproveFlow rawDataApproveFlow, User submitter, String approveUserId, List<?> rawDataLines, ApproveDataType approveDataType);

    /**
     * <p>通过审核节点接口</p>
     * <p>1.修改审批流数据(data_approve_flow)的当前审批人(currentReviewer()</p>
     * <p>2.通过用户所在审批节点(data_approve_flow)</p>
     * <p>3.生成新的审批节点(data_approve_node)：下一级审批人的审批节点</p>
     * <p>如果approveUserId为空，那么不生成新的审批节点</p>
     *
     * @param approveUser 当前审核人
     * @param nextApproveUserId 下级审核人id
     * @param pageDataApproveFlow 页面传过来的审批信息
     * @param pageDataApproveNode 页面传过来的节点信息
     */
    void approveDataApproveNode(
            User approveUser, String nextApproveUserId, DataApproveFlow pageDataApproveFlow, DataApproveNode pageDataApproveNode);

    /**
     * <p>如果没有下一级用户，完成审批</p>
     * <p>1.修改审批流数据(data_approve_flow)为已完成</p>
     * <p>2.修改旧的审批流数据为已过时，提交新的审批流数据</p>
     * <p>单独对应lkj_data_line进路数据，在ApproveDataType方法中：</p>
     * <p>3.如果审批的数据中对应任务(lkj_task)，修改任务为已完成</p>
     * <p>4.生成lkj版本(lkjVersion)</p>
     */
    void completeDataApproveFlow(String rawDataApproveFlowId, ApproveDataType approveDataType) throws CustomException;


    /**
     * <p>拒绝审核节点接口</p>
     * <p>1.修改审批流数据(data_approve_flow)的当前审批人(currentReviewer()</p>
     * <p>2.通过用户所在审批节点(data_approve_flow)</p>
     * <p>3.生成新的审批节点(data_approve_node)：下一级审批人的审批节点</p>
     * <p>如果approveUserId为空，那么不生成</p>
     *
     * @param approveUser 当前审核人
     * @param nextApproveUserId 下级审核人id
     * @param pageDataApproveFlow 页面传过来的审批信息
     * @param pageDataApproveNode 页面传过来的节点信息
     *
     */
    void refuseDataApproveNode(
            User approveUser, String nextApproveUserId, DataApproveFlow pageDataApproveFlow, DataApproveNode pageDataApproveNode);

    /**
     * <p>拒绝审批流接口，不同于refuseDataApproveNode这个接口用来最终结束审批流</p>
     * <p>1.修改审批流数据(lkj_approve_flow)为未通过</p>
     * <p>2.数据提交为未通过</p>
     *
     */
    void refuseDataApproveFlow(DataApproveFlow rawDataApproveFlow, ApproveDataType approveDataType);
}


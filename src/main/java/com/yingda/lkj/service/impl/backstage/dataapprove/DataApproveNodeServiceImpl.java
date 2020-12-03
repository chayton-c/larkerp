package com.yingda.lkj.service.impl.backstage.dataapprove;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveNode;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.dataapprove.DataApproveNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/6/3
 */
@Service("dataApproveNodeService")
public class DataApproveNodeServiceImpl implements DataApproveNodeService {

    @Autowired
    private BaseDao<DataApproveNode> dataApproveNodeBaseDao;

    @Override
    public List<DataApproveNode> getDataApproveNodes(String userId) {
        return dataApproveNodeBaseDao.find(
                "from DataApproveNode where approveUserId = :userId or previousNodeId = :userId",
                Map.of("userId", userId)
        );
    }

    @Override
    public DataApproveNode getDataApproveNode(String userId, String dataApproveFlowId) {
        return dataApproveNodeBaseDao.get(
                "from DataApproveNode where approveUserId = :userId and dataApproveFlowId = :dataApproveFlowId",
                Map.of("userId", userId, "dataApproveFlowId", dataApproveFlowId)
        );
    }
}

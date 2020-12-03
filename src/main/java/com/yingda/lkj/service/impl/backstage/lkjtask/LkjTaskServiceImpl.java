package com.yingda.lkj.service.impl.backstage.lkjtask;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveFlow;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjTask;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.pojo.lkj.lkjtask.UserLkjTask;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.lkjapprove.LkjApproveFlowService;
import com.yingda.lkj.service.backstage.lkjdataline.LkjDataLineService;
import com.yingda.lkj.service.backstage.lkjtask.LkjTaskService;
import com.yingda.lkj.service.base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/3/1
 */
@Service("lkjTaskService")
public class LkjTaskServiceImpl implements LkjTaskService {

    @Autowired
    private BaseDao<LkjTask> lkjTaskBaseDao;
    @Autowired
    private BaseService<LkjDataLine> lkjDataLineBaseService;
    @Autowired
    private LkjDataLineService lkjDataLineService;
    @Autowired
    private LkjApproveFlowService lkjApproveFlowService;

    @Override
    public LkjTask createLkjTask(LkjTask rawLkjTask, User submitter, User executer, List<LkjDataLine> rawLkjDataLines) {
        // 1.生成任务(lkj_approve_flow)
        LkjTask lkjTask = new LkjTask(rawLkjTask, submitter, executer);
        lkjTaskBaseDao.saveOrUpdate(lkjTask);

        // 2.生成lkj数据(lkj_data_line)
        rawLkjDataLines.forEach(x -> x.setDistance(0)); // 因为可能用有距离的excel导入，要提前将距离设置为0
        lkjDataLineService.createLkjDataLine(lkjTask, rawLkjDataLines);

        return lkjTask;
    }

    @Override
    public LkjTask createLkjTask(LkjTask rawLkjTask, User submitter, User executer) {
        LkjTask lkjTask = new LkjTask(rawLkjTask, submitter, executer);
        lkjTaskBaseDao.saveOrUpdate(lkjTask);
        return lkjTask;
    }

    @Override
    public DataApproveFlow submitLkjTask(String lkjTaskId, DataApproveFlow rawLkjApproveFlow, User submitter, String approveUserId) {
        DataApproveFlow dataApproveFlowByLkjTask = lkjApproveFlowService.createDataApproveFlowByLkjTask(lkjTaskId, rawLkjApproveFlow, submitter, approveUserId);

        LkjTask lkjTask = lkjTaskBaseDao.get(LkjTask.class, lkjTaskId);
        lkjTask.setFinishedStatus(LkjTask.PENDING_APPROVAL);
        lkjTaskBaseDao.saveOrUpdate(lkjTask);

        return dataApproveFlowByLkjTask;
    }

    @Override
    public void completeLkjTasks(List<String> lkjTaskIds) {
        List<LkjTask> lkjTasks = lkjTaskBaseDao.find("from LkjTask where id in :lkjTaskIds", Map.of("lkjTaskIds", lkjTaskIds));
        lkjTasks.forEach(x -> x.setFinishedStatus(LkjTask.COMPLETED));
        lkjTaskBaseDao.bulkInsert(lkjTasks);
    }

    @Override
    public void refuseLkjTasks(List<String> lkjTaskIds) {
        List<LkjTask> lkjTasks = lkjTaskBaseDao.find("from LkjTask where id in :lkjTaskIds", Map.of("lkjTaskIds", lkjTaskIds));
        lkjTasks.forEach(x -> x.setFinishedStatus(LkjTask.REFUSED));
        lkjTaskBaseDao.bulkInsert(lkjTasks);
    }

    @Override
    public UserLkjTask userTaskInfo(String userId) {
        UserLkjTask userLkjTask = new UserLkjTask();
        List<LkjTask> lkjTasks = lkjTaskBaseDao.find(
                "from LkjTask where executeUserId = :executeUserId",
                Map.of("executeUserId", userId)
        );

        List<LkjTask> pendingHandleTasks = new ArrayList<>();
        List<LkjTask> completeTasks = new ArrayList<>();
        List<LkjTask> refusedTasks = new ArrayList<>();
        List<LkjTask> closedTasks = new ArrayList<>();
        for (LkjTask lkjTask : lkjTasks) {
            // 任务完成状态
            byte finishedStatus = lkjTask.getFinishedStatus();

            switch (finishedStatus) {
                case LkjTask.PENDING_HANDLE:
                case LkjTask.PENDING_APPROVAL:
                    pendingHandleTasks.add(lkjTask);
                    break;
                case LkjTask.COMPLETED:
                    completeTasks.add(lkjTask);
                    break;
                case LkjTask.REFUSED:
                    refusedTasks.add(lkjTask);
                    break;
                case LkjTask.CLOSED:
                    closedTasks.add(lkjTask);
                    break;
            }
        }

        userLkjTask.setTotalTasks(lkjTasks);
        userLkjTask.setPendingHandleTasks(pendingHandleTasks);
        userLkjTask.setCompleteTasks(completeTasks);
        userLkjTask.setRefusedTasks(refusedTasks);
        userLkjTask.setClosedTasks(closedTasks);

        return userLkjTask;
    }

    @Override
    public long lkjUpdateCount(List<String> lkjTaskIds) throws Exception {
        return lkjDataLineBaseService.getObjectNum(LkjDataLine.class, Map.of("lkjTaskId", lkjTaskIds), Map.of("lkjTaskId", "in"));
    }
}

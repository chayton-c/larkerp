package com.yingda.lkj.service.backstage.lkjtask;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveFlow;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjTask;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.pojo.lkj.lkjtask.UserLkjTask;

import java.util.List;

/**
 * @author hood  2020/3/1
 */
public interface LkjTaskService {
    /**
     * 生成任务(导入)
     */
    LkjTask createLkjTask(LkjTask rawLkjTask, User submitter, User executer, List<LkjDataLine> rawLkjDataLines);

    /**
     * 生成任务(管理界面)
     */
    LkjTask createLkjTask(LkjTask rawLkjTask, User submitter, User executer);

    /**
     * 提交任务
     */
    DataApproveFlow submitLkjTask(String lkjTaskId, DataApproveFlow rawLkjApproveFlow, User submitter, String approveUserId);

    /**
     * 完成任务
     */
    void completeLkjTasks(List<String> lkjTaskIds);

    /**
     * 未完成任务
     */
    void refuseLkjTasks(List<String> lkjTaskIds);

    /**
     * 任务完成情况
     */
    UserLkjTask userTaskInfo(String userId);

    /**
     * 查询lkjTaskIds对应的lkj任务包含了多少条lkj进路更改
     * @param lkjTaskIds
     * @return
     */
    long lkjUpdateCount(List<String> lkjTaskIds) throws Exception;


}
